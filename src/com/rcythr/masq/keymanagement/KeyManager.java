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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;

import com.rcythr.masq.SetupActivity;
import com.rcythr.masq.util.AES;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

/**
 * Singleton class used to store key data to the file system.
 * 
 * @author Richard Laughlin
 */
public class KeyManager {
	
	private static final String KEYSTORE = "KEYSTORE";
	private static final String PREFERENCES_NAME = "com.rcythr.masq";
	private static final String USE_INTERNAL_STORAGE = "useInternalStorage";
	private static final String PASSWORD_PROTECTED = "passwordProtected";
	private static final String SETUP_COMPLETE = "setupComplete";
	
	private HashMap<String, Key> lookup;
	private boolean setupComplete;
	private boolean internalStorage;
	private boolean passwordProtected;
	private byte[] keyStoreKey;
	
	private boolean storeLoaded = false;
	
	private static KeyManager instance = new KeyManager();
	public static KeyManager getInstance() {
		return instance;
	}
	
	/**
	 * Create a new KeyManager from the information in the context
	 */
	protected KeyManager() {
		lookup = new HashMap<String, Key>();
	}
	
	public void init(Activity context) throws InvalidCipherTextException, IOException {
		if(!storeLoaded) {
			SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
			internalStorage = preferences.getBoolean(USE_INTERNAL_STORAGE, true);
			passwordProtected = preferences.getBoolean(PASSWORD_PROTECTED, true);
			setupComplete = preferences.getBoolean(SETUP_COMPLETE, false);
			if(passwordProtected) {
				Intent intent = new Intent(context, SetupActivity.class);
				context.startActivity(intent);
			} else {
				load(context);
			}
		}
	}
	
	/**
	 * @return a java.io.File to the keystore location on the sdcard
	 */
	private File getSDCardFile() {
		
		//First make sure the directories exist
		File dir = new File(Environment.getExternalStorageDirectory(), "Android/data/com.rcythr.masq/files/");
		dir.mkdirs();
		
		//Return the actual location
		return new File(dir, KEYSTORE);
		
	}
	
	/**
	 * Returns a file in stream for a keystore. It will use the settings loaded to determine where it is.
	 * @param context the context to use
	 * @return the opened FileInputStream 
	 * 
	 * @throws FileNotFoundException if the file is not found
	 */
	private FileInputStream getAssociatedInFileStream(Context context) throws FileNotFoundException {
		if(internalStorage) {
			return context.openFileInput(KEYSTORE);
		} else {
			return new FileInputStream(getSDCardFile());
		}
	}
	
	/**
	 * Returns a file out stream for a keystore. It will use the settings loaded to determine where it goes.
	 * @param context the context to use
	 * @return the opened FileInputStream 
	 * 
	 * @throws FileNotFoundException if the file is not found
	 */
	private FileOutputStream getAssociatedOutFileStream(Context context) throws FileNotFoundException {
		if(internalStorage) {
			return context.openFileOutput(KEYSTORE, Context.MODE_PRIVATE);
		} else {
			return new FileOutputStream(getSDCardFile());
		}
	}
	
	/**
	 * Load a Keystore. Uses either internal or external memory depending on settings.
	 * @param context the context to use.
	 * 
	 * @throws IOException if the stream is bad
	 * @throws InvalidCipherTextException if the key or data is bad
	 */
	public void load(Context context) throws IOException, InvalidCipherTextException {
		
		DataInputStream stream = null;
		if(passwordProtected) {
			byte[] data = AES.handle(false, IOUtils.toByteArray(getAssociatedInFileStream(context)), keyStoreKey);
			stream = new DataInputStream(new ByteArrayInputStream(data));
		} else {
			stream = new DataInputStream(getAssociatedInFileStream(context));
		}
		
		String type = stream.readUTF();
		if(type.equals("RCYTHR1")) {
			//Valid
			int count = stream.readInt();
			for(int i=0; i < count; ++i) {
				String name = stream.readUTF();
				lookup.put(name, Key.readData(stream));
			}
		} else {
			throw new IOException("Bad Format");
		}
		
		storeLoaded = true;
	}
	
