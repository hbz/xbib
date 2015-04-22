/*
 * $Source$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1998, Hoylen Sue.  All Rights Reserved.
 * <h.sue@ieee.org>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 *
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:21 UTC
 */



package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>TermListDetails_scanInfo</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * TermListDetails_scanInfo ::=
 * SEQUENCE {
 *   maxStepSize [0] IMPLICIT INTEGER OPTIONAL
 *   collatingSequence [1] IMPLICIT HumanString OPTIONAL
 *   increasing [2] IMPLICIT BOOLEAN OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class TermListDetails_scanInfo extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a TermListDetails_scanInfo.
     */

    public TermListDetails_scanInfo() {
    }



    /**
     * Constructor for a TermListDetails_scanInfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public TermListDetails_scanInfo(BEREncoding ber, boolean check_tag)
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
        // TermListDetails_scanInfo should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun TermListDetails_scanInfo: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_maxStepSize = null;
        s_collatingSequence = null;
        s_increasing = null;

        // Decoding: maxStepSize [0] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxStepSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: collatingSequence [1] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_collatingSequence = new HumanString(p, false);
            part++;
        }

        // Decoding: increasing [2] IMPLICIT BOOLEAN OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_increasing = new ASN1Boolean(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun TermListDetails_scanInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the TermListDetails_scanInfo.
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
     * Returns a BER encoding of TermListDetails_scanInfo, implicitly tagged.
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

        int num_fields = 0; // number of mandatories
        if (s_maxStepSize != null) {
            num_fields++;
        }
        if (s_collatingSequence != null) {
            num_fields++;
        }
        if (s_increasing != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_maxStepSize: INTEGER OPTIONAL

        if (s_maxStepSize != null) {
            fields[x++] = s_maxStepSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_collatingSequence: HumanString OPTIONAL

        if (s_collatingSequence != null) {
            fields[x++] = s_collatingSequence.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_increasing: BOOLEAN OPTIONAL

        if (s_increasing != null) {
            fields[x++] = s_increasing.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the TermListDetails_scanInfo.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_maxStepSize != null) {
            str.append("maxStepSize ");
            str.append(s_maxStepSize);
            outputted++;
        }

        if (s_collatingSequence != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("collatingSequence ");
            str.append(s_collatingSequence);
            outputted++;
        }

        if (s_increasing != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("increasing ");
            str.append(s_increasing);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Integer s_maxStepSize; // optional
    public HumanString s_collatingSequence; // optional
    public ASN1Boolean s_increasing; // optional

} // TermListDetails_scanInfo


//EOF
