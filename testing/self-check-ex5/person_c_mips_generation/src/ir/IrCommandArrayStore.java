package ir;

import temp.*;

/**
 * IR Command for Array Element Store
 *
 * Semantics:
 * - Stores value into array[index]
 * - Must check bounds: index >= 0 && index < array.length
 * - Array layout: [length][elem0][elem1]...[elemN-1]
 *
 * Runtime checks needed:
 * - Check if array pointer is nil
 * - Check if index < 0
 * - Check if index >= array.length
 * - If any check fails: print error and exit
 *
 * Usage Pattern:
 *   Temp t_array = <address of array>
 *   Temp t_index = <index value>
 *   Temp t_value = <value to store>
 *   ARRAY_STORE(t_array, t_index, t_value)
 *
 * Example:
 *   ARRAY_STORE(Temp_5[Temp_6], Temp_7)
 *   // Temp_5 points to array
 *   // Temp_6 contains index (e.g., 3)
 *   // Temp_7 contains value to store
 *   // Result: array[3] = value
 */
public class IrCommandArrayStore extends IrCommand
{
	public Temp array;      // Address of array
	public Temp index;      // Index to store at
	public Temp value;      // Value to store
	public int elementSize; // Size of each element in bytes (4 for int)

	public IrCommandArrayStore(Temp array, Temp index, Temp value, int elementSize)
	{
		this.array = array;
		this.index = index;
		this.value = value;
		this.elementSize = elementSize;
	}

	// Simplified constructor assuming 4-byte elements (ints)
	public IrCommandArrayStore(Temp array, Temp index, Temp value)
	{
		this(array, index, value, 4);
	}

	public String toString()
	{
		return String.format("ARRAY_STORE(Temp_%d[Temp_%d], Temp_%d, elemSize=%d)",
			array.getSerialNumber(),
			index.getSerialNumber(),
			value.getSerialNumber(),
			elementSize);
	}
}
