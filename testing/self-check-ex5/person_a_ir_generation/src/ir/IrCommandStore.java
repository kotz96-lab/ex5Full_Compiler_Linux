package ir;

import temp.*;

public class IrCommandStore extends IrCommand
{
	public String varName;
	public Temp src;

	public IrCommandStore(String varName, Temp src)
	{
		this.src      = src;
		this.varName = varName;
	}

	public String toString()
	{
		return String.format("%s := Temp_%d", varName, src.getSerialNumber());
	}
}
