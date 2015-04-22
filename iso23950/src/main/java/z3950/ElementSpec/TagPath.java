package z3950.ElementSpec;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>TagPath</code> from <code>ElementSpecificationFormat-eSpec-1</code>
 *
 * <pre>
 * TagPath ::=
 * SEQUENCE OF TagPath1
 * </pre>
 *
 */



public final class TagPath extends ASN1Any {


    /**
     * Default constructor for a TagPath.
     */

    public TagPath() {
    }



    /**
     * Constructor for a TagPath from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public TagPath(BEREncoding ber, boolean check_tag)
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
        // TagPath should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun TagPath: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        value = new TagPath1[num_parts];
        int p;
        for (p = 0; p < num_parts; p++) {
            value[p] = new TagPath1(ber_cons.elementAt(p), true);
        }
    }



    /**
     * Returns a BER encoding of the TagPath.
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
     * Returns a BER encoding of TagPath, implicitly tagged.
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
        BEREncoding fields[] = new BERConstructed[value.length];
        int p;

        for (p = 0; p < value.length; p++) {
            fields[p] = value[p].ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the TagPath.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int p;

        for (p = 0; p < value.length; p++) {
            str.append(value[p]);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public TagPath1 value[];

}