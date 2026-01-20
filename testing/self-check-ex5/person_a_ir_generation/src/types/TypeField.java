package types;

/**
 * Represents a class field with a name and underlying type.
 * This allows storing field names in TypeList for class data members.
 */
public class TypeField extends Type
{
	/**
	 * The actual type of the field (e.g., TypeInt, TypeClass)
	 */
	public Type fieldType;
	
	/**
	 * Constructor
	 * @param fieldName The name of the field
	 * @param fieldType The type of the field
	 */
	public TypeField(String fieldName, Type fieldType)
	{
		this.name = fieldName;
		this.fieldType = fieldType;
	}

	@Override
	public boolean isClass()
	{
		return fieldType.isClass();
	}

	@Override
	public boolean isArray()
	{
		return fieldType.isArray();
	}
}
