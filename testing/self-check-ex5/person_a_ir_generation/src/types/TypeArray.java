package types;

public class TypeArray extends Type
{
	public Type type;

	public TypeArray(Type type, String name)
	{
		this.name = name;
		this.type = type;
	}
    public boolean isArray(){ return true;}
}
