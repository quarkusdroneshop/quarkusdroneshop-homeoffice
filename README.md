# homeoffice-backend

Quarkus ベースの GraphQL API サービス。複数店舗の売上・注文データを集計し、ホームオフィスダッシュボード UI (`homeoffice-ui`) に提供します。

- **バージョン**: 2.0.4
- **Quarkus**: 3.33.2
- **主要ライブラリ**: SmallRye GraphQL, Hibernate ORM Panache, JDBC PostgreSQL, SmallRye Reactive Messaging (Kafka), Flyway

## アーキテクチャ

```
homeoffice-ui
    │  (GraphQL over HTTPS)
    ▼
homeoffice-backend  ──Kafka consumer──▶  shop-asite.orders-in
                                    ──Kafka consumer──▶  shop-bsite.orders-up
                                    ──Kafka consumer──▶  shop-asite.loyalty-updates
                    ──PostgreSQL──▶  droneshopdb (schema: droneshop)
```

## GraphQL API エンドポイント

`/graphql` (SmallRye GraphQL)

| Query | 説明 |
|---|---|
| `ordersForLocation(location)` | 指定ロケーションの注文一覧 |
| `getOrdersByLocation` | 全ロケーションの注文集計 |
| `getItemSales` | 商品別売上合計 |
| `getItemSalesTotalsByDate(startDate, endDate)` | 期間別・商品別売上合計 |
| `getProductSalesByDate(startDate, endDate)` | 日別・商品別売上推移（DB永続化） |
| `getStoreServerSales` | 店舗別・サーバー別売上 |
| `getStoreServerSalesByDate(startDate, endDate)` | 期間指定・店舗別・サーバー別売上 |
| `getAverageOrderUpTime(startDate, endDate)` | 平均注文処理時間（ミリ秒） |
| `liveOrders` | 直近4時間のライブ注文（IN_QUEUE / IN_PROGRESS / FULFILLED） |

GraphQL UI: `/q/graphql-ui` (always-include=true)

## Kafka トピック

| チャネル | トピック | 方向 |
|---|---|---|
| orders-created | `shop-asite.orders-in` | 受信 |
| orders-updated | `shop-bsite.orders-up` | 受信 |
| loyalty-member-purchase | `shop-asite.loyalty-updates` | 受信 |

## CORS 設定

`CorsRouteFilter.java` (Vert.x `@RouteFilter` / `@ApplicationScoped`) が SmallRye GraphQL より先に OPTIONS プリフライトを処理し、`Access-Control-Allow-Origin: *` を返します。これは SmallRye GraphQL が Quarkus CORS フィルターより先に OPTIONS を横取りするための回避策です。

## ローカル開発

### 前提条件

- JDK 17 以上
- Docker (PostgreSQL + Kafka 起動用)

### インフラ起動

```shell
git clone https://github.com/quarkusdroneshop/quarkusdroneshop-support.git
cd quarkusdroneshop-support
docker compose up
```

### アプリ起動

```shell
./mvnw quarkus:dev
```

dev モードでは以下の設定が適用されます:
- `POSTGRESQL_JDBC_URL` → `jdbc:postgresql://localhost:5432/droneshopdb?sslmode=disable`
- `KAFKA_BOOTSTRAP_URLS` → `localhost:9092`

## 環境変数 (本番)

| 変数名 | 説明 |
|---|---|
| `POSTGRESQL_JDBC_URL` | PostgreSQL JDBC URL |
| `POSTGRESQL_USER` | DB ユーザー名 |
| `POSTGRESQL_PASSWORD` | DB パスワード |
| `KAFKA_BOOTSTRAP_URLS` | Kafka ブローカー URL |
| `STORE_LOCATIONS` | 店舗ロケーション (例: `ATLANTA,TOKYO`) |

## OpenShift デプロイ

```shell
# Route に TLS edge termination を設定 (HTTPS アクセスに必須)
oc patch route homeoffice-backend -n quarkusdroneshop-demo \
  --type merge \
  -p '{"spec":{"tls":{"termination":"edge","insecureEdgeTerminationPolicy":"Allow"}}}'
```

## パッケージング

```shell
# JVM モード
./mvnw package

# ネイティブビルド (コンテナ内)
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

## 参考

- [Quarkus](https://quarkus.io/)
- [quarkusdroneshop.github.io](https://quarkusdroneshop.github.io)
