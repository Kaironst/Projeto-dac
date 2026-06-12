CREATE SCHEMA IF NOT EXISTS users_schema;

CREATE table IF NOT EXISTS outbox(
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type VARCHAR(255) NOT NULL,
  data_type VARCHAR(255) NOT NULL,
  data_id bigint NOT NULL,
  data JSONB NOT NULL,
  created_at TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT( NOW() AT TIME ZONE 'UTC' )
);
