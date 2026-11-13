# sample-ec-back

---

## 1. プロジェクト概要

### アプリケーション概要

家具・家電・アクセサリー等を扱う BtoB EC サイトのバックエンド API サーバー。  
フロントエンド（Vue 3 + TypeScript）からの REST API リクエストを受け付け、商品管理・注文管理・認証機能を提供する。

### システム構成

```
┌─────────────────────────────┐
│  フロントエンド              │
│  Vue 3 + TypeScript + Vite  │
│  http://localhost:5173      │
└────────────┬────────────────┘
             │ HTTP / REST API (CORS)
             ▼
┌─────────────────────────────┐
│  バックエンド                │
│  Spring Boot 3.2.5          │
│  http://localhost:8080      │
│                             │
│  ┌─────────────────────┐   │
│  │  presentation       │   │
│  ├─────────────────────┤   │
│  │  application-core   │   │
│  ├─────────────────────┤   │
│  │  infrastructure     │   │
│  └─────────────────────┘   │
│             │               │
│             ▼               │
│  ┌──────────────────┐      │
│  │  H2 (インメモリ)  │      │
│  └──────────────────┘      │
└─────────────────────────────┘
```

### 想定ユースケース

| ユースケース | 概要 |
|-------------|------|
| ログイン | メールアドレス・パスワードで認証し JWT トークンを取得 |
| 商品一覧表示 | ページネーション・カテゴリ絞り込みで商品を取得 |
| 商品詳細表示 | 商品 ID で詳細情報を取得 |
| 注文作成 | カートの商品をまとめて注文（在庫確認・楽観ロック更新） |
| 注文履歴確認 | ログインユーザーの過去注文を一覧取得 |

---

## 2. アーキテクチャ概要

### クリーンアーキテクチャの考え方を含んだレイヤード設計

依存は必ず「外側 → 内側」の一方向に限定し、ビジネスロジックをフレームワーク・DB から独立させる。

```
presentation      ← HTTP の入出力・バリデーション・DTO 変換
application-core  ← ビジネスルール・Entity・DomainService・ApplicationService
infrastructure    ← DB アクセス・外部連携の実装
system-common     ← 全層横断の共通部品（例外・ユーティリティ）
```

`application-core` が Repository の **インターフェースを定義** し、`infrastructure` が **実装する** ことで依存の方向を逆転させている。これにより `application-core` は Spring Boot や DB に依存せず、ビジネスロジックを独立して保てる。

### ドメインモデルを中心とした設計

- **Entity**: ID を持つビジネスオブジェクト。Lombok の `@Value @Builder` で不変オブジェクトとして実装する。
  - `User` ― メールアドレス・パスワードハッシュ・氏名・ロール
  - `Product` ― 商品名・価格・在庫数・楽観ロック用バージョン
  - `Order` ― 注文者ID・注文明細リスト・合計金額・ステータス
- **Value Object**: 取りうる値が限定される概念を enum で表現する。
  - `Role` ― `ADMIN` / `BUYER`
  - `Currency` ― `JPY` / `USD`
  - `OrderStatus` ― `PENDING` / `CONFIRMED` / `SHIPPED` / `DELIVERED`
- **Domain Service**: 単一 Entity に収まらない業務ルールを担う（`StockDomainService`：在庫確認・楽観ロック更新）
- **Repository Interface**: `application-core` 内に定義し、実装は `infrastructure` 層に委ねる
- **Application Service**: ユースケース単位でトランザクションを管理し、各オブジェクトを組み合わせる

### マルチプロジェクト構成

Gradle マルチプロジェクトで各レイヤーをモジュールとして分割。  
`application-core` は Spring Boot に依存せず、業務ロジックのテスト容易性を確保する。

### CSR + REST API 構成

フロントエンド（CSR: Client Side Rendering）と完全分離した API サーバーとして設計。  
セッションは持たず、JWT によるステートレス認証を採用。

---

## 3. 技術スタック

