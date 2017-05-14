package cop5556sp17;
import cop5556sp17.AST.*;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p1 = null;
		p1 = program();
		matchEOF();
		//System.out.println("Parse done, EOF matched");
		return p1;
	}

	Expression expression() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		Token firstToken = t, opToken; Expression e1 = null, e2 = null;
		e1 = term();
		while (predictSetMembershipTest(Kind.LT, Kind.LE, Kind.GT, Kind.GE, Kind.EQUAL, Kind.NOTEQUAL)) //predict set for relOp
		{
			
			opToken = consume(); //consume relOp
			
			e2 = term();
			e1 = new BinaryExpression(firstToken, e1, opToken, e2);
			
		}
		return e1;
	}

	Expression term() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		Token firstToken = t, opToken; Expression e1 = null, e2 = null;
		e1 = elem();
		while (predictSetMembershipTest(Kind.PLUS, Kind.MINUS, Kind.OR)) //predict set for weakOp
		{
			
			opToken = consume(); //consume weakOp
			
			e2 = elem();
			e1 = new BinaryExpression(firstToken, e1, opToken, e2);
			
		}
		return e1;
	}

	Expression elem() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		Token firstToken = t, opToken; Expression e1 = null, e2 = null;
		e1 = factor();
		while (predictSetMembershipTest(Kind.TIMES, Kind.DIV, Kind.AND, Kind.MOD)) //predict set for strongOp
		{
			
			opToken = consume(); //consume strongOp

			e2 = factor();
			e1 = new BinaryExpression(firstToken, e1, opToken, e2);
			
		}
		return e1;
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		
		Expression e = null;
		Token temp;
		
		switch (kind) {
		case IDENT: {
			//System.out.println("factor -> IDENT case");
			temp = consume();
			e = new IdentExpression(temp);
		}
			break;
		case INT_LIT: {
			//System.out.println("factor -> INT_LIT case");
			temp = consume();
			e = new IntLitExpression(temp);
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			//System.out.println("factor -> KW_TRUE/KW_FALSE case");
			temp = consume();
			e = new BooleanLitExpression(temp);
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			temp = consume();
			e = new ConstantExpression(temp);
		}
			break;
		case LPAREN: {
			
			consume();
			e = expression();
			match(RPAREN);

		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor: " + kind + " @" + t.getLinePos());
		}
		return e;
	}

	Block block() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		//System.out.println("block()->");
		ArrayList<Dec> decList = new ArrayList<Dec>(); Dec d1 = null;
		ArrayList<Statement> statementList = new ArrayList<Statement>(); Statement s1 = null;
		Token firstToken;
		
		firstToken = match(Kind.LBRACE);
		while (predictSetMembershipTest(Kind.KW_INTEGER, Kind.KW_BOOLEAN, Kind.KW_IMAGE, Kind.KW_FRAME) || predictSetMembershipTest(OP_SLEEP, KW_WHILE, KW_IF, IDENT, OP_BLUR, OP_GRAY, OP_CONVOLVE, KW_SHOW, KW_HIDE, KW_MOVE, KW_XLOC, KW_YLOC, OP_WIDTH, OP_HEIGHT, KW_SCALE)) //possible typo here
		{
			if (predictSetMembershipTest(Kind.KW_INTEGER, Kind.KW_BOOLEAN, Kind.KW_IMAGE, Kind.KW_FRAME)) //dec
			{
				//System.out.println("block()->dec()->");
				d1 = dec();
				decList.add(d1);
			}
			else {
				//System.out.println("block()->statement()");
				s1 = statement();
				statementList.add(s1);
			}
		}
		match(Kind.RBRACE);
		return (new Block(firstToken, decList, statementList));
	}

	Program program() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		if (t.kind == Kind.IDENT)
		{
			Token firstToken;
			ArrayList<ParamDec> params = new ArrayList<ParamDec>(); ParamDec p1 = null;
			Block b1 = null;
			
			//System.out.println("program() -> Kind.IDENT");
			firstToken = consume(); //consumes the IDENT
			if (t.isKind(Kind.LBRACE))
			{
				//program::=IDENT block
				b1 = block();
			}
			else if (predictSetMembershipTest(Kind.KW_URL, Kind.KW_FILE, Kind.KW_INTEGER, Kind.KW_BOOLEAN))
			{
				p1 = paramDec();
				params.add(p1);
				while (predictSetMembershipTest(Kind.COMMA))
				{
					consume();
					p1 = paramDec();
					params.add(p1);
				}
				b1 = block();
			}
			else
			{
				throw new SyntaxException("Unexpected token(" + t.kind + ") |program() -> IDENT| @ " + t.getLinePos());
			}
			return (new Program(firstToken, params, b1));
		}
		/*
		else if (t.kind == Kind.EOF) //if the input is just an empty string, return. Matching EOF is done after program() returns.
		{
			//System.out.println("program() -> Kind.EOF");
			return;
		}
		*/
		else
		{
			throw new SyntaxException("Unexpected token(" + t.kind + ") |program()| @ " + t.getLinePos());
		}
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		if (predictSetMembershipTest(Kind.KW_URL, Kind.KW_FILE, Kind.KW_INTEGER, Kind.KW_BOOLEAN))
		{
			Token firstToken, ident;
			firstToken = consume();
			ident = match(Kind.IDENT);
			return (new ParamDec(firstToken, ident)); //type ident
		}
		else
		{
			throw new SyntaxException("Unexpected token(" + t.kind + ") |paramDec()| @ " + t.getLinePos());
		}
	}

	Dec dec() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		//System.out.println("dec()->");
		if (predictSetMembershipTest(Kind.KW_INTEGER, Kind.KW_BOOLEAN, Kind.KW_IMAGE, Kind.KW_FRAME))
		{
			Token firstToken, ident;
			firstToken = consume();
			ident = match(Kind.IDENT);
			Dec e = new Dec(firstToken, ident); //type ident
			return e;
		}
		else
		{
			throw new SyntaxException("Unexpected token(" + t.kind + ") |dec()| @ " + t.getLinePos());
		}
	}

	Statement statement() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		//System.out.println("statement()->");
		Token firstToken;
		
		if (predictSetMembershipTest(Kind.OP_SLEEP))
		{
			firstToken = consume();
			Expression e1 = null;
			e1 = expression();
			match(Kind.SEMI);
			return (new SleepStatement(firstToken, e1));
		}
		else if (predictSetMembershipTest(Kind.KW_WHILE))
		{
			WhileStatement e1 = null;
			e1 = whileStatement();
			return e1;
		}
		else if (predictSetMembershipTest(Kind.KW_IF))
		{
			IfStatement e1 = null;
			e1 = ifStatement();
			return e1;
		}
		else if (predictSetMembershipTest(Kind.IDENT) && ((scanner.peek()).isKind(Kind.ASSIGN))) //assign(). Careful of scanner.peek(). Might result in out of bounds
		{
			AssignmentStatement e1 = null;
			e1 = assign();
			match(Kind.SEMI);
			return e1;
		}
		else if (predictSetMembershipTest(Kind.IDENT,OP_BLUR, OP_GRAY, OP_CONVOLVE, KW_SHOW, KW_HIDE, KW_MOVE, KW_XLOC, KW_YLOC, OP_WIDTH, OP_HEIGHT, KW_SCALE))
		{
			//System.out.println("statement()->chain() "+t.kind);
			Chain binChain = null;
			binChain = chain();
			match(Kind.SEMI);
			return binChain;
		}
		else
		{
			throw new SyntaxException("Unexpected token(" + t.kind + ") |statement()| @ " + t.getLinePos());
		}
	}

	AssignmentStatement assign() throws SyntaxException {
		//System.out.println("assign()->");
		Token firstToken; IdentLValue lValue = null; Expression e1 = null;
		
		firstToken = match(Kind.IDENT);
		lValue = new IdentLValue(firstToken); //IdentLValue is also first token?
		match(Kind.ASSIGN);
		//System.out.println("assign() -> going into expression()");
		e1 = expression();
		return (new AssignmentStatement(firstToken, lValue, e1));
	}
	
	WhileStatement whileStatement() throws SyntaxException {
		Token firstToken; Expression e1 = null; Block b1 = null;
		
		firstToken = match(Kind.KW_WHILE);
		match(Kind.LPAREN);
		e1 = expression();
		match(Kind.RPAREN);
		b1 = block();
		return (new WhileStatement(firstToken, e1, b1));
	}
	
	IfStatement ifStatement() throws SyntaxException {
		Token firstToken; Expression e1 = null; Block b1 = null;
		
		firstToken = match(Kind.KW_IF);
		match(Kind.LPAREN);
		e1 = expression();
		match(Kind.RPAREN);
		b1 = block();
		return (new IfStatement(firstToken, e1, b1));
	}
	
	Chain chain() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		//System.out.println("chain()->");
		BinaryChain e1 = null; ChainElem c1 = null, c2 = null; Token op;
		
		Token firstToken = t;
		
		c1 = chainElem();
		op = arrowOp();
		c2 = chainElem();
		
		e1 = new BinaryChain(firstToken, c1, op, c2);
		
		while (predictSetMembershipTest(Kind.ARROW, Kind.BARARROW))
		{
			firstToken = t;
			op = consume();

			c2 = chainElem();
			e1 = new BinaryChain(firstToken, e1, op, c2);

			//System.out.println("chain()->inside while loop, returned from chainElem");
		}
		return e1;
		//System.out.println("chain()->while loop exited, chain() returning");
	}

	ChainElem chainElem() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		//System.out.println("chainElem()->");
		Token firstToken = t; Tuple t1 = null;
		if (predictSetMembershipTest(Kind.IDENT))
		{
			firstToken = consume();
			return (new IdentChain(firstToken));
		}
		else if(predictSetMembershipTest(Kind.OP_BLUR, Kind.OP_GRAY, Kind.OP_CONVOLVE)) //filterOp
		{
			firstToken = consume();
			t1 = arg();
			return (new FilterOpChain(firstToken, t1));
		}
		else if(predictSetMembershipTest(Kind.KW_SHOW, Kind.KW_HIDE, Kind.KW_MOVE, Kind.KW_XLOC, Kind.KW_YLOC)) //frameOp
		{
			firstToken = consume();
			t1 = arg();
			return (new FrameOpChain(firstToken, t1));
		}
		else if(predictSetMembershipTest(Kind.OP_WIDTH, Kind.OP_HEIGHT, Kind.KW_SCALE)) //imageOp
		{
			firstToken = consume();
			t1 = arg();
			return (new ImageOpChain(firstToken, t1));
		}
		else
		{
			throw new SyntaxException("Unexpected token(" + t.kind + ") |chainElem()| @ " + t.getLinePos());
		}
	}

	Tuple arg() throws SyntaxException {
		//TODO
		//throw new UnimplementedFeatureException();
		Token firstToken = t; Expression e1 = null; Tuple t1 = null;
		List<Expression> exprList = new ArrayList<Expression>();
		
		if (predictSetMembershipTest(Kind.LPAREN)) //(expression (,expression)*)
		{
			firstToken = consume(); //consumes (
			e1 = expression();
			exprList.add(e1);
			while (predictSetMembershipTest(Kind.COMMA))
			{
				consume(); //consumes ,
				e1 = expression();
				exprList.add(e1);
			}
			match(Kind.RPAREN); //attempt to match )
			t1 = new Tuple(firstToken, exprList);
			return t1;
		}
		else if (predictSetMembershipTest(Kind.ARROW, Kind.BARARROW, Kind.SEMI)) //generated by the empty set
		{
			t1 = new Tuple(firstToken, exprList); //null and empty list
			return t1;
		}
		else
		{
			throw new SyntaxException("Unexpected token(" + t.kind + ") |arg()| @ " + t.getLinePos());
		}
	}

