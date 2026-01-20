package ast;

import types.*;
import symboltable.*;
import temp.*;
import ir.*;

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

    public Temp irMe()
    {
        Temp dst = TempFactory.getInstance().getFreshTemp();

        /**************************************/
        /* [1] Array allocation: new type[size] */
        /**************************************/
        if (exp != null)
        {
            // Evaluate size expression
            Temp sizeTemp = exp.irMe();

            // Get element size (assume 4 bytes for now)
            int elementSize = 4;

            // Emit NEW_ARRAY command
            Ir.getInstance().AddIrCommand(new IrCommandNewArray(dst, sizeTemp, elementSize));

            return dst;
        }

        /**************************************/
        /* [2] Object allocation: new ClassName */
        /**************************************/
        Type t = SymbolTable.getInstance().find(type.name);
        if (t != null && t.isClass())
        {
            TypeClass classType = (TypeClass) t;

            // Count fields in class (including inherited)
            int fieldCount = 0;
            for (TypeClass c = classType; c != null; c = c.father)
            {
                for (TypeList it = c.dataMembers; it != null; it = it.tail)
                {
                    if (it.head != null)
                    {
                        fieldCount++;
                    }
                }
            }

            // Calculate size needed (number of fields * 4 bytes per field)
            int sizeInBytes = fieldCount * 4;
            if (sizeInBytes == 0) sizeInBytes = 4; // Minimum 1 word

            // Emit NEW_OBJECT command
            Ir.getInstance().AddIrCommand(new IrCommandNewObject(dst, type.name, sizeInBytes));

            return dst;
        }

        return dst;
    }
}