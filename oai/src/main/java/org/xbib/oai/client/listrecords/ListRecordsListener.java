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
package org.xbib.oai.client.listrecords;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.oai.OAIResponseListener;
import org.xbib.oai.util.ResumptionToken;

import org.xbib.service.client.http.SimpleHttpResponse;
import org.xbib.xml.XMLUtil;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

public class ListRecordsListener
        implements OAIResponseListener {

    private final static Logger logger = LogManager.getLogger(ListRecordsListener.class.getName());

    private final static String[] RETRY_AFTER_HEADERS = new String[] {
            "retry-after", "Retry-after", "Retry-After"
    };

    private final ListRecordsRequest request;

    private final ListRecordsResponse response;

    private ListRecordsFilterReader filterreader;

    private StringBuilder body;

    private boolean scrubCharacters;

    private long retryAfterMillis;

    public ListRecordsListener(ListRecordsRequest request) {
        this.request = request;
        this.response = new ListRecordsResponse(request);
        this.body = new StringBuilder();
        this.retryAfterMillis = 20 * 1000; // 20 seconds
    }

    public ListRecordsListener setScrubCharacters(boolean scrub) {
        this.scrubCharacters = scrub;
        return this;
    }

    public ListRecordsListener setRetryAfter(long millis) {
        this.retryAfterMillis = millis;
        return this;
    }

    @Override
    public ListRecordsResponse getResponse() {
        return response;
    }

    public void onError(Throwable error) throws IOException {
        throw new IOException(error);
    }

    public void receivedResponse(SimpleHttpResponse simpleHttpResponse) throws IOException {
        int status = simpleHttpResponse.status().code();
        if (status == 503) {
            logger.warn("retry-after, body={}", body);
            doRetryAfter(simpleHttpResponse);
            return;
        }
        if (status != 200) {
            throw new IOException("status  = " + status + " response = " + body);
        }
        // activate XSLT only if OAI XML content type is returned
        String contentType = simpleHttpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentType.startsWith("text/xml")) {
            StylesheetTransformer transformer = new StylesheetTransformer().setPath("xsl");
            this.filterreader = new ListRecordsFilterReader(request, response);
            String s = !scrubCharacters ? body.toString() : XMLUtil.sanitize(body.toString());
            InputSource source = new InputSource(new StringReader(s));
            transformer.setSource(filterreader, source);
            response.setTransformer(transformer);
        } else {
            throw new IOException("no XML content type in response: " + contentType);
        }
    }

    private void doRetryAfter(SimpleHttpResponse httpResponse) {
        long secs = retryAfterMillis / 1000;
        if (httpResponse.headers() != null) {
            for (String retryAfterHeader : RETRY_AFTER_HEADERS) {
                String retryAfter = httpResponse.headers().get(retryAfterHeader);
                if (retryAfter == null) {
                    continue;
                }
                secs = Long.parseLong(retryAfter);
                if (!isDigits(retryAfter)) {
                    // parse RFC date, e.g. Fri, 31 Dec 1999 23:59:59 GMT
                    Instant instant = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(retryAfter));
                    secs = ChronoUnit.SECONDS.between(instant, Instant.now());
                    logger.debug("parsed delay seconds is {}", secs);
                }
                logger.debug("setting delay seconds to {}", secs);
            }
        }
        response.setDelaySeconds(secs);
        request.setRetry(true);
    }

    private boolean isDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void onReceive(CharSequence message) throws IOException {
        body.append(message);
    }

    public ResumptionToken getResumptionToken() {
        return filterreader != null ? filterreader.getResumptionToken() : null;
    }

}
