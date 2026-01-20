package ast;


public abstract class AstStmt extends AstNode
{
    /***********************************************/
    /* The default semantic action for an AST node */
    /***********************************************/
    public void semantMe() throws SemanticException
    {
    }
}
