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
package org.xbib.elements.marc.zdb;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.iri.IRI;
import org.xbib.keyvalue.KeyValueStreamAdapter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.AbstractResourceContextWriter;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.concurrent.atomic.AtomicInteger;

public class ZDBHolTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(ZDBHolTest.class.getName());

    @Test
    public void testZDBElements() throws Exception {
        final OurContextResourceOutput out = new OurContextResourceOutput();
        final Charset UTF8 = Charset.forName("UTF-8");
        final Charset ISO88591 = Charset.forName("ISO-8859-1");
        final InputStream in =
            getClass().getResourceAsStream("zdblokutf8.mrc");
            //new GZIPInputStream(new FileInputStream(System.getProperty("user.home") + "/Daten/zdb/1302zdblokalgesamt.mrc.gz"));
        BufferedReader br = new BufferedReader(new InputStreamReader(in, ISO88591));
        InputSource source = new InputSource(br);
        MARCElementBuilderFactory factory = new MARCElementBuilderFactory() {
            public MARCElementBuilder newBuilder() {
                MARCElementBuilder builder = new MARCElementBuilder();
                builder.addWriter(out);
                return builder;
            }
        };
        MARCElementMapper mapper = new MARCElementMapper("marc/zdb/hol")
                .detectUnknownKeys(true)
                .start(factory);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                    @Override
                    public String transform(String value) {
                        return Normalizer.normalize(
                                new String(value.getBytes(ISO88591), UTF8),
                                Normalizer.Form.NFKC);
                    }
                })
                .addListener(mapper)
                .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                    @Override
                    public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
                        logger.debug("begin");
                        for (Field f : key) {
                            logger.debug("tag={} ind={} subf={} data={}",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        logger.debug("end");
                        return this;
                    }

                });
        Iso2709Reader reader = new Iso2709Reader().setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MARC");
        reader.setProperty(Iso2709Reader.TYPE, "Holdings");
        reader.parse(source);
        mapper.close();
        logger.info("zdb holdings counter = {}", out.getCounter());
        logger.info("zdb holdings unknown keys = {}", mapper.unknownKeys());
        br.close();
        assertEquals(out.getCounter(), 293);
    }

    class OurContextResourceOutput extends AbstractResourceContextWriter {

        @Override
        public void write(ResourceContext context) throws IOException {
            Resource r = context.getResource();
            r.id(IRI.builder()
                    .scheme("http")
                    .host("zdb")
                    .query("holdings")
                    .fragment(counter.toString()).build());
            StringWriter sw = new StringWriter();
            TurtleWriter tw = new TurtleWriter(sw);
            tw.write(context);
            logger.debug("out={}", sw.toString());
            counter.incrementAndGet();
        }

        public final AtomicInteger counter = new AtomicInteger();

        public int getCounter() {
            return counter.get();
        }

    }


}
