package types;

public class TypeNill extends Type
{
	// Singleton implementation
	private static TypeNill instance = null;

	protected TypeNill() {}

	public static TypeNill getInstance()
	{
		if (instance == null)
		{
			instance = new TypeNill();
            instance.name = "nill";
		}
		return instance;
	}
}
