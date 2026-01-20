package ast;

import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;

import java.util.HashMap;
import java.util.Map;

public class AstExpBinop extends AstExp
{
    public static int ADD = 0;
    public static int SUB = 1;
    public static int MUL = 2;
    public static int DIV = 3;
    public static int LT = 4;
    public static int GT = 5;
    public static int EQ = 6;
	int op;
	public AstExp left;
	public AstExp right;

    Map<Integer, String> opToString = new HashMap<>() {{
        put(ADD, "+");
        put(SUB, "-");
        put(MUL, "*");
        put(DIV, "/");
        put(LT, "<");
        put(GT, ">");
        put(EQ, "=");
    }};
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, int op, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> exp BINOP exp\n");

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
		this.line = line;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		String sop = opToString.get(op);

		/**********************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*********************************/
		System.out.print("AST NODE BINOP EXP\n");
		System.out.format("BINOP EXP(%s)\n",sop);

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null) left.printMe();
		if (right != null) right.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("BINOP(%s)",sop));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left  != null) AstGraphviz.getInstance().logEdge(serialNumber,left.serialNumber);
		if (right != null) AstGraphviz.getInstance().logEdge(serialNumber,right.serialNumber);
	}

	public Type semantMe() throws SemanticException
	{
		Type t1 = null;
		Type t2 = null;

		if (left != null) t1 = left.semantMe();
		if (right != null) t2 = right.semantMe();

		/**************************************/
		/* Check for division by zero         */
		/**************************************/
		if (op == DIV && right instanceof AstExpSimpleExp)
		{
			AstExpSimpleExp simpleExp = (AstExpSimpleExp) right;
			if (simpleExp.value instanceof AstSimpleExpInt)
			{
				AstSimpleExpInt intExp = (AstSimpleExpInt) simpleExp.value;
				if (intExp.value == 0)
				{
					throw new SemanticException(line, "division by zero");
				}
			}
		}

		/**************************************/
		/* Integer operations: +, -, *, /, <, > */
		/**************************************/
		if ((t1 == TypeInt.getInstance()) && (t2 == TypeInt.getInstance()))
		{
			return TypeInt.getInstance();
		}
		
		/**************************************/
		/* String concatenation: +            */
		/**************************************/
		if ((t1 == TypeString.getInstance()) && (t2 == TypeString.getInstance()) && op == ADD)
		{
			return TypeString.getInstance();
		}

		/**************************************/
		/* Equality check: =                  */
		/* Can compare: int=int, string=string, */
		/* arrays of same type, classes (with inheritance), nil */
		/**************************************/
		if (op == EQ)
		{
			// nil can be compared with arrays or classes
			if (t1 == TypeNill.getInstance() && (t2.isArray() || t2.isClass()))
			{
				return TypeInt.getInstance();
			}
			if (t2 == TypeNill.getInstance() && (t1.isArray() || t1.isClass()))
			{
				return TypeInt.getInstance();
			}
			// nil = nil
			if (t1 == TypeNill.getInstance() && t2 == TypeNill.getInstance())
			{
				return TypeInt.getInstance();
			}
			// Same array type
			if (t1.isArray() && t2.isArray())
			{
				TypeArray arr1 = (TypeArray) t1;
				TypeArray arr2 = (TypeArray) t2;
				if (arr1.type.name.equals(arr2.type.name))
				{
					return TypeInt.getInstance();
				}
			}
			// Same class or inheritance relationship
			if (t1.isClass() && t2.isClass())
			{
				TypeClass c1 = (TypeClass) t1;
				TypeClass c2 = (TypeClass) t2;
				if (c1.name.equals(c2.name) || c1.isAncestor(c2) || c2.isAncestor(c1))
				{
					return TypeInt.getInstance();
				}
			}
		}

		throw new SemanticException(line, String.format("type mismatch for operation %s between %s and %s",
			opToString.get(op),
			t1 != null ? t1.name : "null",
			t2 != null ? t2.name : "null"));
	}

    public Temp irMe()
    {
        Temp t1 = null;
        Temp t2 = null;
        Temp dst = TempFactory.getInstance().getFreshTemp();

        // Evaluate left side FIRST (left-to-right evaluation)
        if (left  != null) t1 = left.irMe();
        if (right != null) t2 = right.irMe();

        // Get types to determine which IR command to use
        Type type1 = null;
        Type type2 = null;
        try {
            if (left != null) type1 = left.semantMe();
            if (right != null) type2 = right.semantMe();
        } catch (SemanticException e) {
            // Semantic analysis should have already passed
            System.err.println("Error in irMe: semantic analysis failed");
        }

        // Handle ADD operation (integers or strings)
        if (op == ADD)
        {
            if (type1 == TypeString.getInstance() && type2 == TypeString.getInstance())
            {
                // String concatenation
                Ir.getInstance().AddIrCommand(new IrCommandStringConcat(dst, t1, t2));
            }
            else
            {
                // Integer addition
                Ir.getInstance().AddIrCommand(new IrCommandBinopAddIntegers(dst, t1, t2));
            }
        }
        else if (op == SUB)
        {
            Ir.getInstance().AddIrCommand(new IrCommandBinopSubIntegers(dst, t1, t2));
        }
        else if (op == MUL)
        {
            Ir.getInstance().AddIrCommand(new IrCommandBinopMulIntegers(dst, t1, t2));
        }
        else if (op == DIV)
        {
            Ir.getInstance().AddIrCommand(new IrCommandBinopDivIntegers(dst, t1, t2));
        }
        else if (op == EQ)
        {
            if (type1 == TypeString.getInstance() && type2 == TypeString.getInstance())
            {
                // String equality (content comparison)
                Ir.getInstance().AddIrCommand(new IrCommandStringEqual(dst, t1, t2));
            }
            else
            {
                // Integer/array/class equality
                // For arrays and classes, this compares addresses
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(dst, t1, t2));
            }
        }
        else if (op == LT)
        {
            Ir.getInstance().AddIrCommand(new IrCommandBinopLtIntegers(dst, t1, t2));
        }
        else if (op == GT)
        {
            Ir.getInstance().AddIrCommand(new IrCommandBinopLtIntegers(dst, t2, t1)); // switched order
        }

        return dst;
    }


}
