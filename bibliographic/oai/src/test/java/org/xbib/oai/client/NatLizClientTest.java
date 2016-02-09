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
package org.xbib.oai.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.StringPacket;
import org.xbib.io.archive.tar.TarConnection;
import org.xbib.oai.client.listrecords.ListRecordsListener;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.io.Session;
import org.xbib.io.archive.tar.TarSession;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.xml.XmlSimpleMetadataHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * NatLiz client test
 *
 */
public class NatLizClientTest {

    private final static Logger logger = LogManager.getLogger(NatLizClientTest.class.getName());

    private TarSession session;

    public void testListRecordsNatLiz() throws InterruptedException, TimeoutException, IOException, URISyntaxException {
        try {
            OAIClient client = OAIClientFactory.newClient("http://dl380-47.gbv.de/oai/natliz/");
            ListRecordsRequest request = client.newListRecordsRequest()
                    .setFrom(Instant.parse("2000-01-01T00:00:00Z"))
                    .setUntil(Instant.parse("2014-01-01T00:00:00Z"))
                    .setMetadataPrefix("extpp2"); // extpp, extpp2, oai_dc, mods, marcxml, telap, mab, mab_opc

            Path path = Paths.get("natliz-extpp2.tar.gz");
            TarConnection connection = new TarConnection();
            connection.setPath(path, StandardOpenOption.READ);
            session = connection.createSession();
            session.open(Session.Mode.WRITE);

            final XmlSimpleMetadataHandler metadataHandler = new NatLizHandlerSimple()
                    .setWriter(new StringWriter());

            do {
                try {
                    ListRecordsListener listener = new ListRecordsListener(request);
                    request.addHandler(metadataHandler);
                    request.prepare().execute(listener).waitFor();
                    if (listener.getResponse() != null) {
                        StringWriter sw = new StringWriter();
                        listener.getResponse().to(sw);
                        //logger.info("response from NatLiz = {}", sw);
                    } else {
                        logger.warn("no response");
                    }
                    request = client.resume(request, listener.getResumptionToken());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } while (request != null);
            session.close();
            client.close();
        } catch (OAIException | ConnectException | ExecutionException e) {
            logger.error("skipping");
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw e;
        }
    }

    class NatLizHandlerSimple extends XmlSimpleMetadataHandler {

        public void endDocument() throws SAXException {
            super.endDocument();
            logger.info("got XML document {}", getIdentifier());
            try {
                StringPacket p = session.newPacket();
                p.name(getIdentifier());
                p.packet(getWriter().toString());
                session.write(p);
                //FileWriter fw = new FileWriter("target/" + getIdentifier() + ".xml");
                //fw.write(getContentBuilder().toString());
                //fw.close();
            } catch (IOException e) {
                throw new SAXException(e);
            }
            setWriter(new StringWriter());
        }
    }
}
