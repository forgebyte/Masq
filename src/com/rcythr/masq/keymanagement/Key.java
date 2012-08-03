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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.rcythr.masq.util.AES;

/**
 * 
 * A simple key class that is used to associate a contact's address
 * with their display name and actual key.
 * 
 * @author Richard Laughlin
 */
public class Key {
	
	/**
	 * The display name of the contact
	 */
	public String displayName;
	
	/**
	 * The AES-256 key of the contact.
	 */
	public byte[] key;
	
	/**
	 * Creates a new Key
	 */
	public Key() {
		key = null;
	}
	
	/**
	 * Reads Key data in from an input stream.
	 * @param stream the stream to read
	 * @return the created key
	 * 
	 * @throws IOException if the stream is bad
	 */
	public static Key readData(DataInputStream stream) throws IOException {
		
		Key result = new Key();
		
		//Read display name
		result.displayName = stream.readUTF();
		
		//Read in permanent key
		if(stream.readBoolean()) {
			result.key = new byte[AES.AES_KEY_SIZE];
			stream.read(result.key);
		} else {
			result.key = null;
		}
		
		return result;
	}
	
	/**
	 * Saves key data to an output stream
	 * @param stream the stream
	 * 
	 * @throws IOException if the stream is somehow bad 
	 */
	public void writeData(DataOutputStream stream) throws IOException {
		
		stream.writeUTF(displayName);
		
		//Write out the permanent key
		if(key != null) {
			stream.writeBoolean(true);
			stream.write(key);
		} else {
			stream.writeBoolean(false);
		}
	}
}
