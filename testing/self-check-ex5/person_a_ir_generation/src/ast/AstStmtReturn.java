package ast;

import types.*;
import symboltable.*;
import temp.Temp;
import ir.Ir;
import ir.IrCommandReturn;
import ir.IrCommandReturnVoid;

public class AstStmtReturn extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstExp exp;
	
	// Store the expected return type from enclosing function
	public static Type expectedReturnType = null;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtReturn(AstExp exp, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.exp = exp;
		this.line = line;
	}

	/********************************************************/
	/* The printing message for a return statement AST node */
	/********************************************************/
	public void printMe()
	{
		/***********************************/
		/* AST NODE TYPE = AST RETURN STMT */
		/***********************************/
		System.out.print("AST NODE STMT RETURN\n");

		/*****************************/
		/* RECURSIVELY PRINT exp ... */
		/*****************************/
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"RETURN");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (exp != null) AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	public void semantMe() throws SemanticException
	{
		Type returnType = null;
		
		/**************************************/
		/* [1] Get type of return expression  */
		/**************************************/
		if (exp != null)
		{
			returnType = exp.semantMe();
		}
		else
		{
			returnType = TypeVoid.getInstance();
		}
		
		/**************************************/
		/* [2] Check against expected return type */
		/**************************************/
		if (expectedReturnType != null)
		{
			// Void function should have no return value or return;
			if (expectedReturnType == TypeVoid.getInstance())
			{
				if (exp != null)
				{
					throw new SemanticException(line, "void function cannot return a value");
				}
				return;
			}
			
			// Exact same type
			if (returnType.name.equals(expectedReturnType.name))
			{
				return;
			}
			
			// nil can be returned for arrays and classes
			if (returnType == TypeNill.getInstance() && (expectedReturnType.isArray() || expectedReturnType.isClass()))
			{
				return;
			}
			
			// Class inheritance: can return subclass when parent class expected
			if (returnType.isClass() && expectedReturnType.isClass())
			{
				TypeClass returnClass = (TypeClass) returnType;
				TypeClass expectedClass = (TypeClass) expectedReturnType;
				if (returnClass.isAncestor(expectedClass))
				{
					return;
				}
			}
			
			// Array type matching
			if (returnType.isArray() && expectedReturnType.isArray())
			{
				TypeArray returnArr = (TypeArray) returnType;
				TypeArray expectedArr = (TypeArray) expectedReturnType;
				if (returnArr.type.name.equals(expectedArr.type.name))
				{
					return;
				}
			}
			
			throw new SemanticException(line, String.format("return type mismatch: expected %s, got %s",
				expectedReturnType.name, returnType.name));
		}
	}

	public Temp irMe()
	{
		if (exp != null)
		{
			// Return with value
			Temp t_value = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandReturn(t_value));
		}
		else
		{
			// Void return
			Ir.getInstance().AddIrCommand(new IrCommandReturnVoid());
		}

		return null; // Return statements don't produce a value
	}
}
