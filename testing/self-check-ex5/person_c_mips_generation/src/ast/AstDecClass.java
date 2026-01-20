package ast;

import types.*;
import symboltable.*;

public class AstDecClass extends AstDec
{
	/********/
	/* NAME */
	/********/
	public String name;

	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstTypeNameList dataMembers;
    public String baseClass;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecClass(String name, String baseClass, AstTypeNameList dataMembers)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
	
		this.name = name;
        this.baseClass = baseClass;
		this.dataMembers = dataMembers;
	}

	/*********************************************************/
	/* The printing message for a class declaration AST node */
	/*********************************************************/
	public void printMe()
	{
		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		System.out.format("CLASS DEC = %s\n",name);
		if (dataMembers != null) dataMembers.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("CLASS\n%s",name));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber, dataMembers.serialNumber);
	}
	
	public void semantMe() throws SemanticException
	{
        TypeClass fatherClass = null;
        /*************************/
        /* [0] Find father and enter its scopes*/
        /*************************/
        if (baseClass != null) {
            Type fatherType = SymbolTable.getInstance().find(baseClass);
            if (fatherType == null) {
                throw new SemanticException(String.format("undefined base class: %s", baseClass));
            }
            if (!fatherType.isClass()) {
                throw new SemanticException(String.format("invalid type for class inheritance (must inherit from a class): %s", baseClass));
            }
            fatherClass = (TypeClass)fatherType;

            /**************************************/
            /* Check for inheritance cycles      */
            /**************************************/
            TypeClass ancestor = fatherClass;
            while (ancestor != null) {
                if (ancestor.name.equals(name)) {
                    throw new SemanticException(String.format("cyclic inheritance: %s", name));
                }
                ancestor = ancestor.father;
            }

            fatherClass.enterToSymbolTable();
        }

		/************************************************/
		/* [0.5] Enter a stub class type for self-references */
		/************************************************/
		TypeClass stubType = new TypeClass(fatherClass, name, null);
		SymbolTable.getInstance().enterWithoutCheck(name, stubType);

		/*************************/
		/* [1] Begin Class Scope */
		/*************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [2] Semant Data Members */
		/***************************/
		TypeClass t = new TypeClass(fatherClass, name, dataMembers.semantMe(fatherClass));

		/*****************/
		/* [3] End Scope */
		/*****************/
		SymbolTable.getInstance().endScope();

        /*****************/
        /* [4] Remove father class from symbol table */
        /*****************/
        if (fatherClass != null) {
            fatherClass.removeFromSymbolTable();
        }

		/************************************************/
		/* [5] Update the Class Type in the Symbol Table */
		/* (The stub's dataMembers is null, so just update by adding another entry) */
		/* The newer entry will shadow the stub */
		/************************************************/
		SymbolTable.getInstance().enterWithoutCheck(name, t);
	}
}
