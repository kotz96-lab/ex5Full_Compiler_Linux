package ir;

public class IrCommandJumpLabel extends IrCommand
{
	public String labelName;

	public IrCommandJumpLabel(String labelName)
	{
		this.labelName = labelName;
	}

	public String toString()
	{
		return String.format("Jump %s", labelName);
	}
}
