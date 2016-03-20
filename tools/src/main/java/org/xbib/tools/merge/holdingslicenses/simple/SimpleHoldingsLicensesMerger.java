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
package org.xbib.tools.merge.holdingslicenses.simple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.etl.support.StatusCodeMapper;
import org.xbib.etl.support.ValueMaps;
import org.xbib.metrics.Meter;
import org.xbib.tools.merge.Merger;
import org.xbib.tools.merge.holdingslicenses.entities.SerialRecord;
import org.xbib.tools.merge.holdingslicenses.support.BibdatLookup;
import org.xbib.tools.merge.holdingslicenses.support.BlackListedISIL;
import org.xbib.tools.merge.holdingslicenses.support.ConsortiaLookup;
import org.xbib.tools.merge.holdingslicenses.support.MappedISIL;
import org.xbib.tools.merge.holdingslicenses.support.SerialRecordRequest;
import org.xbib.tools.metrics.Metrics;
import org.xbib.util.ExceptionFormatter;
import org.xbib.util.IndexDefinition;
import org.xbib.util.Strings;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class SimpleHoldingsLicensesMerger extends Merger {

    private final static Logger logger = LogManager.getLogger(SimpleHoldingsLicensesMerger.class);

    private SimpleHoldingsLicensesMerger simpleHoldingsLicensesMerger;

    private BibdatLookup bibdatLookup;

    private ConsortiaLookup consortiaLookup;

    private BlackListedISIL isilbl;

    private MappedISIL isilMapped;

    private StatusCodeMapper statusCodeMapper;

    private Metrics metrics;

    private Meter queryMetric;

    private String sourceTitleIndex;
    private String sourceHoldingsIndex;
    private String sourceLicenseIndex;
    private String sourceIndicatorIndex;
    private String sourceMonographicIndex;
    private String sourceMonographicHoldingsIndex;
    private String sourceOpenAccessIndex;

    private String manifestationsIndex;
    private String manifestationsIndexType;
    private String holdingsIndex;
    private String holdingsIndexType;
    private String volumesIndex;
    private String volumesIndexType;
    private String servicesIndex;
    private String servicesIndexType;

    @Override
    @SuppressWarnings("unchecked")
    public int run(Settings settings) throws Exception {
        this.simpleHoldingsLicensesMerger = this;
        this.metrics = new Metrics();
        this.queryMetric = new Meter();
        queryMetric.spawn(5L);
        metrics.scheduleMetrics(settings, "meterquery", queryMetric);
        return super.run(settings);
    }


    protected void waitFor() throws IOException {
        try {
            // send poison elements and wait for completion
            getPipeline().waitFor(new SerialRecordRequest());
        } finally {
            long total = 0L;
            for (SimpleHoldingsLicensesWorker worker : getPipeline().getWorkers()) {
                logger.info("worker {}, count {}, took {}",
                        worker,
                        worker.getMetric().getCount(),
                        TimeValue.timeValueNanos(worker.getMetric().elapsed()).format());
                total += worker.getMetric().getCount();
            }
            logger.info("worker metric count total = {}", total);
            metrics.append("meterquery", queryMetric);
        }
    }

    @SuppressWarnings("unchecked")
    public Pipeline<SimpleHoldingsLicensesWorker, SerialRecordRequest> getPipeline() {
        return pipeline;
    }

    @Override
    protected WorkerProvider provider() {
        return new WorkerProvider<SimpleHoldingsLicensesWorker>() {
            int i = 0;

            @Override
            @SuppressWarnings("unchecked")
            public SimpleHoldingsLicensesWorker get(Pipeline pipeline) {
                return (SimpleHoldingsLicensesWorker) new SimpleHoldingsLicensesWorker(settings,
                        simpleHoldingsLicensesMerger,
                        i++)
                        .setPipeline(pipeline);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void prepareRequests() throws Exception {
        super.prepareRequests();
        Map<String,IndexDefinition> indexDefinitionMap = getInputIndexDefinitionMap();
        this.sourceTitleIndex = indexDefinitionMap.get("zdb").getIndex();
        if (Strings.isNullOrEmpty(sourceTitleIndex)) {
            throw new IllegalArgumentException("no zdb index given");
        }
        this.sourceHoldingsIndex = indexDefinitionMap.get("zdbholdings").getIndex();
        if (Strings.isNullOrEmpty(sourceHoldingsIndex)) {
            throw new IllegalArgumentException("no zdbholdings index given");
        }
        this.sourceLicenseIndex = indexDefinitionMap.get("ezbxml").getIndex();
        if (Strings.isNullOrEmpty(sourceLicenseIndex)) {
            throw new IllegalArgumentException("no ezbxml index given");
        }
        this.sourceIndicatorIndex = indexDefinitionMap.get("ezbweb").getIndex();
        if (Strings.isNullOrEmpty(sourceIndicatorIndex)) {
            throw new IllegalArgumentException("no ezbweb index given");
        }
        this.sourceMonographicIndex = indexDefinitionMap.get("hbz").getIndex();
        if (Strings.isNullOrEmpty(sourceMonographicIndex)) {
            throw new IllegalArgumentException("no hbz index given");
        }
        this.sourceMonographicHoldingsIndex = indexDefinitionMap.get("hbzholdings").getIndex();
        if (Strings.isNullOrEmpty(sourceMonographicHoldingsIndex)) {
            throw new IllegalArgumentException("no hbzholdings index given");
        }
        this.sourceOpenAccessIndex = indexDefinitionMap.get("doaj").getIndex();
        if (Strings.isNullOrEmpty(sourceOpenAccessIndex)) {
            throw new IllegalArgumentException("no doaj index given");
        }
        logger.info("preparing bibdat lookup...");
        bibdatLookup = new BibdatLookup();
        try {
            bibdatLookup.buildLookup(search.client(), indexDefinitionMap.get("bibdat").getIndex());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("bibdat prepared, {} names, {} organizations, {} regions, {} other",
                bibdatLookup.lookupName().size(),
                bibdatLookup.lookupOrganization().size(),
                bibdatLookup.lookupRegion().size(),
                bibdatLookup.lookupOther().size());

        // prepare "national license" / consortia ISIL expansion
        consortiaLookup = new ConsortiaLookup();
        try {
            consortiaLookup.buildLookup(search.client(), indexDefinitionMap.get("nlzisil").getIndex());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("preparing ISIL blacklist...");
        isilbl = new BlackListedISIL();
        try (InputStream in = getClass().getResourceAsStream("isil.blacklist")) {
            isilbl.buildLookup(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("ISIL blacklist prepared, size = {}", isilbl.lookup().size());

        logger.info("preparing mapped ISIL...");
        isilMapped = new MappedISIL();
        try (InputStream in = getClass().getResourceAsStream("isil.map")) {
            isilMapped.buildLookup(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("mapped ISILs prepared, size = {}", isilMapped.lookup().size());

        logger.info("preparing status code mapper...");
        ValueMaps valueMaps = new ValueMaps();
        Map<String,Object> statuscodes = valueMaps.getMap("org/xbib/analyzer/mab/status.json", "status");
        statusCodeMapper = new StatusCodeMapper();
        statusCodeMapper.add(statuscodes);
        logger.info("status code mapper prepared, size = {}", statusCodeMapper.getMap().size());

        Map<String,IndexDefinition> outputIndexDefinitionMap = simpleHoldingsLicensesMerger.getOutputIndexDefinitionMap();
        String indexName = outputIndexDefinitionMap.get("holdingslicenses").getConcreteIndex();
        this.manifestationsIndex = indexName;
        this.manifestationsIndexType = "manifestations";
        this.holdingsIndex = indexName;
        this.holdingsIndexType = "holdings";
        this.volumesIndex = indexName;
        this.volumesIndexType = "volumes";
        this.servicesIndex = indexName;
        this.servicesIndexType = "services";

        // all prepared. Enter loop over all title records
        int scrollSize = settings.getAsInt("scrollsize", 10);
        long scrollMillis = settings.getAsTime("scrolltimeout", org.xbib.common.unit.TimeValue.timeValueSeconds(60)).millis();
        boolean failure = false;
        SearchRequestBuilder searchRequest = search.client().prepareSearch()
                .setSize(scrollSize)
                .setScroll(TimeValue.timeValueMillis(scrollMillis))
                .addSort(SortBuilders.fieldSort("_doc"));
        searchRequest.setIndices(indexDefinitionMap.get("zdb").getIndex());
        // single identifier?
        String identifier = settings.get("identifier");
        if (identifier != null) {
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        logger.info("merging holdings/licenses for {} title records",
                searchResponse.getHits().getTotalHits());
        do {
            queryMetric.mark();
            for (SearchHit hit : searchResponse.getHits()) {
                try {
                    if (getPipeline().getWorkers().isEmpty()) {
                        logger.error("no more workers left to receive, aborting feed");
                        return;
                    }
                    SerialRecord serialRecord = new SerialRecord(hit.getSource());
                    SerialRecordRequest serialRecordRequest = new SerialRecordRequest().set(serialRecord);
                    getPipeline().putQueue(serialRecordRequest);
                } catch (Throwable e) {
                    logger.error("error passing data to workers, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    failure = true;
                    break;
                }
            }
            searchResponse = search.client()
                    .prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(scrollMillis))
                    .execute().actionGet();
        } while (!failure && searchResponse.getHits().getHits().length > 0);
        simpleHoldingsLicensesMerger.search().client()
                .prepareClearScroll().addScrollId(searchResponse.getScrollId())
                .execute().actionGet();
        logger.info("all title records processed");
    }

    @Override
    protected void disposeRequests(int returncode) throws IOException {
        super.disposeRequests(returncode);
    }

    @Override
    protected void disposeResources(int returncode) throws IOException {
        super.disposeResources(returncode);
    }

    public SearchTransportClient search() {
        return search;
    }

    public Ingest ingest() {
        return ingest;
    }

    public Settings settings() {
        return settings;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public BibdatLookup bibdatLookup() {
        return bibdatLookup;
    }

    public ConsortiaLookup consortiaLookup() {
        return consortiaLookup;
    }

    public BlackListedISIL blackListedISIL() {
        return isilbl;
    }

    public MappedISIL mappedISIL() {
        return isilMapped;
    }

    public StatusCodeMapper statusCodeMapper() {
        return statusCodeMapper;
    }

    public String getSourceTitleIndex() {
        return sourceTitleIndex;
    }

    public String getSourceHoldingsIndex() {
        return sourceHoldingsIndex;
    }

    public String getSourceLicenseIndex() {
        return sourceLicenseIndex;
    }

    public String getSourceIndicatorIndex() {
        return sourceIndicatorIndex;
    }

    public String getSourceMonographicIndex() {
        return sourceMonographicIndex;
    }

    public String getSourceMonographicHoldingsIndex() {
        return sourceMonographicHoldingsIndex;
    }

    public String getSourceOpenAccessIndex() {
        return sourceOpenAccessIndex;
    }

    public String getManifestationsIndex() {
        return manifestationsIndex;
    }

    public String getManifestationsIndexType() {
        return manifestationsIndexType;
    }

    public String getHoldingsIndex() {
        return holdingsIndex;
    }

    public String getHoldingsIndexType() {
        return holdingsIndexType;
    }

    public String getVolumesIndex() {
        return volumesIndex;
    }

    public String getVolumesIndexType() {
        return volumesIndexType;
    }

    public String getServicesIndex() {
        return servicesIndex;
    }

    public String getServicesIndexType() {
        return servicesIndexType;
    }

}