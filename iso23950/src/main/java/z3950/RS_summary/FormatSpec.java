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
package z3950.RS_summary;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;

/**
 * Class for representing a <code>FormatSpec</code> from <code>RecordSyntax-summary</code>
 * <p/>
 * <pre>
 * FormatSpec ::=
 * SEQUENCE {
 *   type [1] IMPLICIT InternationalString
 *   size [2] IMPLICIT INTEGER OPTIONAL
 *   bestPosn [3] IMPLICIT INTEGER OPTIONAL
 * }
 * </pre>
 *
 */
public final class FormatSpec extends ASN1Any {
    /**
     * Default constructor for a FormatSpec.
     */

    public FormatSpec() {
    }

    /**
     * Constructor for a FormatSpec from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public FormatSpec(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag);
    }

    /**
     * Initializing object from a BER encoding.
     * This method is for internal use only. You should use
     * the constructor that takes a BEREncoding.
     *
     * @param ber       the BER to decode.
     * @param check_tag if the tag should be checked.
     * @throws ASN1Exception if the BER encoding is bad.
     */

    public void
    ber_decode(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        // FormatSpec should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun FormatSpec: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: type [1] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun FormatSpec: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun FormatSpec: bad tag in s_type\n");
        }

        s_type = new InternationalString(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_size = null;
        s_bestPosn = null;

        // Decoding: size [2] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_size = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: bestPosn [3] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_bestPosn = new ASN1Integer(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun FormatSpec: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the FormatSpec.
     *
     * @exception ASN1Exception Invalid or cannot be encoded.
     * @return The BER encoding.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
    }

    /**
     * Returns a BER encoding of FormatSpec, implicitly tagged.
     *
     * @param tag_type The type of the implicit tag.
     * @param tag      The implicit tag.
     * @return The BER encoding of the object.
     * @exception ASN1Exception When invalid or cannot be encoded.
     * @see asn1.BEREncoding#UNIVERSAL_TAG
     * @see asn1.BEREncoding#APPLICATION_TAG
     * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
     * @see asn1.BEREncoding#PRIVATE_TAG
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Calculate the number of fields in the encoding

        int num_fields = 1; // number of mandatories
        if (s_size != null) {
            num_fields++;
        }
        if (s_bestPosn != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_type: InternationalString

        fields[x++] = s_type.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_size: INTEGER OPTIONAL

        if (s_size != null) {
            fields[x++] = s_size.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_bestPosn: INTEGER OPTIONAL

        if (s_bestPosn != null) {
            fields[x++] = s_bestPosn.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the FormatSpec.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("type ");
        str.append(s_type);
        outputted++;

        if (s_size != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("size ");
            str.append(s_size);
            outputted++;
        }

        if (s_bestPosn != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("bestPosn ");
            str.append(s_bestPosn);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public InternationalString s_type;
    public ASN1Integer s_size; // optional
    public ASN1Integer s_bestPosn; // optional

}