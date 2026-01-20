package ast;

import temp.Temp;
import types.Type;

public class AstExpSimpleExp extends AstExp
{
	public AstSimpleExp value;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpSimpleExp(AstSimpleExp value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> simpleExp\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
	}

	/************************************************/
	/* The printing message for a minus exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST SIMPLE EXP */
		/*******************************/
		System.out.print("AST NODE SIMPLE EXP\n");
        if (value != null) value.printMe();

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"SIMPLE EXP");
        if (value  != null) AstGraphviz.getInstance().logEdge(serialNumber,value.serialNumber);
	}

    public Type semantMe()
    {
        return value.semantMe();
    }

    public Temp irMe()
    {
        return value.irMe();
    }
}
