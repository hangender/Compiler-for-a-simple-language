package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Expression extends ASTNode {
	
	TypeName type;
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}
	
	public void setType(TypeName type)
	{
		this.type = type;
	}
	
	public TypeName getType()
	{
		return type;
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
