package ast;

import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstExpMinus extends AstExp
{
	public AstSimpleExp value;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpMinus(AstSimpleExp value, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> MINUS simpleExp\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
		this.line = line;
	}

	/************************************************/
	/* The printing message for a minus exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST MINUS EXP */
		/*******************************/
		System.out.print("AST NODE MINUS EXP\n");
        if (value != null) value.printMe();

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"MINUS");
        if (value  != null) AstGraphviz.getInstance().logEdge(serialNumber,value.serialNumber);
	}

	public Type semantMe() throws SemanticException
	{
		Type t = value.semantMe();
		if (t != TypeInt.getInstance())
		{
			throw new SemanticException(line, "unary minus can only be applied to integers");
		}
		return TypeInt.getInstance();
	}

	public Temp irMe()
	{
		Temp t = value.irMe();
		Temp dst = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandBinopMinusInteger(dst, t));

		return dst;
	}
}
