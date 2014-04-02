package org.xbib.xcontent;

import org.testng.annotations.Test;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentFactory;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class XContentBuilderTest {

    private final Logger logger = Loggers.getLogger(XContentBuilderTest.class);

    @Test
    public void testCopy() throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject().field("hello", "world").endObject();
        builder.close();

        XContentBuilder builder2 = jsonBuilder();
        builder2.copy(builder);
        builder2.close();
        assertEquals( builder2.string(), "{\"hello\":\"world\"}");
    }

    @Test
    public void testCopyList() throws IOException {
        XContentBuilder builder1 = jsonBuilder();
        builder1.startObject().field("hello", "world").endObject();
        builder1.close();
        XContentBuilder builder2 = jsonBuilder();
        builder2.startObject().field("hello", "world").endObject();
        builder2.close();

        XContentBuilder builder = jsonBuilder();
        builder.startObject().startArray("list");
        builder.copy(Arrays.asList(builder1, builder2));
        builder.endArray().endObject();
        builder.close();

        assertEquals(builder.string(), "{\"list\":[{\"hello\":\"world\"},{\"hello\":\"world\"}]}");
    }

    @Test
    public void testBuilderAsXContentList() throws IOException {
        XContentBuilder builder1 = jsonBuilder();
        builder1.startObject().field("hello", "world").endObject();
        builder1.close();

        XContentBuilder builder2 = jsonBuilder();
        builder2.startObject().field("hello", "world").endObject();
        builder2.close();

        XContentBuilder builder = jsonBuilder();
        builder.startObject().array("list", builder1, builder2).endObject();
        builder.close();

        assertEquals(builder.string(),"{\"list\":[{\"hello\":\"world\"},{\"hello\":\"world\"}]}");
    }

    @Test
    public void testBigDecimal() throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject().field("value", new BigDecimal("57683974591503.00")).endObject();
        assertEquals(builder.string(), "{\"value\":57683974591503.00}");

        XContentType contentType = XContentFactory.xContentType(builder.string());
        Map<String,Object> map = XContentFactory.xContent(contentType)
                .createParser(builder.string())
                .losslessDecimals(true)
                .mapAndClose();
        assertEquals(map.toString(),"{value=57683974591503.00}");
        assertEquals(map.get("value").getClass().toString(), "class java.math.BigDecimal");

        map = XContentFactory.xContent(contentType)
                .createParser(builder.string())
                .losslessDecimals(false)
                .mapAndClose();
        assertEquals(map.toString(),"{value=5.7683974591503E13}");
        assertEquals(map.get("value").getClass().toString(), "class java.lang.Double");
    }

    @Test
    public void testBigInteger() throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject().field("value", new BigInteger("1234567891234567890123456789")).endObject();
        assertEquals(builder.string(),"{\"value\":1234567891234567890123456789}");

        XContentType contentType = XContentFactory.xContentType(builder.string());
        Map<String,Object> map = XContentFactory.xContent(contentType)
                .createParser(builder.string())
                .losslessDecimals(true)
                .mapAndClose();
        assertEquals(map.toString(),"{value=1234567891234567890123456789}");
        assertEquals(map.get("value").getClass().toString(), "class java.math.BigInteger");

        map = XContentFactory.xContent(contentType)
                .createParser(builder.string())
                .losslessDecimals(false)
                .mapAndClose();
        assertEquals(map.toString(),"{value=1234567891234567890123456789}");
        assertEquals(map.get("value").getClass().toString(), "class java.math.BigInteger");
    }

}
