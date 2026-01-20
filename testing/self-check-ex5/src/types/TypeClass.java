package types;

import symboltable.SymbolTable;

public class TypeClass extends Type
{
	public TypeClass father;
	public TypeList dataMembers;
	
	public TypeClass(TypeClass father, String name, TypeList dataMembers)
	{
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers;
	}

    public boolean isClass(){ return true;}

    // Find a member in the class, traversing the inheritance chain
    public Type find(String memberName)
    {
        // First, try to find the member in current class
        for (TypeList it = dataMembers; it != null; it = it.tail)
        {
            if (it.head != null && memberName.equals(it.head.name))
            {
                return it.head;
            }
        }

        // If not found, recursively search in parent class
        if (father != null)
        {
            return father.find(memberName);
        }

        return null;
    }


    public void enterToSymbolTable()
    {
        // First, enter ancestors (they should be higher in the scopes)
        if (father != null)
        {
            father.enterToSymbolTable();
        }

        // Then enter this class in a scope
        SymbolTable.getInstance().beginScope();
        for (TypeList it = dataMembers; it != null; it = it.tail)
        {
            SymbolTable.getInstance().enterWithoutCheck(it.head.name, it.head); // shouldn't error since it was already validated in declaration
        }
    }

    public void removeFromSymbolTable()
    {
        // Exit scopes for all ancestors
        if (father != null)
        {
            SymbolTable.getInstance().endScope();
        }

        // Then exit from this scope
        SymbolTable.getInstance().endScope();
    }

    public boolean isAncestor(TypeClass possibleAncestor)
    {
        if (father != null)
        {
            if (father.name.equals(possibleAncestor.name)) {
                return true;
            }
            return father.isAncestor(possibleAncestor);
        }

        return false;
    }

    // Get the offset of a field in bytes
    // Fields are laid out in order, 4 bytes each
    public int getFieldOffset(String fieldName)
    {
        int offset = 0;

        // First, count fields from parent class
        if (father != null)
        {
            offset = father.getTotalFieldCount() * 4;
        }

        // Then find the field in this class
        for (TypeList it = dataMembers; it != null; it = it.tail)
        {
            // Skip methods, only count data fields
            if (!(it.head instanceof TypeFunction))
            {
                if (it.head.name.equals(fieldName))
                {
                    return offset;
                }
                offset += 4; // Each field is 4 bytes
            }
        }

        // If not found in this class, search in parent
        if (father != null)
        {
            return father.getFieldOffset(fieldName);
        }

        return 0; // Field not found (shouldn't happen if semantic analysis passed)
    }

    // Get total number of data fields (excluding methods)
    public int getTotalFieldCount()
    {
        int count = 0;

        // Count fields from parent
        if (father != null)
        {
            count = father.getTotalFieldCount();
        }

        // Count fields in this class (excluding methods)
        for (TypeList it = dataMembers; it != null; it = it.tail)
        {
            if (!(it.head instanceof TypeFunction))
            {
                count++;
            }
        }

        return count;
    }
}
