/***********/
/* PACKAGE */
/***********/
package ast;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;
import symboltable.*;

public class AstTypeName extends AstNode
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstType type;
	public String name;
	public AstDecFunc funcDec;  // non-null if this is a method
	public AstDecVar varDec;    // non-null if this is a field

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstTypeName(AstType type, String name)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.type = type;
		this.name = name;
		this.funcDec = null;
		this.varDec = null;
	}

    public AstTypeName(AstDecFunc funcDec)
    {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();
        this.name = funcDec.name;
        this.type = funcDec.returnType;
        this.funcDec = funcDec;
        this.varDec = null;
        this.line = funcDec.line;
    }

    public AstTypeName(AstDecVar varDec)
    {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();
        this.type = varDec.type;
        this.name = varDec.name;
        this.funcDec = null;
        this.varDec = varDec;
        this.line = varDec.line;
    }

	/*************************************************/
	/* The printing message for a type name AST node */
	/*************************************************/
	public void printMe()
	{
		/**************************************/
		/* AST NODE TYPE = AST TYPE NAME NODE */
		/**************************************/
		System.out.format("NAME(%s):TYPE(%s)\n",name,type.name);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("NAME:TYPE\n%s:%s",name,type.name));
	}

	public Type semantMe() throws SemanticException
	{
		return semantMe(null);
	}

	public Type semantMe(TypeClass fatherClass) throws SemanticException
	{
		Type resultType;

		/**************************************/
		/* Handle method (function) members   */
		/**************************************/
		if (funcDec != null)
		{
			Type returnTypeFromTable;
			TypeList paramTypes = null;

			/*********************************/
			/* [1] Check return type exists  */
			/*********************************/
			returnTypeFromTable = SymbolTable.getInstance().find(funcDec.returnType.name);
			if (returnTypeFromTable == null)
			{
				if (funcDec.returnType.name.equals("void")) {
					returnTypeFromTable = TypeVoid.getInstance();
				}
				else {
					throw new SemanticException(line, String.format("non existing return type %s", funcDec.returnType.name));
				}
			}

			/*********************************/
			/* [2] Check parameter types     */
			/*********************************/
			for (AstTypeNameList it = funcDec.params; it != null; it = it.tail)
			{
				Type paramType = SymbolTable.getInstance().find(it.head.type.name);
				if (paramType == null)
				{
					throw new SemanticException(line, String.format("non existing type %s", it.head.type.name));
				}
				if (paramType instanceof TypeVoid)
				{
					throw new SemanticException(line, String.format("parameter %s cannot be of type void", it.head.name));
				}
				paramTypes = new TypeList(paramType, paramTypes);
			}

			/************************************************/
			/* [3] Check shadowing and method overriding   */
			/************************************************/
			if (fatherClass != null)
			{
				Type inheritedMember = fatherClass.find(name);
				if (inheritedMember != null)
				{
					if (inheritedMember instanceof TypeFunction)
					{
						TypeFunction inheritedFunc = (TypeFunction) inheritedMember;

						if (!inheritedFunc.returnType.name.equals(returnTypeFromTable.name))
						{
							throw new SemanticException(line, String.format("invalid override: return type mismatch"));
						}

						TypeList inheritedParams = inheritedFunc.params;
						TypeList currentParams = paramTypes;
						while (inheritedParams != null && currentParams != null)
						{
							if (!inheritedParams.head.name.equals(currentParams.head.name))
							{
								throw new SemanticException(line, String.format("invalid override: parameter type mismatch"));
							}
							inheritedParams = inheritedParams.tail;
							currentParams = currentParams.tail;
						}
						if (inheritedParams != null || currentParams != null)
						{
							throw new SemanticException(line, String.format("invalid override: parameter count mismatch"));
						}
					}
					else
					{
						throw new SemanticException(line, String.format("method cannot shadow field: %s", name));
					}
				}
			}

			TypeFunction funcType = new TypeFunction(returnTypeFromTable, name, paramTypes);
			resultType = funcType;

			/************************************************/
			/* [4] Analyze method body                      */
			/************************************************/
			// Enter the method into symbol table first (for recursive calls)
			SymbolTable.getInstance().enter(name, resultType);

			// Begin method scope
			SymbolTable.getInstance().beginScope();

			// Enter parameters
			for (AstTypeNameList it = funcDec.params; it != null; it = it.tail)
			{
				Type paramType = SymbolTable.getInstance().find(it.head.type.name);
				SymbolTableEntry entry = SymbolTable.getInstance().enter(it.head.name, paramType);

				// Store the parameter variable name with offset for IR generation
				// Use the symbol table's prevtopIndex, NOT the AST node's serialNumber
				String paramVarName = String.format("%s_%d", it.head.name, entry.prevtopIndex);
				((TypeFunction)resultType).paramVarNames.add(0, paramVarName);  // Prepend to reverse order
			}

			// Analyze body
			AstStmtReturn.expectedReturnType = returnTypeFromTable;
			if (funcDec.body != null)
			{
				funcDec.body.semantMe();
			}
			AstStmtReturn.expectedReturnType = null;

			// End method scope
			SymbolTable.getInstance().endScope();

			// Method type was already entered, so return early
			return resultType;
		}
		/**************************************/
		/* Handle variable/field members      */
		/**************************************/
		else
		{
			Type t = SymbolTable.getInstance().find(type.name);
			if (t == null)
			{
				throw new SemanticException(line, String.format("undeclared type %s", type.name));
			}

			if (t instanceof TypeVoid)
			{
				throw new SemanticException(line, String.format("field/variable %s cannot be of type void", name));
			}

			if (fatherClass != null)
			{
				Type inheritedMember = fatherClass.find(name);
				if (inheritedMember != null)
				{
					throw new SemanticException(line, String.format("field %s cannot shadow inherited member", name));
				}
			}

			// Wrap in TypeField to preserve field name for class member lookup
			resultType = new TypeField(name, t);
		}

		/*******************************************************/
		/* Enter member into symbol table                      */
		/*******************************************************/
		SymbolTable.getInstance().enter(name, resultType);

		return resultType;
	}	
}
