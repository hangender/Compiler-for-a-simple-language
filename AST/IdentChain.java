package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IdentChain extends ChainElem {
	
	public int scopeLevel; public Dec dec; public boolean haveBeenInit;

	public IdentChain(Token firstToken) {
		super(firstToken);
		haveBeenInit = false;
	}


	@Override
	public String toString() {
		return "IdentChain [firstToken=" + firstToken + "]";
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentChain(this, arg);
	}

}
