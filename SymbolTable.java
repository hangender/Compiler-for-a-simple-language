package cop5556sp17;



import java.util.Hashtable;
import java.util.Stack; //stack inherited from Vector class
import java.util.Vector;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;


public class SymbolTable {
	
	class attributes
	{
		TypeName type; Dec dec;
		attributes(TypeName type, Dec dec)
		{
			this.type = type;
			this.dec = dec;
		}
		
		@Override
		public String toString() {
			//TODO:  IMPLEMENT THIS
			return ("(" + type.toString() + ", " + dec.toString() + ")");
		}
	}
	//TODO  add fields
	int currentScope; int nextScope; Stack<Integer> scope_stack;
	
	Hashtable<Integer, Hashtable<String, attributes>> scope_hash_table; //table inside a table
	//this table stores hashTables for decs at each different scopes

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		currentScope = nextScope++;
		scope_stack.push(currentScope); //the current scope level is on top of stack, NOT currentScope
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		currentScope = scope_stack.pop();
	}
	
	public boolean insert(String ident, Dec dec) throws SyntaxException, TypeCheckException{
		//TODO:  IMPLEMENT THIS
		//ParamDec is a subclass of dec
		Hashtable<String, attributes> temp_scope_check_table = scope_hash_table.get(scope_stack.peek()); //gets current scope from stack
		//System.out.println(ident+" "+ Type.getTypeName(dec.getType()));
		if (temp_scope_check_table==null)
		{
			//table is null, so we insert dec
			temp_scope_check_table = new Hashtable<String, attributes>();
			temp_scope_check_table.put(ident, new attributes(Type.getTypeName(dec.getType()),dec));
			scope_hash_table.put(scope_stack.peek(), temp_scope_check_table);
		}
		else
		{
			if (temp_scope_check_table.get(ident) != null)
			{
				throw new TypeCheckException("redeclaration: " + Type.getTypeName(dec.getType())+ " " + ident);
			}
			temp_scope_check_table.put(ident, new attributes(Type.getTypeName(dec.getType()),dec));
		}
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		int count = scope_stack.size()-1; //start looking from top of stack
		
		//System.out.println("looking up " + ident + " in table. Stack " + (scope_stack));
		//System.out.println("symtab " + scope_hash_table);
		
		while (count >= 0)
		{
			Hashtable<String, attributes> temp_scope_check_table = scope_hash_table.get(scope_stack.get(count));
			if (temp_scope_check_table != null && temp_scope_check_table.get(ident) != null) //if the table exists and the var have been declared
			{
				//System.out.println("found " + ident + " in Stack level " + (count));
				return (temp_scope_check_table.get(ident).dec); //returns the dec from the pair attributes
			}
			count--;
		}
		
		return null;
	}
	
	public int lookupScope(String ident){
		//TODO:  IMPLEMENT THIS
		int count = scope_stack.size()-1; //start looking from top of stack
		
		//System.out.println("looking up " + ident + " in table. Stack " + (scope_stack));
		//System.out.println("symtab " + scope_hash_table);
		
		while (count >= 0)
		{
			Hashtable<String, attributes> temp_scope_check_table = scope_hash_table.get(scope_stack.get(count));
			if (temp_scope_check_table != null && temp_scope_check_table.get(ident) != null) //if the table exists and the var have been declared
			{
				//System.out.println("found " + ident + " in Stack level " + (count));
				return (scope_stack.get(count)); //returns the dec from the pair attributes
			}
			count--;
		}
		
		return -1;
	}
	
	public Dec lookupExtended(String ident, int pos){ //now checks linepos
		//TODO:  IMPLEMENT THIS
		int count = scope_stack.size()-1; //start looking from top of stack
		
		//System.out.println("looking up " + ident + " in table. Stack " + (scope_stack));
		//System.out.println("symtab " + scope_hash_table);
		
		while (count >= 0)
		{
			Hashtable<String, attributes> temp_scope_check_table = scope_hash_table.get(scope_stack.get(count));
			if (temp_scope_check_table != null && temp_scope_check_table.get(ident) != null && (temp_scope_check_table.get(ident).dec.getIdent().pos < pos)) //if the table exists and the var have been declared
			{
				//System.out.println("found " + ident + " in Stack level " + (count));
				return (temp_scope_check_table.get(ident).dec); //returns the dec from the pair attributes
			}
			count--;
		}
		
		return null;
	}
	
	public int lookupExtendedScope(String ident, int pos){ //now checks linepos
		//TODO:  IMPLEMENT THIS
		int count = scope_stack.size()-1; //start looking from top of stack
		
		//System.out.println("looking up " + ident + " in table. Stack " + (scope_stack));
		//System.out.println("symtab " + scope_hash_table);
		
		while (count >= 0)
		{
			Hashtable<String, attributes> temp_scope_check_table = scope_hash_table.get(scope_stack.get(count));
			if (temp_scope_check_table != null && temp_scope_check_table.get(ident) != null && (temp_scope_check_table.get(ident).dec.getIdent().pos < pos)) //if the table exists and the var have been declared
			{
				//System.out.println("found " + ident + " in Stack level " + (count));
				return (scope_stack.get(count)); //returns the dec from the pair attributes
			}
			count--;
		}
		
		return -1;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		currentScope = 0;
		nextScope = 0;
		
		scope_stack = new Stack<Integer>();
		
		scope_hash_table = new Hashtable<Integer, Hashtable<String, attributes>>(); //table inside a table
		
		enterScope(); //puts 0 on stack
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return (scope_hash_table.toString());
	}
	
	public String printStack() {
		//TODO:  IMPLEMENT THIS
		return (scope_stack.toString()); //uses vector classes toString
	}


}
