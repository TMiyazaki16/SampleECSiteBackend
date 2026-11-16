# リポジトリガイドライン

## プロジェクト概要

BtoB EC サイトのバックエンド API（Spring Boot 3.2.5 / Java 21 / Cloud Run）。インフラコードは置かない。

- フロントエンド: [TMiyazaki16/SampleECSite](https://github.com/TMiyazaki16/SampleECSite)（Vue 3 / Cloud Storage）
- インフラ（IaC）: `sample-ec-infra` リポジトリ（Terraform）。Cloud Run のサービス定義・環境変数・シークレット・公開設定はそちらの管轄で、本リポジトリが担うのはコンテナイメージの中身とそのデプロイに限る
- 本番は外部ロードバランサのパスベース振り分け。`/api/*` が本 API、それ以外がフロントエンド

設計方針・手順・制約は `README.md` を正とする。ここに書いていないことは README の該当節を読む。

## ディレクトリ構成

Gradle マルチプロジェクト。依存は外側 → 内側の一方向のみ。

```
presentation      → application-core, infrastructure, system-common
infrastructure    → application-core, system-common
application-core  → system-common
system-common     → （なし）
```

- `application-core/` — Entity・DomainService・ApplicationService・Repository インターフェース。`authentication` / `catalog` / `order` のコンテキスト単位で分ける
- `infrastructure/` — MyBatis Mapper・XML・Repository 実装・DB 行マッピング用 `TableEntity`
- `presentation/` — Controller・Request/Response DTO・Filter・Config・`application.yml` 等のリソース
- `system-common/` — `BusinessException` / `SystemException`・MessageSource・ユーティリティ

この依存方向を逆転させない。`application-core` に Spring Web や MyBatis の型を持ち込まない。

## 開発コマンド

ホストに Java / Gradle は入れない。Docker だけで完結させる。

```sh
# ビルドとテスト（gradlew は実行権限なしでコミットされているため sh 経由で起動する）
docker run --rm -v "$PWD":/workspace -w /workspace \
  eclipse-temurin:21-jdk-jammy sh ./gradlew --no-daemon build

# イメージのビルドと起動
docker build -t sample-ec-back .
docker run -d --name sample-ec-back -p 8080:8080 \
  -e JWT_SECRET="$(openssl rand -base64 32)" \
  -e ALLOWED_ORIGINS="http://localhost:5173" \
  sample-ec-back

docker logs -f sample-ec-back   # `Started EcApplication in X seconds` で起動完了
docker rm -f sample-ec-back     # 停止・削除（同名で起動し直すには削除が必要）
```

- ホットリロードは未対応。コードを変えたら再ビルドして起動し直す
- `build.gradle` を変えたときやビルド結果が不審なときは `--no-cache` で再ビルドする
- `JWT_SECRET` は必須。未設定だと起動時にエラーで落ちる

## 実装の前提

- **レイヤ責務**: Entity を HTTP 層に露出しない。リクエスト・レスポンスともに専用 DTO を用意し、レスポンスは `ApiResponse<T>`、エラーは `ErrorResponse`（`{ "message": "..." }`）で統一する
- **例外**: 業務ルール違反は `BusinessException`（生成時に `HttpStatus` を指定）、予期しない内部エラーは `SystemException`。Controller で try-catch して握りつぶさず、`GlobalExceptionHandler` に任せる
- **メッセージ**: エラーメッセージを Java コードに直書きしない。`messages.properties` に定義して `MessageSource` 経由で取得する
- **トランザクション**: 境界は `ApplicationService` の `@Transactional`。Controller や Repository に置かない
- **楽観ロック**: 在庫更新は `version` 一致と在庫充足を `WHERE` 句で判定し、更新 0 行なら 409 を返す。この条件を外さない。複数商品を更新するときは商品 ID 順にソートしてアクセス順序を揃える
- **ログ**: Log4j2。`MdcFilter` が付与する Correlation ID (`%X{correlationId}`) を前提にしたパターンを壊さない。`System.out.println` を使わない
- **H2 インメモリ DB**: データは永続化されず、水平スケールもできない。アプリが単一インスタンス前提である点はインフラ側の設定（スケール上限）と対になっているため、永続化や並行処理が要る変更は先に相談する

## AI開発ルール

### 人間確認が必要なとき

次に該当したら、実装に入らず前提・選択肢・影響を提示して確認を待つ。

- 本番環境へのデプロイ（新しいリビジョンの反映）
- モジュール間の依存関係を変える変更、新しいモジュールの追加
- 新しい依存ライブラリの追加、Spring Boot / Java のバージョン変更
- 認証・認可まわりの変更（`SecurityConfig` の `permitAll` 追加、JWT の検証ロジック、CORS 許可オリジンの拡大）
- DB スキーマ（`schema.sql`）・初期データ（`data.sql`）の変更。再起動で初期状態に戻るため影響が見えにくい
- 公開 API のパス・リクエスト/レスポンス形式の変更。フロントエンドの修正が連動して必要になる
- 依頼スコープを超える変更（無関係なレイヤのリファクタ、フロントエンド・インフラリポジトリの変更）
- 3 ファイル以上または複数モジュールにまたがる変更 → 先に計画（変更対象 / README の更新要否 / 検証方針 / リスク）を提示する

### そのまま進めてよいとき

既存パターンに沿ったコントローラ・サービス・Mapper の追加、Validation アノテーションの付与、メッセージ定義の追加、コメント・ドキュメントの追加。置いた仮定は作業報告に明記する。クラス名・パス・設定キーは実在を確認してから参照する。

### 明示依頼なしでは実行しない

- **`git commit`（コミット）** / `git push` / force push / `git reset --hard` / `git rebase -i`
- `JWT_SECRET` の値の生成結果・トークン・パスワードの出力、ログ出力、ファイルへの書き出し、コミット
- `gcloud` による Cloud Run サービスの設定変更（環境変数・シークレット・スケール・公開範囲）。インフラの管轄なので、必要なら `sample-ec-infra` 側の変更として提案する

### 変更時チェックリスト

1. `./gradlew build` が成功する（上記の Docker 経由で実行）
2. 変更したモジュールの依存方向が崩れていない
3. API を追加・変更したら `@Valid` と Request DTO の制約、OpenAPI 上の表示を確認する
4. エンドポイント・環境変数・制約を変えたら `README.md`（「API 一覧」「12. セットアップ方法」「18. 既知の制約」）を更新する
5. フロントエンドに影響する変更は、`SampleECSite` 側で必要になる対応を報告に書く

### 報告

実行したコマンドと結果、置いた仮定、確認が必要な事項を明記する。ビルドやテストを実行できなかった場合はその理由も書く。未実施の手順を実施済みと書かない。
