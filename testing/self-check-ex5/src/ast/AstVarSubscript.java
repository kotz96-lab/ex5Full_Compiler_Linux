package ast;

import types.*;
import symboltable.*;
import temp.Temp;
import temp.TempFactory;
import ir.Ir;
import ir.IrCommandArrayAccess;

public class AstVarSubscript extends AstVar
{
	public AstVar var;
	public AstExp subscript;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSubscript(AstVar var, AstExp subscript, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== var -> var [ exp ]\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.subscript = subscript;
		this.line = line;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void printMe()
	{
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE SUBSCRIPT VAR\n");

		/****************************************/
		/* RECURSIVELY PRINT VAR + SUBSCRIPT ... */
		/****************************************/
		if (var != null) var.printMe();
		if (subscript != null) subscript.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"SUBSCRIPT\nVAR\n...[...]");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var       != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		if (subscript != null) AstGraphviz.getInstance().logEdge(serialNumber,subscript.serialNumber);
	}

	public Type semantMe() throws SemanticException
	{
		/**************************************/
		/* [1] Get the type of the var prefix */
		/**************************************/
		Type varType = var.semantMe();
		
		/**************************************/
		/* [2] Var must be an array type      */
		/**************************************/
		if (!varType.isArray())
		{
			throw new SemanticException(line, String.format("subscript access on non-array type %s", varType.name));
		}
		
		/**************************************/
		/* [3] Subscript must be int          */
		/**************************************/
		Type subscriptType = subscript.semantMe();
		if (subscriptType != TypeInt.getInstance())
		{
			throw new SemanticException(line, String.format("array subscript must be integer, got %s", subscriptType.name));
		}
		
		/**************************************/
		/* [4] Return the element type        */
		/**************************************/
		return ((TypeArray) varType).type;
	}

	public Temp irMe()
	{
		// Generate IR for the array variable (get its address)
		Temp t_array = var.irMe();

		// Generate IR for the subscript expression (get the index)
		Temp t_index = subscript.irMe();

		// Create a fresh temporary to hold the array element value
		Temp t_result = TempFactory.getInstance().getFreshTemp();

		// Generate IR command to access array element
		Ir.getInstance().AddIrCommand(
			new IrCommandArrayAccess(t_result, t_array, t_index)
		);

		return t_result;
	}
}
