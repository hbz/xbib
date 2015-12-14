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
package org.xbib.tools.feed.elasticsearch.serres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCEntityBuilderState;
import org.xbib.etl.marc.direct.MARCDirectQueue;
import org.xbib.util.InputService;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.tools.Feeder;
import org.xbib.util.concurrent.WorkerProvider;
import org.xbib.xml.XMLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class Unimarc extends Feeder {

    private final static Logger logger = LogManager.getLogger(Unimarc.class);

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final static Charset ISO88591 = Charset.forName("ISO-8859-1");

    @Override
    protected WorkerProvider provider() {
        return p -> new Unimarc().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws IOException {
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        Map<String,Object> params = new HashMap<>();
        params.put("identifier", settings.get("identifier", "UNIMARC"));
        params.put("_prefix", "(" + settings.get("identifier", "UNIMARC") + ")");
        final MARCDirectQueue queue = new MyEntityQueue();
        queue.setUnmappedKeyListener((id, key) -> {
            if ((settings.getAsBoolean("detect-unknown", false))) {
                logger.warn("record {} unmapped field {}", id, key);
                unmapped.add("\"" + key + "\"");
            }
        });
        queue.execute();

        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(queue);
        InputStream in = InputService.getInputStream(uri);
        InputStreamReader r = new InputStreamReader(in, ISO88591);
        final Iso2709Reader reader = new Iso2709Reader(r)
                .setStringTransformer(value -> XMLUtil.sanitizeXml10(new String(value.getBytes(ISO88591), UTF8)))
                .setMarcXchangeListener(kv);
        reader.setFormat("UNIMARC").setType("Bibliographic");
        reader.parse();
        r.close();
        queue.close();
        if (settings.getAsBoolean("detect-unknown", false)) {
            logger.info("unknown keys={}", unmapped);
        }
    }

    class MyEntityQueue extends MARCDirectQueue {

        public MyEntityQueue() {
            super(settings.get("package", "org.xbib.analyzer.unimarc.bib"),
                    settings.getAsInt("pipelines", 1),
                    settings.get("elements",  "/org/xbib/analyzer/unimarc/bib.json")
            );
        }

        @Override
        public void afterCompletion(MARCEntityBuilderState state) throws IOException {
            RouteRdfXContentParams params = new RouteRdfXContentParams(
                    getIndex(), getType());
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), state.getRecordNumber(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            if (settings.get("collection") != null) {
                state.getResource().add("collection", settings.get("collection"));
            }
            builder.receive(state.getResource());
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{}", builder.string());
            }
        }
    }

}