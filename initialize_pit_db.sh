#!/usr/bin/env bash

docker run --rm --network cassandra -v "sql/campaign-finance-db.cql:/scripts/data.cql" -e CQLSH_HOST=cassandra -e CQLSH_PORT=9042 -e CQLVERSION=3.4.5 nuvo/docker-cqlsh