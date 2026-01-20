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
		if (src != null) {
			return String.format("%s := Temp_%d", varName, src.getSerialNumber());
		} else {
			return String.format("%s := (null)", varName);
		}
	}
}
