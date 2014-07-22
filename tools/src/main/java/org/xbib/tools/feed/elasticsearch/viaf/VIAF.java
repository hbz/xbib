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
package org.xbib.tools.feed.elasticsearch.viaf;

import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.rdfxml.RdfXmlReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.Feeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * VIAF indexer to Elasticsearch
 */
public class VIAF extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(VIAF.class.getSimpleName());

    private static BlockingQueue<String> pump;

    private static ExecutorService pumpService;


    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new VIAF();
            }
        };
    }

    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        for (int i = 0; i < settings.getAsInt("pumps", 1); i++) {
            pumpService.submit(new VIAFPipeline());
        }
        String line;
        long linecounter = 0;
        while ((line = reader.readLine()) != null) {
            pump.put(line);
            linecounter++;
            if (linecounter % 10000 == 0) {
                logger.info("{}", linecounter);
            }
        }
        in.close();
        for (int i = 0; i < settings.getAsInt("pumps", 1); i++) {
            pump.put("|");
        }
    }

    private class VIAFPipeline implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            try {
                while (true) {
                    String line = pump.take();
                    if ("|".equals(line)) {
                        break;
                    }
                    final ElasticBuilder builder = new ElasticBuilder();
                    RdfXmlReader rdfxml = new RdfXmlReader();
                    rdfxml.parse(new StringReader(line), builder);
                    builder.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
            return true;
        }
    }

    private final ResourceContext context = new SimpleResourceContext();

    private class ElasticBuilder implements TripleListener {

        private Resource resource;

        ElasticBuilder() throws IOException {
            resource = context.newResource();
        }

        @Override
        public TripleListener begin() {
            return this;
        }

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ElasticBuilder newIdentifier(IRI iri) {
            flush();
            resource.id(iri);
            return this;
        }

        @Override
        public ElasticBuilder triple(Triple triple) {
            resource.add(triple);
            return this;
        }

        @Override
        public TripleListener end() {
            return this;
        }

        public void close() throws IOException {
            flush();
        }

        private void flush() {
            try {
                sink.write(context);
            } catch (IOException e) {
                logger.error("flush failed: {}", e.getMessage(), e);
            }
            resource = context.newResource();
        }

    }

}

