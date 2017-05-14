package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	TypeName type; public Kind kind;
	
	public void setType(TypeName type)
	{
		this.type = type;
	}
	
	public TypeName getType()
	{
		return type;
	}
	
	public Chain(Token firstToken) {
		super(firstToken);
	}

}
