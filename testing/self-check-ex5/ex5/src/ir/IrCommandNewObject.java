package ir;

import temp.*;

/**
 * IR Command for Object Allocation
 *
 * Semantics:
 * - Allocates object on heap
 * - Object layout: [vtable ptr (optional)][field0][field1]...[fieldN-1]
 * - Returns address of object in dst
 * - Data members initialized separately after allocation
 *
 * Memory calculation:
 * - Total bytes = number of fields * 4 (assuming all fields are 4 bytes)
 * - May include vtable pointer if using dynamic dispatch
 *
 * Usage Pattern:
 *   Temp dst = NEW_OBJECT(class_name, size_in_bytes)
 *
 * Example:
 *   Temp_8 := NEW_OBJECT("Point", size=8)
 *   // Creates object with 2 fields (8 bytes)
 *   // Temp_8 will point to newly allocated object
 */
public class IrCommandNewObject extends IrCommand
{
	public Temp dst;        // Destination: will hold address of new object
	public String className; // Name of the class
	public int sizeInBytes; // Total size in bytes

	public IrCommandNewObject(Temp dst, String className, int sizeInBytes)
	{
		this.dst = dst;
		this.className = className;
		this.sizeInBytes = sizeInBytes;
	}

	public String toString()
	{
		return String.format("Temp_%d := NEW_OBJECT(\"%s\", size=%d)",
			dst.getSerialNumber(),
			className,
			sizeInBytes);
	}
}
