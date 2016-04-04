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
package org.xbib.etl.marc.dialects.pica.natliz;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.etl.marc.dialects.pica.PicaEntityBuilderState;
import org.xbib.etl.marc.dialects.pica.PicaEntityQueue;
import org.xbib.iri.IRI;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.sax.MarcXchangeReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class NatLizPicaElementsTest extends Assert {

    @Test
    public void testPicaSetup() throws Exception {
        PicaEntityQueue queue = new PicaEntityQueue("org.xbib.analyzer.pica.natliz.bib",
                1,
                "/org/xbib/analyzer/pica/natliz/bib.json");
        queue.execute();
        queue.close();
    }

    @Test
    public void testBib() throws Exception {
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        final MyQueue queue = new MyQueue();
        queue.setUnmappedKeyListener((id, key) -> {
            unmapped.add(key.toString());
            //logger.warn("record {}: unknown key {}", id, key);
        });
        queue.execute();
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .setStringTransformer(new OurTransformer())
                .addListener(queue);
        final InputStream in = getClass().getResourceAsStream("natliz.xml");
        MarcXchangeReader reader = new MarcXchangeReader(new InputStreamReader(in, "UTF-8"));
        reader.addNamespace("info:srw/schema/5/picaXML-v1.0");
        reader.setMarcXchangeListener(kv);
        reader.parse();
        in.close();
        queue.close();
        //logger.info("counter={}, detected unknown elements = {}", queue.getCounter(), unmapped);
        assertEquals(queue.getCounter(), 1);
    }

    class OurTransformer implements StringTransformer {
        @Override
        public String transform(String value) {
            return Normalizer.normalize(value, Normalizer.Form.NFKC);
        }
    }

    class MyQueue extends PicaEntityQueue {

        final AtomicInteger counter = new AtomicInteger();

        public MyQueue() throws Exception {
            super("org.xbib.analyzer.pica.natliz.bib",
                    1,
                    "/org/xbib/analyzer/pica/natliz/bib.json");
        }

        @Override
        public void beforeCompletion(PicaEntityBuilderState context) throws IOException {
            IRI iri = IRI.builder().scheme("http")
                    .host("xbib.org")
                    .query("bib")
                    .fragment(Long.toString(counter.getAndIncrement())).build();
            context.getResource().id(iri);
        }

        public long getCounter() {
            return counter.longValue();
        }
    }

}
