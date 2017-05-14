package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitBinaryChain ASTNode");
		Chain e0 = binaryChain.getE0();
		Token arrow = binaryChain.getArrow();
		ChainElem e1 = binaryChain.getE1();
		e0.visit(this, arg); e1.visit(this, arg);
		
		Token instanceOf = e1.getFirstToken();
		boolean isFilterOp = e1 instanceof FilterOpChain;
		boolean isFrameOp = e1 instanceof FrameOpChain;
		boolean isImageOp = e1 instanceof ImageOpChain;
		
		if (e0.getType() == TypeName.URL && predictSetMembershipTest(arrow, Kind.ARROW) && e1.getType() == TypeName.IMAGE)
		{
			//System.out.println("URL -> IMAGE");
			binaryChain.setType(IMAGE);
		}
		else if (e0.getType() == TypeName.FILE && predictSetMembershipTest(arrow, Kind.ARROW) && e1.getType() == TypeName.IMAGE)
		{
			//System.out.println("FILE -> IMAGE");
			binaryChain.setType(IMAGE);
		}
		else if (e0.getType() == TypeName.FRAME && predictSetMembershipTest(arrow, Kind.ARROW) && isFrameOp && predictSetMembershipTest(instanceOf, KW_XLOC, KW_YLOC))
		{
			//System.out.println("FRAME -> XLOC, YLOC");
			binaryChain.setType(INTEGER);
		}
		else if (e0.getType() == TypeName.FRAME && predictSetMembershipTest(arrow, Kind.ARROW) && isFrameOp && predictSetMembershipTest(instanceOf, KW_SHOW, KW_HIDE, KW_MOVE))
		{
			//System.out.println("FRAME -> SHOW, HIDE, MOVE");
			binaryChain.setType(FRAME);
		}
		else if (e0.getType() == TypeName.IMAGE && predictSetMembershipTest(arrow, Kind.ARROW) && isImageOp && predictSetMembershipTest(instanceOf, OP_WIDTH, OP_HEIGHT))
		{
			//System.out.println("IMAGE -> WIDTH, HEIGHT");
			binaryChain.setType(INTEGER);
		}
		else if (e0.getType() == TypeName.IMAGE && predictSetMembershipTest(arrow, Kind.ARROW) && e1.getType() == TypeName.FRAME)
		{
			//System.out.println("IMAGE -> FRAME");
			binaryChain.setType(FRAME);
		}
		else if (e0.getType() == TypeName.IMAGE && predictSetMembershipTest(arrow, Kind.ARROW) && e1.getType() == TypeName.FILE)
		{
			//System.out.println("IMAGE -> FILE");
			binaryChain.setType(NONE);
		}
		else if (e0.getType() == TypeName.IMAGE && predictSetMembershipTest(arrow, Kind.ARROW, Kind.BARARROW) && isFilterOp && predictSetMembershipTest(instanceOf, OP_GRAY, OP_BLUR, OP_CONVOLVE))
		{
			//System.out.println("IMAGE -> & |-> BLUR, GRAY, CONVOLVE");
			binaryChain.setType(IMAGE);
		}
		else if (e0.getType() == TypeName.IMAGE && predictSetMembershipTest(arrow, Kind.ARROW) && isImageOp && predictSetMembershipTest(instanceOf, KW_SCALE))
		{
			//System.out.println("IMAGE -> SCALE");
			binaryChain.setType(IMAGE);
		}
		else if (e0.getType() == TypeName.IMAGE && predictSetMembershipTest(arrow, Kind.ARROW) && e1 instanceof IdentChain && e1.getType() == TypeName.IMAGE)
		{
			//System.out.println("IMAGE -> instance IdentChain");
			binaryChain.setType(IMAGE);
			//System.out.println("Image->Url");
		}
		else if (e0.getType() == TypeName.INTEGER && predictSetMembershipTest(arrow, Kind.ARROW) && e1 instanceof IdentChain && e1.getType() == TypeName.INTEGER)
		{
			//System.out.println("INT -> INT");
			binaryChain.setType(INTEGER);
		}
		else
		{
			throw new TypeCheckException("illegal binaryChain combination: " + binaryChain);
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitBinaryExpression ASTNode");
		Expression e0 = binaryExpression.getE0();
		Token op = binaryExpression.getOp();
		Expression e1 = binaryExpression.getE1();
		
		e0.visit(this, arg); e1.visit(this, arg);
		//System.out.println(e0.getType() + " " + e1.getType());
		if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && (op.isKind(Kind.MOD))) //mod
		{
			binaryExpression.setType(INTEGER);
		}
		else if (e0.getType() == TypeName.BOOLEAN && e1.getType() == TypeName.BOOLEAN && predictSetMembershipTest(op, Kind.OR, Kind.AND)) //and, or not implemented for integers
		{
			binaryExpression.setType(BOOLEAN);
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && (op.isKind(Kind.PLUS) || op.isKind(Kind.MINUS)))
		{
			binaryExpression.setType(INTEGER);
		}
		else if (e0.getType() == TypeName.IMAGE && e1.getType() == TypeName.IMAGE && (op.isKind(Kind.PLUS) || op.isKind(Kind.MINUS)))
		{
			binaryExpression.setType(IMAGE);
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && (op.isKind(Kind.TIMES) || op.isKind(Kind.DIV)))
		{
			binaryExpression.setType(INTEGER);
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && predictSetMembershipTest(op, Kind.LT, Kind.GT, Kind.LE, Kind.GE))
		{
			binaryExpression.setType(BOOLEAN);
		}
		else if (e0.getType() == TypeName.BOOLEAN && e1.getType() == TypeName.BOOLEAN && predictSetMembershipTest(op, Kind.LT, Kind.GT, Kind.LE, Kind.GE))
		{
			binaryExpression.setType(BOOLEAN);
		}
		else if (e0.getType() == TypeName.IMAGE && e1.getType() == TypeName.INTEGER && predictSetMembershipTest(op, Kind.TIMES, Kind.DIV, Kind.MOD))
		{
			binaryExpression.setType(IMAGE);
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.IMAGE && predictSetMembershipTest(op, Kind.TIMES))
		{
			binaryExpression.setType(IMAGE);
		}
		else if (predictSetMembershipTest(op, Kind.NOTEQUAL, Kind.EQUAL))
		{
			if (e0.getType() != e1.getType())
			{
				throw new TypeCheckException("mismatch of types using == or !=: " + binaryExpression);
			}
			else
			{
				binaryExpression.setType(BOOLEAN);
			}
		}
		else
		{
			throw new TypeCheckException("illegal binaryExpression combination: " + binaryExpression);
		}
		//e0.getType(); e1.getType();
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visiting Block ASTNode");
		symtab.enterScope();
		ArrayList<Dec> decList = block.getDecs();
		ArrayList<Statement> statementList = block.getStatements();
		
		for (Dec i : decList) //for each item in decList
		{
			i.visit(this, arg);
			//System.out.println(i.getTypeName());
			String ident = i.getIdent().getText(); //token->getText
			//System.out.println(ident);
			symtab.insert(ident, i);
		}
		
		//System.out.println("after visiting Block ASTNode: " + symtab);
		//System.out.println("after visiting Block ASTNode: " + symtab.printStack());
		
		for (Statement i : statementList) //for each item in statementList
		{
			i.visit(this, arg);
		}
		symtab.leaveScope();
		//System.out.println("after visiting program ASTNode: " + symtab.printStack());
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitBooleanLitExpression ASTNode");
		booleanLitExpression.setType(TypeName.BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tuple = filterOpChain.getArg();
		filterOpChain.kind = filterOpChain.getFirstToken().kind;
		
		int size = tuple.getExprList().size(); //tuple len must be 0
		if (size > 0)
		{
			throw new TypeCheckException("args of FilterOp greater than 0: " + filterOpChain);
		}
		filterOpChain.setType(IMAGE);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tuple = frameOpChain.getArg();
		
		frameOpChain.kind = frameOpChain.getFirstToken().kind;
		
		if (predictSetMembershipTest(frameOpChain.getFirstToken(), Kind.KW_SHOW, Kind.KW_HIDE)) 
		{
			//condition: Tuple.length == 0
			//FrameOpChain.type <- NONE
			int size = tuple.getExprList().size(); //tuple len must be 0
			if (size > 0)
			{
				throw new TypeCheckException("args of FrameOp greater than 0: " + frameOpChain);
			}
			frameOpChain.setType(NONE);
		}
		else if (predictSetMembershipTest(frameOpChain.getFirstToken(), Kind.KW_XLOC, Kind.KW_YLOC))
		{
			//condition: Tuple.length == 0
			//FrameOpChain.type <- INTEGER
			int size = tuple.getExprList().size(); //tuple len must be 0
			if (size > 0)
			{
				throw new TypeCheckException("args of FrameOp greater than 0: " + frameOpChain);
			}
			frameOpChain.setType(INTEGER);
		}
		else if(predictSetMembershipTest(frameOpChain.getFirstToken(), Kind.KW_MOVE)){
			//condition: Tuple.length == 2
			//FrameOpChain.type <- NONE
			int size = tuple.getExprList().size(); //tuple len must be 2
			if (size != 2)
			{
				throw new TypeCheckException("args of FrameOp not 2: " + frameOpChain);
			}
			frameOpChain.setType(NONE);
		}
		else //there is a bug in your parser
		{
			throw new TypeCheckException("Bugs in parser?");
		}
		tuple.visit(this, arg);
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitIdentChain ASTNode");
		Dec dec = symtab.lookupExtended(identChain.getFirstToken().getText(), identChain.getFirstToken().pos);
		if (dec != null) //visible in scope and declared BEFORE assignment
		{
			identChain.dec = dec;
			identChain.setType(Type.getTypeName(dec.getType()));
			identChain.scopeLevel = symtab.lookupExtendedScope(identChain.getFirstToken().getText(), identChain.getFirstToken().pos);
		}
		else
		{
			throw new TypeCheckException("IdentChain have not been declared: " + identChain);
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitIdentExpression ASTNode");
		//System.out.println(identExpression.getFirstToken().getText());
		Dec dec = symtab.lookupExtended(identExpression.getFirstToken().getText(), identExpression.getFirstToken().pos);
		//System.out.println("Ident Expression: " + identExpression.getFirstToken().getText() + " " + symtab.lookupScope(identExpression.getFirstToken().getText()) + " " + symtab.lookupPreviousScope(identExpression.getFirstToken().getText()));
		//System.out.println("null2?");
		if (dec != null) //visible in scope and declared BEFORE assignment
		{
			identExpression.dec = dec;
			identExpression.setType(Type.getTypeName(dec.getType()));
			identExpression.scopeLevel = symtab.lookupExtendedScope(identExpression.getFirstToken().getText(), identExpression.getFirstToken().pos);
		}
		else
		{
			throw new TypeCheckException("IdentExpression have not been declared: " + identExpression);
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitIfStatement ASTNode");
		
		Expression e = ifStatement.getE();
		Block b = ifStatement.getB();
		e.visit(this, arg);
		
		if (e.getType() != BOOLEAN)
		{
			throw new TypeCheckException("If condition not boolean: " + e);
		}
		b.visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitIntLitExpression ASTNode");
		intLitExpression.setType(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitSleepStatement ASTNode");
		Expression e = sleepStatement.getE();
		e.visit(this,arg);
		if (e.getType() != TypeName.INTEGER)
		{
			throw new TypeCheckException("Sleep statement needs an expression, but we have: " + "(" + e.getType() + ") " + e);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitWhileStatement ASTNode");
		
		Expression e = whileStatement.getE();
		Block b = whileStatement.getB();
		e.visit(this, arg);
		
		if (e.getType() != BOOLEAN)
		{
			throw new TypeCheckException("While condition not boolean: " + e);
		}
		b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		declaration.setTypeName(Type.getTypeName(declaration.getFirstToken()));
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception { //visit first comes here!
		// TODO Auto-generated method stub
		//System.out.println("visiting program ASTNode");
		ArrayList<ParamDec> params = program.getParams();
		int size = params.size();
		for (int i = 0; i<size; i++)
		{
			params.get(i).visit(this, arg);
			//System.out.println(params.get(i).getTypeName());
			String ident = params.get(i).getIdent().getText(); //token->getText
			//System.out.println(ident);
			symtab.insert(ident, params.get(i));
		}
		//System.out.println("after visiting program ASTNode: " + symtab);
		//System.out.println("after visiting program ASTNode: " + symtab.printStack());
		
		program.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitAssignmentStatement ASTNode");
		
		Expression e = assignStatement.e;
		e.visit(this, arg); //visit the rhs first?
		
		//System.out.println("null?");
		IdentLValue var = assignStatement.var;
		var.visit(this, arg);
		
		if (var.getType() != e.getType())
		{
			throw new TypeCheckException("IdentLValue and Expression type mismatch: " + var + ", " + e);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec dec = symtab.lookupExtended(identX.getText(), identX.getFirstToken().pos);
		if (dec != null) //visible in scope and declared BEFORE assignment
		{
			identX.dec = dec;
			identX.setType(Type.getTypeName(dec.getType()));
			identX.scopeLevel = symtab.lookupExtendedScope(identX.getText(), identX.getFirstToken().pos);
		}
		else
		{
			throw new TypeCheckException("IdentLValue have not been declared: " + identX);
		}
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		paramDec.setTypeName(Type.getTypeName(paramDec.getFirstToken()));
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		//System.out.println("visitConstantExpression ASTNode");
		constantExpression.setType(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tuple = imageOpChain.getArg();
		
		//Kind frameKind = frameOpChain.getFirstToken().kind;
		imageOpChain.kind = imageOpChain.getFirstToken().kind;
		
		if (predictSetMembershipTest(imageOpChain.getFirstToken(), Kind.OP_WIDTH, Kind.OP_HEIGHT)) 
		{
			int size = tuple.getExprList().size(); //tuple len must be 0
			if (size > 0)
			{
				throw new TypeCheckException("args of imageOpChain greater than 0: " + imageOpChain);
			}
			imageOpChain.setType(INTEGER);
		}
		else if (predictSetMembershipTest(imageOpChain.getFirstToken(), Kind.KW_SCALE)) 
		{
			int size = tuple.getExprList().size(); //tuple len must be 1
			if (size != 1)
			{
				throw new TypeCheckException("args of imageOpChain not 1: " + imageOpChain);
			}
			imageOpChain.setType(IMAGE);
		}
		else
		{
			throw new TypeCheckException("Bug in parser?");
		}
		tuple.visit(this, arg);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("visitTuple ASTNode");
		
		List<Expression> exprList = tuple.getExprList();
		for (Expression e:exprList)
		{
			e.visit(this, arg); //visit all expression first
		}
		for (Expression e:exprList)
		{
			if (e.getType() != INTEGER) //check their types
			{
				throw new TypeCheckException("All expressions in tuple must be integer: " + e);
			}
		}
		return null;
	}
	
	private boolean predictSetMembershipTest(Token op, Kind... kinds) {
		// test if the token is a member of the predict set
		// CAREFUL. This method does not throw SyntaxException. It is up to the caller to check
		
		for (Kind someKind : kinds) //iterate through list of kinds passed in
		{
			if (op.kind == someKind)
			{
				//System.out.println("predictSetMembershipTest -> " + t.kind);
				return true; //found the token in some predict set
			}
		}
		return false;
	}


}
