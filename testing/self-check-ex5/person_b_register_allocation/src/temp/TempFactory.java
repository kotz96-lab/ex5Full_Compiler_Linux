package temp;

public class TempFactory
{
	private int counter=0;
	
	public Temp getFreshTemp()
	{
		return new Temp(counter++);
	}
	// Singleton implementation
	private static TempFactory instance = null;

	protected TempFactory() {}

	public static TempFactory getInstance()
	{
		if (instance == null)
		{
			instance = new TempFactory();
		}
		return instance;
	}
}
