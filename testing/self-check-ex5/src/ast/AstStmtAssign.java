package ast;

import ir.Ir;
import ir.IrCommandStore;
import ir.IrCommandArrayStore;
import ir.IrCommandFieldStore;
import temp.Temp;
import types.*;

public class AstStmtAssign extends AstStmt
{
	/***************/
	/*  var := exp */
	/***************/
	public AstVar var;
	public AstExp exp;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstVar var, AstExp exp, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
		this.line = line;
	}

	/*********************************************************/
	/* The printing message for an assign statement AST node */
	/*********************************************************/
	public void printMe()
	{
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/
		System.out.print("AST NODE ASSIGN STMT\n");

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
		/* [2] Get type of right side (exp)   */
		/**************************************/
		if (exp != null) rightType = exp.semantMe();
		
		/**************************************/
		/* [3] Check type compatibility       */
		/**************************************/
		if (leftType == null || rightType == null)
		{
			throw new SemanticException(line, "null type in assignment");
		}
		
		// Cannot assign void to anything
		if (rightType instanceof TypeVoid)
		{
			throw new SemanticException(line, "cannot assign void to variable");
		}
		
		// Exact same type
		if (rightType.name.equals(leftType.name))
		{
			return;
		}
		
		// nil can be assigned to arrays and classes
		if (rightType == TypeNill.getInstance() && (leftType.isArray() || leftType.isClass()))
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
        // Evaluate right-hand side first (left-to-right evaluation)
        Temp src = exp.irMe();

        // Handle different types of variables
        if (var instanceof AstVarSimple simpleVar) {
            // Simple variable assignment
            String varNameWithOffset = String.format("%s_%d", simpleVar.name, simpleVar.offset);
            Ir.getInstance().AddIrCommand(new IrCommandStore(varNameWithOffset, src));
        }
        else if (var instanceof AstVarSubscript arrVar) {
            // Array element assignment: array[index] := value
            Temp t_array = arrVar.var.irMe();
            Temp t_index = arrVar.subscript.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandArrayStore(t_array, t_index, src));
        }
        else if (var instanceof AstVarField fieldVar) {
            // Field assignment: object.field := value
            Temp t_object = fieldVar.var.irMe();

            // Use cached offset from semantic analysis
            int fieldOffset = fieldVar.getCachedOffset();

            Ir.getInstance().AddIrCommand(
                new IrCommandFieldStore(t_object, fieldOffset, src, fieldVar.fieldName)
            );
        }

        return null; // Assignments don't produce a value
    }

}
