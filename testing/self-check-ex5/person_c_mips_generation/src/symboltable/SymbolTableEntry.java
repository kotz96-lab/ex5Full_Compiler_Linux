package symboltable;

import types.*;

public class SymbolTableEntry
{
	int index;
	public String name;
	public Type type;
	public SymbolTableEntry prevtop;
	public SymbolTableEntry next;
	public int prevtopIndex;
	
	public SymbolTableEntry(
		String name,
		Type type,
		int index,
		SymbolTableEntry next,
		SymbolTableEntry prevtop,
		int prevtopIndex)
	{
		this.index = index;
		this.name = name;
		this.type = type;
		this.next = next;
		this.prevtop = prevtop;
		this.prevtopIndex = prevtopIndex;
	}
}
