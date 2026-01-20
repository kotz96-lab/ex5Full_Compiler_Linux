package ast;

import temp.Temp;
import types.*;

public class AstExpLparenPraren extends AstExp
{
	public AstExp value;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpLparenPraren(AstExp value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> LPAREN exp RPAREN\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		
		/*************************************/
		/* AST NODE TYPE = AST LPAREN EXP RPAREN */
		/*************************************/
		System.out.print("AST NODE LPAREN EXP RPAREN\n");

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (value != null) value.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"(EXP)");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
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
