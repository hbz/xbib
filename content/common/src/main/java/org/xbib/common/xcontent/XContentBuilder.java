
package org.xbib.common.xcontent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.xbib.common.Strings;
import org.xbib.common.geo.GeoPoint;
import org.xbib.io.BytesArray;
import org.xbib.io.BytesReference;
import org.xbib.io.stream.BytesStream;
import org.xbib.io.FastByteArrayOutputStream;

public final class XContentBuilder implements BytesStream, ToXContent {


    public enum FieldCaseConversion {
        /**
         * No came conversion will occur.
         */
        NONE,
        /**
         * Camel Case will be converted to Underscore casing.
         */
        UNDERSCORE,
        /**
         * Underscore will be converted to Camel case conversion.
         */
        CAMELCASE
    }

    protected static FieldCaseConversion globalFieldCaseConversion = FieldCaseConversion.NONE;

    private XContentGenerator generator;

    private final OutputStream bos;

    private FieldCaseConversion fieldCaseConversion = globalFieldCaseConversion;

    private StringBuilder cachedStringBuilder;


    public static void globalFieldCaseConversion(FieldCaseConversion globalFieldCaseConversion) {
        XContentBuilder.globalFieldCaseConversion = globalFieldCaseConversion;
    }

    /**
     * Constructs a new builder using a fresh {@link FastByteArrayOutputStream}.
     */
    public static XContentBuilder builder(XContent xContent) throws IOException {
        return new XContentBuilder(xContent, new FastByteArrayOutputStream());
    }

    public static XContentBuilder builder(XContent xContent, OutputStream out) throws IOException {
        return new XContentBuilder(xContent, out);
    }
    /**
     * Constructs a new builder using the provided xcontent and an OutputStream. Make sure
     * to call {@link #close()} when the builder is done with.
     */
    public XContentBuilder(XContent xContent, OutputStream bos) throws IOException {
        this.bos = bos;
        this.generator = xContent.createGenerator(bos);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder.copy(this);
    }

    public XContentBuilder fieldCaseConversion(FieldCaseConversion fieldCaseConversion) {
        this.fieldCaseConversion = fieldCaseConversion;
        return this;
    }

    public XContent content() {
        return generator.content();
    }

    public XContentGenerator generator() {
        return generator;
    }

    public XContentBuilder prettyPrint() {
        generator.usePrettyPrint();
        return this;
    }

    public XContentBuilder lfAtEnd() {
        generator.usePrintLineFeedAtEnd();
        return this;
    }

    public XContentBuilder field(String name, ToXContent xContent) throws IOException {
        field(name);
        xContent.toXContent(this, ToXContent.EMPTY_PARAMS);
        return this;
    }

    public XContentBuilder field(String name, ToXContent xContent, ToXContent.Params params) throws IOException {
        field(name);
        xContent.toXContent(this, params);
        return this;
    }

    public XContentBuilder startObject(String name) throws IOException {
        field(name);
        startObject();
        return this;
    }

    public XContentBuilder startObject(String name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startObject();
        return this;
    }

    public XContentBuilder startObject(XContentBuilderString name) throws IOException {
        field(name);
        startObject();
        return this;
    }

    public XContentBuilder startObject(XContentBuilderString name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startObject();
        return this;
    }

    public XContentBuilder startObject() throws IOException {
        generator.writeStartObject();
        return this;
    }

    public XContentBuilder endObject() throws IOException {
        generator.writeEndObject();
        return this;
    }

