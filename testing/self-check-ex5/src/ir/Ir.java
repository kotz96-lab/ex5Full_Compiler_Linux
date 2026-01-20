package ir;

import java.util.ArrayList;
import java.util.List;

public class Ir
{
	private IrCommand head=null;
	private IrCommandList tail=null;

	// Add IR command
	public void AddIrCommand(IrCommand cmd)
	{
		if ((head == null) && (tail == null))
		{
			this.head = cmd;
		}
		else if ((head != null) && (tail == null))
		{
			this.tail = new IrCommandList(cmd,null);
		}
		else
		{
			IrCommandList it = tail;
			while ((it != null) && (it.tail != null))
			{
				it = it.tail;
			}
			it.tail = new IrCommandList(cmd,null);
		}
	}

	// Print IR to standard output
	public void printIR()
	{
		System.out.println("\n==================== IR CODE ====================");

		// Print head command
		if (head != null)
		{
			System.out.println(head);
		}

		// Print tail commands
		IrCommandList it = tail;
		while (it != null)
		{
			if (it.head != null)
			{
				System.out.println(it.head);
			}
			it = it.tail;
		}

		System.out.println("=================================================\n");
	}

	/**
	 * Get all IR commands as a list
	 * (Added for Person B - Register Allocation)
	 */
	public List<IrCommand> getCommands()
	{
		List<IrCommand> commands = new ArrayList<>();

		// Add head command
		if (head != null)
		{
			commands.add(head);
		}

		// Add tail commands
		IrCommandList it = tail;
		while (it != null)
		{
			if (it.head != null)
			{
				commands.add(it.head);
			}
			it = it.tail;
		}

		return commands;
	}

	// Singleton implementation
	private static Ir instance = null;

	protected Ir() {}

	public static Ir getInstance()
	{
		if (instance == null)
		{
			instance = new Ir();
		}
		return instance;
	}
}
