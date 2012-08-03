/**	This file is part of Masq.

    Masq is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Masq is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Masq.  If not, see <http://www.gnu.org/licenses/>.
**/

package com.rcythr.masq.keymanagement;

/**
 * 
 * A simple contact class that is used to store contacts.
 * 
 * @author Richard Laughlin
 */
public class Contact {
	
	/**
	 * The phone number of the contact. Stripped of all extraneous characters.
	 */
	public String address;
	
	/**
	 * The display name of the contact
	 */
	public String name;
}