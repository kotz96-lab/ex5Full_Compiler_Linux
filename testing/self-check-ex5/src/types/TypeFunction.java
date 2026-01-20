package types;

import java.util.List;
import java.util.ArrayList;

public class TypeFunction extends Type
{
	public Type returnType;
	public TypeList params;
	public List<String> paramVarNames; // Parameter variable names with offsets (e.g., "p_7", "start_8")

	public TypeFunction(Type returnType, String name, TypeList params)
	{
		this.name = name;
		this.returnType = returnType;
		this.params = params;
		this.paramVarNames = new ArrayList<>();
	}
}
