# BigQuery Analytics Setup

End-to-end runbook for loading HealthySocial data into BigQuery and pointing the backend at it. All commands are intended to be run from **Cloud Shell** (https://shell.cloud.google.com).

## Architecture

```
┌──────────────────┐    1. \COPY        ┌──────────┐    3. bq load    ┌───────────────┐
│ Postgres on VM   │ ─────────────────▶ │   GCS    │ ───────────────▶ │  BigQuery     │
│ (Compute Engine) │   (Cloud Shell)    │  bucket  │                  │  dataset      │
└──────────────────┘                    └──────────┘                  └───────┬───────┘
                                                                              │ 4. queries
                                                                      ┌───────▼───────┐
                                                                      │ Spring Boot   │
                                                                      │ /api/analytics│
                                                                      └───────┬───────┘
                                                                              │
                                                                      ┌───────▼───────┐
                                                                      │ React UI      │
                                                                      │ /analytics    │
                                                                      └───────────────┘
```

GCP services used: **Compute Engine** (existing VM hosting Postgres), **Cloud Shell** (runs the export), **Cloud Storage** (CSV staging), **BigQuery** (warehouse + queries).

## Prerequisites

Granted to your Google account on the GCP project:
- `BigQuery Admin`
- `Storage Admin`
- `Service Account User`

A read-only Postgres user reachable from Cloud Shell (port 5432 open or via SSH tunnel).

## Variables

Set these once at the top of your Cloud Shell session:

```bash
export PROJECT_ID="project-48fb2f0d-5b16-4887-a6d"
export REGION="europe-central2"
export BUCKET="gs://healthysocial-bq-staging"
export DATASET="healthysocial_analytics"
export PG_HOST="34.158.235.57"
export PG_USER="bq_export"
export PG_DB="healthy_social"
read -s -p "Postgres password: " PGPASSWORD && export PGPASSWORD && echo
gcloud config set project "$PROJECT_ID"
```

## 1. Create the staging bucket and BigQuery dataset (once)

```bash
gcloud storage buckets create "$BUCKET" \
    --location="$REGION" \
    --project="$PROJECT_ID"

bq --location="$REGION" mk --dataset "$PROJECT_ID:$DATASET"
```

## 2. Export Postgres tables to CSV

```bash
TABLES="users habits habit_logs goals posts comments likes follows challenges challenge_participants"

mkdir -p /tmp/bq-export && cd /tmp/bq-export
for t in $TABLES; do
  echo "Exporting $t…"
  psql -h "$PG_HOST" -U "$PG_USER" -d "$PG_DB" \
       -c "\COPY $t TO '/tmp/bq-export/$t.csv' CSV HEADER"
done
ls -la
```

## 3. Upload to GCS

```bash
gcloud storage cp /tmp/bq-export/*.csv "$BUCKET/raw/"
gcloud storage ls "$BUCKET/raw/"
```

## 4. Load into BigQuery

```bash
for t in $TABLES; do
  echo "Loading $t…"
  bq load \
      --autodetect \
      --replace \
      --source_format=CSV \
      --skip_leading_rows=1 \
      "$DATASET.$t" \
      "$BUCKET/raw/$t.csv"
done

bq ls "$DATASET"
```

## 5. Smoke-test a query

```bash
bq query --use_legacy_sql=false \
  "SELECT COUNT(*) AS users FROM \`$PROJECT_ID.$DATASET.users\`"
```

## 6. Configure the backend

The VM has a service account attached directly (`healthysocial-backend-bq@…`), so the Spring Boot process picks up credentials via Application Default Credentials from the metadata server — no JSON key file required. Just set these env vars on the backend process (docker-compose `environment:` or systemd unit):

```
BQ_ENABLED=true
BQ_PROJECT_ID=project-48fb2f0d-5b16-4887-a6d
BQ_DATASET=healthysocial_analytics
BQ_LOCATION=europe-central2
```

The attached SA needs roles `BigQuery Job User` + `BigQuery Data Viewer`.

Restart the backend, then verify:

```bash
curl http://34.158.235.57:8080/api/analytics/top-users?limit=3
```

## 7. Refresh the data (later)

The export is idempotent — `--replace` rewrites each table. Rerun steps 2-4 whenever you want fresh data. For a student project this is fine to do manually.

## Troubleshooting

- **`psql: connection refused`** — port 5432 isn't open from Cloud Shell. Easiest fix: add a temporary firewall rule allowing `0.0.0.0/0` on tcp:5432, run the export, then remove the rule.
- **`Access Denied: BigQuery BigQuery: Permission … denied`** — your account is missing `BigQuery Admin`, or the service account is missing `BigQuery Data Viewer`.
- **`Application Default Credentials not found`** on the backend — `GOOGLE_APPLICATION_CREDENTIALS` is unset or points to a non-existent file. Confirm the path inside the running container, not just the host.
- **Empty results from `/api/analytics/*`** — check the backend logs for `BigQuery query failed`. The service swallows BigQuery errors and returns `[]` so the UI degrades gracefully; the cause is always logged.
