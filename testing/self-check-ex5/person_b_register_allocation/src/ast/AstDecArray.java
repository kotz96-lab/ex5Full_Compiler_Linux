package ast;

import symboltable.SymbolTable;
import types.*;

public class AstDecArray extends AstDec
{
	public AstArrayTypedef arrayTypedef;

	public AstDecArray(AstArrayTypedef arrayTypedef)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.arrayTypedef = arrayTypedef;
		AstGraphviz.getInstance().logNode(serialNumber, "DecArray");
		if (arrayTypedef != null) AstGraphviz.getInstance().logEdge(serialNumber, arrayTypedef.serialNumber);
	}

	public void printMe()
	{
		if (arrayTypedef != null) arrayTypedef.printMe();
	}

    public void semantMe() throws SemanticException {
        Type t;

        /*******************/
        /* [0] array type */
        /*******************/
        t = SymbolTable.getInstance().find(arrayTypedef.elementType.name);
        if (t == null) {
            throw new SemanticException(String.format("non existing type for array %s", arrayTypedef.elementType.name));
        }

        /***************************************************/
        /* [1] Enter the Array Type to the Symbol Table */
        /***************************************************/
        SymbolTable.getInstance().enter(arrayTypedef.name, new TypeArray(t, arrayTypedef.name));
    }
}
