package ir;

import temp.*;

public class IrCommandBinopMulIntegers extends IrCommand
{
	public Temp t1;
	public Temp t2;
	public Temp dst;
	
	public IrCommandBinopMulIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}

    public String toString()
    {
		String dstStr = (dst != null) ? "Temp_" + dst.getSerialNumber() : "(null)";
		String t1Str = (t1 != null) ? "Temp_" + t1.getSerialNumber() : "(null)";
		String t2Str = (t2 != null) ? "Temp_" + t2.getSerialNumber() : "(null)";
        return String.format("%s := %s * %s", dstStr, t1Str, t2Str);
    }
}
