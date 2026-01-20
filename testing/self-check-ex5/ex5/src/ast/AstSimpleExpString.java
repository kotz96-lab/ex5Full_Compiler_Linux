package ast;

import types.Type;
import types.TypeString;
import temp.Temp;
import temp.TempFactory;
import ir.Ir;
import ir.IrCommandConstString;

public class AstSimpleExpString extends AstSimpleExp
{
	public String value;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstSimpleExpString(String value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== simpleExp -> STRING( %s )\n", value);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
	}

	/************************************************/
	/* The printing message for a string exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST STRING EXP */
		/*******************************/
		System.out.format("AST NODE STRING( %s )\n",value);

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("STRING(%s)",value.replace("\"", "\\\"")));
	}

    public Type semantMe()
    {
        return TypeString.getInstance();
    }

    public Temp irMe()
    {
        // Create a fresh temporary to hold the string address
        Temp t = TempFactory.getInstance().getFreshTemp();

        // Strip surrounding quotes from the string value
        String strValue = value;
        if (strValue.startsWith("\"") && strValue.endsWith("\"") && strValue.length() >= 2) {
            strValue = strValue.substring(1, strValue.length() - 1);
        }

        // Generate IR command to load string constant
        Ir.getInstance().AddIrCommand(new IrCommandConstString(t, strValue));

        return t;
    }
}
