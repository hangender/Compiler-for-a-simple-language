package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.BOOLEAN;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.NONE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
	
	@SuppressWarnings("serial")
	public static class CodeGenException extends Exception {
		CodeGenException(String message) {
			super(message);
		}
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
	
	class methodVInnerScoper
	{
		MethodVisitor mv; int index;
		
		methodVInnerScoper (MethodVisitor mv, int index)
		{
			this.mv = mv; this.index = index;
		}
	}
	
	SymbolTable symtab; //keep track of nesting
	Hashtable<String, Integer> slotTable; //slots assigned to local vars

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		symtab = new SymbolTable();
		slotTable = new Hashtable<String, Integer>();
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
		{
			dec.visit(this, mv); //Visit paramDec
			symtab.insert(dec.getIdent().getText(), dec);
		}
		
		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		
		int count = 0; //assume args have the right size to match number of declared vars
		for (ParamDec dec : params)
		{
			switch (dec.getTypeName())
			{
				case INTEGER:
					//System.out.println(dec.getTypeName());
					//System.out.println(dec.getIdent().getText());
					mv.visitVarInsn(ALOAD, 0); //loads the address for the PUTFIELD instruction
					mv.visitVarInsn(ALOAD, 1); //base address of args
					mv.visitIntInsn(SIPUSH, count); //index of args
					mv.visitInsn(AALOAD); //loads the index'th element
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
					mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), "I");
				break;
				
				case BOOLEAN:
					//System.out.println(dec.getTypeName());
					//System.out.println(dec.getIdent().getText());
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitIntInsn(SIPUSH, count);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
					mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), "Z");

				break;
				
				case FILE:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(NEW, TypeName.FILE.getJVMClass());
					mv.visitInsn(DUP); //dupe reference
					mv.visitVarInsn(ALOAD, 1);
					mv.visitIntInsn(SIPUSH, count);
					mv.visitInsn(AALOAD); //load args[count]
					mv.visitMethodInsn(INVOKESPECIAL, TypeName.FILE.getJVMClass(), "<init>", "(Ljava/lang/String;)V", false);
					mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), TypeName.FILE.getJVMTypeDesc());
					break;
				case URL:
					/*
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(NEW, URL.getJVMClass());
					mv.visitInsn(DUP); //dupe reference
					mv.visitVarInsn(ALOAD, 1);
					mv.visitIntInsn(SIPUSH, count);
					mv.visitInsn(AALOAD); //load args[count]
					mv.visitMethodInsn(INVOKESPECIAL, URL.getJVMClass(), "<init>", "(Ljava/lang/String;)V", false);
					mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), URL.getJVMTypeDesc());
					*/
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitIntInsn(SIPUSH, count);
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL", PLPRuntimeImageIO.getURLSig, false);
					mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), URL.getJVMTypeDesc());
					break;
				
				default:
				{
					throw new CodeGenException ("not yet implemented (constructor init)"); 
				}
			}
			count++;
		}
		
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, new methodVInnerScoper(mv, 1)); //block is guaranteed to have the correct mv?
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg); //push the expression value to stack first
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		
		//then, istore/putfield
		assignStatement.getVar().visit(this, arg); //var is internally IDENT+INDEX
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		Chain e0 = binaryChain.getE0();
		Token arrow = binaryChain.getArrow();
		ChainElem e1 = binaryChain.getE1();
		
		if (e0 instanceof IdentChain)
		{
			e0.visit(this, 'L'); 
		}
		else
		{
			e0.visit(this, arg); 
		}
		
		if (e1 instanceof FilterOpChain)
		{
			if (arrow.isKind(BARARROW))
			{
				//System.out.println("BARROW");
				e1.visit(this, 'B');
			}
			else
			{
				e1.visit(this, 'R');
			}
		}
		else if (e1 instanceof IdentChain)
		{
			e1.visit(this, 'R');
		}
		else
		{
			e1.visit(this, arg);
		}
		
		/*
		if (e0 instanceof BinaryChain && ((BinaryChain) e0).getE1() instanceof IdentChain)
		{
			((BinaryChain) e0).getE1().visit(this, 'L'); //if the right of e0 is identChain, we must load it
		}
		*/
		//always push value on top of stack if you are the right end of binaryChain
		
		Token instanceOf = e1.getFirstToken();
		boolean isFilterOp = e1 instanceof FilterOpChain;
		boolean isFrameOp = e1 instanceof FrameOpChain;
		boolean isImageOp = e1 instanceof ImageOpChain;
		
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		methodVInnerScoper container = (methodVInnerScoper) arg;
		MethodVisitor mv = container.mv;
		int scopeIndex = container.index;
		
		Expression e0 = binaryExpression.getE0();
		Token op = binaryExpression.getOp();
		Expression e1 = binaryExpression.getE1();
		//System.out.println("visiting codegen binary op");
		e0.visit(this, arg); e1.visit(this, arg);
		//System.out.println(e0.getType() + " " + e1.getType());
		if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && (op.isKind(Kind.MOD))) //mod
		{
			mv.visitInsn(IREM);
		}
		else if (e0.getType() == TypeName.BOOLEAN && e1.getType() == TypeName.BOOLEAN && predictSetMembershipTest(op, Kind.OR, Kind.AND))
		{
			if (op.isKind(Kind.OR))
			{
				mv.visitInsn(IOR);
			}
			else
			{
				mv.visitInsn(IAND);
			}
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && (op.isKind(Kind.PLUS) || op.isKind(Kind.MINUS)))
		{
			//binaryExpression.setType(INTEGER);
			if (op.isKind(Kind.PLUS))
			{
				mv.visitInsn(IADD);
			}
			else
			{
				mv.visitInsn(ISUB);
			}
		}
		else if (e0.getType() == TypeName.IMAGE && e1.getType() == TypeName.IMAGE && (op.isKind(Kind.PLUS) || op.isKind(Kind.MINUS)))
		{
			//binaryExpression.setType(IMAGE);
			if (op.isKind(Kind.PLUS))
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			}
			else
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			}
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && (op.isKind(Kind.TIMES) || op.isKind(Kind.DIV)))
		{
			//binaryExpression.setType(INTEGER);
			if (op.isKind(Kind.TIMES))
			{
				mv.visitInsn(IMUL);
			}
			else
			{
				mv.visitInsn(IDIV);
			}
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.INTEGER && predictSetMembershipTest(op, Kind.LT, Kind.GT, Kind.LE, Kind.GE))
		{
			//binaryExpression.setType(BOOLEAN);
			//Kind.LT, Kind.GT, Kind.LE, Kind.GE
			Label caseFalse = new Label();
			Label caseTrue = new Label();
			switch (op.kind)
			{
				case LT:
					mv.visitJumpInsn(IF_ICMPGE, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				case GT:
					mv.visitJumpInsn(IF_ICMPLE, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				case LE:
					mv.visitJumpInsn(IF_ICMPGT, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				case GE:
					mv.visitJumpInsn(IF_ICMPLT, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				
				default:
				{
					throw new CodeGenException("unsupported binary operation: " + binaryExpression);
				}
			}
			mv.visitLabel(caseFalse);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(caseTrue);
		}
		else if (e0.getType() == TypeName.BOOLEAN && e1.getType() == TypeName.BOOLEAN && predictSetMembershipTest(op, Kind.LT, Kind.GT, Kind.LE, Kind.GE))
		{
			//binaryExpression.setType(BOOLEAN);
			Label caseFalse = new Label();
			Label caseTrue = new Label();
			switch (op.kind)
			{
				case LT:
					mv.visitJumpInsn(IF_ICMPGE, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				case GT:
					mv.visitJumpInsn(IF_ICMPLE, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				case LE:
					mv.visitJumpInsn(IF_ICMPGT, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				case GE:
					mv.visitJumpInsn(IF_ICMPLT, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
				break;
				
				default:
				{
					throw new CodeGenException("unsupported binary operation: " + binaryExpression);
				}
			}
			mv.visitLabel(caseFalse);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(caseTrue);
		}
		else if (e0.getType() == TypeName.IMAGE && e1.getType() == TypeName.INTEGER && predictSetMembershipTest(op, Kind.TIMES))
		{
			//binaryExpression.setType(IMAGE);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
		}
		else if (e0.getType() == TypeName.IMAGE && e1.getType() == TypeName.INTEGER && predictSetMembershipTest(op, Kind.DIV))
		{
			//binaryExpression.setType(IMAGE);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
		}
		else if (e0.getType() == TypeName.IMAGE && e1.getType() == TypeName.INTEGER && predictSetMembershipTest(op, Kind.MOD))
		{
			//binaryExpression.setType(IMAGE);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
		}
		else if (e0.getType() == TypeName.INTEGER && e1.getType() == TypeName.IMAGE && predictSetMembershipTest(op, Kind.TIMES))
		{
			//binaryExpression.setType(IMAGE);
			mv.visitInsn(SWAP);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
		}
		else if (predictSetMembershipTest(op, Kind.NOTEQUAL, Kind.EQUAL))
		{
			if (e0.getType() != e1.getType())
			{
				throw new CodeGenException("mismatch of types using == or !=: " + binaryExpression);
			}
			else if (e0.getType() == TypeName.INTEGER || e0.getType() == TypeName.BOOLEAN)
			{
				//binaryExpression.setType(BOOLEAN);
				Label caseFalse = new Label();
				Label caseTrue = new Label();
				if (op.isKind(Kind.NOTEQUAL))
				{
					mv.visitJumpInsn(IF_ICMPEQ, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
					mv.visitLabel(caseFalse);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(caseTrue);
				}
				else
				{
					mv.visitJumpInsn(IF_ICMPNE, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
					mv.visitLabel(caseFalse);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(caseTrue);
				}
			}
			else
			{
				Label caseFalse = new Label();
				Label caseTrue = new Label();
				if (op.isKind(Kind.NOTEQUAL))
				{
					mv.visitJumpInsn(IF_ACMPEQ, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
					mv.visitLabel(caseFalse);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(caseTrue);
				}
				else
				{
					mv.visitJumpInsn(IF_ACMPNE, caseFalse);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, caseTrue);
					mv.visitLabel(caseFalse);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(caseTrue);
				}
			}
		}
		else
		{
			throw new CodeGenException("illegal binaryExpression combination: " + binaryExpression);
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		symtab.enterScope();
		//System.out.println("entering block codegen:");
		methodVInnerScoper container = (methodVInnerScoper) arg;
		MethodVisitor mv = container.mv;
		int scopeIndex = container.index;
		
		Label startBlock = new Label(); //starts a new scope
		mv.visitLabel(startBlock); //which mv?
		
		ArrayList<Dec> decList = block.getDecs();
		ArrayList<Statement> statementList = block.getStatements();
		
		int scopeIndexTemp = scopeIndex;
		for (Dec i : decList) //for each item in decList
		{
			symtab.insert(i.getIdent().getText(), i);
			//System.out.println("Inserting into slot table: " + ((i.getIdent().getText())+symtab.lookupScope(i.getIdent().getText())) + " @" + scopeIndexTemp);
			slotTable.put(((i.getIdent().getText())+symtab.lookupScope(i.getIdent().getText())), scopeIndexTemp);
			
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, scopeIndexTemp);
			
			scopeIndexTemp++;
		} //remembers which slot goes to which local variable. Local variable name is combined with their scope level to form unique key
		
		for (Statement i : statementList) //for each item in statementList
		{
			i.visit(this, new methodVInnerScoper(mv, scopeIndex + statementList.size()));
			if (i instanceof BinaryChain || i instanceof IdentChain || i instanceof FilterOpChain || i instanceof ImageOpChain || i instanceof FrameOpChain) 
			{
				mv.visitInsn(POP); //pop binarychain return value because it is not being assigned
			}
		}
		
		Label endBlock = new Label();
		mv.visitLabel(endBlock);
		//visit all local vars
		
		for (Dec i : decList) //for each item in decList
		{
			//visits each local var, at the end of all statements
			switch(i.getTypeName())
			{
				case INTEGER:
					//System.out.println(i.getIdent().getText() + " @ slot (visit localvar)" + scopeIndex);
					mv.visitLocalVariable(i.getIdent().getText(), "I", null, startBlock, endBlock, scopeIndex);
				break;
				
				case BOOLEAN:
					//System.out.println(i.getIdent().getText() + " @ slot (visit localvar)" + scopeIndex);
					mv.visitLocalVariable(i.getIdent().getText(), "Z", null, startBlock, endBlock, scopeIndex);
				break;
				
				case IMAGE:
					//the constructor haven't been called yet
					mv.visitLocalVariable(i.getIdent().getText(), TypeName.IMAGE.getJVMTypeDesc(), null, startBlock, endBlock, scopeIndex);
					break;
				case FRAME:
					//the constructor haven't been called yet
					mv.visitLocalVariable(i.getIdent().getText(), "Lcop5556sp17/PLPRuntimeFrame;", null, startBlock, endBlock, scopeIndex);
					break;
				
				default:
				{
					throw new CodeGenException ("not yet implemented (visitBlock dec)"); 
				}
			}
			
			scopeIndex++;
		}
		
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		//System.out.println("Pushed bool onto stack");
		if (booleanLitExpression.getValue())
		{
			mv.visitInsn(ICONST_1);
		}
		else
		{
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		//assert false : "not yet implemented";
		if (constantExpression.getFirstToken().isKind(KW_SCREENHEIGHT))
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", "()I", false);
		}
		else
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", "()I", false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		Token token = filterOpChain.getFirstToken();

		if ((char) arg == 'B') //barrow
		{
			//System.out.println("barrow ImageOpChain");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			mv.visitInsn(SWAP);
		}
		else
		{
			mv.visitInsn(ACONST_NULL);
		}
		
		if (token.isKind(Kind.OP_GRAY))
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		else if (token.isKind(Kind.OP_BLUR))
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		else //OP_CONVOLVE
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		Token token = frameOpChain.getFirstToken();
		Tuple tuple = frameOpChain.getArg();
		if (token.isKind(Kind.KW_XLOC))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}
		else if (token.isKind(Kind.KW_YLOC))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}
		else if (token.isKind(Kind.KW_SHOW))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		}
		else if (token.isKind(Kind.KW_HIDE))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}
		else //KW_MOVE
		{
			//frameVar.moveFrame(1,1)
			tuple.visit(this, arg);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		Dec dec = identChain.dec;
		int scope = identChain.scopeLevel; //get the scope of the variable
		TypeName type = identChain.getType(); //type was set in TypeCheckVistor
		
		scope = identChain.scopeLevel; //the newest scope getter
		
		char loadOrStore = (char) arg;
		if (loadOrStore == 'L') //puts stuff on stack
		{
			if (scope == 0)
			{
				switch (dec.getTypeName())
				{
					case INTEGER:
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), "I");
						//System.out.println("->" + dec.getIdent().getText());
						break;
					case BOOLEAN:
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), "Z");
						//System.out.println("->" + dec.getIdent().getText());
						break;
					case URL:
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), "Ljava/net/URL;");
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL", "(Ljava/net/URL;)Ljava/awt/image/BufferedImage;", false);
						break;
					case FILE:
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), "Ljava/io/File;");
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", "(Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
						break;
					default:
					{
						throw new CodeGenException ("not yet implemented (visitIdentChain) " + dec.getTypeName());  
					}
				}
			}
			else if (scope > 0)
			{
				//System.out.println("->" + (dec.getIdent().getText())); //v5 1 -> v51.
				//Combine var name with scope level of var to get local variable slot
				
				int slot = slotTable.get((dec.getIdent().getText())+scope);
				//System.out.println("->slot:" + slot);
				
				switch (dec.getTypeName())
				{
					case INTEGER:
						mv.visitVarInsn(ILOAD, slot);
						break;
					case BOOLEAN:
						mv.visitVarInsn(ILOAD, slot);
						break;
					case IMAGE:
						mv.visitVarInsn(ALOAD, slot);
						break;
					case FRAME:
						mv.visitVarInsn(ALOAD, slot);
						break;
					default:
					{
						throw new CodeGenException ("not yet implemented (visitIdentChain) " + dec.getTypeName());  
					}
				}
			}
			else
			{
				throw new CodeGenException("Scope level of var is -1! " + dec);
			}
		}
		else //'R', which means store it
		{
			if (scope == 0)
			{
				switch (dec.getTypeName())
				{
					case INTEGER:
						mv.visitInsn(DUP);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitInsn(SWAP); //swap exression return and addr
						mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), "I");
						//System.out.println("->" + identX.getText());
						break;
					case BOOLEAN:
						mv.visitInsn(DUP);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitInsn(SWAP); //swap exression return and addr
						mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), "Z");
						//System.out.println("->" + identX.getText());
						break;
					case FILE:
						//mv.visitVarInsn(ALOAD, 1); //the BufferedImage should be already on stack
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), "Ljava/io/File;");
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write", "(Ljava/awt/image/BufferedImage;Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
						//mv.visitInsn(DUP); //the function call already puts BufferedImage on stack
						break;
					//case URL: break; //URL should not appear on the right side of binary chain
					default:
					{
						throw new CodeGenException ("not yet implemented (visitIdentChain) " + dec.getTypeName());  
					}
				}
			}
			else if (scope > 0)
			{
				//System.out.println("->" + (identX.getText())); //v5 1 -> v51.
				//Combine var name with scope level of var to get local variable slot
				
				int slot = slotTable.get((dec.getIdent().getText())+scope);
				//System.out.println("->slot:" + slot);
				
				switch (dec.getTypeName())
				{
					case INTEGER:
						//System.out.println("IdentChain r.h.s INT");
						mv.visitInsn(DUP);
						mv.visitVarInsn(ISTORE, slot);
						break;
					case BOOLEAN:
						mv.visitInsn(DUP);
						mv.visitVarInsn(ISTORE, slot);
						break;
					case IMAGE:
						/*
						mv.visitVarInsn(ALOAD, 2);
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
						mv.visitVarInsn(ASTORE, 1);
						*/
						//store the image in var. assumes BufferedImage is on stack
						//IMAGE->instanceof IdentChain
						//not image,frame, or file,
						
						//mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
						mv.visitInsn(DUP);
						/*
						if (identChain.haveBeenInit == false)
						{
							mv.visitInsn(ACONST_NULL);
							mv.visitVarInsn(ASTORE, slot);
							identChain.haveBeenInit = true;
						}
						*/
						mv.visitVarInsn(ASTORE, slot);
						//System.out.println("image r.h.s of IdentChain");
						break;
					case FRAME:
						//mv.visitVarInsn(ALOAD, 1); //the image, which should already be on stack
						/*
						if (identChain.haveBeenInit == false)
						{
							mv.visitInsn(ACONST_NULL);
							mv.visitVarInsn(ASTORE, slot);
							identChain.haveBeenInit = true;
						}
						*/
						mv.visitVarInsn(ALOAD, slot);
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame", "(Ljava/awt/image/BufferedImage;Lcop5556sp17/PLPRuntimeFrame;)Lcop5556sp17/PLPRuntimeFrame;", false);
						mv.visitInsn(DUP);
						mv.visitVarInsn(ASTORE, slot);
						break;
					default:
					{						
						//mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
						//mv.visitInsn(DUP);
						//mv.visitVarInsn(ASTORE, slot);
						
						//System.out.println("instanceof IdentChain");
						
						throw new CodeGenException ("not yet implemented (visitIdentChain) " + dec.getTypeName());  
					}
				}
			}
			else
			{
				throw new CodeGenException("Scope level of var is -1! " + dec);
			}
		}
		
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		Dec dec = identExpression.dec;
		int scope = identExpression.scopeLevel; //get the scope of the variable
		
		methodVInnerScoper container = (methodVInnerScoper) arg;
		MethodVisitor mv = container.mv;
		int scopeIndex = container.index;
		
		scope = identExpression.scopeLevel; //the newest scope getter
		//System.out.println("CGV Ident Expression:" +  identExpression.getFirstToken().getText() + " " + scope);
		//System.out.println(dec.getIdent().getText()+ " Scope:" + scope);
		if (scope == 0)
		{
			switch (dec.getTypeName())
			{
				case INTEGER:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), "I");
					//System.out.println("->" + dec.getIdent().getText());
					break;
				case BOOLEAN:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), "Z");
					//System.out.println("->" + dec.getIdent().getText());
					break;
				case URL:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), TypeName.URL.getJVMTypeDesc());
					break;
				case FILE:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), TypeName.FILE.getJVMTypeDesc());
					break;
				default:
				{
					throw new CodeGenException ("not yet implemented (visitIdentExpression)");  
				}
			}
		}
		else if (scope > 0)
		{
			//System.out.println("->" + (dec.getIdent().getText())); //v5 1 -> v51.
			//Combine var name with scope level of var to get local variable slot
			
			int slot = slotTable.get((dec.getIdent().getText())+scope);
			//System.out.println("->slot:" + slot);
			
			switch (dec.getTypeName())
			{
				case INTEGER:
					mv.visitVarInsn(ILOAD, slot);
					break;
				case BOOLEAN:
					mv.visitVarInsn(ILOAD, slot);
					break;
				case IMAGE:
					mv.visitVarInsn(ALOAD, slot);
					break;
				case FRAME:
					mv.visitVarInsn(ALOAD, slot);
					break;
				default:
				{
					throw new CodeGenException ("not yet implemented (visitIdentExpression)");  
				}
			}
		}
		else
		{
			throw new CodeGenException("Scope level of var is -1! " + dec);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		Dec dec = identX.dec;
		int scope = identX.scopeLevel; //get the scope of the variable
		
		methodVInnerScoper container = (methodVInnerScoper) arg;
		MethodVisitor mv = container.mv;
		int scopeIndex = container.index;
		
		scope = identX.scopeLevel; //the newest scope getter
		//System.out.println("CGV Ident Lvalue:" +  identX.getText() + " " + scope);
		//System.out.println(dec.getIdent().getText()+ " Scope:" + scope);
		if (scope == 0)
		{
			switch (identX.getType())
			{
				case INTEGER:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP); //swap exression return and addr
					mv.visitFieldInsn(PUTFIELD, className, identX.getText(), "I");
					//System.out.println("->" + identX.getText());
					break;
				case BOOLEAN:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP); //swap exression return and addr
					mv.visitFieldInsn(PUTFIELD, className, identX.getText(), "Z");
					//System.out.println("->" + identX.getText());
					break;
				case URL:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP); //swap exression return and addr
					mv.visitFieldInsn(PUTFIELD, className, identX.getText(), TypeName.URL.getJVMTypeDesc());
					break;
				case FILE:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP); //swap exression return and addr
					mv.visitFieldInsn(PUTFIELD, className, identX.getText(), TypeName.FILE.getJVMTypeDesc());
					break;
				default:
				{
					throw new CodeGenException ("not yet implemented (visitIdentLValue) " + identX.getType());  
				}
			}
		}
		else if (scope > 0)
		{
			//System.out.println("->" + (identX.getText())); //v5 1 -> v51.
			//Combine var name with scope level of var to get local variable slot
			
			int slot = slotTable.get((identX.getText())+scope);
			//System.out.println("->slot:" + slot);
			
			switch (identX.getType())
			{
				case INTEGER:
					mv.visitVarInsn(ISTORE, slot);
					break;
				case BOOLEAN:
					mv.visitVarInsn(ISTORE, slot);
					break;
				case IMAGE:
					/*
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
					mv.visitVarInsn(ASTORE, 1);
					*/
					//store the image in var. assumes BufferedImage is on stack
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
					mv.visitVarInsn(ASTORE, slot);
					break;
				case FRAME:
					mv.visitVarInsn(ASTORE, slot);
					break;
				default:
				{
					throw new CodeGenException ("not yet implemented (visitIdentLValue) " + identX.getType());  
				}
			}
		}
		else
		{
			throw new CodeGenException("Scope level of var is -1! " + dec);
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Expression e = ifStatement.getE();
		Block b = ifStatement.getB();
		e.visit(this, arg);
		
		Label jumpPast = new Label();
		mv.visitJumpInsn(IFEQ, jumpPast); //IFEQ = if equal 0 (false), jump past body
		b.visit(this, arg); //body
		mv.visitLabel(jumpPast);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		Token token = imageOpChain.getFirstToken();
		Tuple tuple = imageOpChain.getArg();
		if (token.isKind(Kind.OP_WIDTH))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
			//System.out.println("imageOp width");
		}
		else if (token.isKind(Kind.OP_HEIGHT))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
			//System.out.println("imageOp height");
		}
		else //KW_SCALE
		{
			tuple.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "scale", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		//System.out.println("Pushed int onto stack");
		mv.visitLdcInsn(new Integer(intLitExpression.value));
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		TypeName type = paramDec.getTypeName();
		FieldVisitor fv;
		switch (type)
		{
			case INTEGER:
				//System.out.println(paramDec.getIdent().getText());
				fv = cw.visitField(0, paramDec.getIdent().getText(), "I", null, null);
				fv.visitEnd();
			break;
			
			case BOOLEAN:
				//System.out.println(paramDec.getIdent().getText());
				fv = cw.visitField(0, paramDec.getIdent().getText(), "Z", null, null);
				fv.visitEnd();
			break;
			
			case FILE:
				fv = cw.visitField(0, paramDec.getIdent().getText(), TypeName.FILE.getJVMTypeDesc(), null, null);
				fv.visitEnd();
				break;
			case URL:
				fv = cw.visitField(0, paramDec.getIdent().getText(), TypeName.URL.getJVMTypeDesc(), null, null);
				fv.visitEnd();
				break;
			
			default:
			{
				throw new CodeGenException ("not yet implemented (fields init)");  
			}
		}
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		//assert false : "not yet implemented";
		int sleepTime = ((IntLitExpression) sleepStatement.getE()).value;
		mv.visitLdcInsn(new Integer(sleepTime)); //push int
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		//assert false : "not yet implemented";
		List<Expression> exprList = tuple.getExprList();
		for (Expression e:exprList)
		{
			e.visit(this, arg); //visit all expression first
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Expression e = whileStatement.getE();
		Block b = whileStatement.getB();

		Label guard = new Label();
		Label body = new Label();
		
		mv.visitJumpInsn(GOTO, guard); //IFEQ = if equal 0 (false)
		mv.visitLabel(body);
		b.visit(this, arg); //body
		
		mv.visitLabel(guard);
		e.visit(this, arg);
		mv.visitJumpInsn(IFNE, body); //IFEQ = if equal 0 (false)
		return null;
	}

}
