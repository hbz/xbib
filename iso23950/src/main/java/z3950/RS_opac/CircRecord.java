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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:22 UTC
 */



package z3950.RS_opac;

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>CircRecord</code> from <code>RecordSyntax-opac</code>
 * <p/>
 * <pre>
 * CircRecord ::=
 * SEQUENCE {
 *   availableNow [1] IMPLICIT BOOLEAN
 *   availablityDate [2] IMPLICIT InternationalString OPTIONAL
 *   availableThru [3] IMPLICIT InternationalString OPTIONAL
 *   restrictions [4] IMPLICIT InternationalString OPTIONAL
 *   itemId [5] IMPLICIT InternationalString OPTIONAL
 *   renewable [6] IMPLICIT BOOLEAN
 *   onHold [7] IMPLICIT BOOLEAN
 *   enumAndChron [8] IMPLICIT InternationalString OPTIONAL
 *   midspine [9] IMPLICIT InternationalString OPTIONAL
 *   temporaryLocation [10] IMPLICIT InternationalString OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class CircRecord extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a CircRecord.
     */

    public CircRecord() {
    }



    /**
     * Constructor for a CircRecord from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public CircRecord(BEREncoding ber, boolean check_tag)
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
        // CircRecord should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun CircRecord: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: availableNow [1] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CircRecord: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun CircRecord: bad tag in s_availableNow\n");
        }

        s_availableNow = new ASN1Boolean(p, false);
        part++;

        // Decoding: availablityDate [2] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CircRecord: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_availablityDate = new InternationalString(p, false);
            part++;
        }

        // Decoding: availableThru [3] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CircRecord: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_availableThru = new InternationalString(p, false);
            part++;
        }

        // Decoding: restrictions [4] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CircRecord: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_restrictions = new InternationalString(p, false);
            part++;
        }

        // Decoding: itemId [5] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CircRecord: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_itemId = new InternationalString(p, false);
            part++;
        }

        // Decoding: renewable [6] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CircRecord: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 6 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun CircRecord: bad tag in s_renewable\n");
        }

        s_renewable = new ASN1Boolean(p, false);
        part++;

        // Decoding: onHold [7] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CircRecord: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 7 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun CircRecord: bad tag in s_onHold\n");
        }

        s_onHold = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_enumAndChron = null;
        s_midspine = null;
        s_temporaryLocation = null;

        // Decoding: enumAndChron [8] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 8 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_enumAndChron = new InternationalString(p, false);
            part++;
        }

        // Decoding: midspine [9] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 9 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_midspine = new InternationalString(p, false);
            part++;
        }

        // Decoding: temporaryLocation [10] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 10 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_temporaryLocation = new InternationalString(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun CircRecord: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the CircRecord.
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
     * Returns a BER encoding of CircRecord, implicitly tagged.
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

        int num_fields = 3; // number of mandatories
        if (s_availablityDate != null) {
            num_fields++;
        }
        if (s_availableThru != null) {
            num_fields++;
        }
        if (s_restrictions != null) {
            num_fields++;
        }
        if (s_itemId != null) {
            num_fields++;
        }
        if (s_enumAndChron != null) {
            num_fields++;
        }
        if (s_midspine != null) {
            num_fields++;
        }
        if (s_temporaryLocation != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_availableNow: BOOLEAN

        fields[x++] = s_availableNow.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_availablityDate: InternationalString OPTIONAL

        if (s_availablityDate != null) {
            fields[x++] = s_availablityDate.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_availableThru: InternationalString OPTIONAL

        if (s_availableThru != null) {
            fields[x++] = s_availableThru.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_restrictions: InternationalString OPTIONAL

        if (s_restrictions != null) {
            fields[x++] = s_restrictions.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_itemId: InternationalString OPTIONAL

        if (s_itemId != null) {
            fields[x++] = s_itemId.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_renewable: BOOLEAN

        fields[x++] = s_renewable.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);

        // Encoding s_onHold: BOOLEAN

        fields[x++] = s_onHold.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);

        // Encoding s_enumAndChron: InternationalString OPTIONAL

        if (s_enumAndChron != null) {
            fields[x++] = s_enumAndChron.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
        }

        // Encoding s_midspine: InternationalString OPTIONAL

        if (s_midspine != null) {
            fields[x++] = s_midspine.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
        }

        // Encoding s_temporaryLocation: InternationalString OPTIONAL

        if (s_temporaryLocation != null) {
            fields[x++] = s_temporaryLocation.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the CircRecord.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("availableNow ");
        str.append(s_availableNow);
        outputted++;

        if (s_availablityDate != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("availablityDate ");
            str.append(s_availablityDate);
            outputted++;
        }

        if (s_availableThru != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("availableThru ");
            str.append(s_availableThru);
            outputted++;
        }

        if (s_restrictions != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("restrictions ");
            str.append(s_restrictions);
            outputted++;
        }

        if (s_itemId != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("itemId ");
            str.append(s_itemId);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("renewable ");
        str.append(s_renewable);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("onHold ");
        str.append(s_onHold);
        outputted++;

        if (s_enumAndChron != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("enumAndChron ");
            str.append(s_enumAndChron);
            outputted++;
        }

        if (s_midspine != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("midspine ");
            str.append(s_midspine);
            outputted++;
        }

        if (s_temporaryLocation != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("temporaryLocation ");
            str.append(s_temporaryLocation);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Boolean s_availableNow;
    public InternationalString s_availablityDate; // optional
    public InternationalString s_availableThru; // optional
    public InternationalString s_restrictions; // optional
    public InternationalString s_itemId; // optional
    public ASN1Boolean s_renewable;
    public ASN1Boolean s_onHold;
    public InternationalString s_enumAndChron; // optional
    public InternationalString s_midspine; // optional
    public InternationalString s_temporaryLocation; // optional

} // CircRecord


//EOF
