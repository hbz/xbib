package org.xbib.io.field;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StreamTest extends Assert {

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