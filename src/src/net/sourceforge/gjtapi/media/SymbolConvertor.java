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
import java.util.Vector;

import javax.telephony.media.*;
/**
 * Static utility class for converting Symbols to and from Strings.
 * Creation date: (2000-03-31 15:26:18)
 * @author: Richard Deadman
 */
public class SymbolConvertor implements SignalConstants {
/**
 * Convert a set of Symbols to a string of DTMF signals.  Unknown characters are ignored.
 * Creation date: (2000-03-14 8:06:08)
 * @author: Richard Deadman
 * @param syms The DTMF Signals to convert to a string.
 * @return The string-encoding of the DTMF
 */
public static String convert(Symbol[] syms) {
	return convert(syms, "");
}

/**
 * Convert a set of Symbols to a string of DTMF signals.  Unknown characters are replaced
 * with the placeholder string.
 * Creation date: (2000-03-14 8:06:08)
 * @author: Richard Deadman
 * @param syms The DTMF Signals to convert to a string.
 * @return The string-encoding of the DTMF
 */
public static String convert(Symbol[] syms, String placeholder) {
	StringBuffer msg = new StringBuffer();
	for (int i = 0; i < syms.length; i++) {
		if (syms[i].equals(SignalConstants.v_DTMF0))
			msg.append('0');
		else if (syms[i].equals(SignalConstants.v_DTMF1))
			msg.append('1');
		else if (syms[i].equals(SignalConstants.v_DTMF2))
			msg.append('2');
		else if (syms[i].equals(SignalConstants.v_DTMF3))
			msg.append('3');
		else if (syms[i].equals(SignalConstants.v_DTMF4))
			msg.append('4');
		else if (syms[i].equals(SignalConstants.v_DTMF5))
			msg.append('5');
		else if (syms[i].equals(SignalConstants.v_DTMF6))
			msg.append('6');
		else if (syms[i].equals(SignalConstants.v_DTMF7))
			msg.append('7');
		else if (syms[i].equals(SignalConstants.v_DTMF8))
			msg.append('8');
		else if (syms[i].equals(SignalConstants.v_DTMF9))
			msg.append('9');
		else if (syms[i].equals(SignalConstants.v_DTMFStar))
			msg.append('*');
		else if (syms[i].equals(SignalConstants.v_DTMFHash))
			msg.append('#');
		else if (syms[i].equals(SignalConstants.v_DTMFA))
			msg.append('A');
		else if (syms[i].equals(SignalConstants.v_DTMFB))
			msg.append('B');
		else if (syms[i].equals(SignalConstants.v_DTMFC))
			msg.append('C');
		else if (syms[i].equals(SignalConstants.v_DTMFD))
			msg.append('D');
		else
			msg.append(placeholder);
	}
	return msg.toString();
}
/**
 * Convert a string of DTMF signals to symbols.  Unknown characters are ignored.
 * Creation date: (2000-03-14 8:06:08)
 * @author: Richard Deadman
 * @return javax.telephony.media.Symbol[]
 * @param signals java.lang.String
 */
public static Symbol[] convert(String signals) {
	Vector<Symbol> v = new Vector<Symbol>();
	int size = signals.length();
	Symbol s;
	for (int i = 0; i < size; i++) {
		s = null;
		switch (signals.charAt(i)) {
			case '0': {
				s = v_DTMF0;
				break;
			}
			case '1': {
				s = v_DTMF1;
				break;
			}
			case '2': {
				s = v_DTMF2;
				break;
			}
			case '3': {
				s = v_DTMF3;
				break;
			}
			case '4': {
				s = v_DTMF4;
				break;
			}
			case '5': {
				s = v_DTMF5;
				break;
			}
			case '6': {
				s = v_DTMF6;
				break;
			}
			case '7': {
				s = v_DTMF7;
				break;
			}
			case '8': {
				s = v_DTMF8;
				break;
			}
			case '9': {
				s = v_DTMF9;
				break;
			}
			case 'a':
			case 'A': {
				s = v_DTMFA;
				break;
			}
			case 'b':
			case 'B': {
				s = v_DTMFB;
				break;
			}
			case 'c':
			case 'C': {
				s = v_DTMFC;
				break;
			}
			case 'd':
			case 'D': {
				s = v_DTMFD;
				break;
			}
			case '*': {
				s = v_DTMFStar;
				break;
			}
			case '#': {
				s = v_DTMFHash;
				break;
			}
		}
		if (s != null)
			v.add(s);
	}
	return (Symbol[])v.toArray(new Symbol[0]);
}
/**
 * Convert a Symbol to a DTMF character.  Unknown characters return ' '.
 * Creation date: (2000-03-14 8:06:08)
 * @author: Richard Deadman
 * @param syms The DTMF Signal to convert to a character.
 * @return The character-encoding of the DTMF
 */
public static char toChar(Symbol sym) {
	if (sym.equals(SignalConstants.v_DTMF0))
		return ('0');
	else
		if (sym.equals(SignalConstants.v_DTMF1))
			return ('1');
		else
			if (sym.equals(SignalConstants.v_DTMF2))
				return ('2');
			else
				if (sym.equals(SignalConstants.v_DTMF3))
					return ('3');
				else
					if (sym.equals(SignalConstants.v_DTMF4))
						return ('4');
					else
						if (sym.equals(SignalConstants.v_DTMF5))
							return ('5');
						else
							if (sym.equals(SignalConstants.v_DTMF6))
								return ('6');
							else
								if (sym.equals(SignalConstants.v_DTMF7))
									return ('7');
								else
									if (sym.equals(SignalConstants.v_DTMF8))
										return ('8');
									else
										if (sym.equals(SignalConstants.v_DTMF9))
											return ('9');
										else
											if (sym.equals(SignalConstants.v_DTMFStar))
												return ('*');
											else
												if (sym.equals(SignalConstants.v_DTMFHash))
													return ('#');
												else
													return (' ');
}
}
