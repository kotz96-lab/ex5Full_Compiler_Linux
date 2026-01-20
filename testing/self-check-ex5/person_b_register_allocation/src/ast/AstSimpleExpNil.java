package ast;

import types.Type;
import types.TypeNill;
import temp.Temp;
import temp.TempFactory;
import ir.Ir;
import ir.IrCommandNilConst;

public class AstSimpleExpNil extends AstSimpleExp
{
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstSimpleExpNil()
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== simpleExp -> NIL\n");
	}

	/************************************************/
	/* The printing message for a nil exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST nul EXP */
		/*******************************/
		System.out.print("AST NODE NIL\n");

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
                "NIL");
	}

    public Type semantMe()
    {
        return TypeNill.getInstance();
    }

    public Temp irMe()
    {
        // Create a fresh temporary to hold nil (0)
        Temp t = TempFactory.getInstance().getFreshTemp();

        // Generate IR command to load nil constant
        Ir.getInstance().AddIrCommand(new IrCommandNilConst(t));

        return t;
    }
}