//fncs not used----------------------------------------------------------------------------
	Token strongOp() throws SyntaxException
	{
		return match(Kind.TIMES, Kind.DIV, Kind.AND, Kind.MOD);
	}
	
	Token weakOp() throws SyntaxException
	{
		return match(Kind.PLUS, Kind.MINUS, Kind.OR);
	}
	
	Token relOp() throws SyntaxException
	{
		return match(Kind.LT, Kind.LE, Kind.GT, Kind.GE, Kind.EQUAL, Kind.NOTEQUAL);
	}
	
	Token imageOp() throws SyntaxException
	{
		return match(Kind.OP_WIDTH, Kind.OP_HEIGHT, Kind.KW_SCALE);
	}
	
	Token frameOp() throws SyntaxException
	{
		return match(Kind.KW_SHOW, Kind.KW_HIDE, Kind.KW_MOVE, Kind.KW_XLOC, Kind.KW_YLOC);
	}
	
	Token filterOp() throws SyntaxException
	{
		return match(Kind.OP_BLUR, Kind.OP_GRAY, Kind.OP_CONVOLVE);
	}
	
	Token arrowOp() throws SyntaxException
	{
		return match(Kind.ARROW, Kind.BARARROW);
	}
//-------------------------------------------------------------------------------------------
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + ", expected " + kind + " " + t.getLinePos());
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		String errMessageAppend = "";
		for (Kind someKind : kinds) //iterate through list of kinds passed in
		{
			if (t.kind == someKind)
			{
				//System.out.println("match(<List>Kinds) -> " + t.kind);
				return consume(); //replace this statement
			}
			errMessageAppend = errMessageAppend + " " + someKind;
		}
		throw new SyntaxException("Unexpected token(" + t.kind + ")," + " expected(" + errMessageAppend + " ) @" + t.getLinePos());
	}
	
	private boolean predictSetMembershipTest(Kind... kinds) {
		// test if the token is a member of the predict set
		// CAREFUL. This method does not throw SyntaxException. It is up to the caller to check
		
		for (Kind someKind : kinds) //iterate through list of kinds passed in
		{
			if (t.kind == someKind)
			{
				//System.out.println("predictSetMembershipTest -> " + t.kind);
				return true; //found the token in some predict set
			}
		}
		return false;
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		//System.out.println("next token up for consumption: " + t.kind);
		return tmp;
	}

}
