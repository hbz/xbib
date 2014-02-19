package org.xbib.xml.stream;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Abstract base class for SAX <code>XMLReader</code> implementations.
 * Contains properties as defined in {@link XMLReader}, and does not recognize any features.
 */
abstract class AbstractXMLReader implements XMLReader {

    private DTDHandler dtdHandler;

    private ContentHandler contentHandler;

    private EntityResolver entityResolver;

    private ErrorHandler errorHandler;

    private LexicalHandler lexicalHandler;

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public void setDTDHandler(DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    protected LexicalHandler getLexicalHandler() {
        return lexicalHandler;
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception.
     *
     * @throws org.xml.sax.SAXNotRecognizedException always
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception.
     *
     * @throws SAXNotRecognizedException always
     */
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception when the given property does not signify a lexical
     * handler. The property name for a lexical handler is <code>http://xml.org/sax/properties/lexical-handler</code>.
     */
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            return lexicalHandler;
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }

    /**
     * Throws a <code>SAXNotRecognizedException</code> exception when the given property does not signify a lexical
     * handler. The property name for a lexical handler is <code>http://xml.org/sax/properties/lexical-handler</code>.
     */
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            lexicalHandler = (LexicalHandler) value;
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }
}