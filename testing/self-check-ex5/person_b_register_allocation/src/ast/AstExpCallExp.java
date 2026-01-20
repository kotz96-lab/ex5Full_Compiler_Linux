package ast;

import temp.Temp;
import types.*;

public class AstExpCallExp extends AstExp
{
	public AstExpCall value;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpCallExp(AstExpCall value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> callExp\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
	}

	/************************************************/
	/* The printing message for a cull exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST CALL EXP */
		/*******************************/
		System.out.print("AST NODE CALL EXP\n");
        if (value != null) value.printMe();

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"CALL EXP");
        if (value  != null) AstGraphviz.getInstance().logEdge(serialNumber,value.serialNumber);
	}

	public Type semantMe() throws SemanticException
	{
		return value.semantMe();
	}

    public Temp irMe()
    {
        return value.irMe();
    }
}
