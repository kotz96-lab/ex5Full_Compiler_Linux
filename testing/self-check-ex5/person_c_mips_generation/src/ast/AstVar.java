package ast;

import types.Type;

public abstract class AstVar extends AstNode
{
    /***********************************************/
    /* The default semantic action for an AST node */
    /***********************************************/
    public Type semantMe() throws SemanticException
    {
        return null;
    }
}
