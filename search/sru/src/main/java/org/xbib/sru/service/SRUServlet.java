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
package org.xbib.sru.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xbib.io.OutputFormat;
import org.xbib.io.negotiate.ContentTypeNegotiator;
import org.xbib.io.negotiate.MediaRangeSpec;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUVersion;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.SRUConstants;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.util.SRUContentTypeNegotiator;
import org.xbib.sru.util.SRURequestDumper;
import org.xbib.xml.transform.StylesheetTransformer;

/**
 * SRU servlet
 *
 * @author <a href="mailto:joergprante@gmail.com"> Jörg Prante</a>
 */
public class SRUServlet extends HttpServlet implements SRUConstants {

    private final Logger logger = LoggerFactory.getLogger(SRUServlet.class.getName());

    private final String responseEncoding = "UTF-8";

    private final SRURequestDumper requestDumper = new SRURequestDumper();

    private ServletConfig config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.config = config;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String mediaType = getMediaType(request);
        response.setContentType(mediaType);
        response.setHeader("Server", "Java");
        response.setHeader("X-Powered-By", getClass().getName());
        if (logger.isDebugEnabled()) {
            // a better method is adding a filter for HTTP request dumping
            logger.info(requestDumper.toString(request));
        }
        SRUService service = createService(request);
        SRUClient client = service.newClient();
        try {
            String operation = request.getParameter(OPERATION_PARAMETER);
            if (SEARCH_RETRIEVE_COMMAND.equals(operation)) {
                SearchRetrieveRequest sruRequest = client.newSearchRetrieveRequest()
                    .setURI(getBaseURI(request))
                    .setPath(request.getPathInfo())
                    .setVersion(request.getParameter(VERSION_PARAMETER))
                    .setQuery(request.getParameter(QUERY_PARAMETER))
                    .setFilter(request.getParameter(FILTER_PARAMETER));
                int startRecord = Integer.parseInt(
                        request.getParameter(START_RECORD_PARAMETER) != null
                        ? request.getParameter(START_RECORD_PARAMETER) : "1");
                sruRequest.setStartRecord(startRecord);
                int maxRecords = Integer.parseInt(
                        request.getParameter(MAXIMUM_RECORDS_PARAMETER) != null
                        ? request.getParameter(MAXIMUM_RECORDS_PARAMETER) : "10");
                sruRequest.setMaximumRecords(maxRecords);
                String recordPacking = request.getParameter(RECORD_PACKING_PARAMETER) != null
                        ? request.getParameter(RECORD_PACKING_PARAMETER) : "xml";
                sruRequest.setRecordPacking(recordPacking);
                String recordSchema = request.getParameter(RECORD_SCHEMA_PARAMETER) != null
                        ? request.getParameter(RECORD_SCHEMA_PARAMETER) : "mods";
                sruRequest.setRecordSchema(recordSchema);
                int ttl = Integer.parseInt(
                        request.getParameter(RESULT_SET_TTL_PARAMETER) != null
                        ? request.getParameter(RESULT_SET_TTL_PARAMETER) : "0");
                sruRequest.setResultSetTTL(ttl);
                sruRequest.setSortKeys(request.getParameter(SORT_KEYS_PARAMETER));

                sruRequest.setFacetLimit(request.getParameter(FACET_LIMIT_PARAMETER));
                sruRequest.setFacetCount(request.getParameter(FACET_COUNT_PARAMETER));
                sruRequest.setFacetStart(request.getParameter(FACET_START_PARAMETER));
                sruRequest.setFacetSort(request.getParameter(FACET_SORT_PARAMETER));

                sruRequest.setExtraRequestData(request.getParameter(EXTRA_REQUEST_DATA_PARAMETER));

                SRUVersion version = SRUVersion.fromString(sruRequest.getVersion());

                SearchRetrieveResponse sruResponse = client.execute(sruRequest);

                String contentType = version.equals(SRUVersion.VERSION_2_0) ?
                        OutputFormat.SRU.mimeType() : OutputFormat.XML.mimeType();

                response.setStatus(200);
                response.setContentType(contentType);
                response.setCharacterEncoding("UTF-8");
                response.addHeader("X-SRU-origin",
                        sruRequest.getURI() != null ? sruRequest.getURI().toASCIIString() : "undefined");

                String s = config.getInitParameter(version.name().toLowerCase());
                String[] stylesheets = s != null ? s.split(",") : null;

                sruResponse.setOutputFormat(OutputFormat.SRU)
                        .setStylesheetTransformer(new StylesheetTransformer("/xsl"))
                        .setStylesheets(version, stylesheets)
                        .to(response.getWriter());
                logger.debug("SRU servlet response sent");

            } else {
                throw new Diagnostics(1, "operation " + operation + " currently not supported :(");
            }
        } catch (Diagnostics diag) {
            logger.warn(diag.getMessage(), diag);
            //response.setStatus(500); SRU does not use 500 HTTP errors :(
            response.setStatus(200);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(OutputFormat.SRU.mimeType());
            response.getOutputStream().write(diag.getXML().getBytes(responseEncoding));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setStatus(500);
        } finally {
            service.close(client);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private final Map<String, String> mediaTypes = new HashMap();

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
            final ContentTypeNegotiator ctn = new SRUContentTypeNegotiator();
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

    private final static Map<String, SRUService> services = new HashMap();

    private synchronized SRUService createService(HttpServletRequest request)
            throws ServletException, IOException {
        SRUService service = null;
        String[] reqPath = request.getRequestURI().split("/");
        // last part of URI component is the name of the service
        String name = reqPath[reqPath.length - 1];
        try {
            if (!services.containsKey(name)) {
                service = PropertiesSRUServiceFactory.getInstance().getService(name);
                services.put(name, service);
                logger.debug("new SRU service {}", name);
            } else {
                service = services.get(name);
            }
        } catch (IllegalArgumentException e) {
            // skip
        }
        if (service == null) {
            try {
                // class name in web.xml?
                name = config.getInitParameter("name");
                if (!services.containsKey(name)) {
                    service = SRUServiceFactory.getInstance().getService(name);
                    services.put(name, service);
                    logger.debug("new SRU service {}", name);
                } else {
                    service = services.get(name);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                // skip
            }
        }
        if (service == null) {
            throw new ServletException("can't create SRUService from service name = "
                    + config.getInitParameter("name")
                    + " or request URI = " + request.getRequestURI());
        }
        return service;
    }

    private URI getBaseURI(HttpServletRequest request) {
        String uri = request.getRequestURL().toString();
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && forwardedHost.length() > 0) {
            uri = uri.replaceAll("://[^/]*", "://" + forwardedHost);
        }
        return URI.create(uri);
    }

}
