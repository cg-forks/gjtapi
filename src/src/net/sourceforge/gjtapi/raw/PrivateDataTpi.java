package net.sourceforge.gjtapi.raw;

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
import net.sourceforge.gjtapi.CallId;
/**
 * These are the methods required by TelephonyProviders that support private data for JTAPI
 * Creation date: (2000-10-04 13:46:43)
 * @author: Richard Deadman
 */
public interface PrivateDataTpi extends BasicJtapiTpi {
/**
 * Get the PrivateData from a raw TelephonyProviderInterface Object.
 * This applies to the previous action on the Object.
 * <P>This method is mapped to any of six logical raw TPI provider objects based on the following
 * mapping, where the three columns represent the three method parameters:
 * <table BORDER >
 *<tr BGCOLOR="#C0C0C0">
 *<td></td>
 *
 *<th>Call</th>
 *
 *<th>Address</th>
 *
 *<th>Terminal</th>
 *</tr>
 *
 *<tr>
 *<td>Provider</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Call</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Address</td>
 *
 *<td></td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Terminal</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td>X</td>
 *</tr>
 *
 *<tr>
 *<td>Connection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>TerminalConnection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *</tr>
 *
 * <caption ALIGN=BOTTOM><i>"blank" indicates void</i></caption>
 * </table>
 * Creation date: (2000-08-05 22:25:45)
 * @return Any object.  IF PrivateDataCapabilities.canGetPrivateData() returns false, this will return null.
 * @param call A CallId or void
 * @param address An Address name or void
 * @param terminal A Terminal name or void
 */
Object getPrivateData(CallId call, String address, String terminal);
/**
 * Sends PrivateData for a raw TelephonyProviderInterface Object for it to act upon immediately.
 * <P>This method is mapped to any of six logical raw TPI provider objects based on the following
 * mapping, where the three columns represent the three method parameters:
 * <table BORDER >
 *<tr BGCOLOR="#C0C0C0">
 *<td></td>
 *
 *<th>Call</th>
 *
 *<th>Address</th>
 *
 *<th>Terminal</th>
 *</tr>
 *
 *<tr>
 *<td>Provider</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Call</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Address</td>
 *
 *<td></td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Terminal</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td>X</td>
 *</tr>
 *
 *<tr>
 *<td>Connection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>TerminalConnection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *</tr>
 *
 * <caption ALIGN=BOTTOM><i>"blank" indicates void</i></caption>
 * </table>
 * Creation date: (2000-08-05 22:25:45)
 * return Any object.  If the object's Capabilities.canSendPrivateData() returns false, this returns null.
 * @param call A CallId or void
 * @param address An Address name or void
 * @param terminal A Terminal name or void
 * @param data Any object.
 */
Object sendPrivateData(CallId call, String address, String terminal, Object data);
/**
 * Set the PrivateData for a raw TelephonyProviderInterface Object.
 * This applies to the next action on the Object.
 * <P>This method is mapped to any of six logical raw TPI provider objects based on the following
 * mapping, where the three columns represent the three method parameters:
 * <table BORDER >
 *<tr BGCOLOR="#C0C0C0">
 *<td></td>
 *
 *<th>Call</th>
 *
 *<th>Address</th>
 *
 *<th>Terminal</th>
 *</tr>
 *
 *<tr>
 *<td>Provider</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Call</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Address</td>
 *
 *<td></td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Terminal</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td>X</td>
 *</tr>
 *
 *<tr>
 *<td>Connection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>TerminalConnection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *</tr>
 *
 * <caption ALIGN=BOTTOM><i>"blank" indicates void</i></caption>
 * </table>
 * Creation date: (2000-08-05 22:25:45)
 * @param call A CallId or void
 * @param address An Address name or void
 * @param terminal A Terminal name or void
 * @param data Any object.
 */
void setPrivateData(CallId call, String address, String terminal, Object data);
}
