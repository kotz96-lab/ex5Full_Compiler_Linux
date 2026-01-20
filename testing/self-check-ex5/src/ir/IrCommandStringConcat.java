package ir;

import temp.*;

/**
 * IR Command for String Concatenation
 *
 * Semantics:
 * - Concatenates two strings (s1 + s2)
 * - Allocates new string on heap
 * - Result is null-terminated
 * - Returns address of new string in dst
 *
 * Usage Pattern:
 *   Temp t1 = <address of string 1>
 *   Temp t2 = <address of string 2>
 *   Temp dst = STRING_CONCAT(t1, t2)
 *
 * Example:
 *   Temp_5 := STRING_CONCAT(Temp_3, Temp_4)
 *   // Temp_3 points to "hello"
 *   // Temp_4 points to "world"
 *   // Temp_5 will point to newly allocated "helloworld"
 */
public class IrCommandStringConcat extends IrCommand
{
	public Temp dst;   // Destination: will hold address of concatenated string
	public Temp str1;  // Address of first string
	public Temp str2;  // Address of second string

	public IrCommandStringConcat(Temp dst, Temp str1, Temp str2)
	{
		this.dst = dst;
		this.str1 = str1;
		this.str2 = str2;
	}

	public String toString()
	{
		return String.format("Temp_%d := STRING_CONCAT(Temp_%d, Temp_%d)",
			dst.getSerialNumber(),
			str1.getSerialNumber(),
			str2.getSerialNumber());
	}
}
