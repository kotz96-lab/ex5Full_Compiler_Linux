package ast;

import types.*;
import symboltable.*;
import temp.Temp;
import temp.TempFactory;
import ir.Ir;
import ir.IrCommandFieldAccess;

public class AstVarField extends AstVar
{
	public AstVar var;
	public String fieldName;
	private int cachedOffset = -1;  // Cache field offset from semantic analysis
	private String cachedClassName = null;  // Cache class name
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarField(AstVar var, String fieldName, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== var -> var DOT ID( %s )\n",fieldName);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.fieldName = fieldName;
		this.line = line;
	}

	/*************************************************/
	/* The printing message for a field var AST node */
	/*************************************************/
	public void printMe()
	{
		/*********************************/
		/* AST NODE TYPE = AST FIELD VAR */
		/*********************************/
		System.out.print("AST NODE FIELD VAR\n");

		/**********************************************/
		/* RECURSIVELY PRINT VAR, then FIELD NAME ... */
		/**********************************************/
		if (var != null) var.printMe();
		System.out.format("FIELD NAME( %s )\n",fieldName);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("FIELD\nVAR\n...->%s",fieldName));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}

	public Type semantMe() throws SemanticException
	{
		/**************************************/
		/* [1] Get the type of the var prefix */
		/**************************************/
		Type varType = var.semantMe();
		
		/**************************************/
		/* [2] Var must be a class type       */
		/**************************************/
		if (!varType.isClass())
		{
			throw new SemanticException(line, String.format("field access on non-class type %s", varType.name));
		}
		
		/**************************************/
		/* [3] Find the field in the class    */
		/**************************************/
		TypeClass classType = (TypeClass) varType;
		Type fieldType = classType.find(fieldName);

		if (fieldType == null)
		{
			throw new SemanticException(line, String.format("field %s not found in class %s", fieldName, classType.name));
		}

		// Cache offset and class name for IR generation
		cachedOffset = classType.getFieldOffset(fieldName);
		cachedClassName = classType.name;
		
		// If it's a function, return the function type itself
		if (fieldType instanceof TypeFunction)
		{
			return fieldType;
		}
		
		// If it's a TypeField wrapper, unwrap to get the actual type
		if (fieldType instanceof TypeField)
		{
			return ((TypeField) fieldType).fieldType;
		}
		
		return fieldType;
	}

	public int getCachedOffset()
	{
		return cachedOffset;
	}

	public Temp irMe()
	{
		// Generate IR for the object variable (get its address)
		Temp t_object = var.irMe();

		// Use cached offset from semantic analysis
		int fieldOffset = cachedOffset;

		// Create a fresh temporary to hold the field value
		Temp t_result = TempFactory.getInstance().getFreshTemp();

		// Generate IR command to access field
		Ir.getInstance().AddIrCommand(
			new IrCommandFieldAccess(t_result, t_object, fieldOffset, fieldName)
		);

		return t_result;
	}
}
