package org.xbib.io.field;

import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.InputStreamReader;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;

public class StreamTest {

    private static final Logger logger = LoggerFactory.getLogger(StreamTest.class.getName());

    @Test
    public void testStream() throws Exception {

        BufferedFieldStreamReader streamReader = new BufferedFieldStreamReader(new InputStreamReader( getClass().getResourceAsStream("/sequential.groupstream")));
        streamReader.fields().forEach(
                new Consumer<Separable>() {
                    @Override
                    public void accept(Separable separable) {
                        //logger.info("sep={}", separable.getClass().getSimpleName());
                    }
                }
        );

        long count = new BufferedFieldStreamReader(new InputStreamReader(getClass().getResourceAsStream("/sequential.groupstream")))
                .fields().count();

        assertEquals(390L, count);
    }
}