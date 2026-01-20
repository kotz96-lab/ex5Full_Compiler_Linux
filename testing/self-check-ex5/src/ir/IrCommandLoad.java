package ir;

import temp.*;

public class IrCommandLoad extends IrCommand
{
	public Temp dst;
	public String varName;

	public IrCommandLoad(Temp dst, String varName)
	{
		this.dst      = dst;
		this.varName = varName;
	}

	public String toString()
	{
		if (dst != null) {
			return String.format("Temp_%d := %s", dst.getSerialNumber(), varName);
		} else {
			return String.format("(null) := %s", varName);
		}
	}
}
