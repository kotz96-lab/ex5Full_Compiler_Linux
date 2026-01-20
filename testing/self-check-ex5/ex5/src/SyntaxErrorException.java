public class SyntaxErrorException extends Exception
{
	private int line;

	public SyntaxErrorException(int line)
	{
		super("Syntax error at line " + line);
		this.line = line;
	}

	public int getLine()
	{
		return line;
	}
}