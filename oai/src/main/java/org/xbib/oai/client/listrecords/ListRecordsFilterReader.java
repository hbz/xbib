package org.xbib.oai.client.listrecords;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.util.RecordHeader;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.oai.xml.MetadataHandler;
import org.xbib.xml.XMLFilterReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ListRecordsFilterReader extends XMLFilterReader {

    private static final Logger logger = LogManager.getLogger(ListRecordsFilterReader.class);

    private final ListRecordsRequest request;

    private final ListRecordsResponse response;

    private StringBuilder content;

    private RecordHeader header;

    private ResumptionToken token;

    private boolean inMetadata;

    ListRecordsFilterReader(ListRecordsRequest request, ListRecordsResponse response) {
        super();
        this.request = request;
        this.response = response;
        this.content = new StringBuilder();
        this.inMetadata = false;
    }

    public ResumptionToken getResumptionToken() {
        return token;
    }

    public ListRecordsResponse getResponse() {
        return response;
    }

    @Override
    public void startDocument() throws SAXException {
        logger.debug("start of document");
        super.startDocument();
        request.setResumptionToken(null);
    }

    @Override
    public void endDocument() throws SAXException {
        logger.debug("end of document");
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException {
        super.startElement(uri, localname, qname, atts);
        if (OAIConstants.NS_URI.equals(uri)) {
            switch (localname) {
                case "header":
                    header = new RecordHeader();
                    break;
                case "error":
                    response.setError(atts.getValue("code"));
                    break;
                case "metadata":
                    inMetadata = true;
                    for (MetadataHandler mh : request.getHandlers()) {
                        mh.startDocument();
                    }
                    break;
                case "resumptionToken":
                    try {
                        token = ResumptionToken.newToken(null);
                        String cursor = atts.getValue("cursor");
                        if (cursor != null) {
                            token.setCursor(Integer.parseInt(cursor));
                        }
                        String completeListSize = atts.getValue("completeListSize");
                        if (completeListSize != null) {
                            token.setCompleteListSize(Integer.parseInt(completeListSize));
                        }
                        if (!token.isComplete()) {
                            request.setResumptionToken(token);
                        }
                    } catch (Exception e) {
                        throw new SAXException(e);
                    }
                    break;
            }
            return;
        }
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.startElement(uri, localname, qname, atts);
            }
        }
    }

    @Override
    public void endElement(String nsURI, String localname, String qname) throws SAXException {
        super.endElement(nsURI, localname, qname);
        if (OAIConstants.NS_URI.equals(nsURI)) {
            switch (localname) {
                case "header":
                    for (MetadataHandler mh : request.getHandlers()) {
                        mh.setHeader(header);
                    }
                    header = new RecordHeader();
                    break;
                case "metadata":
                    for (MetadataHandler mh : request.getHandlers()) {
                        mh.endDocument();
                    }
                    inMetadata = false;
                    break;
                case "responseDate":
                    response.setDate(Instant.parse(content.toString().trim()));
                    break;
                case "resumptionToken":
                    if (token != null && content != null && content.length() > 0) {
                        token.setValue(content.toString());
                        // feedback to request
                        request.setResumptionToken(token);
                    } else {
                        logger.warn("empty resumption token value");
                        // some servers send a null or an empty token as last token
                        token = null;
                        request.setResumptionToken(null);
                    }
                    break;
                case "identifier":
                    if (header != null && content != null && content.length() > 0) {
                        String id = content.toString().trim();
                        header.setIdentifier(id);
                    }
                    break;
                case "datestamp":
                    if (header != null && content != null && content.length() > 0) {
                        try {
                            header.setDate(Instant.parse(content.toString().trim()));
                        } catch (DateTimeParseException e) {
                            // not "seconds ISO"
                        }
                        try {
                            LocalDateTime ldt = LocalDateTime.parse(content.toString().trim(),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            header.setDate(Instant.from(ldt));
                        } catch (DateTimeParseException e) {
                            // not "day ISO"
                        }
                    }
                    break;
                case "setSpec":
                    if (header != null && content != null && content.length() > 0) {
                        header.setSetspec(content.toString().trim());
                    }
                    break;
            }
            if (content != null) {
                content.setLength(0);
            }
            return;
        }
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.endElement(nsURI, localname, qname);
            }
        }
        content.setLength(0);
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        super.characters(chars, start, length);
        content.append(new String(chars, start, length).trim());
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.characters(chars, start, length);
            }
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.startPrefixMapping(prefix, uri);
            }
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        super.endPrefixMapping(prefix);
        if (inMetadata) {
            for (MetadataHandler mh : request.getHandlers()) {
                mh.endPrefixMapping(prefix);
            }
        }
    }

}