| 種別 | ライブラリ | バージョン |
|------|-----------|-----------|
| 言語 | Java | 21 |
| フレームワーク | Spring Boot | 3.2.5 |
| Web | Spring MVC | 6.1.6 |
| セキュリティ | Spring Security | 6.2.4 |
| 認証トークン | jjwt | 0.12.6 |
| バリデーション | Spring Validation (Hibernate Validator) | 8.0.x |
| ORM | MyBatis Spring Boot Starter | 3.0.3 |
| DB | H2 Database (インメモリ) | — |
| API ドキュメント | springdoc-openapi | 2.4.0 |
| ログ | Apache Log4j2 | (Boot 管理) |
| ユーティリティ | Lombok | (Boot 管理) |
| ビルド | Gradle Wrapper | 8.14.3 |
| コンテナ | Docker (マルチステージビルド) | — |
| ベースイメージ | Eclipse Temurin (JDK 21 / JRE 21) | 21 |
| 実行基盤 | Google Cloud Run | — |

---

## 4. フォルダ構成

```
sample-ec-back/
├── application-core/               # ドメインモデル・業務ロジック
│   └── src/main/java/com/example/sampleec/
│       ├── authentication/         # 認証コンテキスト
│       │   ├── entity/             # User, Role
│       │   ├── repository/         # UserRepository (interface)
│       │   └── service/            # AuthApplicationService
│       ├── catalog/                # 商品コンテキスト
│       │   ├── entity/             # Product, Currency
│       │   ├── repository/         # ProductRepository (interface)
│       │   └── service/            # ProductApplicationService, ProductListResult
│       └── order/                  # 注文コンテキスト
│           ├── entity/             # Order, OrderItem, OrderStatus
│           ├── repository/         # OrderRepository (interface)
│           └── service/
│               ├── domain/         # StockDomainService
│               └── OrderApplicationService
│
├── infrastructure/                 # DB アクセス実装
│   └── src/main/java/com/example/sampleec/infrastructure/
│       ├── config/                 # MyBatisConfig (@MapperScan)
│       ├── entity/                 # TableEntity (DB 行マッピング用 POJO)
│       ├── mapper/                 # MyBatis Mapper インターフェース
│       └── repository/             # Repository 実装クラス
│   └── src/main/resources/mapper/ # MyBatis XML マッパー
│
├── presentation/                   # REST API・起動クラス
│   └── src/main/java/com/example/sampleec/
│       ├── EcApplication.java      # @SpringBootApplication
│       └── presentation/
│           ├── advice/             # GlobalExceptionHandler
│           ├── config/             # SecurityConfig, JwtConfig, OpenApiConfig, CustomAuthenticationEntryPoint, CustomAccessDeniedHandler
│           ├── controller/         # AuthController, ProductController, OrderController
│           ├── filter/             # JwtAuthenticationFilter, MdcFilter
│           ├── request/            # リクエスト DTO
│           └── response/           # レスポンス DTO
│   └── src/main/resources/
│       ├── application.yml
│       ├── schema.sql
│       ├── data.sql
│       ├── log4j2.xml
│       └── messages.properties
│
├── system-common/                  # 全層共通部品
│   └── src/main/java/com/example/sampleec/common/
│       ├── exception/              # BusinessException, SystemException
│       ├── message/                # MessageConfig (MessageSource Bean)
│       └── util/                   # UuidUtil, DateTimeUtil
│
├── Dockerfile                      # 本番用イメージ定義 (ビルド: JDK / 実行: JRE の 2 ステージ)
├── .dockerignore                   # イメージに含めないファイル
├── build.gradle                    # ルート Gradle 設定 (BOM・共通設定)
├── settings.gradle
├── gradle.properties               # org.gradle.java.home 設定
└── gradlew / gradlew.bat
```

**モジュール依存関係:**

```
presentation → application-core, infrastructure, system-common
infrastructure → application-core, system-common
application-core → system-common
system-common → (なし)
```

---

## 5. レイヤ責務

### プレゼンテーション層 (`presentation`)

| コンポーネント | 責務 |
|-------------|------|
| **Controller** | HTTP リクエストを受け付け ApplicationService を呼び出す。レスポンス DTO に変換して返す |
| **Validation** | `@Valid` + Bean Validation アノテーション（`@NotBlank`, `@NotEmpty` 等）でリクエストを検証。バリデーションエラーは `GlobalExceptionHandler` が 400 に変換 |
| **DTO (Request/Response)** | HTTP 入出力専用のデータ構造。Entity をそのまま公開しない。Java Record で不変に定義 |

### アプリケーションコア層 (`application-core`)

