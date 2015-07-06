package org.xbib.rdf.jsonld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.rdf.io.nquads.NQuadsSerializer;
import org.xbib.rdf.io.sink.CharOutputSink;
import org.xbib.rdf.io.source.StreamProcessor;
import org.testng.annotations.Test;
import org.xbib.io.stream.Streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;

public class JsonLdParserTest {

    private final static Logger logger = LogManager.getLogger(JsonLdParserTest.class);

    @Test
    public void testToRdf() throws Exception {
        for (int i = 1; i < 120; i++) {
            InputStream in = getClass().getResourceAsStream(String.format("/json-ld-org/toRdf-%04d-in.jsonld", i));
            if (in != null) {
                CharOutputSink charOutputSink = new CharOutputSink();
                charOutputSink.connect(System.err);
                StreamProcessor streamProcessor = new StreamProcessor(JsonLdReader.connect(NQuadsSerializer.connect(charOutputSink)));
                String uri = String.format("http://json-ld.org/test-suite/tests/toRdf-%04d-in.jsonld", i);
                Reader input = new InputStreamReader(in, "UTF-8");
                Writer writer = new StringWriter();
                charOutputSink.connect(writer);
                streamProcessor.process(input, uri);
                input.close();
                writer.close();
                InputStream in2 = getClass().getResourceAsStream(String.format("/json-ld-org/toRdf-%04d-out.nq", i));
                StringWriter writer2 = new StringWriter();
                Streams.copy(new InputStreamReader(in2, "UTF-8"), writer2);
                Collection<String> s1 = sortByLines(writer.toString().trim());
                Collection<String> s2 = sortByLines(writer2.toString().trim());
                //logger.info("{}", s1);
                //logger.info("{}", s2);
                try {
                    assertEquals(s1, s2, "i=" + i);
                } catch (AssertionError e) {
                    logger.info("{} {}", i, s1);
                    logger.info("{} {}", i, s2);
                }
            }
        }
    }

    @Test
    public void testNormalize() throws Exception {
        for (int i = 1; i < 58; i++) {
            InputStream in = getClass().getResourceAsStream(String.format("/json-ld-org/normalize-%04d-in.jsonld", i));
            if (in != null) {
                CharOutputSink charOutputSink = new CharOutputSink();
                charOutputSink.connect(System.err);
                StreamProcessor streamProcessor = new StreamProcessor(JsonLdReader.connect(NQuadsSerializer.connect(charOutputSink)));
                String uri = String.format("http://json-ld.org/test-suite/tests/normalize-%04d-in.jsonld", i);
                Reader input = new InputStreamReader(in, "UTF-8");
                Writer writer = new StringWriter();
                charOutputSink.connect(writer);
                streamProcessor.process(input, uri);
                input.close();
                writer.close();
                InputStream in2 = getClass().getResourceAsStream(String.format("/json-ld-org/normalize-%04d-out.nq", i));
                StringWriter writer2 = new StringWriter();
                Streams.copy(new InputStreamReader(in2, "UTF-8"), writer2);
                Collection<String> s1 = sortByLines(writer.toString().trim());
                Collection<String> s2 = sortByLines(writer2.toString().trim());
                assertEquals(s1, s2, "normalize i=" + i);
            }
        }
    }

    private Collection<String> sortByLines(String input) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(input));
        String inputLine;
        List<String> lineList = new ArrayList<String>();
        while ((inputLine = bufferedReader.readLine()) != null) {
            lineList.add(inputLine);
        }
        bufferedReader.close();
        Collections.sort(lineList);
        return new TreeSet<String>(lineList);
    }

}