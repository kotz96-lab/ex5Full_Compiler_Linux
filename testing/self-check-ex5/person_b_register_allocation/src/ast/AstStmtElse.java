package ast;

import temp.Temp;

public class AstStmtElse extends AstNode
{
	public AstStmtList body;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtElse(AstStmtList body)
	{
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== stmtElse ->  ELSE LBRACE stmtList RBRACE\n");

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
		this.body = body;
	}

    /*********************************************************/
    /* The printing message for an else statement AST node */
    /*********************************************************/
    public void printMe()
    {
        /********************************************/
        /* AST NODE TYPE = AST ELSE STATEMENT */
        /********************************************/
        System.out.print("AST NODE ELSE\n");

        /***********************************/
        /* RECURSIVELY PRINT BODY ... */
        /***********************************/
        if (body != null) body.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "ELSE {body}\n");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
    }

    public Temp irMe()
    {
        return body.irMe();
    }
}