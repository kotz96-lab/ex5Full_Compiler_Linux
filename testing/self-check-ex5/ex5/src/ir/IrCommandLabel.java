package ir;

public class IrCommandLabel extends IrCommand
{
	public String labelName;

	public IrCommandLabel(String labelName)
	{
		this.labelName = labelName;
	}

	public String toString()
	{
		return String.format("%s:", labelName);
	}
}