| コンポーネント | 責務 |
|-------------|------|
| **Entity** | ドメインの中心概念。ID で同一性を識別する不変オブジェクト（Lombok `@Value @Builder`） |
| **Value Object** | 不変・値で同一性を識別（`Role`, `Currency`, `OrderStatus` 等の enum） |
| **Domain Service** | 複数 Entity にまたがる業務ルール（`StockDomainService`：在庫確認・楽観ロック更新） |
| **Application Service** | ユースケース単位のオーケストレーション。`@Transactional` でトランザクション境界を管理 |

### インフラストラクチャ層 (`infrastructure`)

| コンポーネント | 責務 |
|-------------|------|
| **Repository 実装** | `application-core` で定義した Repository Interface を実装。MyBatis Mapper を呼び出し Entity に変換 |
| **Table Entity** | DB のテーブル行に対応する POJO（Lombok `@Data`）。ドメイン Entity とは別クラスで分離 |
| **MyBatis** | XML マッパーで SQL を定義。楽観ロック UPDATE・IN 句一括取得（N+1 対策）を実装 |

---

## 6. API 設計方針

### REST API

- リソース指向の URL 設計（`/api/products`, `/api/orders` 等）
- HTTP メソッドでアクション表現（GET: 取得, POST: 作成）
- 注文作成（`POST /api/orders`）は `@ResponseStatus(HttpStatus.CREATED)` で 201 を返す

### DTO 分離

- リクエスト・レスポンスともに専用 DTO を用意し、Entity を HTTP 層に露出しない
- レスポンスは `ApiResponse<T>` でラップ: `{ "data": { ... } }`
- エラーは `ErrorResponse` で統一: `{ "message": "..." }`

### OpenAPI 対応

- springdoc-openapi により Swagger UI を自動生成
- JWT Bearer 認証スキームを OpenAPI 定義に組み込み、Swagger UI 上でトークンをセット可能

### Validation 方針

- Controller のリクエスト引数に `@Valid` を付与
- Request DTO にアノテーションで制約を宣言（`@NotBlank`, `@Email`, `@NotEmpty`, `@Min` 等）
- バリデーションエラーは `GlobalExceptionHandler` で 400 Bad Request + `{ "message": "..." }` に変換

---

## 7. 例外設計方針

| 例外クラス | 用途 | HTTP ステータス |
|-----------|------|---------------|
| `BusinessException` | 業務ルール違反（在庫不足・認証失敗・リソース未存在等）| 例外生成時に `HttpStatus` を指定（400 / 401 / 404 / 409 等） |
| `SystemException` | 予期しない内部エラー | 500 Internal Server Error |

### GlobalExceptionHandler (`@RestControllerAdvice`)

| キャッチする例外 | レスポンス |
|---------------|----------|
| `BusinessException` | 例外に設定された HTTP ステータス + `{ "message": "..." }` |
| `MethodArgumentNotValidException` | 400 + `{ "message": "バリデーションエラーメッセージ" }` |
| `Exception`（その他全て） | 500 + `{ "message": "内部エラーが発生しました" }` |

### 未認証・認可エラー

| 状況 | レスポンス |
|------|----------|
| JWT なし・無効（未認証） | 401 + `{ "message": "認証が必要です" }` |
| 認証済みだが権限不足 | 403 + `{ "message": "アクセス権限がありません" }` |

---

## 8. ログ設計方針

### Log4j2

`spring-boot-starter-logging`（Logback）を除外し、`spring-boot-starter-log4j2` を採用。  
設定ファイル: `presentation/src/main/resources/log4j2.xml`

### ログレベル

| ロガー | レベル | 用途 |
|--------|-------|------|
| `com.example.sampleec` | DEBUG | アプリケーション全体 |
| `com.example.sampleec.infrastructure.mapper` | DEBUG | MyBatis SQL ログ |
| `org.springframework` | INFO | Spring フレームワーク |
| `org.mybatis` | WARN | MyBatis フレームワーク |
| Root | INFO | その他 |

### 通信ログ（Correlation ID）

`MdcFilter`（`OncePerRequestFilter`）がリクエスト受信時に UUID の Correlation ID を生成し、`MDC` に設定。  
ログパターンに `%X{correlationId}` を含めることで、1 リクエストのログを横断して追跡可能。

```
2026-05-25 10:00:00.000 [http-nio-8080-exec-1] INFO  [a1b2c3d4-...] com.example.sampleec... - メッセージ
                                                                      ^^^^^^^^^^^^^^^^^^^^^^^^
                                                                      Correlation ID
```

