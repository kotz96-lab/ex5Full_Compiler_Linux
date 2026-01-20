package ir;

public class IrCommandAllocate extends IrCommand
{
	public String varName;

	public IrCommandAllocate(String varName)
	{
		this.varName = varName;
	}

	public String toString()
	{
		return String.format("Allocate %s", varName);
	}
}
