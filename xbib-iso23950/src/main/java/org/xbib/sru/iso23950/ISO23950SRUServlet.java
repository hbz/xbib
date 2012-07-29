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
package org.xbib.sru.iso23950;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xbib.io.negotiate.ContentTypeNegotiator;
import org.xbib.io.negotiate.MediaRangeSpec;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.ExplainResponse;
import org.xbib.sru.SRU;
import org.xbib.sru.SRUAdapter;
import org.xbib.sru.SRUContentTypeNegotiator;
import org.xbib.sru.SRURequestDumper;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.sru.explain.Explain;
import org.xbib.xml.transform.StylesheetTransformer;

public class ISO23950SRUServlet extends HttpServlet implements SRU {

    private static final Logger logger = LoggerFactory.getLogger(ISO23950SRUServlet.class.getName());
    private final Map<String, String> mediaTypes = new HashMap<>();
    private final ContentTypeNegotiator ctn = new SRUContentTypeNegotiator();
    private final SRURequestDumper requestDumper = new SRURequestDumper();
    private final StylesheetTransformer transformer = new StylesheetTransformer("/xsl");
    private final String responseEncoding = "UTF-8";
    private SRUAdapter adapter;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String adapterName = config.getInitParameter("name");
        this.adapter = ISO23950SRUAdapterFactory.getAdapter(adapterName);
        if (adapter == null) {
            throw new ServletException("can't get adapter from name =" + adapterName);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info(requestDumper.toString(request));
        String mediaType = getMediaType(request);
        response.setContentType(mediaType);
        response.setHeader("Server", "Java");
        response.setHeader("X-Powered-By", getClass().getName());
        final OutputStream out = response.getOutputStream();
        if (adapter == null) {
            adapter = createAdapter(request);
        }
        try {
            adapter.connect();
            adapter.setStylesheetTransformer(transformer);
            String operation = request.getParameter(OPERATION_PARAMETER);
            if (SEARCH_RETRIEVE_COMMAND.equals(operation)) {
                SearchRetrieve op = new SearchRetrieve();
                op.setVersion(request.getParameter(VERSION_PARAMETER));
                op.setQuery(request.getParameter(QUERY_PARAMETER));
                int startRecord = Integer.parseInt(
                        request.getParameter(START_RECORD_PARAMETER) != null
                        ? request.getParameter(START_RECORD_PARAMETER) : "1");
                op.setStartRecord(startRecord);
                int maxRecords = Integer.parseInt(
                        request.getParameter(MAXIMUM_RECORDS_PARAMETER) != null
                        ? request.getParameter(MAXIMUM_RECORDS_PARAMETER) : "10");
                op.setMaximumRecords(maxRecords);
                String recordPacking = request.getParameter(RECORD_PACKING_PARAMETER) != null
                        ? request.getParameter(RECORD_PACKING_PARAMETER) : "xml";
                op.setRecordPacking(recordPacking);
                String recordSchema = request.getParameter(RECORD_SCHEMA_PARAMETER) != null
                        ? request.getParameter(RECORD_SCHEMA_PARAMETER) : "marcxchange";
                op.setRecordSchema(recordSchema);
                int ttl = Integer.parseInt(
                        request.getParameter(RESULT_SET_TTL_PARAMETER) != null
                        ? request.getParameter(RESULT_SET_TTL_PARAMETER) : "0");
                op.setResultSetTTL(ttl);
                op.setSortKeys(request.getParameter(SORT_KEYS_PARAMETER));
                op.setExtraRequestData(request.getParameter(EXTRA_REQUEST_DATA_PARAMETER));
                SearchRetrieveResponse srr = new SearchRetrieveResponse(response, responseEncoding);
                adapter.searchRetrieve(op, srr);                
                response.setStatus(200);
            } else {
                Explain op = new Explain();
                response.setStatus(200);
                adapter.explain(op, new ExplainResponse(out, responseEncoding));
            }
        } catch (Diagnostics diag) {
            logger.warn(diag.getMessage(), diag);
            response.setStatus(500);
            out.write(diag.getXML().getBytes(responseEncoding));
        } finally {
            out.flush();
            adapter.disconnect();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
        adapter.disconnect();
    }

    private String getMediaType(HttpServletRequest req) {
        String useragent = req.getHeader("User-Agent");
        String mediaType, mimeType = req.getParameter("http:accept");
        if (mimeType == null) {
            mimeType = req.getParameter("httpAccept");
        }
        if (mimeType == null) {
            mimeType = req.getHeader("accept");
        }
        mediaType = mediaTypes.get(mimeType);
        if (mediaType == null) {
            MediaRangeSpec mrs = useragent != null
                    ? ctn.getBestMatch(mimeType, useragent) : ctn.getBestMatch(mimeType);
            if (mrs != null) {
                mediaType = mrs.getMediaType();
            } else {
                mediaType = "";
            }
            mediaTypes.put(mimeType, mediaType);
        }
        logger.debug("mimeType = {} -> mediaType = {}", mimeType, mediaType);
        return mediaType;
    }
    
    private SRUAdapter createAdapter(HttpServletRequest request) throws ServletException, IOException {
        String[] reqPath = request.getRequestURI().split("/");
        String name = reqPath[reqPath.length - 1];
        this.adapter = ISO23950SRUAdapterFactory.getAdapter(name);
        if (adapter == null) {
            throw new ServletException("can't get adapter from " + request.getRequestURI());
        }
        return adapter;
    }
    

}
