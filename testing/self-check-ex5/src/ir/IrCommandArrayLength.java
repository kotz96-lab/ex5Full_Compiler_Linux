package ir;

import temp.*;

/**
 * IR Command for Array Length
 *
 * Semantics:
 * - Retrieves the length of an array
 * - Array layout: [length][elem0][elem1]...[elemN-1]
 * - Length is stored in first word (offset 0)
 *
 * Runtime checks needed:
 * - Check if array pointer is nil
 *
 * Usage Pattern:
 *   Temp t_array = <address of array>
 *   Temp dst = ARRAY_LENGTH(t_array)
 *
 * Example:
 *   Temp_6 := ARRAY_LENGTH(Temp_5)
 *   // Temp_5 points to array
 *   // Temp_6 will contain the array length
 */
public class IrCommandArrayLength extends IrCommand
{
	public Temp dst;    // Destination: will hold array length
	public Temp array;  // Address of array

	public IrCommandArrayLength(Temp dst, Temp array)
	{
		this.dst = dst;
		this.array = array;
	}

	public String toString()
	{
		String dstStr = (dst != null) ? "Temp_" + dst.getSerialNumber() : "(null)";
		String arrayStr = (array != null) ? "Temp_" + array.getSerialNumber() : "(null)";
		return String.format("%s := ARRAY_LENGTH(%s)", dstStr, arrayStr);
	}
}
