package ir;

import temp.*;

/**
 * IR Command for Class Field Store
 *
 * Semantics:
 * - Stores value into object.field
 * - Field offset is calculated based on field position in class
 *
 * Runtime checks needed:
 * - Check if object pointer is nil
 * - If nil: print "Invalid Pointer Dereference" and exit
 *
 * Usage Pattern:
 *   Temp t_object = <address of object>
 *   Temp t_value = <value to store>
 *   FIELD_STORE(t_object, field_offset, t_value)
 *
 * Example:
 *   FIELD_STORE(Temp_5.field, offset=4, Temp_7)
 *   // Temp_5 points to object
 *   // Offset 4 means second field
 *   // Temp_7 contains value to store
 *   // Result: object.field = value
 */
public class IrCommandFieldStore extends IrCommand
{
	public Temp object;      // Address of object
	public int fieldOffset;  // Offset of field in bytes
	public Temp value;       // Value to store
	public String fieldName; // Optional: field name for debugging

	public IrCommandFieldStore(Temp object, int fieldOffset, Temp value, String fieldName)
	{
		this.object = object;
		this.fieldOffset = fieldOffset;
		this.value = value;
		this.fieldName = fieldName;
	}

	// Simplified constructor without field name
	public IrCommandFieldStore(Temp object, int fieldOffset, Temp value)
	{
		this(object, fieldOffset, value, "");
	}

	public String toString()
	{
		if (fieldName != null && !fieldName.isEmpty()) {
			return String.format("FIELD_STORE(Temp_%d.%s, offset=%d, Temp_%d)",
				object.getSerialNumber(),
				fieldName,
				fieldOffset,
				value.getSerialNumber());
		} else {
			return String.format("FIELD_STORE(Temp_%d, offset=%d, Temp_%d)",
				object.getSerialNumber(),
				fieldOffset,
				value.getSerialNumber());
		}
	}
}
