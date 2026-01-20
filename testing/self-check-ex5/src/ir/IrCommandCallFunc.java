package ir;

import temp.*;

public class IrCommandCallFunc extends IrCommand
{
    public String name;
	public Temp t;

	public IrCommandCallFunc(String name, Temp t)
	{
        this.name = name;
		this.t = t;
	}

	public String toString()
	{
		if (t != null) {
			return String.format("Temp_%d := %s()", t.getSerialNumber(), name);
		} else {
			return String.format("%s()", name);
		}
	}
}
