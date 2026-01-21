package ast;

import temp.Temp;
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
        }

		/************************************************/
		/* [0.5] Enter a stub class type for self-references */
		/* IMPORTANT: Enter stub BEFORE father scopes so it won't be removed! */
		/************************************************/
		TypeClass stubType = new TypeClass(fatherClass, name, null);
		SymbolTable.getInstance().enterWithoutCheck(name, stubType);

		// Now enter father's scopes after the stub
		if (fatherClass != null) {
			fatherClass.enterToSymbolTable();
		}

		/*************************/
		/* [1] Begin Class Scope */
		/*************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [2] Semant Data Members */
		/***************************/
		TypeList members = dataMembers.semantMe(fatherClass);

		// Update the stub with the actual data members instead of creating a new TypeClass
		// This ensures that any references to the stub (from self-referential fields)
		// will now see the complete class with all members
		stubType.dataMembers = members;

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

		// The stub has been updated in-place and is already in the symbol table!
	}

	private int countMembers(TypeList list)
	{
		int count = 0;
		for (TypeList it = list; it != null; it = it.tail) count++;
		return count;
	}

	/**
	 * Generate IR for class methods
	 * Called during IR generation phase to emit method code
	 */
	public Temp irMe()
	{
		// Iterate through all data members and generate IR for methods
		AstTypeNameList it = dataMembers;
		while (it != null)
		{
			if (it.head != null && it.head.funcDec != null)
			{
				// This is a method - need to prefix with class name to avoid collisions
				// Save original name
				String originalName = it.head.funcDec.name;

				// Temporarily change name to ClassName_methodName
				it.head.funcDec.name = name + "_" + originalName;

				// Generate IR with prefixed name
				it.head.funcDec.irMe();

				// Restore original name
				it.head.funcDec.name = originalName;
			}
			it = it.tail;
		}
		return null;
	}
}
