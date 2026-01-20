package ast;

import ir.Ir;
import ir.IrCommandLoad;
import temp.Temp;
import temp.TempFactory;
import types.*;
import symboltable.*;

public class AstVarSimple extends AstVar
{
	/************************/
	/* simple variable name */
	/************************/
	public String name;
	public int offset = -1;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSimple(String name, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
	
		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== var -> ID( %s )\n",name);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.name = name;
		this.line = line;
	}

	/**************************************************/
	/* The printing message for a simple var AST node */
	/**************************************************/
	public void printMe()
	{
		/**********************************/
		/* AST NODE TYPE = AST SIMPLE VAR */
		/**********************************/
		System.out.format("AST NODE SIMPLE VAR( %s )\n",name);

		/**********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("SIMPLE\nVAR\n(%s)",name));
	}

	public Type semantMe() throws SemanticException
	{
        SymbolTableEntry entry = SymbolTable.getInstance().findEntry(name);
		if (entry == null)
		{
			throw new SemanticException(line, String.format("variable %s not found", name));
		}

        this.offset = entry.prevtopIndex; // Store the offset for later IR generation

		// Unwrap TypeField if present
		if (entry.type instanceof TypeField)
		{
			return ((TypeField) entry.type).fieldType;
		}
		return entry.type;
	}

    public Temp irMe()
    {
        /*******************************************************/
        /* Use the offset captured during semantic analysis   */
        /*******************************************************/
        String varNameWithOffset = String.format("%s_%d", name, offset);

        /**************************************/
        /* Generate IR with offset annotation */
        /**************************************/
        Temp t = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandLoad(t, varNameWithOffset));
        return t;
    }
}
