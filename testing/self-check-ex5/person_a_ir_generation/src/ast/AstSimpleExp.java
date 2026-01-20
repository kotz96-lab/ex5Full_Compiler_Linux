package ast;

import types.Type;

public abstract class AstSimpleExp extends AstNode
{
    /***********************************************/
    /* The default semantic action for an AST node */
    /***********************************************/
    public Type semantMe()
    {
        return null;
    }
}