package ir;

import temp.*;

public class IrCommandJumpIfEqToZero extends IrCommand
{
	public Temp t;
	public String labelName;

	public IrCommandJumpIfEqToZero(Temp t, String labelName)
	{
		this.t          = t;
		this.labelName = labelName;
	}

	public String toString()
	{
		return String.format("JumpIfEqToZero Temp_%d %s", t.getSerialNumber(), labelName);
	}
}
