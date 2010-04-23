package net.sourceforge.gjtapi.media;

/*
	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 

	All rights reserved. 

	Permission is hereby granted, free of charge, to any person obtaining a 
	copy of this software and associated documentation files (the 
	"Software"), to deal in the Software without restriction, including 
	without limitation the rights to use, copy, modify, merge, publish, 
	distribute, and/or sell copies of the Software, and to permit persons 
	to whom the Software is furnished to do so, provided that the above 
	copyright notice(s) and this permission notice appear in all copies of 
	the Software and that both the above copyright notice(s) and this 
	permission notice appear in supporting documentation. 

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 

	Except as contained in this notice, the name of a copyright holder 
	shall not be used in advertising or otherwise to promote the sale, use 
	or other dealings in this Software without prior written authorization 
	of the copyright holder.
*/
import java.util.*;
import javax.telephony.media.*;
/**
 * This is a holder class that allows Symbols to be serialized and re-constituted.
 * Note that it depends on the Signal values being accessible through hashCode().
 * Creation date: (2000-03-08 10:46:23)
 * @author: Richard Deadman
 */
public class SymbolHolder implements java.io.Serializable {
	static final long serialVersionUID = -6638508529572869814L;
	
	private int value = 0;

	private transient Symbol symbol = null;
/**
 * Create a holder for an Symbol.
 * Relies on hashCode() returning the value!
 * Creation date: (2000-03-08 11:18:53)
 * @author: Richard Deadman
 * @param sym The symbol to hold a value for
 */
public SymbolHolder(Symbol sym) {
	super();
	
	this.symbol = sym;
	this.value = sym.hashCode();
}
/**
 * Utility method to morph an array of Symbols into an array of Symbol Holders.
 * Creation date: (2000-04-25 1:02:35)
 * @author: Richard Deadman
 * @return An array of serializable SymbolHolders.
 * @param syms The Symbol set to translate.
 */
public static SymbolHolder[] create(Symbol[] syms) {
	SymbolHolder[] set = new SymbolHolder[syms.length];
	for (int i = 0; i < syms.length; i++) {
		set[i] = new SymbolHolder(syms[i]);
	}
	return set;
}
/**
 * Utility method to create a Dictionary of SymbolHolders from an Dictionary of Symbols.
 * Creation date: (2000-04-25 1:02:35)
 * @author: Richard Deadman
 * @param dict An dictionary of serializable SymbolHolders.
 * @return The Symbol Dictionary reconstituted.
 */
public static Dictionary<SymbolHolder, SymbolHolder> create(Dictionary<Symbol, Symbol> dict) {
	if (dict == null)
		return null;
	Dictionary<SymbolHolder, SymbolHolder> holders = new Hashtable<SymbolHolder, SymbolHolder>();
	Enumeration<Symbol> e = dict.keys();
	while (e.hasMoreElements()) {
		Symbol key = e.nextElement();
		holders.put(new SymbolHolder(key), new SymbolHolder(dict.get(key)));
	}
	return holders;
}
/**
 * Utility method to morph an array of SymbolHolders into an array of Symbols.
 * Creation date: (2000-04-25 1:02:35)
 * @author: Richard Deadman
 * @param holders An array of serializable SymbolHolders.
 * @return The Symbol set reconstituted.
 */
public static Symbol[] decode(SymbolHolder[] holders) {
	if (holders == null)
		return null;
	Symbol[] set = new Symbol[holders.length];
	for (int i = 0; i < holders.length; i++) {
		set[i] = holders[i].getSymbol();
	}
	return set;
}
/**
 * Utility method to decode a Dictionary of SymbolHolders into a Dictionary of Symbols.
 * Creation date: (2000-04-25 1:02:35)
 * @author: Richard Deadman
 * @param dict An dictionary of serializable SymbolHolders.
 * @return The Symbol Dictionary reconstituted.
 */
public static Dictionary<Symbol, Symbol> decode(Dictionary<SymbolHolder, SymbolHolder> holders) {
	if (holders == null)
		return null;
	Dictionary<Symbol, Symbol> dict = new Hashtable<Symbol, Symbol>();
	Enumeration<SymbolHolder> e = holders.keys();
	while (e.hasMoreElements()) {
		SymbolHolder key = e.nextElement();
		dict.put(key.getSymbol(), holders.get(key).getSymbol());
	}
	return dict;
}
/**
 * Am I equal to another object?
 * Creation date: (2000-05-09 14:32:23)
 * @author: Richard Deadman
 * @param obj The object to compare to.
 * @return true if logically equal
 */
public boolean equals(Object obj) {
	if (obj instanceof SymbolHolder)
		return this.value == ((SymbolHolder)obj).value;
	return false;
}
/**
 * Lazy accessor for the Symbol.  Recreates it if the transient handle was lost.
 * Creation date: (2000-03-08 10:56:30)
 * @author: Richard Deadman
 */
public Symbol getSymbol() {
	if (this.symbol == null) {
		this.symbol = Symbol.getSymbol(this.value);
	}

	return this.symbol;
}
/**
 * Returns the hashCode to this object.
 * Creation date: (2000-05-09 14:32:23)
 * @author: Richard Deadman
 * @return int
 */
public int hashCode() {
	return this.value;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Holder for: " + this.getSymbol().toString();
}
}
