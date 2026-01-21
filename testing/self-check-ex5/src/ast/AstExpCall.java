package ast;

import ir.Ir;
import ir.IrCommandCallFunc;
import temp.Temp;
import types.*;
import symboltable.*;

public class AstExpCall extends AstExp
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String funcName;
	public AstExpList params;
    public AstVar var;
    private String cachedClassName = null;  // Cache class name for method calls

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpCall(AstVar var, String funcName, AstExpList params, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (var != null && params != null) System.out.print("====================== callExp ->  var DOT ID LPAREN expList RPAREN\n");
        if (var == null && params != null) System.out.print("====================== callExp ->  ID LPAREN expList RPAREN\n");
        if (var != null && params == null) System.out.print("====================== callExp ->  var DOT ID LPAREN RPAREN\n");
        if (var == null && params == null) System.out.print("====================== callExp ->  ID LPAREN RPAREN\n");

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.var = var;
        this.funcName = funcName;
		this.params = params;
		this.line = line;
	}

	/************************************************/
	/* The printing message for a call exp AST node */
	/************************************************/
	public void printMe()
	{
		/********************************/
		/* AST NODE TYPE = AST CALL EXP */
		/********************************/
		System.out.format("CALL(%s)\nWITH:\n",funcName);

		/***************************************/
		/* RECURSIVELY PRINT params + body ... */
		/***************************************/
		if (params != null) params.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("CALL(%s)\nWITH",funcName));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (params != null) AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
        if (var != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}

	public Type semantMe() throws SemanticException
	{
		TypeFunction funcType = null;
		
		/**************************************/
		/* [1] Find the function              */
		/**************************************/
		if (var != null)
		{
			// Method call: var.funcName(...)
			Type varType = var.semantMe();

			if (!varType.isClass())
			{
				throw new SemanticException(line, String.format("method call on non-class type %s", varType.name));
			}

			TypeClass classType = (TypeClass) varType;

			// Find the method, searching up the inheritance chain
			Type memberType = classType.find(funcName);

			if (memberType == null)
			{
				throw new SemanticException(line, String.format("method %s not found in class %s", funcName, classType.name));
			}

			if (!(memberType instanceof TypeFunction))
			{
				throw new SemanticException(line, String.format("%s is not a method", funcName));
			}

			funcType = (TypeFunction) memberType;

			// Cache the class name WHERE THE METHOD IS ACTUALLY DEFINED (for inheritance)
			// Search up the inheritance hierarchy to find which class defines this method
			TypeClass definingClass = classType;
			boolean foundInCurrent = false;
			while (definingClass != null)
			{
				// Check if this class defines the method
				for (TypeList it = definingClass.dataMembers; it != null; it = it.tail)
				{
					if (it.head != null && funcName.equals(it.head.name))
					{
						foundInCurrent = true;
						break;
					}
				}
				if (foundInCurrent)
				{
					cachedClassName = definingClass.name;
					break;
				}
				definingClass = definingClass.father;
			}

			// Fallback to object's class if not found (shouldn't happen)
			if (cachedClassName == null)
			{
				cachedClassName = classType.name;
			}
		}
		else
		{
			// Global function call: funcName(...)
			Type t = SymbolTable.getInstance().find(funcName);
			
			if (t == null)
			{
				throw new SemanticException(line, String.format("function %s not found", funcName));
			}
			
			if (!(t instanceof TypeFunction))
			{
				throw new SemanticException(line, String.format("%s is not a function", funcName));
			}
			
			funcType = (TypeFunction) t;
		}
		
		/**************************************/
		/* [2] Check argument count and types */
		/**************************************/
		TypeList expectedParams = funcType.params;
		AstExpList actualParams = params;
		
		// Count and check each parameter
		while (expectedParams != null && actualParams != null)
		{
			Type expectedType = expectedParams.head;
			Type actualType = actualParams.head.semantMe();
			
			// Check type compatibility
			boolean compatible = false;
			
			// Exact same type
			if (actualType.name.equals(expectedType.name))
			{
				compatible = true;
			}
			// nil can be passed for arrays and classes
			else if (actualType == TypeNill.getInstance() && (expectedType.isArray() || expectedType.isClass()))
			{
				compatible = true;
			}
			// Class inheritance
			else if (actualType.isClass() && expectedType.isClass())
			{
				TypeClass actualClass = (TypeClass) actualType;
				TypeClass expectedClass = (TypeClass) expectedType;
				if (actualClass.isAncestor(expectedClass))
				{
					compatible = true;
				}
			}
			// Array type matching
			else if (actualType.isArray() && expectedType.isArray())
			{
				TypeArray actualArr = (TypeArray) actualType;
				TypeArray expectedArr = (TypeArray) expectedType;
				if (actualArr.type.name.equals(expectedArr.type.name))
				{
					compatible = true;
				}
			}
			
			if (!compatible)
			{
				throw new SemanticException(line, String.format("argument type mismatch: expected %s, got %s", 
					expectedType.name, actualType.name));
			}
			
			expectedParams = expectedParams.tail;
			actualParams = actualParams.tail;
		}
		
		// Check that argument counts match
		if (expectedParams != null || actualParams != null)
		{
			throw new SemanticException(line, String.format("wrong number of arguments for function %s", funcName));
		}
		
		return funcType.returnType;
	}

    public Temp irMe()
    {
        // First, we need to find the function to get parameter names
        TypeFunction funcType = null;
        String callName = funcName;  // Will be modified for method calls
        try {
            if (var != null) {
                // For method calls, use cached class name (can't call semantMe again)
                if (cachedClassName != null) {
                    callName = cachedClassName + "_" + funcName;
                }
                // Try to find funcType in symbol table (may not work during IR phase)
                funcType = (TypeFunction) symboltable.SymbolTable.getInstance().find(funcName);
            } else {
                funcType = (TypeFunction) symboltable.SymbolTable.getInstance().find(funcName);
            }
        } catch (Exception e) {
            // If we can't find it, just call without parameters
        }

        // Store each argument into its corresponding parameter variable
        if (funcType != null && params != null && funcType.paramVarNames != null) {
            AstExpList argList = params;
            int paramIndex = 0;

            while (argList != null && paramIndex < funcType.paramVarNames.size()) {
                // Evaluate the argument expression
                Temp argTemp = argList.head.irMe();

                // Store it into the parameter variable using the name from TypeFunction
                String paramVarName = funcType.paramVarNames.get(paramIndex);
                Ir.getInstance().AddIrCommand(new ir.IrCommandStore(paramVarName, argTemp));

                argList = argList.tail;
                paramIndex++;
            }
        }

        // Call the function (with prefixed name for methods)
        Temp resultTemp = temp.TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandCallFunc(callName, resultTemp));

        return resultTemp;
    }

}
