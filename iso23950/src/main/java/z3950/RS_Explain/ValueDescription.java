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



package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1OctetString;
import asn1.BEREncoding;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;
import z3950.v3.Unit;



/**
 * Class for representing a <code>ValueDescription</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * ValueDescription ::=
 * CHOICE {
 *   integer INTEGER
 *   string InternationalString
 *   octets OCTET STRING
 *   oid OBJECT IDENTIFIER
 *   unit [1] IMPLICIT Unit
 *   valueAndUnit [2] IMPLICIT IntUnit
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class ValueDescription extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a ValueDescription.
     */

    public ValueDescription() {
    }



    /**
     * Constructor for a ValueDescription from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ValueDescription(BEREncoding ber, boolean check_tag)
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
        // Null out all choices

        c_integer = null;
        c_string = null;
        c_octets = null;
        c_oid = null;
        c_unit = null;
        c_valueAndUnit = null;

        // Try choice integer
        try {
            c_integer = new ASN1Integer(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        // Try choice string
        try {
            c_string = new InternationalString(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        // Try choice octets
        try {
            c_octets = new ASN1OctetString(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        // Try choice oid
        try {
            c_oid = new ASN1ObjectIdentifier(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        // Try choice unit
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_unit = new Unit(ber, false);
            return;
        }

        // Try choice valueAndUnit
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_valueAndUnit = new IntUnit(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun ValueDescription: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of ValueDescription.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_integer
        if (c_integer != null) {
            chosen = c_integer.ber_encode();
        }

        // Encoding choice: c_string
        if (c_string != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_string.ber_encode();
        }

        // Encoding choice: c_octets
        if (c_octets != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_octets.ber_encode();
        }

        // Encoding choice: c_oid
        if (c_oid != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_oid.ber_encode();
        }

        // Encoding choice: c_unit
        if (c_unit != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_unit.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_valueAndUnit
        if (c_valueAndUnit != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_valueAndUnit.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Check for error of having none of the choices set
        if (chosen == null) {
            throw new ASN1Exception("CHOICE not set");
        }

        return chosen;
    }



    /**
     * Generating a BER encoding of the object
     * and implicitly tagging it.
     * <p/>
     * This method is for internal use only. You should use
     * the ber_encode method that does not take a parameter.
     * <p/>
     * This function should never be used, because this
     * production is a CHOICE.
     * It must never have an implicit tag.
     * <p/>
     * An exception will be thrown if it is called.
     *
     * @param tag_type the type of the tag.
     * @param tag      the tag.
     * @throws ASN1Exception if it cannot be BER encoded.
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // This method must not be called!

        // Method is not available because this is a basic CHOICE
        // which does not have an explicit tag on it. So it is not
        // permitted to allow something else to apply an implicit
        // tag on it, otherwise the tag identifying which CHOICE
        // it is will be overwritten and lost.

        throw new ASN1EncodingException("Zebulun ValueDescription: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the ValueDescription.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_integer != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: integer> ");
            }
            found = true;
            str.append("integer ");
            str.append(c_integer);
        }

        if (c_string != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: string> ");
            }
            found = true;
            str.append("string ");
            str.append(c_string);
        }

        if (c_octets != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: octets> ");
            }
            found = true;
            str.append("octets ");
            str.append(c_octets);
        }

        if (c_oid != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: oid> ");
            }
            found = true;
            str.append("oid ");
            str.append(c_oid);
        }

        if (c_unit != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: unit> ");
            }
            found = true;
            str.append("unit ");
            str.append(c_unit);
        }

        if (c_valueAndUnit != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: valueAndUnit> ");
            }
            found = true;
            str.append("valueAndUnit ");
            str.append(c_valueAndUnit);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Integer c_integer;
    public InternationalString c_string;
    public ASN1OctetString c_octets;
    public ASN1ObjectIdentifier c_oid;
    public Unit c_unit;
    public IntUnit c_valueAndUnit;

} // ValueDescription


//EOF
