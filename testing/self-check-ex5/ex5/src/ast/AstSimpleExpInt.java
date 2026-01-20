package ast;

import ir.IRcommandConstInt;
import ir.Ir;
import temp.Temp;
import temp.TempFactory;
import types.Type;
import types.TypeInt;

public class AstSimpleExpInt extends AstSimpleExp
{
	public int value;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstSimpleExpInt(int value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== simpleExp -> INT( %d )\n", value);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
	}

	/************************************************/
	/* The printing message for an int exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST INT EXP */
		/*******************************/
		System.out.format("AST NODE INT( %d )\n",value);

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("INT(%d)",value));
	}

    public Type semantMe()
    {
        return TypeInt.getInstance();
    }

    public Temp irMe()
    {
        Temp t = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IRcommandConstInt(t,value));
        return t;
    }
}
