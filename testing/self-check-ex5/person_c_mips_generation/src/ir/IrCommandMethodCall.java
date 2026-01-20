package ir;

import temp.*;
import java.util.List;

/**
 * IR Command for Method Call
 *
 * Semantics:
 * - Calls a method on an object
 * - Object is passed as implicit first parameter (like 'this')
 * - May use vtable for dynamic dispatch (depends on implementation)
 * - Returns result in dst
 *
 * Runtime checks needed:
 * - Check if object pointer is nil
 * - If nil: print "Invalid Pointer Dereference" and exit
 *
 * Usage Pattern:
 *   Temp t_object = <address of object>
 *   Temp t_arg1 = <first argument>
 *   Temp t_arg2 = <second argument>
 *   Temp dst = METHOD_CALL(t_object, method_name, [t_arg1, t_arg2])
 *
 * Example:
 *   Temp_10 := METHOD_CALL(Temp_5, "getX", [])
 *   // Temp_5 points to object
 *   // Calls method getX() on object
 *   // Temp_10 will contain return value
 */
public class IrCommandMethodCall extends IrCommand
{
	public Temp dst;             // Destination: will hold return value (null for void)
	public Temp object;          // Object to call method on
	public String methodName;    // Name of method to call
	public List<Temp> arguments; // Method arguments (excluding implicit object)

	public IrCommandMethodCall(Temp dst, Temp object, String methodName, List<Temp> arguments)
	{
		this.dst = dst;
		this.object = object;
		this.methodName = methodName;
		this.arguments = arguments;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		if (dst != null) {
			sb.append(String.format("Temp_%d := ", dst.getSerialNumber()));
		}

		sb.append(String.format("METHOD_CALL(Temp_%d.%s(",
			object.getSerialNumber(),
			methodName));

		if (arguments != null && !arguments.isEmpty()) {
			for (int i = 0; i < arguments.size(); i++) {
				if (i > 0) sb.append(", ");
				sb.append(String.format("Temp_%d", arguments.get(i).getSerialNumber()));
			}
		}

		sb.append("))");

		return sb.toString();
	}
}