    public XContentBuilder array(String name, Collection values) throws IOException {
        startArray(name);
        for (Object value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XContentBuilder array(String name, String... values) throws IOException {
        startArray(name);
        for (String value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XContentBuilder array(String name, Object... values) throws IOException {
        startArray(name);
        for (Object value : values) {
            value(value);
        }
        endArray();
        return this;
    }

    public XContentBuilder startArray(String name, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        startArray();
        return this;
    }

    public XContentBuilder startArray(String name) throws IOException {
        field(name);
        startArray();
        return this;
    }

    public XContentBuilder startArray(XContentBuilderString name) throws IOException {
        field(name);
        startArray();
        return this;
    }

    public XContentBuilder startArray() throws IOException {
        generator.writeStartArray();
        return this;
    }

    public XContentBuilder endArray() throws IOException {
        generator.writeEndArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name) throws IOException {
        if (fieldCaseConversion == FieldCaseConversion.UNDERSCORE) {
            generator.writeFieldName(name.underscore());
        } else if (fieldCaseConversion == FieldCaseConversion.CAMELCASE) {
            generator.writeFieldName(name.camelCase());
        } else {
            generator.writeFieldName(name.underscore());
        }
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, FieldCaseConversion conversion) throws IOException {
        if (conversion == FieldCaseConversion.UNDERSCORE) {
            generator.writeFieldName(name.underscore());
        } else if (conversion == FieldCaseConversion.CAMELCASE) {
            generator.writeFieldName(name.camelCase());
        } else {
            generator.writeFieldName(name.underscore());
        }
        return this;
    }


    public XContentBuilder field(String name) throws IOException {
        if (fieldCaseConversion == FieldCaseConversion.UNDERSCORE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toUnderscoreCase(name, cachedStringBuilder);
        } else if (fieldCaseConversion == FieldCaseConversion.CAMELCASE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toCamelCase(name, cachedStringBuilder);
        }
        if (name == null) {
            throw new IOException("null key not allowed");
        }
        generator.writeFieldName(name);
        return this;
    }

    public XContentBuilder field(String name, FieldCaseConversion conversion) throws IOException {
        if (conversion == FieldCaseConversion.UNDERSCORE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toUnderscoreCase(name, cachedStringBuilder);
        } else if (conversion == FieldCaseConversion.CAMELCASE) {
            if (cachedStringBuilder == null) {
                cachedStringBuilder = new StringBuilder();
            }
            name = Strings.toCamelCase(name, cachedStringBuilder);
        }
        if (name == null) {
            throw new IOException("null key not allowed");
        }
        generator.writeFieldName(name);
        return this;
    }

    public XContentBuilder field(String name, char[] value, int offset, int length) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value, offset, length);
        }
        return this;
    }

    public XContentBuilder field(String name, String value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, String value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, String value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder field(String name, String value, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, String value, FieldCaseConversion conversion) throws IOException {
        field(name, conversion);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, String value, FieldCaseConversion conversion) throws IOException {
        if (value != null) {
            field(name, conversion);
            generator.writeString(value);
        }
        return this;
    }

    public XContentBuilder field(String name, Integer value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Integer value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Integer value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(String name, int value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, int value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, Long value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Long value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Long value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(String name, long value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, long value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, Float value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Float value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Float value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(String name, float value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, float value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, Double value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Double value) throws IOException {
        if (value != null) {
            field(name);
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Double value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeNumber(value);
        }
        return this;
    }

    public XContentBuilder field(String name, double value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, double value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, BigInteger value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, BigInteger value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, BigDecimal value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, BigDecimal value) throws IOException {
        field(name);
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder field(String name, BytesReference value) throws IOException {
        field(name);
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, BytesReference value) throws IOException {
        field(name);
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XContentBuilder field(String name, byte[] value, int offset, int length) throws IOException {
        field(name);
        generator.writeBinary(value, offset, length);
        return this;
    }

    public XContentBuilder field(String name, Map<String, Object> value) throws IOException {
        field(name);
        value(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Map<String, Object> value) throws IOException {
        field(name);
        value(value);
        return this;
    }

    public XContentBuilder field(String name, Iterable value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Iterable value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, String... value) throws IOException {
        startArray(name);
        for (String o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, String... value) throws IOException {
        startArray(name);
        for (String o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, Object... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Object... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, int... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, int offset, int length, int... value) throws IOException {
        assert ((offset >= 0) && (value.length > length));
        startArray(name);
        for (int i = offset; i < length; i++) {
            value(value[i]);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, int... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, long... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, long... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, float... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, float... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, double... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, double... value) throws IOException {
        startArray(name);
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder field(String name, Object value) throws IOException {
        field(name);
        writeValue(value);
        return this;
    }

    public XContentBuilder field(XContentBuilderString name, Object value) throws IOException {
        field(name);
        writeValue(value);
        return this;
    }

    public XContentBuilder fieldIfNotNull(String name, Object value) throws IOException {
        if (value != null) {
            return field(name, value);
        }
        return this;
    }

    public XContentBuilder value(Object value) throws IOException {
        writeValue(value);
        return this;
    }

    public XContentBuilder field(String name, boolean value) throws IOException {
        field(name);
        generator.writeBoolean(value);
        return this;
    }

    public XContentBuilder field(String name, byte[] value) throws IOException {
        field(name);
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeBinary(value);
        }
        return this;
    }

    public XContentBuilder nullField(String name) throws IOException {
        generator.writeNullField(name);
        return this;
    }

    public XContentBuilder nullValue() throws IOException {
        generator.writeNull();
        return this;
    }

    public XContentBuilder rawField(String fieldName, byte[] content) throws IOException {
        generator.writeRawField(fieldName, content, bos);
        return this;
    }

    public XContentBuilder rawField(String fieldName, byte[] content, int offset, int length) throws IOException {
        generator.writeRawField(fieldName, content, offset, length, bos);
        return this;
    }

    public XContentBuilder rawField(String fieldName, InputStream content) throws IOException {
        generator.writeRawField(fieldName, content, bos);
        return this;
    }

    public XContentBuilder rawField(String fieldName, BytesReference content) throws IOException {
        generator.writeRawField(fieldName, content, bos);
        return this;
    }

    public XContentBuilder value(XContentBuilder builder) throws IOException {
        generator.writeValue(builder);
        return this;
    }

    public XContentBuilder copy(XContentBuilder builder) throws IOException {
        generator.copy(builder, bos);
        return this;
    }

    public XContentBuilder copy(List<XContentBuilder> builder) throws IOException {
        for (int i = 0; i < builder.size(); i++) {
            if (i > 0) {
                bos.write(',');
            }
            generator.copy(builder.get(i), bos);
        }
        return this;
    }

    public XContentBuilder value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.booleanValue());
    }

    public XContentBuilder value(boolean value) throws IOException {
        generator.writeBoolean(value);
        return this;
    }

    public XContentBuilder value(Integer value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.intValue());
    }

    public XContentBuilder value(int value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(Long value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.longValue());
    }

    public XContentBuilder value(long value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(Float value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.floatValue());
    }

    public XContentBuilder value(float value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(Double value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        return value(value.doubleValue());
    }

    public XContentBuilder value(double value) throws IOException {
        generator.writeNumber(value);
        return this;
    }

    public XContentBuilder value(BigInteger bi) throws IOException {
        generator.writeNumber(bi);
        return this;
    }

    public XContentBuilder value(BigDecimal bd) throws IOException {
        generator.writeNumber(bd);
        return this;
    }

    public XContentBuilder value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeString(value);
        return this;
    }

    public XContentBuilder value(byte[] value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeBinary(value);
        return this;
    }

    public XContentBuilder value(byte[] value, int offset, int length) throws IOException {
        if (value == null) {
            return nullValue();
        }
        generator.writeBinary(value, offset, length);
        return this;
    }

    public XContentBuilder value(BytesReference value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        if (!value.hasArray()) {
            value = value.toBytesArray();
        }
        generator.writeBinary(value.array(), value.arrayOffset(), value.length());
        return this;
    }

    public XContentBuilder map(Map<String, Object> map) throws IOException {
        if (map == null) {
            return nullValue();
        }
        writeMap(map);
        return this;
    }

    public XContentBuilder value(Map<String, Object> map) throws IOException {
        if (map == null) {
            return nullValue();
        }
        writeMap(map);
        return this;
    }

    public XContentBuilder value(Iterable value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        startArray();
        for (Object o : value) {
            value(o);
        }
        endArray();
        return this;
    }

    public XContentBuilder copyCurrentStructure(XContentParser parser) throws IOException {
        generator.copyCurrentStructure(parser);
        return this;
    }

    public XContentBuilder flush() throws IOException {
        generator.flush();
        return this;
    }

    public void close() {
        try {
            generator.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public OutputStream stream() {
        return this.bos;
    }

    public BytesReference bytes() {
        close();
        return ((BytesStream) bos).bytes();
    }

    /**
     * Returns the actual stream used.
     */
    public BytesStream bytesStream() throws IOException {
        close();
        return (BytesStream) bos;
    }

    /**
     * Returns a string representation of the builder (only applicable for text based xcontent).
     *
     * Only applicable when the builder is constructed with {@link FastByteArrayOutputStream}.
     */
    public String string() throws IOException {
        close();
        BytesArray bytesArray = bytes().toBytesArray();
        return new String(bytesArray.array(), bytesArray.arrayOffset(), bytesArray.length(), "UTF-8");
    }

    private void writeMap(Map<String, Object> map) throws IOException {
        generator.writeStartObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            field(entry.getKey());
            Object value = entry.getValue();
            if (value == null) {
                generator.writeNull();
            } else {
                writeValue(value);
            }
        }
        generator.writeEndObject();
    }

    private void writeValue(Object value) throws IOException {
        if (value == null) {
            generator.writeNull();
            return;
        }
        Class type = value.getClass();
        if (type == String.class) {
            generator.writeString((String) value);
        } else if (type == Integer.class) {
            generator.writeNumber((Integer) value);
        } else if (type == Long.class) {
            generator.writeNumber((Long) value);
        } else if (type == Float.class) {
            generator.writeNumber((Float) value);
        } else if (type == Double.class) {
            generator.writeNumber((Double) value);
        } else if (type == Short.class) {
            generator.writeNumber((Short) value);
        } else if (type == Boolean.class) {
            generator.writeBoolean((Boolean) value);
        } else if (type == GeoPoint.class) {
            generator.writeStartObject();
            generator.writeNumberField("lat", ((GeoPoint) value).lat());
            generator.writeNumberField("lon", ((GeoPoint) value).lon());
            generator.writeEndObject();
        } else if (value instanceof Map) {
            writeMap((Map) value);
        } else if (value instanceof Iterable) {
            generator.writeStartArray();
            for (Object v : (Iterable) value) {
                writeValue(v);
            }
            generator.writeEndArray();
        } else if (value instanceof Object[]) {
            generator.writeStartArray();
            for (Object v : (Object[]) value) {
                writeValue(v);
            }
            generator.writeEndArray();
        } else if (type == byte[].class) {
            generator.writeBinary((byte[]) value);
        } else if (value instanceof Date) {
            Date date = (Date)value;
            Instant instant = Instant.ofEpochMilli(date.getTime());
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            generator.writeString(zdt.format(DateTimeFormatter.ISO_INSTANT));
        } else if (value instanceof Calendar) {
            Calendar calendar = (Calendar)value;
            Instant instant = Instant.ofEpochMilli(calendar.getTime().getTime());
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            generator.writeString(zdt.format(DateTimeFormatter.ISO_INSTANT));
        } else if (value instanceof BytesReference) {
            BytesReference bytes = (BytesReference) value;
            if (!bytes.hasArray()) {
                bytes = bytes.toBytesArray();
            }
            generator.writeBinary(bytes.array(), bytes.arrayOffset(), bytes.length());
        } else if (value instanceof XContentBuilder) {
            value((XContentBuilder)value);
        } else if (value instanceof ToXContent) {
            ((ToXContent) value).toXContent(this, ToXContent.EMPTY_PARAMS);
        } else if (value instanceof double[]) {
            generator.writeStartArray();
            for (double v : (double[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof long[]) {
            generator.writeStartArray();
            for (long v : (long[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof int[]) {
            generator.writeStartArray();
            for (int v : (int[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof float[]) {
            generator.writeStartArray();
            for (float v : (float[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else if (value instanceof short[]) {
            generator.writeStartArray();
            for (float v : (short[]) value) {
                generator.writeNumber(v);
            }
            generator.writeEndArray();
        } else {
            // if this is a "value" object, like enum, DistanceUnit, ..., just toString it
            // yea, it can be misleading when toString a Java class, but really, jackson should be used in that case
            generator.writeString(value.toString());
            //throw new ElasticsearchIllegalArgumentException("type not supported for generic value conversion: " + type);
        }
    }
}