### 監査ログ

- 認証失敗・アクセス拒否は `CustomAuthenticationEntryPoint` / `CustomAccessDeniedHandler` で DEBUG ログ出力
- 在庫更新成功・失敗は `StockDomainService` で INFO / WARN ログ出力
- 予期しないエラーは `GlobalExceptionHandler` で ERROR ログ出力（スタックトレース含む）

---

## 9. トランザクション管理方針

### 宣言的トランザクション（`@Transactional`）

`ApplicationService` のメソッドに `@Transactional` を付与することでトランザクション境界を宣言。  
業務ロジックはトランザクション管理を意識せずに記述できる。

```java
// OrderApplicationService
@Transactional
public Order createOrder(String buyerId, List<OrderItemRequest> items) {
    // 在庫確認・更新・注文保存がひとつのトランザクションで実行される
}
```

### 楽観ロック

複数ユーザーが同時に同じ商品を注文した際の在庫二重減算を防ぐため、`products` テーブルに `version` カラムを設け楽観ロックを実装。

```sql
-- 在庫更新（楽観ロック）
UPDATE products
SET stock = stock - #{quantity}, version = version + 1
WHERE id = #{id}
  AND version = #{version}   -- 取得時と同じバージョンのみ更新
  AND stock >= #{quantity}   -- 在庫が足りる場合のみ更新
```

更新行数が 0 の場合（バージョン不一致または在庫不足）→ `BusinessException(409 Conflict)` をスロー。

### デッドロック対策

- `ApplicationService` 内で複数商品の在庫を更新する際は、商品 ID 順にソートしてアクセス順序を統一
- H2 インメモリ DB のため、本番相当のデッドロックは発生しないが、設計としてのガイドを踏襲

---

## 10. メッセージ管理方針

### MessageSource

`MessageConfig` で `ReloadableResourceBundleMessageSource` を Bean 定義し、メッセージを外部ファイルで管理。

### messages.properties

`presentation/src/main/resources/messages.properties` にエラーメッセージを定義：

```properties
error.occurred=エラーが発生しました
product.notFound=商品が見つかりません
auth.failed=認証に失敗しました
stock.insufficient=在庫が不足しています
```

### 多言語化対応可能設計

`messages_en.properties` 等を追加するだけで多言語対応が可能な構造。  
`MessageSource.getMessage(code, args, locale)` の `locale` に Accept-Language ヘッダーの値を渡すことで切り替え可能。

---

## 11. テスト方針

> 本プロジェクトはサンプル実装のためテストコードは含まないが、以下の方針で実装することを想定している。

### Unit Test

- `application-core` の Entity・Domain Service・Application Service を JUnit 5 + Mockito でテスト
- 外部依存（DB、外部 API）は Mock で置き換え、ビジネスロジックのみを検証

### Mock Test

- `@WebMvcTest` で Controller 層のみをロードし、Application Service を `@MockBean` で差し替え
- リクエストの Validation・レスポンス形式・HTTP ステータスを検証

### H2 Database を使ったテスト

- `@SpringBootTest` + H2 インメモリ DB でインテグレーションテストを実施
- `schema.sql` / `data.sql` で再現性のある初期状態を確保

### Repository Test

- `@MybatisTest` で MyBatis Mapper と XML マッパーを H2 上で動作確認
- 楽観ロック UPDATE（0 行更新ケース）や IN 句一括取得を重点テスト

---

## 12. セットアップ方法

### 前提条件

| 用途 | 必要なもの |
|------|-----------|
| Docker で起動する場合 | Docker（Docker Desktop / Rancher Desktop など）のみ。Java のインストールは不要 |
| Gradle で直接起動する場合 | Java 21（`gradle.properties` に `org.gradle.java.home` 設定済み）。Gradle は Wrapper (`./gradlew`) を使うためインストール不要 |

### 環境変数

| 変数名 | 必須 | 既定値 | 説明 |
|--------|------|--------|------|
| `JWT_SECRET` | **必須** | なし | JWT 署名鍵。Base64 エンコード済み 32 バイト以上。**未設定の場合は起動時にエラーとなる** |
| `PORT` | 任意 | `8080` | リッスンポート（Cloud Run では自動注入） |
| `ALLOWED_ORIGINS` | 任意 | `http://localhost:5173` | CORS 許可オリジン。カンマ区切りで複数指定可 |
| `JWT_EXPIRATION_HOURS` | 任意 | `24` | JWT の有効期間（時間） |
| `H2_CONSOLE_ENABLED` | 任意 | `true` | H2 コンソールの有効化。**本番環境では `false` にすること** |

