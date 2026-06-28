# Homeoffice Backend マイクロサービス

## 概要

Homeoffice Backend はドローンショップの **ホームオフィス管理バックエンドサービス** です。

- ホームオフィスからの注文管理・閲覧機能を提供
- GraphQL API でフロントエンド（homeoffice-ui）と連携
- 注文・在庫データを PostgreSQL から取得

**フレームワーク**: Quarkus  
**デプロイ先クラスター**: c-cluster

---

## アーキテクチャ

```
Homeoffice UI（c-cluster）
        │
        ▼ GraphQL
┌────────────────────┐
│  Homeoffice        │
│  Backend           │──► PostgreSQL (droneshopdb)
│                    │
│  /graphql          │──► Kafka（注文状況監視）
└────────────────────┘
```

### API エンドポイント

| エンドポイント | プロトコル | 説明 |
|--------------|----------|------|
| `/graphql` | GraphQL | 注文・在庫クエリ |
| `/q/health` | HTTP | ヘルスチェック |

### 依存サービス

- **PostgreSQL** (droneshopdb): 注文・在庫データ参照
- **quarkusdroneshop-homeoffice-ui**: フロントエンド

---

## ローカル開発

### 前提条件

- Java 17+
- Docker / Docker Compose

### 1. インフラ起動

```shell
git clone https://github.com/quarkusdroneshop/quarkusdroneshop-support.git
cd quarkusdroneshop-support
docker compose up -d
```

### 2. アプリケーション起動

```shell
git clone https://github.com/quarkusdroneshop/quarkusdroneshop-homeoffice.git
cd quarkusdroneshop-homeoffice
./mvnw clean compile quarkus:dev
```

GraphQL UI: http://localhost:8080/q/graphql-ui

### 環境変数

| 変数名 | デフォルト | 説明 |
|--------|-----------|------|
| `PGSQL_URL` | `jdbc:postgresql://localhost:5432/droneshopdb?currentSchema=droneshop` | DB 接続 URL |
| `PGSQL_USER` | `droneshopuser` | DB ユーザー名 |
| `PGSQL_PASS` | `redhat-21` | DB パスワード |

### GraphQL クエリ例

```graphql
# 全注文一覧
query {
  orders {
    orderId
    item
    status
    location
  }
}
```

---

## 本番デプロイ（Tekton Pipeline）

### パイプライン概要

```
fetch-repository → semgrep-scan → maven-run → push-oc-apps
```

### 手動実行

```shell
tkn pipeline start build-and-push-quarkusdroneshop-homeoffice \
  -n quarkusdroneshop-cicd \
  --use-param-defaults
```

### Native ビルド（オプション）

```shell
# GraalVM なしでネイティブビルド
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

---

## テスト

```shell
# ユニットテスト(ArchUnit含む)
./mvnw test

# 統合テスト（Jacoco含む）
./mvnw verify

# チェックスタイル
./mvnw checkstyle:check

# PMD
./mvnw pmd:pmd

# SpotBugs
./mvnw spotbugs:spotbugs

# semgrep
semgrep scan --config p/default --json > target/semgrep-results.json

# secret scan
gitleaks detect --source . --report-format json --report-path target/gitleaks-report.json --exit-code 1

# 脆弱性テスト
trivy fs --scanners vuln,secret,misconfig,license --exit-code=1 --ignorefile ./.trivyignore.yaml ./ > target/trivy.txt

# セキュリティテスト
mvn quarkus:dev > quarkus.log 2>&1 & QUARKUS_PID=$!; sleep 10; wapiti -u http://localhost:8080 -f json -o ./target/wapiti.json; kill $QUARKUS_PID

# テストレポートの作成
./mvnw exec:exec@generate-report
```

---

## 注意事項

- **GraphQL スキーマ**: `src/main/resources/graphql/` に定義。スキーマ変更時は homeoffice-ui との互換性を確認してください。
- **読み取り専用**: このサービスは主に参照系 API です。注文作成は Web サービス経由で行います。
- **c-cluster 接続**: RHDH の Kubernetes タブで c-cluster のポッド状態を確認できます。
