package ast;

import temp.Temp;
import types.*;

public class AstExpVar extends AstExp
{
    public AstVar value;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstExpVar(AstVar value)
    {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        System.out.print("====================== exp -> var\n");
        this.value = value;
    }

    /******************************************************/
    /* The printing message for a VAR EXP AST node */
    /******************************************************/
    public void printMe()
    {
        /*******************************/
        /* AST NODE TYPE = AST VAR EXP */
        /*******************************/
        System.out.print("AST NODE EXP VAR\n");
        value.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "VAR\n");
        AstGraphviz.getInstance().logEdge(serialNumber,value.serialNumber);
    }

    public Type semantMe() throws SemanticException
    {
        return value.semantMe();
    }

    public Temp irMe()
    {
        return value.irMe();
    }
}
