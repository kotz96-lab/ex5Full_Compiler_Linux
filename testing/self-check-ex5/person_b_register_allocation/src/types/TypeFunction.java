package types;

public class TypeFunction extends Type
{
	public Type returnType;
	public TypeList params;
	
	public TypeFunction(Type returnType, String name, TypeList params)
	{
		this.name = name;
		this.returnType = returnType;
		this.params = params;
	}
}
