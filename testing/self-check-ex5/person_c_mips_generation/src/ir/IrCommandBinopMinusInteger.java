package ir;

import temp.Temp;

public class IrCommandBinopMinusInteger extends IrCommand
{
	public Temp t;
	public Temp dst;

	public IrCommandBinopMinusInteger(Temp dst, Temp t)
	{
		this.dst = dst;
		this.t = t;
	}

	public String toString()
	{
		return String.format("Temp_%d := -Temp_%d",
			dst.getSerialNumber(), t.getSerialNumber());
	}
}
