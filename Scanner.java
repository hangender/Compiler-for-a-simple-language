package cop5556sp17;

import java.util.ArrayList;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public static enum State {
		START, IN_DIGIT, IN_IDENT, AFTER_EQ, AFTER_EXCLAM, AFTER_LESS, 
		AFTER_GREATER, AFTER_DASH, IN_COMMENT, IN_COMMENT_CHK, OUT_COMMENT_CHK, IN_TRANSFORM_CHK, IN_TRANSFORM_CHK2;
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;
		
		public boolean isKind(Kind someKind)
		{
			if (kind == someKind)
			{
				return true;
			}
			return false;
		}

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			if (kind == Kind.IDENT || kind == Kind.INT_LIT)
			{
				return (chars.substring(pos, pos+length));
			}
			return this.kind.getText();
		}
		
		public String toString() {
			return this.getText();
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS
			int curPos = 0;
			int curPosHolder = 0;
			int curLine = 0;
			
			while (curPos < pos)
			{
				curPosHolder++;
				if (chars.charAt(curPos) == '\n')
				{
					curPosHolder = 0;
					curLine++;
				}
				curPos++;
			}

			return (new LinePos(curLine, curPosHolder));
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			return Integer.parseInt(chars.substring(pos, pos + length));
		}
		
		  @Override
		  public int hashCode() {
			   final int prime = 31;
			   int result = 1;
			   result = prime * result + getOuterType().hashCode();
			   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			   result = prime * result + length;
			   result = prime * result + pos;
			   return result;
		  }
	
		  @Override
		  public boolean equals(Object obj) {
			   if (this == obj) {
			    return true;
			   }
			   if (obj == null) {
			    return false;
			   }
			   if (!(obj instanceof Token)) {
			    return false;
			   }
			   Token other = (Token) obj;
			   if (!getOuterType().equals(other.getOuterType())) {
			    return false;
			   }
			   if (kind != other.kind) {
			    return false;
			   }
			   if (length != other.length) {
			    return false;
			   }
			   if (pos != other.pos) {
			    return false;
			   }
			   return true;
		  }
	
		 
	
		  private Scanner getOuterType() {
			  return Scanner.this;
		  }
		
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();


	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		//TODO IMPLEMENT THIS!!!!
		int pos = 0;
		int startPos = 0; //keep tracking of starting position
		
		State state = State.START;
		char currentChar;
		
		while (pos<=chars.length()) //scans until EOF
		{

			currentChar = pos< chars.length()?chars.charAt(pos):EOF_HOLDER; //EOF
			//System.out.println(state + " " + currentChar);
			
			switch (state) {
			
				case START: {
					//System.out.println("entered starting state");
					startPos = pos;//startPos should only be modified here
					if (Character.isWhitespace(currentChar))
					{
						if (currentChar == '\n')
						{
							//linePos increment?
							//System.out.println("read in new line");
						}
						pos++;
					}
					else {
						switch (currentChar){
							case '0':
								//System.out.println("read in " + chars.substring(startPos,startPos+1));
								tokens.add(new Token(Kind.INT_LIT,startPos,1));
								pos++;

								break;
							case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
								//System.out.println("reading in digit");
								state = state.IN_DIGIT;
								pos++;
								break;
								
							case ';': //separator
								tokens.add(new Token(Kind.SEMI,startPos,1));
								pos++;
								break;
							case ',': //separator
								tokens.add(new Token(Kind.COMMA,startPos,1));
								pos++;
								break;
							case '(': //separator
								tokens.add(new Token(Kind.LPAREN,startPos,1));
								pos++;
								break;
							case ')': //separator
								tokens.add(new Token(Kind.RPAREN,startPos,1));
								pos++;
								break;
							case '{': //separator
								tokens.add(new Token(Kind.LBRACE,startPos,1));
								pos++;
								break;
							case '}': //separator	
								tokens.add(new Token(Kind.RBRACE,startPos,1));
								pos++;
								break;
							case '&':	
								tokens.add(new Token(Kind.AND,startPos,1));
								pos++;
								break;
							case '+':	
								tokens.add(new Token(Kind.PLUS,startPos,1));
								pos++;
								break;
							case '*':	
								tokens.add(new Token(Kind.TIMES,startPos,1));
								pos++;
								break;
							case '%':	
								tokens.add(new Token(Kind.MOD,startPos,1));
								pos++;
								break;
								
							case '=':	
								state = state.AFTER_EQ;
								pos++;
								break;
							case '!':	
								state = state.AFTER_EXCLAM;
								pos++;
								break;
								
							case '<':	
								state = state.AFTER_LESS;
								pos++;
								break;
							case '>':	
								state = state.AFTER_GREATER;
								pos++;
								break;
							case '-':	
								state = state.AFTER_DASH;
								pos++;
								break;
							case '|':	
								state = state.IN_TRANSFORM_CHK;
								pos++;
								break;
								
							case '/': //possibly in comments	
								state = state.IN_COMMENT_CHK;
								pos++;
								break;
								
							default:
								//System.out.println("default case of START state");
								if (Character.isJavaIdentifierStart(currentChar))
								{
									//System.out.println("reading in ident");
									state = state.IN_IDENT;
									pos++;
								}
								else
								{
									if (pos >= chars.length() && currentChar == EOF_HOLDER)
									{
										//System.out.println("END OF FILE");
										pos++;
									}
									else
									{
										throw new IllegalCharException("Illegal char \"" + currentChar + "\"");
									}
									
								}
						}
					}
				} break;
				
				case IN_TRANSFORM_CHK: {
					if (currentChar == '-') // |-
					{
						state = state.IN_TRANSFORM_CHK2;
						pos++;
					}
					else // |
					{
						tokens.add(new Token(Kind.OR,startPos,1));
						state = state.START;
					}
				}break;
				
				case IN_TRANSFORM_CHK2: {
					if (currentChar == '>') // |->
					{
						tokens.add(new Token(Kind.BARARROW,startPos,3));
						state = state.START;
						pos++;
					}
					else
					{
						tokens.add(new Token(Kind.OR,startPos,1));
						state = state.START;
						pos--; //we actually have to go back and reparse the -
					}
				}break;
				
				case AFTER_DASH: {
					if (currentChar == '>') //->
					{
						tokens.add(new Token(Kind.ARROW,startPos,2));
						state = state.START;
						pos++;
					}
					else // >
					{
						tokens.add(new Token(Kind.MINUS,startPos,1));
						state = state.START;
					}
				}break;
				
				case AFTER_GREATER: {
					if (currentChar == '=') //>=
					{
						tokens.add(new Token(Kind.GE,startPos,2));
						state = state.START;
						pos++;
					}
					else // >
					{
						tokens.add(new Token(Kind.GT,startPos,1));
						state = state.START;
					}
				}break;
				
				case AFTER_LESS: {
					if (currentChar == '=') //<=
					{
						tokens.add(new Token(Kind.LE,startPos,2));
						state = state.START;
						pos++;
					}
					else if (currentChar == '-')//<-
					{
						tokens.add(new Token(Kind.ASSIGN,startPos,2));
						state = state.START;
						pos++;
					}
					else // <
					{
						tokens.add(new Token(Kind.LT,startPos,1));
						state = state.START;
					}
				}break;
				
				case AFTER_EXCLAM: {
					if (currentChar != '=') //not a !=
					{
						tokens.add(new Token(Kind.NOT,startPos,1));
						state = state.START;
					}
					else //!=
					{
						tokens.add(new Token(Kind.NOTEQUAL,startPos,2));
						state = state.START;
						pos++;
					}
				}break;
				
				case AFTER_EQ: {
					if (currentChar != '=') //not a ==
					{
						throw new IllegalCharException("Illegal char following \"=\"");
					}
					else
					{
						tokens.add(new Token(Kind.EQUAL,startPos,2));
						state = state.START;
						pos++;
					}
				}break;
				
				case IN_COMMENT_CHK:{
					if (currentChar != '*') //not a /*
					{
						tokens.add(new Token(Kind.DIV,startPos,1));
						state = state.START;
					}
					else // /*
					{
						state = state.IN_COMMENT;
						pos++;
					}
				}break;
				
				case IN_COMMENT:{
					//System.out.println("IN_COMMENT " + currentChar + " " + pos + " " + chars.length());
					if (currentChar != '*') //still in comment
					{
						pos++;
					}
					else 
					{
						state = state.OUT_COMMENT_CHK;
						pos++;
					}
				}break;
				
				case OUT_COMMENT_CHK:{
					if (currentChar != '/')
					{
						state = state.IN_COMMENT; //not */, so goes back to comment
						//pos++;
					}
					else 
					{
						state = state.START; //out of comments
						pos++;
					}
				}break;
				
				case IN_DIGIT:{
					if (Character.isDigit(currentChar)) 
					{
						//System.out.println("reading in digit (IN_DIGIT)");
						pos++;
					}
					else
					{
						String num_lit_string = chars.substring(startPos, pos);
						
						try{
							Integer.parseInt(num_lit_string); //buggy chk
						}
						catch (NumberFormatException e)
						{
							throw new IllegalNumberException("Num Literal is too big!");
						}
						
						//System.out.println("read in a num_lit : " + chars.substring(startPos, pos));
						tokens.add(new Token(Kind.INT_LIT,startPos,pos-startPos));
						state = state.START;
					}
				}break;
				
				case IN_IDENT:{
					if (Character.isJavaIdentifierPart(currentChar)) 
					{
						//System.out.println("reading in ident (IN_IDENT)");
						pos++;
					}
					else
					{
						//System.out.println("read in an ident : " + chars.substring(startPos, pos));
						String identString = chars.substring(startPos, pos);
						if (identString.compareTo("true") == 0)
						{
							tokens.add(new Token(Kind.KW_TRUE,startPos,pos-startPos));
						}
						else if (identString.compareTo("false") == 0)
						{
							tokens.add(new Token(Kind.KW_FALSE,startPos,pos-startPos));
						}
						else if (identString.compareTo("xloc") == 0)
						{
							tokens.add(new Token(Kind.KW_XLOC,startPos,pos-startPos));
						}
						else if (identString.compareTo("yloc") == 0)
						{
							tokens.add(new Token(Kind.KW_YLOC,startPos,pos-startPos));
						}
						else if (identString.compareTo("hide") == 0)
						{
							tokens.add(new Token(Kind.KW_HIDE,startPos,pos-startPos));
						}
						else if (identString.compareTo("show") == 0)
						{
							tokens.add(new Token(Kind.KW_SHOW,startPos,pos-startPos));
						}
						else if (identString.compareTo("move") == 0)
						{
							tokens.add(new Token(Kind.KW_MOVE,startPos,pos-startPos));
						}
						else if (identString.compareTo("width") == 0)
						{
							tokens.add(new Token(Kind.OP_WIDTH,startPos,pos-startPos));
						}
						else if (identString.compareTo("height") == 0)
						{
							tokens.add(new Token(Kind.OP_HEIGHT,startPos,pos-startPos));
						}
						else if (identString.compareTo("gray") == 0)
						{
							tokens.add(new Token(Kind.OP_GRAY,startPos,pos-startPos));
						}
						else if (identString.compareTo("convolve") == 0)
						{
							tokens.add(new Token(Kind.OP_CONVOLVE,startPos,pos-startPos));
						}
						else if (identString.compareTo("blur") == 0)
						{
							tokens.add(new Token(Kind.OP_BLUR,startPos,pos-startPos));
						}
						else if (identString.compareTo("scale") == 0)
						{
							tokens.add(new Token(Kind.KW_SCALE,startPos,pos-startPos));
						}
						else if (identString.compareTo("integer") == 0)
						{
							tokens.add(new Token(Kind.KW_INTEGER,startPos,pos-startPos));
						}
						else if (identString.compareTo("boolean") == 0)
						{
							tokens.add(new Token(Kind.KW_BOOLEAN,startPos,pos-startPos));
						}
						else if (identString.compareTo("image") == 0)
						{
							tokens.add(new Token(Kind.KW_IMAGE,startPos,pos-startPos));
						}
						else if (identString.compareTo("url") == 0)
						{
							tokens.add(new Token(Kind.KW_URL,startPos,pos-startPos));
						}
						else if (identString.compareTo("file") == 0)
						{
							tokens.add(new Token(Kind.KW_FILE,startPos,pos-startPos));
						}
						else if (identString.compareTo("frame") == 0)
						{
							tokens.add(new Token(Kind.KW_FRAME,startPos,pos-startPos));
						}
						else if (identString.compareTo("while") == 0)
						{
							tokens.add(new Token(Kind.KW_WHILE,startPos,pos-startPos));
						}
						else if (identString.compareTo("if") == 0)
						{
							tokens.add(new Token(Kind.KW_IF,startPos,pos-startPos));
						}
						else if (identString.compareTo("sleep") == 0)
						{
							tokens.add(new Token(Kind.OP_SLEEP,startPos,pos-startPos));
						}
						else if (identString.compareTo("screenheight") == 0)
						{
							tokens.add(new Token(Kind.KW_SCREENHEIGHT,startPos,pos-startPos));
						}
						else if (identString.compareTo("screenwidth") == 0)
						{
							tokens.add(new Token(Kind.KW_SCREENWIDTH,startPos,pos-startPos));
						}
						else 
						{
							tokens.add(new Token(Kind.IDENT,startPos,pos-startPos));
						}
						state = state.START;
					}
				}break;
				
			}
		}
		
		//post scan checks
		if (state == state.IN_COMMENT || state == state.OUT_COMMENT_CHK)
		{
			throw new IllegalCharException("comment not closed");
		}
		tokens.add(new Token(Kind.EOF,pos-1,0)); //auto add in eof token. The class will always have EOF token
		//System.out.println("Scanner complete. pos is: "+pos); //pos is always +2 from string index

		return this;  
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum; //used to traverse ArrayList of tokens
	
	char EOF_HOLDER = '^'; //this char defines EOF

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		int curPos = 0;
		int curPosHolder = 0;
		int curLine = 0;
		
		while (curPos < t.pos)
		{
			curPosHolder++;
			if (chars.charAt(curPos) == '\n')
			{
				curPosHolder = 0;
				curLine++;
			}
			curPos++;
		}

		return (new LinePos(curLine, curPosHolder));
	}


}
