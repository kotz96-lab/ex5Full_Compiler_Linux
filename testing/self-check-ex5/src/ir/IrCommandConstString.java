package ir;

import temp.*;

/**
 * IR Command for String Constants
 *
 * Semantics:
 * - Loads address of a string literal into a temporary
 * - String is stored in data section with label
 * - String must be null-terminated
 *
 * Usage Pattern:
 *   Temp dst = "hello world"
 *
 * Example:
 *   Temp_5 := "hello"
 */
public class IrCommandConstString extends IrCommand
{
	public Temp dst;       // Destination: will hold address of string
	public String value;   // The actual string literal

	public IrCommandConstString(Temp dst, String value)
	{
		this.dst = dst;
		this.value = value;
	}

	public String toString()
	{
		return String.format("Temp_%d := \"%s\"",
			dst.getSerialNumber(),
			value);
	}
}
