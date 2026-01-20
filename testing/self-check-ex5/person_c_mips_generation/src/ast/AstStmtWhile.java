package ast;

import ir.*;
import temp.Temp;
import types.*;
import symboltable.*;

public class AstStmtWhile extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtWhile(AstExp cond, AstStmtList body, int line)
	{
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== stmt -> WHILE LPAREN exp RPAREN LBRACE stmtList RBRACE\n");

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
		this.cond = cond;
		this.body = body;
		this.line = line;
	}

    /*********************************************************/
    /* The printing message for a while statement AST node */
    /*********************************************************/
    public void printMe()
    {
        /********************************************/
        /* AST NODE TYPE = AST WHILE STATEMENT */
        /********************************************/
        System.out.print("AST NODE WHILE\n");

        /***********************************/
        /* RECURSIVELY PRINT COND + BODY ... */
        /***********************************/
        if (cond != null) cond.printMe();
        if (body != null) body.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "WHILE (exp) {body}\n");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (cond != null) AstGraphviz.getInstance().logEdge(serialNumber,cond.serialNumber);
        if (body != null) AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
    }

    public void semantMe() throws SemanticException
    {
        /****************************/
        /* [0] Semant the Condition */
        /****************************/
        if (cond.semantMe() != TypeInt.getInstance())
        {
            throw new SemanticException(line, "condition inside WHILE is not integral");
        }
        
        /*************************/
        /* [1] Begin While Scope */
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
    }

    public Temp irMe()
    {
        /*******************************/
        /* [1] Allocate 2 fresh labels */
        /*******************************/
        String labelEnd   = IrCommand.getFreshLabel("end");
        String labelStart = IrCommand.getFreshLabel("start");

        /*********************************/
        /* [2] entry label for the while */
        /*********************************/
        Ir.getInstance().AddIrCommand(new IrCommandLabel(labelStart));

        /********************/
        /* [3] cond.IRme(); */
        /********************/
        Temp condTemp = cond.irMe();

        /******************************************/
        /* [4] Jump conditionally to the loop end */
        /******************************************/
        Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp,labelEnd));

        /*******************/
        /* [5] body.IRme() */
        /*******************/
        body.irMe();

        /******************************/
        /* [6] Jump to the loop entry */
        /******************************/
        Ir.getInstance().AddIrCommand(new IrCommandJumpLabel(labelStart));

        /**********************/
        /* [7] Loop end label */
        /**********************/
        Ir.getInstance().AddIrCommand(new IrCommandLabel(labelEnd));

        /*******************/
        /* [8] return null */
        /*******************/
        return null;
    }

}