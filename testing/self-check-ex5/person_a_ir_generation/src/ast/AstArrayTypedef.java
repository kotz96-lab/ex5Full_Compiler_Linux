package ast;

public class AstArrayTypedef extends AstNode
{
	public String name;
	public AstType elementType;

	public AstArrayTypedef(String name, AstType elementType)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.name = name;
		this.elementType = elementType;
		AstGraphviz.getInstance().logNode(serialNumber, "ArrayTypedef: " + name);
		if (elementType != null) AstGraphviz.getInstance().logEdge(serialNumber, elementType.serialNumber);
	}

	public void printMe()
	{
		System.out.println("ArrayTypedef: " + name);
	}
}