署名鍵は鍵の漏洩を避けるためリポジトリに含めていない。以下のコマンドで生成する。

```bash
openssl rand -base64 32
```

> 鍵を変更すると発行済みの JWT は無効になり、再ログインが必要になる。開発中は同じ値を使い回すとよい。

---

## 13. 起動方法

起動方法は 2 通りある。本番（Cloud Run）と同じ構成で確認したい場合は Docker を使う。

| 方法 | 用途 |
|------|------|
| **A. Docker** | 本番と同一構成での動作確認。Java のインストール不要 |
| **B. Gradle 直接起動** | コード変更を素早く反映したい開発時 |

### A. Docker で起動する

#### イメージのビルド

```bash
cd sample-ec-back
docker build -t sample-ec-back .
```

初回はライブラリのダウンロードが入るため数分かかる。2 回目以降はレイヤーキャッシュが効くため数秒で完了する。

#### 起動

```bash
docker run -d --name sample-ec-back -p 8080:8080 \
  -e JWT_SECRET="$(openssl rand -base64 32)" \
  -e ALLOWED_ORIGINS="http://localhost:5173" \
  sample-ec-back
```

PowerShell の場合:

```powershell
$secret = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
docker run -d --name sample-ec-back -p 8080:8080 `
  -e JWT_SECRET="$secret" `
  -e ALLOWED_ORIGINS="http://localhost:5173" `
  sample-ec-back
```

#### 起動確認・ログ確認

```bash
docker ps                      # 起動中のコンテナ一覧
docker logs -f sample-ec-back  # ログを追従表示（Ctrl+C で表示終了。コンテナは停止しない）
```

`Started EcApplication in X seconds` が出力されれば起動完了。

#### 停止・削除

```bash
docker stop sample-ec-back     # 停止
docker rm sample-ec-back       # 削除（同じ名前で再起動する場合は削除が必要）
```

> H2 はインメモリ DB のため、コンテナを停止するとデータは失われ、次回起動時に `data.sql` の初期状態へ戻る。

### B. Gradle で直接起動する

```bash
cd sample-ec-back
JWT_SECRET="$(openssl rand -base64 32)" ./gradlew :presentation:bootRun
```

PowerShell の場合:

```powershell
cd sample-ec-back
$env:JWT_SECRET = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
./gradlew :presentation:bootRun
```

停止は `Ctrl+C`。

ポートの使用状況を確認する場合（PowerShell）:

```powershell
netstat -ano | findstr ":8080"
```

`LISTENING` の行が表示されれば起動中、何も表示されなければ停止中。

プロセスを強制停止する場合（PowerShell）:

```powershell
netstat -ano | findstr ":8080" | findstr "LISTENING" | ForEach-Object { ($_ -split '\s+')[-1] } | ForEach-Object { Stop-Process -Id $_ -Force; Write-Host "停止しました (PID: $_)" }
```

---

## 14. Google Cloud Run へのデプロイ

### Dockerfile の構成

ビルドと実行を分けたマルチステージ構成になっている。

| ステージ | ベースイメージ | 役割 |
|---------|--------------|------|
| builder | `eclipse-temurin:21-jdk-jammy` | Gradle Wrapper で `:presentation:bootJar` を実行し JAR を生成 |
| runtime | `eclipse-temurin:21-jre-jammy` | 生成された JAR のみを配置して起動（非 root ユーザー uid 1001） |

実行ステージには JDK もソースコードも含まれないため、イメージサイズは約 465MB に収まる。

補足事項:

- `gradle.properties` の `org.gradle.java.home` はローカル環境固有のパスを指すため、コンテナ内では除去したうえでビルドしている（リポジトリのファイルは変更していない）
- Cloud Run が送出する SIGTERM を JVM が直接受け取れるよう、`ENTRYPOINT` は `exec` 形式で起動している
- JVM オプションに `-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC` を指定している。CPU を多く割り当てる構成に変更する場合は `UseSerialGC` の見直しを検討する

### 署名鍵の登録（初回のみ）

鍵が設定値として記録されないよう、Secret Manager 経由で渡す。

```bash
openssl rand -base64 32 | gcloud secrets create sample-ec-jwt-secret --data-file=-

