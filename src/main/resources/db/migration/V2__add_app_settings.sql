-- App-wide settings persisted in a single row (id = 1)
CREATE TABLE IF NOT EXISTS app_settings (
    id              BIGINT PRIMARY KEY DEFAULT 1,
    cluster_domains TEXT NOT NULL DEFAULT '{}',
    service_cluster TEXT NOT NULL DEFAULT '{}'
);

-- Seed with sensible defaults so the row always exists
INSERT INTO app_settings (id, cluster_domains, service_cluster)
VALUES (
    1,
    '{"a-cluster":"","b-cluster":"","c-cluster":""}',
    '{"Web":"b-cluster","Counter":"b-cluster","QDCA10":"b-cluster","QDCA10Pro":"b-cluster","Inventory":"b-cluster","Homeoffice":"a-cluster","HomeofficUI":"a-cluster"}'
)
ON CONFLICT (id) DO NOTHING;
