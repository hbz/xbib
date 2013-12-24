

package org.xbib.common.xcontent.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;
import org.xbib.common.xcontent.support.AbstractXContentParser;

import java.io.IOException;

public class JsonXContentParser extends AbstractXContentParser {

    final JsonParser parser;

    public JsonXContentParser(JsonParser parser) {
        this.parser = parser;
    }

    
    public XContentType contentType() {
        return XContentType.JSON;
    }

    
    public XContentParser.Token nextToken() throws IOException {
        return convertToken(parser.nextToken());
    }

    
    public void skipChildren() throws IOException {
        parser.skipChildren();
    }

    
    public XContentParser.Token currentToken() {
        return convertToken(parser.getCurrentToken());
    }

    
    public XContentParser.NumberType numberType() throws IOException {
        return convertNumberType(parser.getNumberType());
    }

    
    public boolean estimatedNumberType() {
        return true;
    }

    
    public String currentName() throws IOException {
        return parser.getCurrentName();
    }

    
    protected boolean doBooleanValue() throws IOException {
        return parser.getBooleanValue();
    }

    
    public String text() throws IOException {
        return parser.getText();
    }

    
    public boolean hasTextCharacters() {
        return parser.hasTextCharacters();
    }

    
    public char[] textCharacters() throws IOException {
        return parser.getTextCharacters();
    }

    
    public int textLength() throws IOException {
        return parser.getTextLength();
    }

    
    public int textOffset() throws IOException {
        return parser.getTextOffset();
    }

    
    public Number numberValue() throws IOException {
        return parser.getNumberValue();
    }

    
    public short doShortValue() throws IOException {
        return parser.getShortValue();
    }

    
    public int doIntValue() throws IOException {
        return parser.getIntValue();
    }

    
    public long doLongValue() throws IOException {
        return parser.getLongValue();
    }

    
    public float doFloatValue() throws IOException {
        return parser.getFloatValue();
    }

    
    public double doDoubleValue() throws IOException {
        return parser.getDoubleValue();
    }

    
    public byte[] binaryValue() throws IOException {
        return parser.getBinaryValue();
    }

    
    public void close() {
        try {
            parser.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private NumberType convertNumberType(JsonParser.NumberType numberType) {
        switch (numberType) {
            case INT:
                return NumberType.INT;
            case LONG:
                return NumberType.LONG;
            case FLOAT:
                return NumberType.FLOAT;
            case DOUBLE:
                return NumberType.DOUBLE;
        }
        throw new IllegalStateException("No matching token for number_type [" + numberType + "]");
    }

    private Token convertToken(JsonToken token) {
        if (token == null) {
            return null;
        }
        switch (token) {
            case FIELD_NAME:
                return Token.FIELD_NAME;
            case VALUE_FALSE:
            case VALUE_TRUE:
                return Token.VALUE_BOOLEAN;
            case VALUE_STRING:
                return Token.VALUE_STRING;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return Token.VALUE_NUMBER;
            case VALUE_NULL:
                return Token.VALUE_NULL;
            case START_OBJECT:
                return Token.START_OBJECT;
            case END_OBJECT:
                return Token.END_OBJECT;
            case START_ARRAY:
                return Token.START_ARRAY;
            case END_ARRAY:
                return Token.END_ARRAY;
            case VALUE_EMBEDDED_OBJECT:
                return Token.VALUE_EMBEDDED_OBJECT;
        }
        throw new IllegalStateException("No matching token for json_token [" + token + "]");
    }
}
