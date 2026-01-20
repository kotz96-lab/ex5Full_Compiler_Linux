package types;

public class TypeString extends Type
{
	// Singleton implementation
	private static TypeString instance = null;

	protected TypeString() {}

	public static TypeString getInstance()
	{
		if (instance == null)
		{
			instance = new TypeString();
			instance.name = "string";
		}
		return instance;
	}
}
