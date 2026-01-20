package ir;

import temp.*;

/**
 * IR Command for Return with Value
 *
 * Semantics:
 * - Returns from function with a value
 * - Value is placed in designated return location
 * - Control returns to caller
 *
 * Usage Pattern:
 *   Temp t_value = <return value>
 *   RETURN t_value
 *
 * Example:
 *   RETURN Temp_5
 *   // Returns with value in Temp_5
 */
public class IrCommandReturn extends IrCommand
{
	public Temp returnValue; // Value to return

	public IrCommandReturn(Temp returnValue)
	{
		this.returnValue = returnValue;
	}

	public String toString()
	{
		if (returnValue != null) {
			return String.format("RETURN Temp_%d",
				returnValue.getSerialNumber());
		} else {
			return "RETURN (null)";
		}
	}
}
