package ir;

import temp.*;

/**
 * IR Command for Array Allocation
 *
 * Semantics:
 * - Allocates array on heap
 * - Array layout: [length][elem0][elem1]...[elemN-1]
 * - First word stores array length
 * - Remaining words are array elements
 * - Returns address of array in dst
 *
 * Memory calculation:
 * - Total bytes = 4 (for length) + (size * elementSize)
 * - For int array of size N: 4 + (N * 4) bytes
 *
 * Usage Pattern:
 *   Temp t_size = <array size>
 *   Temp dst = NEW_ARRAY(t_size, element_size)
 *
 * Example:
 *   Temp_8 := NEW_ARRAY(Temp_7, elemSize=4)
 *   // Temp_7 contains size (e.g., 10)
 *   // Temp_8 will point to newly allocated array of 10 ints
 *   // Memory layout: [10][0][0][0][0][0][0][0][0][0][0]
 */
public class IrCommandNewArray extends IrCommand
{
	public Temp dst;        // Destination: will hold address of new array
	public Temp size;       // Number of elements
	public int elementSize; // Size of each element in bytes
	public String typeName; // Optional: type name for debugging

	public IrCommandNewArray(Temp dst, Temp size, int elementSize, String typeName)
	{
		this.dst = dst;
		this.size = size;
		this.elementSize = elementSize;
		this.typeName = typeName;
	}

	// Simplified constructor for int arrays
	public IrCommandNewArray(Temp dst, Temp size, int elementSize)
	{
		this(dst, size, elementSize, "int");
	}

	public String toString()
	{
		return String.format("Temp_%d := NEW_ARRAY(%s[Temp_%d], elemSize=%d)",
			dst.getSerialNumber(),
			typeName,
			size.getSerialNumber(),
			elementSize);
	}
}
