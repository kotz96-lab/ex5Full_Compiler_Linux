package ast;

import ir.Ir;
import ir.IrCommandAllocate;
import ir.IrCommandStore;
import temp.Temp;
import types.*;
import symboltable.*;

public class AstDecVar extends AstDec
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstType type;
	public String name;
	public AstExp initialValue;
	public int offset = -1;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecVar(AstType type, String name, AstExp initialValue, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.type = type;
		this.name = name;
		this.initialValue = initialValue;
		this.line = line;
	}

	/************************************************************/
	/* The printing message for a variable declaration AST node */
	/************************************************************/
	public void printMe()
	{
		/****************************************/
		/* AST NODE TYPE = AST VAR DECLARATION */
		/***************************************/
		if (initialValue != null) System.out.format("VAR-DEC(%s):%s := initialValue\n",name,type);
		if (initialValue == null) System.out.format("VAR-DEC(%s):%s                \n",name,type);

		/**************************************/
		/* RECURSIVELY PRINT initialValue ... */
		/**************************************/
		if (initialValue != null) initialValue.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("VAR\nDEC(%s)\n:%s",name,type.name));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (initialValue != null) AstGraphviz.getInstance().logEdge(serialNumber,initialValue.serialNumber);
			
	}

	public void semantMe() throws SemanticException
	{
        Type leftType;

        /****************************/
        /* [1] Check If Type exists */
        /****************************/
        leftType = SymbolTable.getInstance().find(type.name);
        if (leftType == null)
        {
            throw new SemanticException(line, String.format("non existing type %s", type.name));
        }

        /********************************/
        /* [2] Check type is not void */
        /********************************/
        if (leftType instanceof TypeVoid)
        {
            throw new SemanticException(line, String.format("variable %s cannot be of type void", name));
        }

        if (initialValue != null) {
            Type rightType = initialValue.semantMe();
            
            // Cannot assign void to variable
            if (rightType instanceof TypeVoid)
            {
                throw new SemanticException(line, "cannot assign void to variable");
            }
            
            boolean isAssignmentAllowed = rightType.name.equals(leftType.name) || // covers exactly the same type
                    (initialValue instanceof AstNewExp && rightType.isArray() && leftType.isArray() && ((TypeArray)rightType).type.name.equals(((TypeArray)leftType).type.name)) || // new array with same type
                    (rightType.isClass() && leftType.isClass() && ((TypeClass)rightType).isAncestor((TypeClass)leftType)) || // class inheritance
                    (rightType.name.equals("nill") && (leftType.isClass() || leftType.isArray())); // nill
            if (!isAssignmentAllowed) {
                throw new SemanticException(line, String.format("mismatching types in var dec: %s := %s", leftType.name, leftType.name));
            }
        }

		/************************************************/
		/* [2] Enter the Identifier to the Symbol Table */
		/*     with duplicate checking                  */
		/************************************************/
		try {
            SymbolTableEntry entry = SymbolTable.getInstance().enter(name, leftType);

			/************************************************/
			/* [3] Store the offset for later IR generation */
			/************************************************/
            this.offset = entry.prevtopIndex;
		} catch (SemanticException e) {
			// Re-throw with line number if it doesn't have one
			if (e.getLine() == 0) {
				throw new SemanticException(line, e.getMessage());
			}
			throw e;
		}
	}

    public Temp irMe()
    {
        String varNameWithOffset = String.format("%s_%d", name, offset);
        Ir.getInstance().AddIrCommand(new IrCommandAllocate(varNameWithOffset));

        if (initialValue != null)
        {
            Ir.getInstance().AddIrCommand(new IrCommandStore(varNameWithOffset, initialValue.irMe()));
        }
        return null;
    }
	
}
