package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public class IdentLValue extends ASTNode {
	
	public int scopeLevel;
	
	public IdentLValue(Token firstToken) {
		super(firstToken);
	}
	
	TypeName type; public Dec dec;
	
	public void setType(TypeName type)
	{
		this.type = type;
	}
	
	public TypeName getType()
	{
		return type;
	}
	
	@Override
	public String toString() {
		return "IdentLValue [firstToken=" + firstToken + "]";
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentLValue(this,arg);
	}

	public String getText() {
		return firstToken.getText();
	}

}
