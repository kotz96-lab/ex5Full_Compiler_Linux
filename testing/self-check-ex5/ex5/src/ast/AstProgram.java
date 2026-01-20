package ast;

import temp.Temp;

public class AstProgram extends AstNode
{
	public AstDecList decList;

	public AstProgram(AstDecList decList)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.decList = decList;
		AstGraphviz.getInstance().logNode(serialNumber, "Program");
		if (decList != null) AstGraphviz.getInstance().logEdge(serialNumber, decList.serialNumber);
	}

	public void printMe()
	{
		if (decList != null) decList.printMe();
	}

	public void semantMe() throws SemanticException
	{
		if (decList != null) decList.semantMe();
	}

    public Temp irMe()
    {
        return decList.irMe();
    }
}