	/**
	 * Saves the Keystore. Uses either internal or external memory depending on settings.
	 * @param context the context to use
	 * 
	 * @throws IOException if the outstream is somehow bad or interrupted
	 * @throws InvalidCipherTextException if the key is bad or the data is bad
	 */
	public void commit(Context context) throws IOException, InvalidCipherTextException {
		//Commit Preferences
		Editor edit = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
		edit.putBoolean(USE_INTERNAL_STORAGE, internalStorage);
		edit.putBoolean(PASSWORD_PROTECTED, passwordProtected);
		edit.putBoolean(SETUP_COMPLETE, true);
		edit.commit();
		
		//Commit Key Data
		DataOutputStream writer = null;
		ByteArrayOutputStream output = null;
		
		//Create the proper streams
		if(passwordProtected) {
			output = new ByteArrayOutputStream();
			writer = new DataOutputStream(output);
		} else {
			writer = new DataOutputStream(getAssociatedOutFileStream(context));
		}
		
		//Write everything out to the stream
		writer.writeUTF("RCYTHR1"); //Special indicator to determine if we decrypt properly
		writer.writeInt(lookup.size());
		for(Entry<String, Key> entry : lookup.entrySet()) {
			writer.writeUTF(entry.getKey());
			entry.getValue().writeData(writer);
		}
		writer.writeByte(1); //Prevent null padding from causing too much truncation.
		
		writer.flush();
		
		//If we're password protecting we still need to encrypt and output to file
		if(passwordProtected) {
			OutputStream finalOut = getAssociatedOutFileStream(context);
			finalOut.write(AES.handle(true, output.toByteArray(), keyStoreKey));
			finalOut.close();
		}
		
		writer.close();
	}

	/**
	 * Deletes the keystore from internal or external memory depending on settings
	 * @param context the context to use
	 */
	public void delete(Context context) {
		
		//Perform the delete.
		if(internalStorage) {
			context.deleteFile(KEYSTORE);
		} else {
			getSDCardFile().delete();
		}
		
		//Clear out shared preferences because they are not longer valid
		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		preferences.edit().clear().commit();
	}
	
	/**
	 * Move Keystore from internal to external memory or vice versa depending on settings
	 * @param context the context to use
	 * @return true if successful, false otherwise
	 */
	public boolean swap(Context context) {
		boolean initialState = internalStorage;
		try {
			//Toggle it
			internalStorage ^= true;
			
			//Create it all new
			commit(context);
			
			//Delete the old location
			if(!internalStorage) {
				context.deleteFile(KEYSTORE);
			} else {
				getSDCardFile().delete();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			internalStorage = initialState;
			return false;
		}
		return true;
	}
	
	/**
	 * @return the lookup
	 */
	public HashMap<String, Key> getLookup() {
		return lookup;
	}

	/**
	 * @param lookup the lookup to set
	 */
	public void setLookup(HashMap<String, Key> lookup) {
		this.lookup = lookup;
	}

	/**
	 * @return the setupComplete
	 */
	public boolean isSetupComplete() {
		return setupComplete;
	}

	/**
	 * @param setupComplete the setupComplete to set
	 */
	public void setSetupComplete(boolean setupComplete) {
		this.setupComplete = setupComplete;
	}

	/**
	 * @return the internalStorage
	 */
	public boolean isInternalStorage() {
		return internalStorage;
	}

	/**
	 * @param internalStorage the internalStorage to set
	 */
	public void setInternalStorage(boolean internalStorage) {
		this.internalStorage = internalStorage;
	}

	/**
	 * @return the passwordProtected
	 */
	public boolean isPasswordProtected() {
		return passwordProtected;
	}

	/**
	 * @param passwordProtected the passwordProtected to set
	 */
	public void setPasswordProtected(boolean passwordProtected) {
		this.passwordProtected = passwordProtected;
	}

	/**
	 * @return the keyStoneKey
	 */
	public byte[] getKeyStoneKey() {
		return keyStoreKey;
	}

	/**
	 * @param keyStoneKey the keyStoneKey to set
	 */
	public void setKeyStoreKey(byte[] keyStoneKey) {
		this.keyStoreKey = keyStoneKey;
	}
	
	
}
