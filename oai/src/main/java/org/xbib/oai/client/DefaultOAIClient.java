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

import java.io.IOException;
import java.net.URL;

import org.xbib.io.http.netty.NettyHttpSession;
import org.xbib.oai.client.getrecord.GetRecordRequest;
import org.xbib.oai.client.identify.IdentifyRequest;
import org.xbib.oai.client.listidentifiers.ListIdentifiersRequest;
import org.xbib.oai.client.listmetadataformats.ListMetadataFormatsRequest;
import org.xbib.oai.client.listrecords.ListRecordsRequest;
import org.xbib.oai.client.listsets.ListSetsRequest;
import org.xbib.oai.util.ResumptionToken;

/**
 * Default OAI client
 */
public class DefaultOAIClient extends NettyHttpSession implements OAIClient {

    private URL url;

    @Override
    public DefaultOAIClient setURL(URL url) {
        this.url = url;
        return this;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public DefaultOAIClient setProxy(String host, int port) {
        super.setProxy(host, port);
        return this;
    }

    @Override
    public NettyHttpSession getSession() {
        return this;
    }

    @Override
    public IdentifyRequest newIdentifyRequest() {
        ensureOpen();
        IdentifyRequest request = new IdentifyRequest(this);
        request.setURL(getURL());
        return request;
    }

    @Override
    public ListMetadataFormatsRequest newListMetadataFormatsRequest() {
        ensureOpen();
        ListMetadataFormatsRequest request = new ListMetadataFormatsRequest(this);
        request.setURL(getURL());
        return request;
    }

    @Override
    public ListSetsRequest newListSetsRequest() {
        ensureOpen();
        ListSetsRequest request = new ListSetsRequest(this);
        request.setURL(getURL());
        return request;
    }

    @Override
    public ListIdentifiersRequest newListIdentifiersRequest() {
        ensureOpen();
        ListIdentifiersRequest request = new ListIdentifiersRequest(this);
        request.setURL(getURL());
        return request;
    }

    @Override
    public GetRecordRequest newGetRecordRequest() {
        ensureOpen();
        GetRecordRequest request = new GetRecordRequest(this);
        request.setURL(getURL());
        return request;
    }

    @Override
    public ListRecordsRequest newListRecordsRequest() {
        ensureOpen();
        ListRecordsRequest request = new ListRecordsRequest(this);
        request.setURL(getURL());
        return request;
    }

    @Override
    public IdentifyRequest resume(IdentifyRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        ensureOpen();
        request = newIdentifyRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListRecordsRequest resume(ListRecordsRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        ensureOpen();
        request = newListRecordsRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListIdentifiersRequest resume(ListIdentifiersRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        ensureOpen();
        request = newListIdentifiersRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListMetadataFormatsRequest resume(ListMetadataFormatsRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        ensureOpen();
        request = newListMetadataFormatsRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public ListSetsRequest resume(ListSetsRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        ensureOpen();
        request = newListSetsRequest();
        request.setResumptionToken(token);
        return request;
    }

    @Override
    public GetRecordRequest resume(GetRecordRequest request, ResumptionToken token) {
        if (request.isRetry()) {
            request.setRetry(false);
            return request;
        }
        if (token == null) {
            return null;
        }
        ensureOpen();
        request = newGetRecordRequest();
        request.setResumptionToken(token);
        return request;
    }

    private void ensureOpen() {
        if (url == null) {
            throw new IllegalArgumentException("no URL set for session");
        }
        if (!isOpen()) {
            try {
                open(Mode.READ);
            } catch (IOException e) {
                // ignore
            }
        }
    }

}