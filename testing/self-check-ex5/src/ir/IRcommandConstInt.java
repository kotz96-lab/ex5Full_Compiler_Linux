package ir;

import temp.*;

public class IRcommandConstInt extends IrCommand
{
	public Temp t;
	public int value;

	public IRcommandConstInt(Temp t, int value)
	{
		this.t = t;
		this.value = value;
	}

	public String toString()
	{
		return String.format("Temp_%d := %d", t.getSerialNumber(), value);
	}
}
