package ir;

public class IrCommandList
{
	public IrCommand head;
	public IrCommandList tail;

	IrCommandList(IrCommand head, IrCommandList tail)
	{
		this.head = head;
		this.tail = tail;
	}

    public String toString()
    {
        StringBuilder out = new StringBuilder();
        if (head != null)
        {
            out.append(head);
        }

        // Print tail commands
        IrCommandList it = tail;
        while (it != null)
        {
            if (it.head != null)
            {
                out.append(it.head);
            }
            it = it.tail;
        }
        return out.toString();
    }
}
