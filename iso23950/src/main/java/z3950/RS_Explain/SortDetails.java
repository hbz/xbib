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
package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.DatabaseName;

/**
 * Class for representing a <code>SortDetails</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * SortDetails ::=
 * SEQUENCE {
 *   commonInfo [0] IMPLICIT CommonInfo OPTIONAL
 *   databaseName [1] IMPLICIT DatabaseName
 *   sortKeys [2] IMPLICIT SEQUENCE OF SortKeyDetails OPTIONAL
 * }
 * </pre>
 *
 */
public final class SortDetails extends ASN1Any {

    /**
     * Default constructor for a SortDetails.
     */

    public SortDetails() {
    }

    /**
     * Constructor for a SortDetails from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public SortDetails(BEREncoding ber, boolean check_tag)
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
        // SortDetails should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun SortDetails: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: commonInfo [0] IMPLICIT CommonInfo OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonInfo = new CommonInfo(p, false);
            part++;
        }

        // Decoding: databaseName [1] IMPLICIT DatabaseName

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SortDetails: bad tag in s_databaseName\n");
        }

        s_databaseName = new DatabaseName(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_sortKeys = null;

        // Decoding: sortKeys [2] IMPLICIT SEQUENCE OF SortKeyDetails OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_sortKeys = new SortKeyDetails[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_sortKeys[n] = new SortKeyDetails(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun SortDetails: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the SortDetails.
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
     * Returns a BER encoding of SortDetails, implicitly tagged.
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
        if (s_commonInfo != null) {
            num_fields++;
        }
        if (s_sortKeys != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_commonInfo: CommonInfo OPTIONAL

        if (s_commonInfo != null) {
            fields[x++] = s_commonInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_databaseName: DatabaseName

        fields[x++] = s_databaseName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_sortKeys: SEQUENCE OF OPTIONAL

        if (s_sortKeys != null) {
            f2 = new BEREncoding[s_sortKeys.length];

            for (p = 0; p < s_sortKeys.length; p++) {
                f2[p] = s_sortKeys[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the SortDetails.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_commonInfo != null) {
            str.append("commonInfo ");
            str.append(s_commonInfo);
            outputted++;
        }

        if (0 < outputted) {
             str.append(", ");
        }
        str.append("databaseName ");
        str.append(s_databaseName);
        outputted++;

        if (s_sortKeys != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("sortKeys ");
            str.append("{");
            for (p = 0; p < s_sortKeys.length; p++) {
                if (p != 0) {
                   str.append(", ");
                }
                str.append(s_sortKeys[p]);
            }
            str.append("}");
            outputted++;
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public CommonInfo s_commonInfo; // optional
    public DatabaseName s_databaseName;
    public SortKeyDetails s_sortKeys[]; // optional

}