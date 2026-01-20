package ir;

import temp.*;

/**
 * IR Command for Array Element Access (Load)
 *
 * Semantics:
 * - Loads value from array[index]
 * - Must check bounds: index >= 0 && index < array.length
 * - Array layout: [length][elem0][elem1]...[elemN-1]
 * - Returns value in dst
 *
 * Runtime checks needed:
 * - Check if array pointer is nil
 * - Check if index < 0
 * - Check if index >= array.length
 * - If any check fails: print "Access Violation" or "Invalid Pointer Dereference" and exit
 *
 * Usage Pattern:
 *   Temp t_array = <address of array>
 *   Temp t_index = <index value>
 *   Temp dst = ARRAY_ACCESS(t_array, t_index)
 *
 * Example:
 *   Temp_7 := ARRAY_ACCESS(Temp_5, Temp_6)
 *   // Temp_5 points to array
 *   // Temp_6 contains index (e.g., 3)
 *   // Temp_7 will contain array[3]
 */
public class IrCommandArrayAccess extends IrCommand
{
	public Temp dst;        // Destination: will hold array[index] value
	public Temp array;      // Address of array
	public Temp index;      // Index to access
	public int elementSize; // Size of each element in bytes (4 for int)

	public IrCommandArrayAccess(Temp dst, Temp array, Temp index, int elementSize)
	{
		this.dst = dst;
		this.array = array;
		this.index = index;
		this.elementSize = elementSize;
	}

	// Simplified constructor assuming 4-byte elements (ints)
	public IrCommandArrayAccess(Temp dst, Temp array, Temp index)
	{
		this(dst, array, index, 4);
	}

	public String toString()
	{
		return String.format("Temp_%d := ARRAY_ACCESS(Temp_%d[Temp_%d], elemSize=%d)",
			dst.getSerialNumber(),
			array.getSerialNumber(),
			index.getSerialNumber(),
			elementSize);
	}
}
