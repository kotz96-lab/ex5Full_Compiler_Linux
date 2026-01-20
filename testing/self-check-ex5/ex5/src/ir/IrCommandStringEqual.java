package ir;

import temp.*;

/**
 * IR Command for String Equality Comparison
 *
 * Semantics:
 * - Compares two strings by content (not address)
 * - Returns 1 if strings are equal, 0 otherwise
 * - Compares character by character until null terminator
 *
 * Usage Pattern:
 *   Temp t1 = <address of string 1>
 *   Temp t2 = <address of string 2>
 *   Temp dst = STRING_EQUAL(t1, t2)
 *
 * Example:
 *   Temp_7 := STRING_EQUAL(Temp_5, Temp_6)
 *   // Temp_5 points to "hello"
 *   // Temp_6 points to "hello"
 *   // Temp_7 will be 1
 */
public class IrCommandStringEqual extends IrCommand
{
	public Temp dst;   // Destination: will hold 1 or 0
	public Temp str1;  // Address of first string
	public Temp str2;  // Address of second string

	public IrCommandStringEqual(Temp dst, Temp str1, Temp str2)
	{
		this.dst = dst;
		this.str1 = str1;
		this.str2 = str2;
	}

	public String toString()
	{
		return String.format("Temp_%d := STRING_EQUAL(Temp_%d, Temp_%d)",
			dst.getSerialNumber(),
			str1.getSerialNumber(),
			str2.getSerialNumber());
	}
}
