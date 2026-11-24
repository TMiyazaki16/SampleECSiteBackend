# =====================================================================
# サンプルEC サイト バックエンド 本番用 Dockerfile (Google Cloud Run 向け)
#
# このイメージは外部の MySQL 8.0 を必要とする（アプリ内に DB は持たない）。
# ローカルでは docker-compose.yml から MySQL ごと起動するのが前提:
#   JWT_SECRET="$(openssl rand -base64 32)" docker compose up --build
#
# 単体で起動する場合は接続先を環境変数で渡す:
#   docker build -t sample-ec-back .
#   docker run --rm -p 8080:8080 \
#     -e PORT=8080 \
#     -e ALLOWED_ORIGINS="https://www.tmiyazaki-works.com" \
#     -e JWT_SECRET="<base64 エンコードされた 32 バイト以上の秘密鍵>" \
#     -e SPRING_DATASOURCE_URL="jdbc:mysql://<host>:3306/ecdb" \
#     -e SPRING_DATASOURCE_USERNAME="<user>" \
#     -e SPRING_DATASOURCE_PASSWORD="<password>" \
#     sample-ec-back
# =====================================================================

# ---------------------------------------------------------------------
# ビルドステージ: Gradle (Wrapper) で bootJar を生成する
# ---------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# --- 依存解決に必要なファイルのみ先にコピーし、レイヤーキャッシュを効かせる ---
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
COPY system-common/build.gradle    system-common/
COPY application-core/build.gradle application-core/
COPY infrastructure/build.gradle   infrastructure/
COPY presentation/build.gradle     presentation/

# 依存ライブラリを先に解決してレイヤーキャッシュに載せる。
RUN chmod +x gradlew \
 && ./gradlew --no-daemon --quiet :presentation:dependencies > /dev/null 2>&1 || true

# --- ソース一式をコピーしてビルド ---
COPY system-common/src    system-common/src
COPY application-core/src application-core/src
COPY infrastructure/src   infrastructure/src
COPY presentation/src     presentation/src

RUN ./gradlew --no-daemon :presentation:bootJar -x test \
 && cp "$(ls presentation/build/libs/*.jar | grep -v -- '-plain\.jar' | head -n 1)" /workspace/app.jar

# ---------------------------------------------------------------------
# 実行ステージ: JRE のみの軽量イメージ
# ---------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# 非 root ユーザーで実行する
RUN groupadd --system --gid 1001 app \
 && useradd --system --uid 1001 --gid app --home /app --shell /usr/sbin/nologin app

WORKDIR /app

COPY --from=builder --chown=app:app /workspace/app.jar /app/app.jar

USER app

# Cloud Run は PORT 環境変数でリッスンポートを指定する (未設定時は 8080)。
# 実際のバインドは application.yml の server.port / server.address で行う。
ENV PORT=8080 \
    TZ=UTC \
    LANG=C.UTF-8 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# シェル経由で JAVA_OPTS を展開しつつ、exec で PID 1 を Java に渡して
# Cloud Run からの SIGTERM が JVM に届くようにする。
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_OPTS -Dfile.encoding=UTF-8 -jar /app/app.jar"]
