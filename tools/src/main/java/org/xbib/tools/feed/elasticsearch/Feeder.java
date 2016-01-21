/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.tools.feed.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.metric.MeterMetric;
import org.xbib.tools.convert.Converter;
import org.xbib.time.DurationFormatUtil;
import org.xbib.tools.output.ElasticsearchOutput;
import org.xbib.tools.output.IndexDefinition;
import org.xbib.util.FormatUtil;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Feeder extends Converter {

    private final static Logger logger = LogManager.getLogger(Feeder.class);

    protected Ingest ingest;

    protected ElasticsearchOutput elasticsearchOutput = new ElasticsearchOutput();

    protected Map<String,IndexDefinition> indexDefinitionMap = new LinkedHashMap<>();

    protected void setIngest(Ingest ingest) {
        this.ingest = ingest;
    }

    protected void setIndexDefinitionMap(Map<String,IndexDefinition> indexDefinitionMap) {
        this.indexDefinitionMap = indexDefinitionMap;
    }

    @Override
    protected void prepareOutput() throws IOException {
        super.prepareOutput();
        Map<String,Settings> outputMap = settings.getGroups("output");
        for (Map.Entry<String,Settings> entry : outputMap.entrySet()) {
            if ("elasticsearch".equals(entry.getKey())) {
                logger.info("preparing Elasticsearch for output");
                prepareElasticsearch(entry.getValue());
            }
        }
    }

    protected void prepareElasticsearch(Settings elasticsearchSettings) throws IOException {
        ingest = elasticsearchOutput.createIngest(elasticsearchSettings);
        if (ingest != null) {
            indexDefinitionMap = elasticsearchOutput.makeIndexDefinitions(ingest, elasticsearchSettings.getGroups("index"));
            logger.info("creation of {}", indexDefinitionMap.keySet());
            for (Map.Entry<String,IndexDefinition> entry : indexDefinitionMap.entrySet()) {
                elasticsearchOutput.createIndex(ingest, entry.getValue());
            }
            logger.info("startup of {}", indexDefinitionMap.keySet());
            elasticsearchOutput.startup(ingest, indexDefinitionMap);
        }
    }

    @Override
    protected void disposeOutput() throws IOException {
        logger.info("close down of {}", indexDefinitionMap.keySet());
        elasticsearchOutput.close(ingest, indexDefinitionMap);
        performIndexSwitch();
        for (Map.Entry<String,IndexDefinition> entry : indexDefinitionMap.entrySet()) {
            elasticsearchOutput.replica(ingest, entry.getValue());
        }
        elasticsearchOutput.shutdown(ingest);
        super.disposeOutput();
    }

    protected void performIndexSwitch() throws IOException {
        IndexDefinition def = indexDefinitionMap.get("bib");
        if (def != null && def.getTimeWindow() != null) {
            logger.info("switching index {}", def.getIndex());
            elasticsearchOutput.switchIndex(ingest, def, Collections.singletonList(def.getIndex()));
            logger.info("performing retention policy for index {}", def.getIndex());
            elasticsearchOutput.retention(ingest, def);
        }
    }

    @Override
    protected void writeMetrics(MeterMetric metric) throws Exception {
        if (metric == null) {
            return;
        }
        long docs = metric.count();
        double mean = metric.meanRate();
        double oneminute = metric.oneMinuteRate();
        double fiveminute = metric.fiveMinuteRate();
        double fifteenminute = metric.fifteenMinuteRate();
        long bytes = ingest != null && ingest.getMetric() != null ?
                ingest.getMetric().getTotalIngestSizeInBytes().count() : 0;
        //long bytes = 0;
        long elapsed = metric.elapsed() / 1000000;
        String elapsedhuman = DurationFormatUtil.formatDurationWords(elapsed, true, true);
        double avg = bytes / (docs + 1.0); // avoid div by zero
        double mbps = (bytes * 1000.0 / elapsed) / (1024.0 * 1024.0);
        NumberFormat formatter = NumberFormat.getNumberInstance();
        logger.info("indexing metrics: elapsed {}, {} docs, {} bytes, {} avgsize, {} MB/s, {} ({} {} {})",
                elapsedhuman,
                docs,
                FormatUtil.convertFileSize(bytes),
                FormatUtil.convertFileSize(avg),
                formatter.format(mbps),
                mean,
                oneminute,
                fiveminute,
                fifteenminute
        );
    }

    @Override
    protected ForkJoinPipeline newPipeline() {
        return new ConfiguredPipeline();
    }

    @Override
    public Feeder setPipeline(Pipeline<Converter,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        if (pipeline instanceof ConfiguredPipeline) {
            ConfiguredPipeline configuredPipeline = (ConfiguredPipeline) pipeline;
            setSettings(configuredPipeline.getSettings());
            setIngest(configuredPipeline.getIngest());
            setIndexDefinitionMap(configuredPipeline.getIndexDefinitionMap());
        }
        return this;
    }

    class ConfiguredPipeline extends ForkJoinPipeline {
        public org.xbib.common.settings.Settings getSettings() {
            return settings;
        }
        public Ingest getIngest() {
            return ingest;
        }
        public Map<String,IndexDefinition> getIndexDefinitionMap() {
            return indexDefinitionMap;
        }
    }

}
