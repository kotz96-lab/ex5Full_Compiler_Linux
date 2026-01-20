package symboltable;

import java.io.PrintWriter;
import ast.SemanticException;
import types.*;

public class SymbolTable
{
	private int hashArraySize = 13;
	private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
	private SymbolTableEntry top;
	private int topIndex = 0;
	
	private int hash(String s)
	{
		if (s.charAt(0) == 'l') {return 1;}
		if (s.charAt(0) == 'm') {return 1;}
		if (s.charAt(0) == 'r') {return 3;}
		if (s.charAt(0) == 'i') {return 6;}
		if (s.charAt(0) == 'd') {return 6;}
		if (s.charAt(0) == 'k') {return 6;}
		if (s.charAt(0) == 'f') {return 6;}
		if (s.charAt(0) == 'S') {return 6;}
		return 12;
	}

    public SymbolTableEntry enter(String name, Type t) throws SemanticException
    {
        // Check if name already exists in the current scope
        if (existsInCurrentScope(name))
        {
            throw new SemanticException(String.format("parameter %s already exists", name));
        }

        return enterWithoutCheck(name, t);
    }

	public SymbolTableEntry enterWithoutCheck(String name, Type t)
	{
		// Compute the hash value for this new entry
		int hashValue = hash(name);

		// Extract what will eventually be the next entry in the hashed position
		SymbolTableEntry next = table[hashValue];

		// Prepare a new symbol table entry with name, type, next and prevtop
		SymbolTableEntry e = new SymbolTableEntry(name,t,hashValue,next,top, topIndex++);

		// Update the top of the symbol table
		top = e;

		// Enter the new entry to the table
		table[hashValue] = e;

		// Print Symbol Table
		printMe();

        return e;
	}

	public Type find(String name)
	{
		SymbolTableEntry e;

		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				return e.type;
			}
		}

		return null;
	}

	public SymbolTableEntry findEntry(String name)
	{
		SymbolTableEntry e;

		for (e = table[hash(name)]; e != null; e = e.next)
		{
			if (name.equals(e.name))
			{
				return e;
			}
		}

		return null;
	}

	public void beginScope()
	{
        enterWithoutCheck(
			"SCOPE-BOUNDARY",
			new TypeForScopeBoundaries("NONE"));

		printMe();
	}

	public void endScope()
	{
		// Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit
		while (top != null && !top.name.equals("SCOPE-BOUNDARY"))
		{
			table[top.index] = top.next;
			topIndex = topIndex -1;
			top = top.prevtop;
		}
		// Pop the SCOPE-BOUNDARY sign itself
		if (top != null)
		{
			table[top.index] = top.next;
			topIndex = topIndex -1;
			top = top.prevtop;
		}

		printMe();
	}

	public boolean existsInCurrentScope(String name)
	{
		SymbolTableEntry e;

		// Traverse from top until we hit a SCOPE-BOUNDARY or null
		for (e = top; e != null && !e.name.equals("SCOPE-BOUNDARY"); e = e.prevtop)
		{
			if (name.equals(e.name))
			{
				return true;
			}
		}

		return false;
	}

	public static int n=0;
	
	public void printMe()
	{
		int i=0;
		int j=0;
		String dirname="./output/";
		String filename=String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt",n++);

		try
		{
			// Open Graphviz text file for writing
			PrintWriter fileWriter = new PrintWriter(dirname+filename);

			// Write Graphviz dot prolog
			fileWriter.print("digraph structs {\n");
			fileWriter.print("rankdir = LR\n");
			fileWriter.print("node [shape=record];\n");

			// Write Hash Table Itself
			fileWriter.print("hashTable [label=\"");
			for (i=0;i<hashArraySize-1;i++) { fileWriter.format("<f%d>\n%d\n|",i,i); }
			fileWriter.format("<f%d>\n%d\n\"];\n",hashArraySize-1,hashArraySize-1);
		
			// Loop over hash table array and print all linked lists per array cell
			for (i=0;i<hashArraySize;i++)
			{
				if (table[i] != null)
				{
					fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n",i,i);
				}
				j=0;
				for (SymbolTableEntry it = table[i]; it!=null; it=it.next)
				{
					fileWriter.format("node_%d_%d ",i,j);
					fileWriter.format("[label=\"<f0>%s|<f1>%s|<f2>prevtop=%d|<f3>next\"];\n",
						it.name,
						it.type.name,
						it.prevtopIndex);

					if (it.next != null)
					{
						fileWriter.format(
							"node_%d_%d -> node_%d_%d [style=invis,weight=10];\n",
							i,j,i,j+1);
						fileWriter.format(
							"node_%d_%d:f3 -> node_%d_%d:f0;\n",
							i,j,i,j+1);
					}
					j++;
				}
			}
			fileWriter.print("}\n");
			fileWriter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	// Singleton implementation
	private static SymbolTable instance = null;

	protected SymbolTable() {}

	public static SymbolTable getInstance()
	{
		if (instance == null)
		{
			instance = new SymbolTable();

			// Enter primitive types int, string
			instance.enterWithoutCheck("int",   TypeInt.getInstance());
			instance.enterWithoutCheck("string", TypeString.getInstance());

			// Enter void type
			instance.enterWithoutCheck("void", TypeVoid.getInstance());

			// Enter library function PrintInt
			TypeFunction printIntFunc = new TypeFunction(
				TypeVoid.getInstance(),
				"PrintInt",
				new TypeList(
					TypeInt.getInstance(),
					null));
			printIntFunc.paramVarNames.add("p_10");  // Built-in parameter name
			instance.enterWithoutCheck("PrintInt", printIntFunc);

			// Enter library function PrintString
			TypeFunction printStringFunc = new TypeFunction(
				TypeVoid.getInstance(),
				"PrintString",
				new TypeList(
					TypeString.getInstance(),
					null));
			printStringFunc.paramVarNames.add("p_10");  // Built-in parameter name
			instance.enterWithoutCheck("PrintString", printStringFunc);
			
		}
		return instance;
	}
}
