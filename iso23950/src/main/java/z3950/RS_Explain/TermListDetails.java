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
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;
import z3950.v3.Term;

/**
 * Class for representing a <code>TermListDetails</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * TermListDetails ::=
 * SEQUENCE {
 *   commonInfo [0] IMPLICIT CommonInfo OPTIONAL
 *   termListName [1] IMPLICIT InternationalString
 *   description [2] IMPLICIT HumanString OPTIONAL
 *   attributes [3] IMPLICIT AttributeCombinations OPTIONAL
 *   scanInfo [4] IMPLICIT TermListDetails_scanInfo OPTIONAL
 *   estNumberTerms [5] IMPLICIT INTEGER OPTIONAL
 *   sampleTerms [6] IMPLICIT SEQUENCE OF Term OPTIONAL
 * }
 * </pre>
 *
 */

public final class TermListDetails extends ASN1Any {

    /**
     * Default constructor for a TermListDetails.
     */

    public TermListDetails() {
    }

    /**
     * Constructor for a TermListDetails from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public TermListDetails(BEREncoding ber, boolean check_tag)
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
        // TermListDetails should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun TermListDetails: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: commonInfo [0] IMPLICIT CommonInfo OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TermListDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonInfo = new CommonInfo(p, false);
            part++;
        }

        // Decoding: termListName [1] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TermListDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun TermListDetails: bad tag in s_termListName\n");
        }

        s_termListName = new InternationalString(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_description = null;
        s_attributes = null;
        s_scanInfo = null;
        s_estNumberTerms = null;
        s_sampleTerms = null;

        // Decoding: description [2] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Decoding: attributes [3] IMPLICIT AttributeCombinations OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_attributes = new AttributeCombinations(p, false);
            part++;
        }

        // Decoding: scanInfo [4] IMPLICIT TermListDetails_scanInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_scanInfo = new TermListDetails_scanInfo(p, false);
            part++;
        }

        // Decoding: estNumberTerms [5] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_estNumberTerms = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: sampleTerms [6] IMPLICIT SEQUENCE OF Term OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_sampleTerms = new Term[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_sampleTerms[n] = new Term(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun TermListDetails: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the TermListDetails.
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
     * Returns a BER encoding of TermListDetails, implicitly tagged.
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
        if (s_description != null) {
            num_fields++;
        }
        if (s_attributes != null) {
            num_fields++;
        }
        if (s_scanInfo != null) {
            num_fields++;
        }
        if (s_estNumberTerms != null) {
            num_fields++;
        }
        if (s_sampleTerms != null) {
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

        // Encoding s_termListName: InternationalString

        fields[x++] = s_termListName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_attributes: AttributeCombinations OPTIONAL

        if (s_attributes != null) {
            fields[x++] = s_attributes.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_scanInfo: TermListDetails_scanInfo OPTIONAL

        if (s_scanInfo != null) {
            fields[x++] = s_scanInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_estNumberTerms: INTEGER OPTIONAL

        if (s_estNumberTerms != null) {
            fields[x++] = s_estNumberTerms.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_sampleTerms: SEQUENCE OF OPTIONAL

        if (s_sampleTerms != null) {
            f2 = new BEREncoding[s_sampleTerms.length];

            for (p = 0; p < s_sampleTerms.length; p++) {
                f2[p] = s_sampleTerms[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the TermListDetails.
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
        str.append("termListName ");
        str.append(s_termListName);
        outputted++;

        if (s_description != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        if (s_attributes != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("attributes ");
            str.append(s_attributes);
            outputted++;
        }

        if (s_scanInfo != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("scanInfo ");
            str.append(s_scanInfo);
            outputted++;
        }

        if (s_estNumberTerms != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("estNumberTerms ");
            str.append(s_estNumberTerms);
            outputted++;
        }

        if (s_sampleTerms != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("sampleTerms ");
            str.append("{");
            for (p = 0; p < s_sampleTerms.length; p++) {
                if (p != 0) {
                   str.append(", ");
                }
                str.append(s_sampleTerms[p]);
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
    public InternationalString s_termListName;
    public HumanString s_description; // optional
    public AttributeCombinations s_attributes; // optional
    public TermListDetails_scanInfo s_scanInfo; // optional
    public ASN1Integer s_estNumberTerms; // optional
    public Term s_sampleTerms[]; // optional

}