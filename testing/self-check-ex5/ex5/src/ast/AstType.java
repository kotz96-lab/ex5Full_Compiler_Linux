package ast;

public class AstType extends AstNode
{
    public static final int INT = 0;
    public static final int STRING = 1;
    public static final int VOID = 2;
    public static final int ID = 3;
    public String name;
    public int type;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstType(int type, String value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		if(type == INT) {System.out.print("====================== type -> TYPE_INT\n");}
        if(type == STRING) {System.out.print("====================== type -> TYPE_STRING\n");}
        if(type == VOID) {System.out.print("====================== type -> TYPE_VOID\n");}
        if(type == ID) {System.out.print("====================== type -> ID\n");}

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
        this.type = type;
		this.name = switch(type) {
            case ID -> value;
            case STRING -> "string";
            case VOID -> "void";
            case INT -> "int";
            default -> "UNKNOWN_TYPE";
        };
	}
	
	/*************************************************/
	/* The printing message for a simple type AST node */
	/*************************************************/
	public void printMe()
	{
		String stype="";
		
		/*********************************/
		/* CONVERT op to a printable type */
		/*********************************/
        if(type == INT) { stype = "TYPE_INT"; }
        if(type == STRING) { stype = "TYPE_STRING"; }
        if(type == VOID) { stype = "TYPE_VOID"; }
        if(type == ID) { stype =  String.format("ID(%s)", name); }

		/*************************************/
		/* AST NODE TYPE = AST TYPE_X */
		/*************************************/
		System.out.format("AST NODE %s\n", stype);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
        AstGraphviz.getInstance().logNode(serialNumber, String.format("%s",stype));
	}
}
