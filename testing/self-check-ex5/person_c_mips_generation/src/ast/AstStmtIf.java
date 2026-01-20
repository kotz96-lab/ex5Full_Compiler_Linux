package ast;

import ir.*;
import temp.Temp;
import types.*;
import symboltable.*;

public class AstStmtIf extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;
    public AstStmtElse elseBody;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtIf(AstExp cond, AstStmtList body, AstStmtElse elseBody, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (elseBody != null) {System.out.print("====================== stmt -> IF LPAREN exp RPAREN LBRACE stmtList RBRACE stmtElse\n");}
        if (elseBody == null) {System.out.print("====================== stmt -> IF LPAREN exp RPAREN LBRACE stmtList RBRACE\n");}

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
		this.cond = cond;
		this.body = body;
        this.elseBody = elseBody;
        this.line = line;
	}

	/****************************************************/
	/* The printing message for an if statment AST node */
	/****************************************************/
	public void printMe()
	{
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE STMT IF\n");

		/**************************************/
		/* RECURSIVELY PRINT COND + BODY + ELSE ... */
		/**************************************/
		if (cond != null) cond.printMe();
		if (body != null) body.printMe();
        if (elseBody != null) elseBody.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"IF (left)\nTHEN right");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (cond != null) AstGraphviz.getInstance().logEdge(serialNumber,cond.serialNumber);
		if (body != null) AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
        if (elseBody != null) AstGraphviz.getInstance().logEdge(serialNumber,elseBody.serialNumber);
	}

	public void semantMe() throws SemanticException
	{
		/****************************/
		/* [0] Semant the Condition */
		/****************************/
		if (cond.semantMe() != TypeInt.getInstance())
		{
			throw new SemanticException(line, "condition inside IF is not integral");
		}
		
		/*************************/
		/* [1] Begin If Scope */
		/*************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [2] Semant Body */
		/***************************/
		body.semantMe();

		/*****************/
		/* [3] End Scope */
		/*****************/
		SymbolTable.getInstance().endScope();

		/***************************/
		/* [4] Semant Else Body    */
		/***************************/
		if (elseBody != null)
		{
			SymbolTable.getInstance().beginScope();
			elseBody.body.semantMe();
			SymbolTable.getInstance().endScope();
		}
	}

	public Temp irMe()
	{
		/*******************************/
		/* [1] Allocate fresh labels   */
		/*******************************/
		String labelElse = IrCommand.getFreshLabel("else");
		String labelEnd = IrCommand.getFreshLabel("end");

		/******************************/
		/* [2] Evaluate condition     */
		/******************************/
		Temp condTemp = cond.irMe();

		/******************************************/
		/* [3] Jump to else/end if condition is 0 */
		/******************************************/
		if (elseBody != null)
		{
			Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, labelElse));
		}
		else
		{
			Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, labelEnd));
		}

		/****************************/
		/* [4] Generate then body   */
		/****************************/
		body.irMe();

		/********************************/
		/* [5] Jump to end after then   */
		/********************************/
		if (elseBody != null)
		{
			Ir.getInstance().AddIrCommand(new IrCommandJumpLabel(labelEnd));

			/****************************/
			/* [6] Generate else body   */
			/****************************/
			Ir.getInstance().AddIrCommand(new IrCommandLabel(labelElse));
			elseBody.body.irMe();
		}

		/**********************/
		/* [7] End label      */
		/**********************/
		Ir.getInstance().AddIrCommand(new IrCommandLabel(labelEnd));

		return null;
	}
}
