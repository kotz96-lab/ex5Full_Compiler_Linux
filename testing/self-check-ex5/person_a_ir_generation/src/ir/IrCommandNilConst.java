package ir;

import temp.*;

/**
 * IR Command for Nil Constant
 *
 * Semantics:
 * - Loads nil (null pointer, value 0) into temporary
 * - Used for uninitialized or null references
 *
 * Usage Pattern:
 *   Temp dst = NIL
 *
 * Example:
 *   Temp_5 := NIL
 *   // Temp_5 will contain 0 (null pointer)
 */
public class IrCommandNilConst extends IrCommand
{
	public Temp dst; // Destination: will hold 0

	public IrCommandNilConst(Temp dst)
	{
		this.dst = dst;
	}

	public String toString()
	{
		return String.format("Temp_%d := NIL",
			dst.getSerialNumber());
	}
}
