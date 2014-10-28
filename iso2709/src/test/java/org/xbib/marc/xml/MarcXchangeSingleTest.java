package org.xbib.marc.xml;

import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.xml.stream.MarcXchangeWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import static org.testng.Assert.assertNull;

public class MarcXchangeSingleTest {

    private final Logger logger = LoggerFactory.getLogger(MarcXchangeSingleTest.class.getName());

    @Test
    public void testMarcXchangeListener() throws Exception {
        final StringBuilder sb = new StringBuilder();
        MarcXchangeContentHandler handler = new MarcXchangeContentHandler() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void endCollection() {
            }

            @Override
            public void beginRecord(String format, String type) {
                logger.debug("beginRecord format="+format + " type="+type);
                sb.append("beginRecord").append("\n");
                sb.append(format).append("\n");
                sb.append(type).append("\n");
            }

            @Override
            public void leader(String label) {
                logger.debug("leader="+label);
                sb.append("leader").append("\n");
                sb.append(label).append("\n");
            }

            @Override
            public void beginControlField(Field field) {
                logger.debug("beginControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endControlField(Field field) {
                logger.debug("endControlField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginDataField(Field field) {
                logger.debug("beginDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endDataField(Field field) {
                logger.debug("endDataField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void beginSubField(Field field) {
                logger.debug("beginSubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endSubField(Field field) {
                logger.debug("endsubField field="+field);
                sb.append(field).append("\n");
            }

            @Override
            public void endRecord() {
                logger.debug("endRecord");
                sb.append("endRecord").append("\n");
            }

        };

        File file = File.createTempFile("HT016424175-out.", ".xml");
        FileWriter sw = new FileWriter(file);
        //StringWriter sw = new StringWriter();
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.setMarcXchangeListener(handler);

        writer.startDocument();
        writer.beginCollection();

        MarcXchangeReader reader = new MarcXchangeReader();
        reader.setFormat("AlephXML").setType("Bibliographic");
        reader.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        reader.setMarcXchangeListener(writer);
        InputStream in = getClass().getResourceAsStream("HT016424175.xml");
        reader.parse(in);
        in.close();

        reader = new MarcXchangeReader();
        reader.setFormat("AlephXML").setType("Bibliographic");
        reader.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        reader.setMarcXchangeListener(writer);
        in = getClass().getResourceAsStream("HT016424175.xml");
        reader.parse(in);
        in.close();

        writer.endCollection();
        writer.endDocument();
        sw.close();

        assertNull(writer.getException());
        sw.close();

        logger.error("err?", writer.getException());

    }
}