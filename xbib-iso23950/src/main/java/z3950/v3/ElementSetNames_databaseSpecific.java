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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:05 UTC
 */

//----------------------------------------------------------------

package z3950.v3;
import asn1.*;


//================================================================
/**
 * Class for representing a <code>ElementSetNames_databaseSpecific</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * ElementSetNames_databaseSpecific ::=
 * SEQUENCE {
 *   dbName DatabaseName
 *   esn ElementSetName
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class ElementSetNames_databaseSpecific extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a ElementSetNames_databaseSpecific.
 */

public
ElementSetNames_databaseSpecific()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a ElementSetNames_databaseSpecific from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
ElementSetNames_databaseSpecific(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  super(ber, check_tag);
}

//----------------------------------------------------------------
/**
 * Initializing object from a BER encoding.
 * This method is for internal use only. You should use
 * the constructor that takes a BEREncoding.
 *
 * @param ber the BER to decode.
 * @param check_tag if the tag should be checked.
 * @exception ASN1Exception if the BER encoding is bad.
 */

public void
ber_decode(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  // ElementSetNames_databaseSpecific should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun ElementSetNames_databaseSpecific: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: dbName DatabaseName

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ElementSetNames_databaseSpecific: incomplete");
  }
  p = ber_cons.elementAt(part);

  s_dbName = new DatabaseName(p, true);
  part++;

  // Decoding: esn ElementSetName

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ElementSetNames_databaseSpecific: incomplete");
  }
  p = ber_cons.elementAt(part);

  s_esn = new ElementSetName(p, true);
  part++;

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun ElementSetNames_databaseSpecific: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the ElementSetNames_databaseSpecific.
 *
 * @exception	ASN1Exception Invalid or cannot be encoded.
 * @return	The BER encoding.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of ElementSetNames_databaseSpecific, implicitly tagged.
 *
 * @param tag_type	The type of the implicit tag.
 * @param tag	The implicit tag.
 * @return	The BER encoding of the object.
 * @exception	ASN1Exception When invalid or cannot be encoded.
 * @see asn1.BEREncoding#UNIVERSAL_TAG
 * @see asn1.BEREncoding#APPLICATION_TAG
 * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
 * @see asn1.BEREncoding#PRIVATE_TAG
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  // Calculate the number of fields in the encoding

  int num_fields = 2; // number of mandatories

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;

  // Encoding s_dbName: DatabaseName 

  fields[x++] = s_dbName.ber_encode();

  // Encoding s_esn: ElementSetName 

  fields[x++] = s_esn.ber_encode();

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the ElementSetNames_databaseSpecific. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("dbName ");
  str.append(s_dbName);
  outputted++;

  if (0 < outputted)
    str.append(", ");
  str.append("esn ");
  str.append(s_esn);
  outputted++;

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public DatabaseName s_dbName;
public ElementSetName s_esn;

} // ElementSetNames_databaseSpecific

//----------------------------------------------------------------
//EOF
