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
package org.xbib.rdf.io.rdfxml;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xbib.helper.StreamTester;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentFactory;
import org.xbib.rdf.io.IOTests;
import org.xbib.rdf.io.turtle.TurtleContentParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

@Category(IOTests.class)
public class GNDRdfXmlReaderTest extends StreamTester {

    @Test
    public void testGNDfromRdfXmltoTurtle() throws Exception {
        String filename = "/org/xbib/rdf/io/rdfxml/GND.rdf";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        TurtleContentParams params = new TurtleContentParams(IRINamespaceContext.newInstance(), false);
        RdfXmlContentParser reader = new RdfXmlContentParser(in);
        StringBuilder sb = new StringBuilder();
        reader.setRdfContentBuilderProvider(() -> turtleBuilder(params));
        reader.setRdfContentBuilderHandler(builder -> sb.append(builder.string()));
        reader.parse();
        assertStream(new InputStreamReader(getClass().getResource("gnd.ttl").openStream()),
                new StringReader(sb.toString()));
    }

    @Test
    public void testGNDfromRdfXmltoTurtle2() throws Exception {
        String filename = "/org/xbib/rdf/io/rdfxml/GND.rdf";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        TurtleContentParams params = new TurtleContentParams(IRINamespaceContext.newInstance(), false);
        RdfXmlContentParser reader = new RdfXmlContentParser(in);
        StringBuilder sb = new StringBuilder();
        reader.setRdfContentBuilderProvider(() -> turtleBuilder(params));
        reader.setRdfContentBuilderHandler(builder -> sb.append(builder.string()));
        reader.parse();
        assertStream(new InputStreamReader(getClass().getResource("gnd.ttl").openStream()),
                new StringReader(sb.toString()));
    }

    @Test
    public void testGNDtoNtriple() throws Exception {
        String filename = "/org/xbib/rdf/io/rdfxml/GND.rdf";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        RdfXmlContentParser reader = new RdfXmlContentParser(in);
        StringBuilder sb = new StringBuilder();
        reader.setRdfContentBuilderProvider(RdfContentFactory::ntripleBuilder);
        reader.setRdfContentBuilderHandler(builder -> sb.append(builder.string()));
        reader.parse();
        assertStream(new InputStreamReader(getClass().getResource("GND.nt").openStream()),
                new StringReader(sb.toString()));
    }

}
