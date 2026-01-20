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
		return String.format("%s(Temp_%d)", name, t.getSerialNumber());
	}
}
