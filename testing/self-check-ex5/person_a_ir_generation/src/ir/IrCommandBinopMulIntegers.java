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
        return String.format("Temp_%d := Temp_%d * Temp_%d",
                dst.getSerialNumber(), t1.getSerialNumber(), t2.getSerialNumber());
    }
}
