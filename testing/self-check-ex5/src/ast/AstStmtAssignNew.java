package ast;

import types.*;
import temp.*;
import ir.*;

public class AstStmtAssignNew extends AstStmt
{
	/***************/
	/*  var := exp */
	/***************/
	public AstVar var;
	public AstNewExp exp;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssignNew(AstVar var, AstNewExp exp, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== stmt -> var ASSIGN newExp SEMICOLON\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
		this.line = line;
	}

	/*********************************************************/
	/* The printing message for an assign new statement AST node */
	/*********************************************************/
	public void printMe()
	{
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/
		System.out.print("AST NODE ASSIGN NEW STMT\n");

		/***********************************/
		/* RECURSIVELY PRINT VAR + EXP ... */
		/***********************************/
		if (var != null) var.printMe();
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"ASSIGN\nleft := right\n");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	public void semantMe() throws SemanticException
	{
		Type leftType = null;
		Type rightType = null;
		
		/**************************************/
		/* [1] Get type of left side (var)    */
		/**************************************/
		if (var != null) leftType = var.semantMe();
		
		/**************************************/
		/* [2] Get type of right side (newExp)*/
		/**************************************/
		if (exp != null) rightType = exp.semantMe();
		
		/**************************************/
		/* [3] Check type compatibility       */
		/**************************************/
		if (leftType == null || rightType == null)
		{
			throw new SemanticException(line, "null type in assignment");
		}
		
		// Exact same type
		if (rightType.name.equals(leftType.name))
		{
			return;
		}
		
		// Class inheritance: can assign subclass to parent class variable
		if (rightType.isClass() && leftType.isClass())
		{
			TypeClass rightClass = (TypeClass) rightType;
			TypeClass leftClass = (TypeClass) leftType;
			if (rightClass.isAncestor(leftClass))
			{
				return;
			}
		}
		
		// Array type matching
		if (rightType.isArray() && leftType.isArray())
		{
			TypeArray rightArr = (TypeArray) rightType;
			TypeArray leftArr = (TypeArray) leftType;
			if (rightArr.type.name.equals(leftArr.type.name))
			{
				return;
			}
		}
		
		throw new SemanticException(line, String.format("type mismatch in assignment: cannot assign %s to %s",
			rightType.name, leftType.name));
	}

	public Temp irMe()
	{
		/**************************************/
		/* [1] Generate IR for new expression */
		/**************************************/
		Temp src = exp.irMe();

		/**************************************/
		/* [2] Generate IR for variable assignment */
		/* (same logic as AstStmtAssign) */
		/**************************************/
		if (var instanceof AstVarSimple) {
			AstVarSimple simpleVar = (AstVarSimple) var;
			String varNameWithOffset = String.format("%s_%d", simpleVar.name, simpleVar.offset);
			Ir.getInstance().AddIrCommand(new IrCommandStore(varNameWithOffset, src));
		}
		else if (var instanceof AstVarSubscript) {
			AstVarSubscript arrVar = (AstVarSubscript) var;
			Temp t_array = arrVar.var.irMe();
			Temp t_index = arrVar.subscript.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandArrayStore(t_array, t_index, src));
		}
		else if (var instanceof AstVarField) {
			AstVarField fieldVar = (AstVarField) var;
			Temp t_object = fieldVar.var.irMe();

			// Get field offset
			Type varType = null;
			try {
				varType = fieldVar.var.semantMe();
			} catch (SemanticException e) {
				System.err.println("Error: semantic analysis failed in irMe()");
				return null;
			}

			TypeClass classType = (TypeClass) varType;
			int fieldOffset = classType.getFieldOffset(fieldVar.fieldName);

			Ir.getInstance().AddIrCommand(new IrCommandFieldStore(t_object, fieldOffset, src, fieldVar.fieldName));
		}

		return null;
	}
}
