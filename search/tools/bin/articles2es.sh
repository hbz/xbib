#!/bin/bash

java \
    -cp bin:lib/xbib-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.ArticleDB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --mock false \
    --threads 1 \
    --maxbulkactions 100 \
    --maxconcurrentbulkrequests 1 \
    --index "works" \
    --type "articles" \
    --path "${HOME}/import/crossref" \
    --pattern "*.json"