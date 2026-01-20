package ast;

import temp.Temp;
import types.*;

public class AstDecList extends AstNode
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstDec head;
	public AstDecList tail;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecList(AstDec head, AstDecList tail)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.head = head;
		this.tail = tail;
	}

	/********************************************************/
	/* The printing message for a declaration list AST node */
	/********************************************************/
	public void printMe()
	{
		/********************************/
		/* AST NODE TYPE = AST DEC LIST */
		/********************************/
		System.out.print("AST NODE DEC LIST\n");

		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (head != null) head.printMe();
		if (tail != null) tail.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"DEC\nLIST\n");
				
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (head != null) AstGraphviz.getInstance().logEdge(serialNumber,head.serialNumber);
		if (tail != null) AstGraphviz.getInstance().logEdge(serialNumber,tail.serialNumber);
	}

	public void semantMe() throws SemanticException
	{
        /*************************************/
        /* RECURSIVELY PRINT HEAD + TAIL ... */
        /*************************************/
        if (head != null) head.semantMe();
        if (tail != null) tail.semantMe();
	}

    public Temp irMe()
    {
        /*******************************************/
        /* [1] First pass: Generate all globals   */
        /*     (variable declarations only)       */
        /*******************************************/
        AstDecList it = this;
        while (it != null)
        {
            if (it.head instanceof AstDecVar)
            {
                it.head.irMe();
            }
            it = it.tail;
        }

        /*******************************************/
        /* [2] Second pass: Generate everything else */
        /*******************************************/
        it = this;
        while (it != null)
        {
            if (!(it.head instanceof AstDecVar))
            {
                it.head.irMe();
            }
            it = it.tail;
        }

        return null;
    }
}
