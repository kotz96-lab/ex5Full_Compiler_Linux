package ir;

import temp.*;

/**
 * IR Command for Class Field Access (Load)
 *
 * Semantics:
 * - Loads value from object.field
 * - Field offset is calculated based on field position in class
 * - Returns value in dst
 *
 * Runtime checks needed:
 * - Check if object pointer is nil
 * - If nil: print "Invalid Pointer Dereference" and exit
 *
 * Usage Pattern:
 *   Temp t_object = <address of object>
 *   Temp dst = FIELD_ACCESS(t_object, field_offset)
 *
 * Example:
 *   Temp_7 := FIELD_ACCESS(Temp_5, offset=4)
 *   // Temp_5 points to object
 *   // Offset 4 means second field (first field at offset 0)
 *   // Temp_7 will contain the field value
 */
public class IrCommandFieldAccess extends IrCommand
{
	public Temp dst;         // Destination: will hold field value
	public Temp object;      // Address of object
	public int fieldOffset;  // Offset of field in bytes
	public String fieldName; // Optional: field name for debugging

	public IrCommandFieldAccess(Temp dst, Temp object, int fieldOffset, String fieldName)
	{
		this.dst = dst;
		this.object = object;
		this.fieldOffset = fieldOffset;
		this.fieldName = fieldName;
	}

	// Simplified constructor without field name
	public IrCommandFieldAccess(Temp dst, Temp object, int fieldOffset)
	{
		this(dst, object, fieldOffset, "");
	}

	public String toString()
	{
		if (fieldName != null && !fieldName.isEmpty()) {
			return String.format("Temp_%d := FIELD_ACCESS(Temp_%d.%s, offset=%d)",
				dst.getSerialNumber(),
				object.getSerialNumber(),
				fieldName,
				fieldOffset);
		} else {
			return String.format("Temp_%d := FIELD_ACCESS(Temp_%d, offset=%d)",
				dst.getSerialNumber(),
				object.getSerialNumber(),
				fieldOffset);
		}
	}
}