gcloud secrets add-iam-policy-binding sample-ec-jwt-secret \
  --member="serviceAccount:<プロジェクト番号>-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

### デプロイ

```bash
gcloud run deploy sample-ec-back \
  --source . \
  --region asia-northeast1 \
  --allow-unauthenticated \
  --max-instances 1 \
  --set-env-vars "ALLOWED_ORIGINS=https://www.tmiyazaki-works.com,H2_CONSOLE_ENABLED=false" \
  --set-secrets "JWT_SECRET=sample-ec-jwt-secret:latest"
```

### 注意事項

- **`--max-instances 1` を指定している**。H2 インメモリ DB はインスタンスごとに独立するため、複数インスタンスへスケールすると注文データなどに不整合が生じる。恒常運用する場合は Cloud SQL への移行が必要
- データは永続化されない。インスタンスの再起動・スケールインのたびに初期状態へ戻る
- `H2_CONSOLE_ENABLED=false` を必ず指定する。H2 コンソールは認証不要で公開されるため、有効のままでは誰でも DB を操作できてしまう
- `PORT` は Cloud Run が自動的に注入するため指定不要

---

## 15. 動作確認

### H2 Database コンソール

http://localhost:8080/h2-console

| 項目 | 値 |
|------|----|
| JDBC URL | `jdbc:h2:mem:ecdb` |
| User Name | `sa` |
| Password | （空欄） |

> `H2_CONSOLE_ENABLED=false` を指定して起動した場合は利用できない（本番環境では無効化すること）。
> Docker で起動した場合も、コンテナ内の H2 にブラウザから接続できる。

よく使う確認クエリ：

```sql
SELECT id, name, stock, version FROM products;
SELECT * FROM orders;
SELECT oi.*, p.name FROM order_items oi JOIN products p ON oi.product_id = p.id;
```

---

### Swagger UI での打鍵手順

http://localhost:8080/swagger-ui.html

1. `POST /api/auth/login` → **Try it out** → 以下を入力して **Execute**
   ```json
   { "email": "test@example.com", "password": "password123" }
   ```
2. レスポンスの `token` をコピー
3. 画面右上の **Authorize 🔒** → `BearerAuth` にトークンを貼り付け → **Authorize**
4. 各エンドポイントを試す

---

### curl での疎通確認

```bash
# ログインしてトークンを取得
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["token"])')

# 取得したトークンで商品一覧を取得
curl -s http://localhost:8080/api/products -H "Authorization: Bearer $TOKEN"
```

---

### フロントエンドとの接続

バックエンド起動後、フロントエンド（[sample-ec](../sample-ec)）側の `.env.local` を作成：

```bash
# sample-ec/.env.local
VITE_API_BASE_URL=http://localhost:8080/api
```

フロントエンドを起動：

```bash
cd sample-ec
npm run dev
```

http://localhost:5173 でブラウザ確認。

> `.env.backend` が `.env.development`（`VITE_API_BASE_URL=http://localhost:5173/api`）より優先されるため、MSW（Mock Service Worker）は自動的に無効化され、実際のバックエンドへリクエストが送られる。

---

## API 一覧

| メソッド | エンドポイント | 認証 | 概要 |
|--------|------------|------|------|
| POST | `/api/auth/login` | 不要 | ログイン → JWT トークン取得 |
| POST | `/api/auth/logout` | 必要 | ログアウト |
| GET | `/api/auth/me` | 必要 | ログイン中ユーザー情報取得 |
| GET | `/api/products` | 必要 | 商品一覧（`?page=1&perPage=8&category=furniture`） |
| GET | `/api/products/{id}` | 必要 | 商品詳細 |
| GET | `/api/orders` | 必要 | 注文履歴一覧 |
| POST | `/api/orders` | 必要 | 注文作成（HTTP 201） |

**認証方式:** `Authorization: Bearer <JWT トークン>` ヘッダー

**エラーレスポンス（共通）:** `{ "message": "エラーメッセージ" }`

---

## テストデータ

### ユーザー

| メール | パスワード | ロール |
|--------|---------|--------|
| `test@example.com` | `password123` | buyer |
| `admin@example.com` | `password123` | admin |

### 商品カテゴリ

`furniture` / `accessories` / `electronics` / `lighting` の 4 カテゴリ × 各 2 件、計 8 件
