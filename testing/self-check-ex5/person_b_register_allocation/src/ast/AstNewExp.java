package ast;

import types.*;
import symboltable.*;

public class AstNewExp extends AstExp
{
	public AstExp exp;
	public AstType type;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstNewExp(AstType type, AstExp exp, int line)
	{
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (exp != null) {System.out.print("====================== newExp -> NEW type LBRACK exp RBRACK\n");}
        if (exp == null) {System.out.print("====================== newExp -> NEW type\n");}

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
		this.type = type;
        this.exp = exp;
        this.line = line;
	}

    /*********************************************************/
    /* The printing message for a new exp AST node */
    /*********************************************************/
    public void printMe()
    {
        /********************************************/
        /* AST NODE TYPE = AST NEW EXPT */
        /********************************************/
        System.out.print("NEW EXP\n");

        /***********************************/
        /* RECURSIVELY PRINT TYPE + EXP ... */
        /***********************************/
        if (type != null) type.printMe();
        if (exp != null) exp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "NEW\n");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        AstGraphviz.getInstance().logEdge(serialNumber,type.serialNumber);
        if (exp != null) {AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);}
    }

    public Type semantMe() throws SemanticException
    {
        /**************************************/
        /* [1] Find the type in symbol table  */
        /**************************************/
        Type t = SymbolTable.getInstance().find(type.name);
        if (t == null)
        {
            throw new SemanticException(line, String.format("type %s not found", type.name));
        }
        
        /**************************************/
        /* [2] If array allocation (new type[exp]) */
        /**************************************/
        if (exp != null)
        {
            // The subscript must be integer
            Type expType = exp.semantMe();
            if (expType != TypeInt.getInstance())
            {
                throw new SemanticException(line, String.format("array size must be integer, got %s", expType.name));
            }
            // Return array type with element type t
            return new TypeArray(t, type.name + "[]");
        }
        
        /**************************************/
        /* [3] If class instantiation (new type) */
        /**************************************/
        if (!t.isClass())
        {
            throw new SemanticException(line, String.format("cannot instantiate non-class type %s", type.name));
        }
        
        return t;
    }
}