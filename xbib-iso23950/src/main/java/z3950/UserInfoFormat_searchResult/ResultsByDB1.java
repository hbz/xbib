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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:30 UTC
 */

//----------------------------------------------------------------

package z3950.UserInfoFormat_searchResult;
import asn1.*;
import z3950.v3.DatabaseName;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;
import z3950.v3.Query;
import z3950.v3.Term;

//================================================================
/**
 * Class for representing a <code>ResultsByDB1</code> from <code>UserInfoFormat-searchResult-1</code>
 *
 * <pre>
 * ResultsByDB1 ::=
 * SEQUENCE {
 *   databases [1] EXPLICIT ResultsByDB_databases
 *   count [2] IMPLICIT INTEGER OPTIONAL
 *   resultSetName [3] IMPLICIT InternationalString OPTIONAL
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class ResultsByDB1 extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a ResultsByDB1.
 */

public
ResultsByDB1()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a ResultsByDB1 from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
ResultsByDB1(BEREncoding ber, boolean check_tag)
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
  // ResultsByDB1 should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun ResultsByDB1: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;
  BERConstructed tagged;

  // Decoding: databases [1] EXPLICIT ResultsByDB_databases

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun ResultsByDB1: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 1 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun ResultsByDB1: bad tag in s_databases\n");

  try {
    tagged = (BERConstructed) p;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun ResultsByDB1: bad BER encoding: s_databases tag bad\n");
  }
  if (tagged.number_components() != 1) {
    throw new ASN1EncodingException
      ("Zebulun ResultsByDB1: bad BER encoding: s_databases tag bad\n");
  }

  s_databases = new ResultsByDB_databases(tagged.elementAt(0), true);
  part++;

  // Remaining elements are optional, set variables
  // to null (not present) so can return at end of BER

  s_count = null;
  s_resultSetName = null;

  // Decoding: count [2] IMPLICIT INTEGER OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_count = new ASN1Integer(p, false);
    part++;
  }

  // Decoding: resultSetName [3] IMPLICIT InternationalString OPTIONAL

  if (num_parts <= part) {
    return; // no more data, but ok (rest is optional)
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_resultSetName = new InternationalString(p, false);
    part++;
  }

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun ResultsByDB1: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the ResultsByDB1.
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
 * Returns a BER encoding of ResultsByDB1, implicitly tagged.
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

  int num_fields = 1; // number of mandatories
  if (s_count != null)
    num_fields++;
  if (s_resultSetName != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding enc[];

  // Encoding s_databases: ResultsByDB_databases 

  enc = new BEREncoding[1];
  enc[0] = s_databases.ber_encode();
  fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);

  // Encoding s_count: INTEGER OPTIONAL

  if (s_count != null) {
    fields[x++] = s_count.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
  }

  // Encoding s_resultSetName: InternationalString OPTIONAL

  if (s_resultSetName != null) {
    fields[x++] = s_resultSetName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
  }

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the ResultsByDB1. 
 */

public String
toString()
{
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  str.append("databases ");
  str.append(s_databases);
  outputted++;

  if (s_count != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("count ");
    str.append(s_count);
    outputted++;
  }

  if (s_resultSetName != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("resultSetName ");
    str.append(s_resultSetName);
    outputted++;
  }

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public ResultsByDB_databases s_databases;
public ASN1Integer s_count; // optional
public InternationalString s_resultSetName; // optional

} // ResultsByDB1

//----------------------------------------------------------------
//EOF
