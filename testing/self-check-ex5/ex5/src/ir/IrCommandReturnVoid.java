package ir;

/**
 * IR Command for Void Return
 *
 * Semantics:
 * - Returns from void function
 * - No return value
 * - Control returns to caller
 *
 * Usage Pattern:
 *   RETURN_VOID
 *
 * Example:
 *   RETURN_VOID
 *   // Returns from void function
 */
public class IrCommandReturnVoid extends IrCommand
{
	public IrCommandReturnVoid()
	{
		// No parameters needed
	}

	public String toString()
	{
		return "RETURN_VOID";
	}
}
