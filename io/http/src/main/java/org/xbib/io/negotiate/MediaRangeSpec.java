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
package org.xbib.io.negotiate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaRangeSpec {

    private final static Pattern tokenPattern;

    private final static Pattern parameterPattern;

    private final static Pattern mediaRangePattern;

    private final static Pattern qValuePattern;

    static {
        // See RFC 2616, section 2.2
        String token = "[\\x20-\\x7E&&[^()<>@,;:\\\"/\\[\\]?={} ]]+";
        String quotedString = "\"((?:[\\x20-\\x7E\\n\\r\\t&&[^\"\\\\]]|\\\\[\\x00-\\x7F])*)\"";
        // See RFC 2616, section 3.6
        String parameter = ";\\s*(?!q\\s*=)(" + token + ")=(?:(" + token + ")|" + quotedString + ")";
        // See RFC 2616, section 3.9
        String qualityValue = "(?:0(?:\\.\\d{0,3})?|1(?:\\.0{0,3})?)";
        // See RFC 2616, sections 14.1 
        String quality = ";\\s*q\\s*=\\s*([^;,]*)";
        // See RFC 2616, section 3.7
        String regex = "(" + token + ")/(" + token + ")"
                + "((?:\\s*" + parameter + ")*)"
                + "(?:\\s*" + quality + ")?"
                + "((?:\\s*" + parameter + ")*)";
        tokenPattern = Pattern.compile(token);
        parameterPattern = Pattern.compile(parameter);
        mediaRangePattern = Pattern.compile(regex);
        qValuePattern = Pattern.compile(qualityValue);
    }

    private final String type;
    private final String subtype;
    private final List parameterNames;
    private final List parameterValues;
    private final String mediaType;
    private final double quality;
    private MediaRangeSpec(String type, String subtype,
                           List parameterNames, List parameterValues,
                           double quality) {
        this.type = type;
        this.subtype = subtype;
        this.parameterNames = Collections.unmodifiableList(parameterNames);
        this.parameterValues = parameterValues;
        this.mediaType = buildMediaType();
        this.quality = quality;
    }

    /**
     * Parses a media type from a string such as <tt>text/html;charset=utf-8;q=0.9</tt>.
     */
    public static MediaRangeSpec parseType(String mediaType) {
        MediaRangeSpec m = parseRange(mediaType);
        if (m == null || m.isWildcardType() || m.isWildcardSubtype()) {
            return null;
        }
        return m;
    }

    /**
     * Parses a media range from a string such as <tt>text/*;charset=utf-8;q=0.9</tt>.
     * Unlike simple media types, media ranges may include wildcards.
     */
    public static MediaRangeSpec parseRange(String mediaRange) {
        Matcher m = mediaRangePattern.matcher(mediaRange);
        if (!m.matches()) {
            return null;
        }
        String type = m.group(1).toLowerCase();
        String subtype = m.group(2).toLowerCase();
        String unparsedParameters = m.group(3);
        String qValue = m.group(7);
        m = parameterPattern.matcher(unparsedParameters);
        if ("*".equals(type) && !"*".equals(subtype)) {
            return null;
        }
        List<String> parameterNames = new ArrayList<>();
        List<String> parameterValues = new ArrayList<>();
        while (m.find()) {
            String name = m.group(1).toLowerCase();
            String value = (m.group(3) == null) ? m.group(2) : unescape(m.group(3));
            parameterNames.add(name);
            parameterValues.add(value);
        }
        double quality = 1.0;
        if (qValue != null && qValuePattern.matcher(qValue).matches()) {
            try {
                quality = Double.parseDouble(qValue);
            } catch (NumberFormatException ex) {
                // quality stays at default value
            }
        }
        return new MediaRangeSpec(type, subtype, parameterNames, parameterValues, quality);
    }

    /**
     * Parses an HTTP Accept header into a List of MediaRangeSpecs
     *
     * @return A List of MediaRangeSpecs
     */
    public static List<MediaRangeSpec> parseAccept(String s) {
        List<MediaRangeSpec> result = new ArrayList<>();
        Matcher m = mediaRangePattern.matcher(s);
        while (m.find()) {
            result.add(parseRange(m.group()));
        }
        return result;
    }

    private static String unescape(String s) {
        return s.replaceAll("\\\\(.)", "$1");
    }

    private static String escape(String s) {
        return s.replaceAll("[\\\\\"]", "\\\\$0");
    }

    private String buildMediaType() {
        StringBuilder result = new StringBuilder();
        result.append(type);
        result.append("/");
        result.append(subtype);
        for (int i = 0; i < parameterNames.size(); i++) {
            result.append(";");
            result.append(parameterNames.get(i));
            result.append("=");
            String value = (String) parameterValues.get(i);
            if (tokenPattern.matcher(value).matches()) {
                result.append(value);
            } else {
                result.append("\"");
                result.append(escape(value));
                result.append("\"");
            }
        }
        return result.toString();
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getMediaType() {
        return mediaType;
    }

    public List getParameterNames() {
        return parameterNames;
    }

    public String getParameter(String parameterName) {
        for (int i = 0; i < parameterNames.size(); i++) {
            if (parameterNames.get(i).equals(parameterName.toLowerCase())) {
                return (String) parameterValues.get(i);
            }
        }
        return null;
    }

    public boolean isWildcardType() {
        return "*".equals(type);
    }

    public boolean isWildcardSubtype() {
        return !isWildcardType() && "*".equals(subtype);
    }

    public double getQuality() {
        return quality;
    }

    public int getPrecedence(MediaRangeSpec range) {
        if (range.isWildcardType()) {
            return 1;
        }
        if (!range.type.equals(type)) {
            return 0;
        }
        if (range.isWildcardSubtype()) {
            return 2;
        }
        if (!range.subtype.equals(subtype)) {
            return 0;
        }
        if (range.getParameterNames().isEmpty()) {
            return 3;
        }
        int result = 3;
        for (int i = 0; i < range.getParameterNames().size(); i++) {
            String name = (String) range.getParameterNames().get(i);
            String value = range.getParameter(name);
            if (!value.equals(getParameter(name))) {
                return 0;
            }
            result++;
        }
        return result;
    }

    public MediaRangeSpec getBestMatch(List<MediaRangeSpec> mediaRanges) {
        MediaRangeSpec result = null;
        int bestPrecedence = 0;
        for (MediaRangeSpec range : mediaRanges) {
            if (getPrecedence(range) > bestPrecedence) {
                bestPrecedence = getPrecedence(range);
                result = range;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return mediaType + ";q=" + quality;
    }
}