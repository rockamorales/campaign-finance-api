
docker run --rm --network campaign-finance-api_default -v "events-journal-db-cassandra.cql:/scripts/data.cql" -e CQLSH_HOST=campaign_finance_cassandra -e CQLSH_PORT=9042 -e CQLVERSION=3.4.5 nuvo/docker-cqlsh