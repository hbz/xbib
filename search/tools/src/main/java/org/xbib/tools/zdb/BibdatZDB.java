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
package org.xbib.tools.zdb;

import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.dialects.pica.PicaContext;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilder;
import org.xbib.elements.marc.dialects.pica.PicaElementBuilderFactory;
import org.xbib.elements.marc.dialects.pica.PicaElementMapper;
import org.xbib.io.keyvalue.KeyValueStreamAdapter;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.DNBPICAXmlReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.Converter;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.text.Normalizer;

public final class BibdatZDB extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(BibdatZDB.class.getName());

    public static void main(String[] args) {
        try {
            new BibdatZDB()
                .reader(new InputStreamReader(System.in, "UTF-8"))
                .writer(new OutputStreamWriter(System.out, "UTF-8"))
                .run();
            out.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private BibdatZDB () {
    }

    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new BibdatZDB();
            }
        };
    }

    protected BibdatZDB prepare() {
        super.prepare();
        try {
            out.init(settings.get("output", "bibdat.nt"));
        } catch (IOException e) {
            logger.error("error while setting output", e);
        }
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bibdat")
                .pipelines(settings.getAsInt("pipelines", 1))
                .detectUnknownKeys(settings.getAsBoolean("detect", false))
                .start(new PicaElementBuilderFactory() {
                    public PicaElementBuilder newBuilder() {
                        return new PicaElementBuilder().addOutput(out);
                    }
                });
        logger.info("mapper is up, {} elemnents", mapper.map().size());
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(value, Normalizer.Form.NFC);
                    }
                })
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                    @Override
                    public void keyValue(FieldCollection key, String value) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("begin");
                            for (Field f : key) {
                                logger.trace("tag={} ind={} subf={} data={}",
                                        f.tag(), f.indicator(), f.subfieldId(), f.data());
                            }
                            logger.trace("end");
                        }
                    }

                });
        InputStream in = InputService.getInputStream(uri);
        InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
        new DNBPICAXmlReader(source).setListener(kv).parse();
        in.close();
        mapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("detected unknown elements = {}", mapper.unknownKeys());
        }
    }

    private final static OurElementOutput out = new OurElementOutput();

    private final static class OurElementOutput extends CountableElementOutput<PicaContext,Resource> {

        File f;
        FileWriter fw;
        NTripleWriter writer;

        public OurElementOutput init(String filename) throws IOException {
            this.f = new File(filename);
            this.fw = new FileWriter(f);
            this.writer = new NTripleWriter()
                    .output(fw)
                    .setNullPredicate(IRI.builder().scheme("http").host("xbib.org").path("/adr").build());
            return this;
        }

        @Override
        public void output(PicaContext context, ContentBuilder contentBuilder) throws IOException {
            IRI id = IRI.builder().scheme("http").host("xbib.org").path("/pica/zdb/bibdat")
                    .fragment(context.getID()).build();
            context.resource().id(id);
            writer.write(context.resource());
            counter.incrementAndGet();
        }

        public void shutdown() throws IOException {
            if (fw != null) {
                fw.close();
            }
        }
    }

}