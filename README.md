# sample-ec-back

---

## 1. プロジェクト概要

### リポジトリ構成

本システムは 2 つのリポジトリで構成される。

| リポジトリ | 役割 |
|---|---|
| [TMiyazaki16/SampleECSiteBackend](https://github.com/TMiyazaki16/SampleECSiteBackend)（本リポジトリ） | バックエンド API（Spring Boot / Cloud Run） |
| [TMiyazaki16/SampleECSite](https://github.com/TMiyazaki16/SampleECSite) | フロントエンド（Vue 3 + TypeScript / Cloud Storage） |

ローカルでは同一階層に clone されている前提で記載している。

```
work/
├── SampleECSiteBackend/   # 本リポジトリ
└── SampleECSite/          # フロントエンド
```

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
SampleECSiteBackend/
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
├── gradle.properties               # Gradle 実行設定 (daemon・parallel)
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

### テストの実行方法

ローカルに Java を導入せず、JDK イメージ上で Gradle を実行する。

```bash
docker run --rm -v "$PWD":/workspace -w /workspace \
  eclipse-temurin:21-jdk-jammy sh ./gradlew --no-daemon test
```

PowerShell の場合:

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace `
  eclipse-temurin:21-jdk-jammy sh ./gradlew --no-daemon test
```

> `gradlew` は実行権限なし（644）でコミットされているため、`./gradlew` ではなく `sh ./gradlew` で起動する。
> （イメージのビルド時は Dockerfile 内で `chmod +x` している）

> 実行結果は `presentation/build/reports/tests/test/index.html` 等に出力される。
> ホスト側に `build/` ディレクトリが生成される点に注意（`.gitignore` 済み）。

---

## 12. セットアップ方法

### 前提条件

- **Docker**（Docker Desktop / Rancher Desktop など）のみ

Java や Gradle のインストールは不要。ビルドから実行までコンテナ内で完結するため、開発環境による差異が生じない。

### 環境変数

| 変数名 | 必須 | 既定値 | 説明 |
|--------|------|--------|------|
| `JWT_SECRET` | **必須** | なし | JWT 署名鍵。Base64 エンコード済み 32 バイト以上。**未設定の場合は起動時にエラーとなる** |
| `PORT` | 任意 | `8080` | リッスンポート（Cloud Run では自動注入） |
| `ALLOWED_ORIGINS` | 任意 | `http://localhost:5173` | CORS 許可オリジン。カンマ区切りで複数指定可 |
| `JWT_EXPIRATION_HOURS` | 任意 | `24` | JWT の有効期間（時間） |
| `H2_CONSOLE_ENABLED` | 任意 | `true` | H2 コンソールの有効化。**本番環境では `false` にすること** |

上表はアプリが解釈する環境変数とその既定値であり、設定する主体は環境によって異なる。

- **ローカル / Docker 実行** — 上表のとおり自分で指定する
- **本番（Cloud Run）** — `JWT_SECRET` / `ALLOWED_ORIGINS` / `H2_CONSOLE_ENABLED` は `sample-ec-infra` の Terraform が設定する。デプロイ時に `gcloud` 側から指定しない（「15. Google Cloud Run へのデプロイ」参照）
- `PORT` は Cloud Run が自動注入するため、いずれの環境でも指定しない

署名鍵は鍵の漏洩を避けるためリポジトリに含めていない。以下のコマンドで生成する。

```bash
openssl rand -base64 32
```

> 鍵を変更すると発行済みの JWT は無効になり、再ログインが必要になる。開発中は同じ値を使い回すとよい。

---

## 13. 起動方法

### イメージのビルド

```bash
cd SampleECSiteBackend
docker build -t sample-ec-back .
```

初回はライブラリのダウンロードが入るため数分かかる。2 回目以降はレイヤーキャッシュが効くため 10 秒程度で完了する。

### 起動

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

### 起動確認・ログ確認

```bash
docker ps                      # 起動中のコンテナ一覧
docker logs -f sample-ec-back  # ログを追従表示（Ctrl+C で表示終了。コンテナは停止しない）
```

`Started EcApplication in X seconds` が出力されれば起動完了。

### 停止・削除

```bash
docker stop sample-ec-back     # 停止
docker rm sample-ec-back       # 削除（同じ名前で再起動する場合は削除が必要）
```

> H2 はインメモリ DB のため、コンテナを停止するとデータは失われ、次回起動時に `data.sql` の初期状態へ戻る。

### コード変更の反映

ホットリロードには対応していないため、コードを変更した場合はイメージを再ビルドして起動し直す。

```bash
docker rm -f sample-ec-back
docker build -t sample-ec-back .
docker run -d --name sample-ec-back -p 8080:8080 \
  -e JWT_SECRET="$(openssl rand -base64 32)" \
  sample-ec-back
```

> ソースコードのみの変更であれば依存解決のレイヤーはキャッシュが効くため、再ビルドは 10 秒程度で完了する。

依存ライブラリ（`build.gradle`）を変更した場合や、ビルド結果が不審な場合はキャッシュを使わず再ビルドする。

```bash
docker build --no-cache -t sample-ec-back .
```

### ポート 8080 が使用中の場合

`-p` の左側（ホスト側ポート）を変更する。コンテナ内のポートは 8080 のままでよい。

```bash
docker run -d --name sample-ec-back -p 18080:8080 \
  -e JWT_SECRET="$(openssl rand -base64 32)" \
  -e ALLOWED_ORIGINS="http://localhost:5173" \
  sample-ec-back
```

> この場合 API は `http://localhost:18080` で待ち受ける。
> フロントエンドから接続する場合は `.env.backend` の `VITE_API_BASE_URL` も合わせて変更すること。

---

## 14. デバッグ方法（IDE からのリモートデバッグ）

環境変数 `JAVA_TOOL_OPTIONS` でデバッガ待受を有効にし、デバッグ用ポート 5005 を公開して起動する。

```bash
docker run -d --name sample-ec-back -p 8080:8080 -p 5005:5005 \
  -e JWT_SECRET="$(openssl rand -base64 32)" \
  -e JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  sample-ec-back
```

PowerShell の場合:

```powershell
$secret = [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
docker run -d --name sample-ec-back -p 8080:8080 -p 5005:5005 `
  -e JWT_SECRET="$secret" `
  -e JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" `
  sample-ec-back
```

起動ログに以下が出力されればデバッガの接続待ち状態になっている。

```
Picked up JAVA_TOOL_OPTIONS: -agentlib:jdwp=...
Listening for transport dt_socket at address: 5005
```

### IDE からの接続

| IDE | 手順 |
|-----|------|
| IntelliJ IDEA | Run → Edit Configurations → **+** → **Remote JVM Debug** → Host `localhost` / Port `5005` → Debug 実行 |
| VS Code | `.vscode/launch.json` に以下を追加し、実行とデバッグから起動 |
| Eclipse | Run → Debug Configurations → **Remote Java Application** → Host `localhost` / Port `5005` |

VS Code の `launch.json` 設定例:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Docker コンテナにアタッチ",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}
```

接続後はブレークポイントの設定、ステップ実行、変数の内容確認が通常どおり行える。

補足:

- `suspend=n` を指定しているため、デバッガを接続しなくてもアプリは通常どおり起動する。起動直後の処理にブレークポイントを置きたい場合のみ `suspend=y` に変更する（デバッガが接続されるまで起動が待機する）
- `JAVA_TOOL_OPTIONS` は JVM が自動的に読み込む標準の環境変数のため、Dockerfile の変更は不要。本番環境ではこの変数を渡さなければよい
- **デバッグポートは本番環境では絶対に公開しないこと。** 接続できれば任意のコードを実行できてしまう

---

## 15. Google Cloud Run へのデプロイ

### デプロイの順序

パスベース構成では、フロントエンドとバックエンドが互いの URL を知る必要がない。
（フロントエンドは `/api` という相対パスを参照し、振り分けはロードバランサが行うため）
そのため①②の順序は問わず、個別にデプロイ・更新できる。

```
① バックエンドを Cloud Run にデプロイ
② フロントエンドを Cloud Storage にデプロイ（VITE_API_BASE_URL=/api）
      ↓ 両方揃った状態で
③ Cloud Load Balancing の URL マップを設定
   /api/*  → Cloud Run
   /*      → Cloud Storage バケット
```

③のロードバランサ設定が完了するまでは、独自ドメイン経由での疎通はできない。
それ以前にバックエンド単体を確認する場合は、Cloud Run のサービス URL
（`https://sample-ec-back-xxxxx.a.run.app`）へ直接アクセスする。

> ホストベース構成（例: `api.tmiyazaki-works.com`）へ変更する場合は、
> フロントエンドのビルドに API の絶対 URL を埋め込む必要があるため、
> 「バックエンド → フロントエンド」の順序依存が発生する点に注意する。

### 本番構成

**Cloud Load Balancing + 独自ドメイン**（`https://sample-ec-site.tmiyazaki-works.com`）を採用する。

ルーティングは**パスベース**とし、フロントエンドと API を同一オリジンで配信する。

```
         https://sample-ec-site.tmiyazaki-works.com
                              │
                 ┌────────────▼────────────┐
                 │  Cloud Load Balancing   │
                 │ （HTTPS 終端・独自ドメイン） │
                 └──────┬───────────┬──────┘
                        │           │
                   /*   │           │   /api/*
                        ▼           ▼
          ┌──────────────────┐  ┌──────────────────┐
          │  Cloud Storage   │  │   Cloud Run      │
          │ （フロントエンド）   │  │ （本バックエンド）  │
          └──────────────────┘  └──────────────────┘
```

URL マップの設定例:

| パス | 転送先 |
|------|-------|
| `/api/*` | Cloud Run（バックエンド） |
| `/*`（上記以外） | Cloud Storage バケット（フロントエンド） |

> パスの評価順に注意する。`/api/*` を先に一致させること。

Cloud Storage から直接配信しない理由:

- 独自ドメインと HTTPS を使うため（Cloud Storage 単体では対応できない）
- SPA のためリロード・URL 直接アクセス時に `index.html` を **200 で返す**必要があるため
  （Cloud Storage 単体のエラーページ設定では 404 のまま返る。フロントエンド側 README 参照）

### 本番構成における CORS

パスベース構成ではフロントエンドと API が同一オリジン（`https://sample-ec-site.tmiyazaki-works.com`）になるため、
**本番環境ではブラウザの CORS 制約が発生しない。**

ただし以下の理由から、`ALLOWED_ORIGINS` は設定しておくことを推奨する。

- ローカル開発（`http://localhost:5173` → `http://localhost:8080`）では CORS が発生するため
- Cloud Run のサービス URL（`https://sample-ec-back-xxxxx.a.run.app`）へ直接アクセスした場合に備えるため
- 将来ホストベース構成（例: `api.tmiyazaki-works.com`）へ変更する場合に備えるため

```
ALLOWED_ORIGINS=https://sample-ec-site.tmiyazaki-works.com
```

### フロントエンドの API ベース URL

パスベース構成では同一オリジンのため、フロントエンドの `VITE_API_BASE_URL` には
**絶対 URL ではなく相対パスを設定する。**

```
# SampleECSite/.env.production
VITE_API_BASE_URL=/api
```

> バックエンドの Cloud Run URL をフロントエンドのビルドに埋め込む必要がないため、
> Cloud Run の URL が変わってもフロントエンドのリビルドは不要になる。

### Dockerfile の構成

ビルドと実行を分けたマルチステージ構成になっている。

| ステージ | ベースイメージ | 役割 |
|---------|--------------|------|
| builder | `eclipse-temurin:21-jdk-jammy` | Gradle Wrapper で `:presentation:bootJar` を実行し JAR を生成 |
| runtime | `eclipse-temurin:21-jre-jammy` | 生成された JAR のみを配置して起動（非 root ユーザー uid 1001） |

実行ステージには JDK もソースコードも含まれないため、イメージサイズは約 465MB に収まる。

補足事項:

- 環境依存を避けるため、`gradle.properties` には JDK のパス（`org.gradle.java.home`）を記載していない。JDK はベースイメージのものが自動的に使われる
- Cloud Run が送出する SIGTERM を JVM が直接受け取れるよう、`ENTRYPOINT` は `exec` 形式で起動している
- JVM オプションに `-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC` を指定している。CPU を多く割り当てる構成に変更する場合は `UseSerialGC` の見直しを検討する

### 署名鍵の登録（初回のみ）

鍵が設定値として記録されないよう、Secret Manager 経由で渡す。
シークレットの作成だけは手動で行う（Terraform は `data` で参照するのみ）。

```bash
openssl rand -base64 32 | gcloud secrets create sample-ec-jwt-secret --data-file=-
```

> 読み取り権限の付与は `sample-ec-infra` の Terraform が行うため、手動での IAM 設定は不要。
> Cloud Run サービスへの `JWT_SECRET` の紐づけも Terraform 側で定義されている。

### デプロイ

Cloud Run のサービス定義（環境変数・シークレット・スケール上限・公開範囲）は
`sample-ec-infra` の Terraform が管理している。
本リポジトリから行うのは**コンテナイメージの差し替えのみ**で、新しいリビジョンを作る。

```bash
gcloud run deploy sample-ec-back \
  --image asia-northeast1-docker.pkg.dev/ec-site-handson/sample-ec/sample-ec-back:<タグ> \
  --region asia-northeast1
```

> ビルド済みイメージを指定する形が本来の手順。通常は GitHub Actions が
> この手順を自動実行する（後述「GitHub Actions による自動デプロイ」）。`<タグ>` はコミットハッシュ。
>
> イメージを置く Artifact Registry リポジトリ（`asia-northeast1-docker.pkg.dev/ec-site-handson/sample-ec`）は
> `sample-ec-infra` の Terraform 管理下にある。`terraform destroy` するとイメージごと消えるため、
> 再構築した後は**デプロイの前にイメージのビルドと push をやり直す**必要がある。

### GitHub Actions による自動デプロイ

`.github/workflows/deploy-backend.yml` が、`main` への push（および手動実行）を契機に
イメージのビルド → Artifact Registry への push → Cloud Run のリビジョン更新までを行う。

| ステップ | 内容 |
|---------|------|
| Authenticate to Google Cloud | Workload Identity 連携で短期認証情報を取得（鍵ファイルは使わない） |
| Configure Docker credential helper | `gcloud auth configure-docker asia-northeast1-docker.pkg.dev` |
| Build / Push image | 直下の `Dockerfile` でビルドし、`<コミットハッシュ>` と `latest` の 2 タグを push |
| Deploy to Cloud Run | `gcloud run deploy --image <...>:<コミットハッシュ> --region asia-northeast1` |
| Show deployed revision | `gcloud run services describe` で反映されたリビジョンと URL を表示 |

GitHub の Settings → Secrets and variables → Actions → **Variables** に以下を登録しておく
（秘密情報ではないため Secrets ではなく Variables）。

| 変数名 | 値 |
|-------|-----|
| `GCP_WIF_PROVIDER` | Workload Identity Provider のリソース名 |
| `GCP_DEPLOY_SA` | `github-actions-back@ec-site-handson.iam.gserviceaccount.com` |

> デプロイ用サービスアカウントの権限は `roles/artifactregistry.writer` /
> `roles/run.developer` / `roles/iam.serviceAccountUser` の 3 つのみ。
> Cloud Build の権限は無いため、`gcloud builds submit` や
> `gcloud run deploy --source .` は CI からは使えない（ビルドはランナー上の `docker build`）。
>
> ロールバックはコミットハッシュのタグを指定して `gcloud run deploy` する。
> `latest` は最新を指すだけなのでロールバック先には使わない。

ローカルから手動でデプロイする場合も、イメージの置き場を分散させないため、
同じ Artifact Registry リポジトリへビルドしてから反映する 2 段階の手順を踏む。

```bash
# 事前に gcloud auth login で認証しておく

# ① 同じリポジトリへイメージをビルドして push
gcloud builds submit \
  --tag asia-northeast1-docker.pkg.dev/ec-site-handson/sample-ec/sample-ec-back:manual-$(date +%Y%m%d%H%M)

# ② 作成したイメージでリビジョンを更新
gcloud run deploy sample-ec-back \
  --image <① で指定したタグ> \
  --region asia-northeast1
```

> **`gcloud run deploy --source .` は使わないこと。**
> `--source` は Cloud Build が自動作成する `cloud-run-source-deploy` リポジトリへ
> push するため、イメージの置き場が 2 つに分かれる。このリポジトリは Terraform 管理外で
> `terraform destroy` しても残り、気づかないまま保管料が発生し続ける。
> またロールバック時に、対象リビジョンのイメージがどちらのリポジトリのものか追いにくい。

> **`--set-env-vars` / `--set-secrets` / `--max-instances` / `--allow-unauthenticated` を付けないこと。**
> これらは Terraform の管理対象で、`gcloud` 側から指定すると状態が乖離し、
> 次回の `terraform apply` で巻き戻る。オプションを指定しなければ既存の設定は維持される。
>
> Terraform は Cloud Run の `image` を `lifecycle { ignore_changes }` の対象にしているため、
> ここでデプロイしたイメージが `terraform apply` で初期イメージへ戻ることはない。

設定そのものを変えたい場合は、`gcloud` ではなく `sample-ec-infra` 側を修正する。

### 注意事項

- **最大インスタンス数を 1 に固定する必要がある**。H2 インメモリ DB はインスタンスごとに独立するため、複数インスタンスへスケールすると注文データなどに不整合が生じる。恒常運用する場合は Cloud SQL への移行が必要（設定は `sample-ec-infra` 側）
- データは永続化されない。インスタンスの再起動・スケールインのたびに初期状態へ戻る
- **`H2_CONSOLE_ENABLED=false` が設定されている必要がある**。H2 コンソールは認証不要で公開されるため、有効のままでは誰でも DB を操作できてしまう（設定は `sample-ec-infra` 側）
- `PORT` は Cloud Run が自動的に注入するため指定不要
- **Cloud Load Balancing のカスタムエラーレスポンスポリシーを `/api/*` に適用しないこと。**
  フロントエンドは SPA のため、404 時に `index.html` を 200 で返すポリシーをバケット側（`/*`）に設定する。
  これを `/api/*` にも適用すると、本 API が返す 404 が `index.html`（200）へ書き換えられ、
  エラーハンドリングが壊れる（「17. トラブルシューティング」参照）

### ログの確認

```bash
# 直近のログを表示
gcloud run services logs read sample-ec-back --region asia-northeast1 --limit 50

# ログを追従表示
gcloud run services logs tail sample-ec-back --region asia-northeast1
```

起動に失敗する場合は、まず `JWT_SECRET` が正しく渡っているかをログで確認する
（未設定の場合は起動時に日本語のエラーメッセージが出力される）。

---

## 16. 動作確認

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

フロントエンド（[SampleECSite](https://github.com/TMiyazaki16/SampleECSite)）は、
バックエンド接続用の設定ファイル `.env.backend` をリポジトリに含んでいるため、
**追加の設定ファイルを作成する必要はない。**

```bash
cd ../SampleECSite
npm install        # 初回のみ
npm run dev        # = vite --mode backend
```

http://localhost:5173 でブラウザ確認。テストユーザーは下記「テストデータ」を参照。

| コマンド | 読み込まれる設定 | 接続先 | 用途 |
|---------|----------------|-------|------|
| `npm run dev` | `.env.backend` | `http://localhost:8080/api` | 本バックエンドに接続する |
| `npm run mock` | `.env.development` | `http://localhost:5173/api` | MSW のモック API で動作する（バックエンド不要） |

> Vite は `--mode` で読み込む `.env.<mode>` を切り替える。`npm run dev` は `vite --mode backend` のため
> `.env.backend` が使われ、MSW は起動しない。

接続できない場合は「17. トラブルシューティング」を参照。

---

## 17. トラブルシューティング

### 起動時に「環境変数 JWT_SECRET が設定されていません」と出る

`JWT_SECRET` は必須。Base64 エンコードした 32 バイト以上の値を指定する。

```bash
openssl rand -base64 32
```

短すぎる場合は「JWT_SECRET が短すぎます（デコード後 N バイト）」、
Base64 として不正な場合は「Base64 として解釈できません」と出力される。

### `docker: Error response ... name "sample-ec-back" is already in use`

同名のコンテナが残っている。削除してから起動する。

```bash
docker rm -f sample-ec-back
```

### ブラウザのコンソールに CORS エラーが出る

バックエンドの `ALLOWED_ORIGINS` と、ブラウザが送信する `Origin` が一致していない。

```bash
# 実際に許可されているか確認する（200 と Access-Control-Allow-Origin が返れば正常）
curl -i -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST"
```

許可されていないオリジンの場合は 403 が返る。
ポート番号やスキーム（http / https）の違いも別オリジンとして扱われる点に注意。

### API が 401 を返す

- `Authorization: Bearer <トークン>` ヘッダーが付いているか
- トークンの有効期限（既定 24 時間）が切れていないか
- **コンテナを再起動していないか** — `JWT_SECRET` を毎回ランダム生成している場合、
  再起動すると署名鍵が変わり、発行済みトークンはすべて無効になる。再ログインが必要

### 本番環境で API が JSON ではなく HTML を返す / 404 のはずが 200 で返る

Cloud Load Balancing のカスタムエラーレスポンスポリシーが `/api/*` にも適用されている可能性が高い。

```
GET /api/nonexistent
  → Cloud Run      : 404 {"message":"指定されたリソースが見つかりません"}
  → LB がポリシー適用 : 200 + index.html（HTML）
  → フロントエンドは成功と判定し、JSON パースで失敗する
```

エラーレスポンスポリシーはバケット側のパスマッチャ（`/*`）にのみ適用し、
`/api/*` のパスマッチャには設定しないこと。

ローカルのコンテナで同じリクエストを投げて 404 と JSON が返るなら、原因はアプリ側ではなく
ロードバランサの設定にある。

```bash
# /api/** は認証必須のため、トークンを付けて確認する（付けない場合は 401 になる）
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["token"])')

curl -i http://localhost:8080/api/nonexistent -H "Authorization: Bearer $TOKEN"
```

### データが消えている

H2 インメモリ DB のため、コンテナを停止するとデータは失われる（仕様）。
次回起動時に `data.sql` の初期状態へ戻る。

---

## 18. 既知の制約

本プロジェクトはサンプル実装のため、以下の制約がある。

| 制約 | 内容 | 影響 |
|------|------|------|
| データが永続化されない | H2 インメモリ DB を使用 | 再起動・スケールインのたびに初期状態へ戻る |
| 水平スケールできない | DB がインスタンスごとに独立する | インフラ側（`sample-ec-infra`）で `max_instance_count = 1` に固定している。恒常運用には Cloud SQL への移行が必要で、それまでこの固定は解除できない |
| ログアウトがサーバー側で機能しない | `POST /api/auth/logout` は `success: true` を返すのみ | ログアウト後もトークンの控えがあれば有効期限内は使用できる。実際のログアウトはフロントエンドがトークンを破棄することで成立している |
| トークンを即時失効できない | ステートレスな JWT を採用しているため | 最大 24 時間（`JWT_EXPIRATION_HOURS`）有効なまま。即時失効が必要な場合は有効期間の短縮 + リフレッシュトークンの併用が必要 |
| ヘルスチェック用エンドポイントがない | Spring Boot Actuator 未導入 | Cloud Run は既定の TCP チェックで起動を判定するため現状は支障なし。詳細な死活監視が必要なら Actuator の導入を検討 |
| H2 コンソールが認証なしで公開される | Spring Security で `permitAll` 設定 | **インフラ側（`sample-ec-infra`）で `H2_CONSOLE_ENABLED=false` を設定している。** アプリ側の既定値は `true` のため、この設定が外れると誰でも DB を操作できる |

---

## API 一覧

| メソッド | エンドポイント | 認証 | 概要 |
|--------|------------|------|------|
| POST | `/api/auth/login` | 不要 | ログイン → JWT トークン取得 |
| POST | `/api/auth/logout` | 必要 | ログアウト（※サーバー側では何もせず `success: true` を返すのみ。下記「既知の制約」参照） |
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
