/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.util;

/**
 * Encodes and decodes to and from Base64 notation.
 * <p>
 * Based on Base64 encoder and decoder version 2.2.1 written by Robert Harder 
 * (<a href="http://iharder.net/base64">http://iharder.net/base64</a>).
 * Modified by Erwin Vervaet to use the '.' character as padding character
 * when using URL safe encoding, like in the Bouncy Castle URLBase64 encoder
 * (<a href="http://www.bouncycastle.org/java.html">http://www.bouncycastle.org/java.html</a>).
 *
 * @author Robert Harder
 * @author Erwin Vervaet
 */
public class Base64 {
	
	/* static data used by the encoding and decoding algorithm  
	
	/* The equals sign (=) as a byte. */
	private static final byte EQUALS_SIGN = (byte)'=';
	/* The dot (.) as a byte. */
	private static final byte DOT = (byte)'.';
	
	private static final byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding
	private static final byte PADDING_CHAR_ENC = -1; // Indicates padding char in encoding
	
	
	/* ********  S T A N D A R D   B A S E 6 4   A L P H A B E T  ******** */	
	
	/** The 64 valid Base64 values. */
	/* Host platform may be something funny like EBCDIC, so we hardcode these values. */
	private static final byte[] STANDARD_ALPHABET = {
		(byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
		(byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
		(byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', 
		(byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
		(byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
		(byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
		(byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', 
		(byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
		(byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', 
		(byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/'
	};

	/** 
	 * Translates a Base64 value to either its 6-bit reconstruction value
	 * or a negative number indicating some other meaning.
	 **/
	private static final byte[] STANDARD_DECODABET = {   
		-9,-9,-9,-9,-9,-9,-9,-9,-9,				 // Decimal  0 -  8
		-5,-5,									 // Whitespace: Tab and Linefeed
		-9,-9,									 // Decimal 11 - 12
		-5,										 // Whitespace: Carriage Return
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,	 // Decimal 14 - 26
		-9,-9,-9,-9,-9,							 // Decimal 27 - 31
		-5,										 // Whitespace: Space
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,			 // Decimal 33 - 42
		62,										 // Plus sign at decimal 43
		-9,-9,-9,								 // Decimal 44 - 46
		63,										 // Slash at decimal 47
		52,53,54,55,56,57,58,59,60,61,			 // Numbers zero through nine
		-9,-9,-9,								 // Decimal 58 - 60
		-1,										 // Equals sign at decimal 61
		-9,-9,-9,								 // Decimal 62 - 64
		0,1,2,3,4,5,6,7,8,9,10,11,12,13,		 // Letters 'A' through 'N'
		14,15,16,17,18,19,20,21,22,23,24,25,	 // Letters 'O' through 'Z'
		-9,-9,-9,-9,-9,-9,						 // Decimal 91 - 96
		26,27,28,29,30,31,32,33,34,35,36,37,38,	 // Letters 'a' through 'm'
		39,40,41,42,43,44,45,46,47,48,49,50,51,	 // Letters 'n' through 'z'
		-9,-9,-9,-9								 // Decimal 123 - 126
	};

	/* ********  U R L   S A F E   B A S E 6 4   A L P H A B E T  ******** */
	
	/**
	 * Used in the URL- and Filename-safe dialect described in Section 4 of RFC3548: 
	 * <a href="http://www.faqs.org/rfcs/rfc3548.html">http://www.faqs.org/rfcs/rfc3548.html</a>.
	 * Notice that the last two bytes become "hyphen" and "underscore" instead of "plus" and "slash."
	 */
	private static final byte[] URL_SAFE_ALPHABET = {
		(byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
		(byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
		(byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', 
		(byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
		(byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
		(byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
		(byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', 
		(byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
		(byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', 
		(byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'-', (byte)'_'
	};
	
	/**
	 * Used in decoding URL- and Filename-safe dialects of Base64.
	 */
	private static final byte[] URL_SAFE_DECODABET = {   
		-9,-9,-9,-9,-9,-9,-9,-9,-9,				 // Decimal  0 -  8
		-5,-5,									 // Whitespace: Tab and Linefeed
		-9,-9,									 // Decimal 11 - 12
		-5,										 // Whitespace: Carriage Return
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,	 // Decimal 14 - 26
		-9,-9,-9,-9,-9,							 // Decimal 27 - 31
		-5,										 // Whitespace: Space
		-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,			 // Decimal 33 - 42
		-9,										 // Plus sign at decimal 43
		-9,										 // Decimal 44
		62,										 // Minus sign at decimal 45
		-1,										 // Dot at decimal 46
		-9,										 // Slash at decimal 47
		52,53,54,55,56,57,58,59,60,61,			 // Numbers zero through nine
		-9,-9,-9,								 // Decimal 58 - 60
		-9,										 // Equals sign at decimal 61
		-9,-9,-9,								 // Decimal 62 - 64
		0,1,2,3,4,5,6,7,8,9,10,11,12,13,		 // Letters 'A' through 'N'
		14,15,16,17,18,19,20,21,22,23,24,25,	 // Letters 'O' through 'Z'
		-9,-9,-9,-9,							 // Decimal 91 - 94
		63,										 // Underscore at decimal 95
		-9,										 // Decimal 96
		26,27,28,29,30,31,32,33,34,35,36,37,38,	 // Letters 'a' through 'm'
		39,40,41,42,43,44,45,46,47,48,49,50,51,	 // Letters 'n' through 'z'
		-9,-9,-9,-9								 // Decimal 123 - 126
	};
	
	// instance members
	
	/** 
	 * Encode using Base64-like encoding that is URL- and Filename-safe as described
	 * in Section 4 of RFC3548: 
	 * <a href="http://www.faqs.org/rfcs/rfc3548.html">http://www.faqs.org/rfcs/rfc3548.html</a>.
	 */
	private boolean urlSafe;
	
	private byte[] ALPHABET;
	private byte[] DECODABET;
	private byte PADDING_CHAR;
	
	/**
	 * Create a new Base64 encoder and decoder using the standard Base64 alphabet.
	 * Note that the resulting encoded strings are not <i>URL-safe</i>: they will
	 * can contain characters that are subject to URL encoding.
	 */
	public Base64() {
		this(false);
	}
	
	/**
	 * Create a new Base64 encoder and decoder.
	 * <p>Allows Base64-like encoding that is URL- and Filename-safe as described
	 * in Section 4 of RFC3548: 
	 * <a href="http://www.faqs.org/rfcs/rfc3548.html">http://www.faqs.org/rfcs/rfc3548.html</a>.
	 * When URL-safe encoding is used, the standard "=" Base64 padding character is replaced
	 * with the '.' character.
	 * <p>
	 * It is important to note that data encoded this way is <em>not</em> officially valid Base64, 
	 * or at the very least should not be called Base64 without also specifying that is
	 * was encoded using the URL- and Filename-safe dialect
	 * 
	 * @param urlSafe if true, URL safe encoding and decoding will be used
	 */
	public Base64(boolean urlSafe) {
		this.urlSafe = urlSafe;
		if (urlSafe) {
			ALPHABET = URL_SAFE_ALPHABET;
			DECODABET = URL_SAFE_DECODABET;
			PADDING_CHAR = DOT;
		}
		else {
			ALPHABET = STANDARD_ALPHABET;
			DECODABET = STANDARD_DECODABET;
			PADDING_CHAR = EQUALS_SIGN;
		}
	}
	
	/**
	 * Returns whether or not this coder is using Base64-like encoding that is URL- and Filename-safe
	 * as described in Section 4 of RFC3548: 
	 * <a href="http://www.faqs.org/rfcs/rfc3548.html">http://www.faqs.org/rfcs/rfc3548.html</a>.
	 * When URL-safe encoding is used, the standard "=" Base64 padding character is replaced
	 * with the '.' character.
	 * <p>
	 * It is important to note that data encoded this way is <em>not</em> officially valid Base64, 
	 * or at the very least should not be called Base64 without also specifying that is
	 * was encoded using the URL- and Filename-safe dialect.
	 * @return true or false
	 */
	public boolean isUrlSafe() {
		return urlSafe;
	}

	/* ********  E N C O D I N G   M E T H O D S  ******** */

	/**
	 * Encodes up to three bytes of the array <var>source</var>
	 * and writes the resulting four Base64 bytes to <var>destination</var>.
	 * The source and destination arrays can be manipulated
	 * anywhere along their length by specifying 
	 * <var>srcOffset</var> and <var>destOffset</var>.
	 * This method does not check to make sure your arrays
	 * are large enough to accomodate <var>srcOffset</var> + 3 for
	 * the <var>source</var> array or <var>destOffset</var> + 4 for
	 * the <var>destination</var> array.
	 * The actual number of significant bytes in your array is
	 * given by <var>numSigBytes</var>.</p>
	 * <p>This is the lowest level of the encoding methods with
	 * all possible parameters.</p>
	 * @param source the array to convert
	 * @param srcOffset the index where conversion begins
	 * @param numSigBytes the number of significant bytes in your array
	 * @param destination the array to hold the conversion
	 * @param destOffset the index where output will be put
	 * @return the <var>destination</var> array
	 */
	private byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset) {
		//		   1		 2		 3  
		// 01234567890123456789012345678901 Bit position
		// --------000000001111111122222222 Array position from threeBytes
		// --------|	||	||	||	| Six bit groups to index ALPHABET
		//		  >>18  >>12  >> 6  >> 0  Right shift necessary
		//				0x3f  0x3f  0x3f  Additional AND
		
		// Create buffer with zero-padding if there are only one or two
		// significant bytes passed in the array.
		// We have to shift left 24 in order to flush out the 1's that appear
		// when Java treats a value as negative that is cast from a byte to an int.
		int inBuff =   ( numSigBytes > 0 ? ((source[ srcOffset     ] << 24) >>>  8) : 0 )
					 | ( numSigBytes > 1 ? ((source[ srcOffset + 1 ] << 24) >>> 16) : 0 )
					 | ( numSigBytes > 2 ? ((source[ srcOffset + 2 ] << 24) >>> 24) : 0 );

		switch (numSigBytes) {
			case 3:
				destination[ destOffset 	] = ALPHABET[ (inBuff >>> 18)		 ];
				destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
				destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
				destination[ destOffset + 3 ] = ALPHABET[ (inBuff	    ) & 0x3f ];
				return destination;
				
			case 2:
				destination[ destOffset 	] = ALPHABET[ (inBuff >>> 18)		 ];
				destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
				destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
				destination[ destOffset + 3 ] = PADDING_CHAR;
				return destination;
				
			case 1:
				destination[ destOffset 	] = ALPHABET[ (inBuff >>> 18)		 ];
				destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
				destination[ destOffset + 2 ] = PADDING_CHAR;
				destination[ destOffset + 3 ] = PADDING_CHAR;
				return destination;
				
			default:
				return destination;
		}
	}

	/**
	 * Encodes a byte array into Base64 notation.
	 * @param source the data to convert
	 * @param off offset in array where conversion should begin
	 * @param len length of data to convert
	 * @return the encoded data
	 */
	public final byte[] encode(byte[] source, int off, int len) {		
		int	len43 = len * 4 / 3;
		byte[] outBuff = new byte[ ( len43 )					  // main 4:3
								   + ( (len % 3) > 0 ? 4 : 0 ) ]; // account for padding
		int d = 0;
		int e = 0;
		int len2 = len - 2;
		int lineLength = 0;
		for (; d < len2; d+=3, e+=4) {
			encode3to4(source, d+off, 3, outBuff, e);

			lineLength += 4;
		} // end for: each piece of array

		if (d < len) {
			encode3to4(source, d+off, len - d, outBuff, e);
			e += 4;
		} // end if: some padding needed
		
		byte[] out = new byte[e];
		System.arraycopy(outBuff, 0, out, 0, e); 
		return out;
	}

	/**
	 * Encodes a byte array into Base64 notation.
	 * @param source the data to encode
	 * @return the encoded data
	 */
	public final byte[] encode(byte[] source) {
		return encode(source, 0, source.length);
	}
	
	/**
	 * Encodes a byte array into Base64 notation. The resulting string will
	 * be created using the platform default encoding.
	 * @param source the source data to encode
	 * @return the encoded data
	 */
	public final String encodeToString(byte[] source) {
		return new String(encode(source));
	}

	/* ********  D E C O D I N G   M E T H O D S  ******** */
	
	/**
	 * Decodes four bytes from array <var>source</var>
	 * and writes the resulting bytes (up to three of them)
	 * to <var>destination</var>.
	 * The source and destination arrays can be manipulated
	 * anywhere along their length by specifying 
	 * <var>srcOffset</var> and <var>destOffset</var>.
	 * This method does not check to make sure your arrays
	 * are large enough to accomodate <var>srcOffset</var> + 4 for
	 * the <var>source</var> array or <var>destOffset</var> + 3 for
	 * the <var>destination</var> array.
	 * This method returns the actual number of bytes that 
	 * were converted from the Base64 encoding.
	 * <p>This is the lowest level of the decoding methods with
	 * all possible parameters.</p>
	 * @param source the array to convert
	 * @param srcOffset the index where conversion begins
	 * @param destination the array to hold the conversion
	 * @param destOffset the index where output will be put
	 * @return the number of decoded bytes converted
	 */
	private final int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
		// Example: Dk== or Dk..
		if (source[ srcOffset + 2] == PADDING_CHAR) {
			int outBuff =   ( ( DECODABET[ source[ srcOffset	] ] & 0xFF ) << 18 )
						  | ( ( DECODABET[ source[ srcOffset + 1] ] & 0xFF ) << 12 );
			
			destination[ destOffset ] = (byte)( outBuff >>> 16 );
			return 1;
		}
		
		// Example: DkL= or DkL.
		else if (source[ srcOffset + 3 ] == PADDING_CHAR) {
			int outBuff =   ( ( DECODABET[ source[ srcOffset	 ] ] & 0xFF ) << 18 )
						  | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
						  | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6 );
			
			destination[ destOffset	 ] = (byte)( outBuff >>> 16 );
			destination[ destOffset + 1 ] = (byte)( outBuff >>>  8 );
			return 2;
		}
		
		// Example: DkLE
		else {
			int outBuff =   ( ( DECODABET[ source[ srcOffset	 ] ] & 0xFF ) << 18 )
						  | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
						  | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6)
						  | ( ( DECODABET[ source[ srcOffset + 3 ] ] & 0xFF )	  );

			destination[ destOffset	 ] = (byte)( outBuff >> 16 );
			destination[ destOffset + 1 ] = (byte)( outBuff >>  8 );
			destination[ destOffset + 2 ] = (byte)( outBuff	   );

			return 3;
		}
	}
	
	/**
	 * Very low-level access to decoding ASCII characters in
	 * the form of a byte array.
	 * @param source the Base64 encoded data
	 * @param off the offset of where to begin decoding
	 * @param len the length of characters to decode
	 * @return decoded data
	 */
	public final byte[] decode(byte[] source, int off, int len) {
		int	len34 = len * 3 / 4;
		byte[] outBuff = new byte[len34]; // upper limit on size of output
		int	outBuffPosn = 0;
		
		byte[] b4 = new byte[4];
		int	b4Posn = 0;
		int	i = 0;
		byte sbiCrop = 0;
		byte sbiDecode = 0;
		for (i = off; i < off + len; i++) {
			sbiCrop = (byte)(source[i] & 0x7f); // only the low seven bits
			sbiDecode = DECODABET[sbiCrop];
			
			if (sbiDecode >= WHITE_SPACE_ENC) { // white space, equals sign or better
				if (sbiDecode >= PADDING_CHAR_ENC) {
					b4[b4Posn++] = sbiCrop;
					if (b4Posn > 3) {
						outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
						b4Posn = 0;
						
						// if that was the padding char, break out of 'for' loop
						if (sbiCrop == PADDING_CHAR) {
							break;
						}
					} // end if: quartet built
				} // end if: equals sign or better
			} // end if: white space, equals sign or better
			else {
				//discard
			} 
		} // each input character
								   
		byte[] out = new byte[outBuffPosn];
		System.arraycopy(outBuff, 0, out, 0, outBuffPosn); 
		return out;
	}
	
	/**
	 * Decodes data from Base64 notation.
	 * @param source the source data
	 * @return the decoded data
	 */
	public final byte[] decode(byte[] source) {
		return decode(source, 0, source.length);
	}
	
	/**
	 * Decodes data from Base64 notation. Uses the platform default
	 * character set to obtain bytes from given string.
	 * @param s the string to decode
	 * @return the decoded data
	 */
	public final byte[] decodeFromString(String s) {   
		return decode(s.getBytes());
	}	
}