package org.xbib.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An LCCN is an identifier assigned by the Library of Congress for a metadata record
 * (e.g., bibliographic record, authority record).
 *
 * LCCNs have three components: prefix, year, and serial number.
 * The prefix is optional; if present, it has one to three lowercase alphabetic
 * characters. (Prefixes are maintained in a controlled list.)
 * The year is two or four digits. (For 2000 and earlier the year is two digits,
 * for 2001 and later, four digits.)
 * The serial number (after normalization) is six digits.
 * (An un-normalized LCCN may have a serial number of 1-6 digits, which is
 * left-filled with zeros when normalized.)
 *
 * Syntax of a Normalized LCCN
 *
 *  A normalized LCCN is a character string eight to twelve characters in length.
 *  (For purposes of this description characters are ordered
 *  from left to right -- "first" means "leftmost".)
 *  The rightmost eight characters are always digits.
 *   If the length is 9, then the first character must be alphabetic.
 *   If the length is 10, then the first two characters must be either both
 *   digits or both alphabetic.
 *   If the length is 11, then the first character must be alphabetic and
 *   the next two characters must be either both digits or both alphabetic.
 *   If the length is 12, then the first two characters must be alphabetic
 *   and the remaining characters digits.
 *
 *  Normalization of LCCNs
 *
 *  An LCCN is to be normalized to its canonical form described in the syntax
 *  description above, as follows:
 *
 *  Remove all blanks.
 *  If there is a forward slash (/) in the string, remove it, and remove all
 *  characters to the right of the forward slash.
 *  If there is a hyphen in the string:
 *  Remove it.
 *  Inspect the substring following (to the right of) the (removed) hyphen.
 *  Then (and assuming that steps 1 and 2 have been carried out):
 *  All these characters should be digits, and there should be six or less.
 *  If the length of the substring is less than 6, left-fill the substring
 *  with zeros until the length is six.
 *
 *  Examples:
 *
 *  "n78-890351" normalizes to "n78890351".
 *  "n78-89035" normalizes to "n78089035".
 *  "n 78890351 " normalizes to "n78890351".
 *  " 85000002 " normalizes to "85000002"
 *  "85-2 " normalizes to "85000002"
 *  "2001-000002" normalizes to "2001000002"
 *  "75-425165//r75" normalizes to "75425165"
 *  " 79139101 /AC/r932" normalizes to "79139101"
 *
 *  @see <a href="http://www.loc.gov/marc/lccn-namespace.html#syntax">LCCN</a>
 */
public class LCCN implements Comparable<LCCN>, StandardNumber {

    private final static Pattern PATTERN = Pattern.compile("[\\p{Alnum}\\.\\-\\s]{0,11}");

    private String value;

    private String formatted;

    public LCCN setValue(String value) {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = value.substring(m.start(), m.end());
        }
        return this;
    }

    @Override
    public int compareTo(LCCN o) {
        return value != null ?  value.compareTo(o.getValue()) : -1;
    }

    public String getValue() {
        return value;
    }

    public LCCN checksum() {
        return this;
    }

    public LCCN parse() {
        this.value = clean(value);
        return this;
    }

    public LCCN verify() throws NumberFormatException {
        return this;
    }

    public String format() {
        if (formatted == null) {
            this.formatted = value;
        }
        return formatted;
    }

    private String clean(String value) {
        StringBuilder sb = new StringBuilder(value);
        // remove all spaces
        int i = sb.indexOf(" ");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf(" ");
        }
        // remove all characters right from '/'
        i = sb.indexOf("/");
        if (i >= 0) {
            sb = new StringBuilder(sb.subSequence(0, i));
        }
        // dehyphenate
        i = sb.indexOf("-");
        while (i >= 0) {
            sb.deleteCharAt(i);
            int begin = i;
            // fill  block up to six characters wilth leading zeroes
            i = sb.indexOf("-");
            int end = i >= 0 ? i : sb.length();
            int l = 6 - (end - begin);
            while (l-- > 0) {
                sb.insert(begin, '0');
            }
        }
        return sb.toString();
    }
}
