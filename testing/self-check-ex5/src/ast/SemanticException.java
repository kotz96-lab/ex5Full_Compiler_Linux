package ast;

public class SemanticException extends Exception
{
	private int line;

	public SemanticException(String message)
	{
		super(message);
		this.line = 0;
	}

	public SemanticException(int line, String message)
	{
		super(message);
		// Line numbers from parser are 0-indexed, convert to 1-indexed
		this.line = line + 1;
	}

	public int getLine()
	{
		return line;
	}
}