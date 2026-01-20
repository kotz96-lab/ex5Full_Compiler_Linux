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
		String dstStr = (dst != null) ? "Temp_" + dst.getSerialNumber() : "(null)";
		String tStr = (t != null) ? "Temp_" + t.getSerialNumber() : "(null)";
		return String.format("%s := -%s", dstStr, tStr);
	}
}
