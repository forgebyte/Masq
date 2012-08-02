package com.rcythr.secretsms.keymanagement;

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

import com.rcythr.secretsms.util.AES;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

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
	private byte[] keyStoneKey;
	
	public static KeyManager instance;
	
	public KeyManager(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		internalStorage = preferences.getBoolean(USE_INTERNAL_STORAGE, true);
		passwordProtected = preferences.getBoolean(PASSWORD_PROTECTED, true);
		setupComplete = preferences.getBoolean(SETUP_COMPLETE, false);
		
		lookup = new HashMap<String, Key>();
	}
	
	private File getSDCardFile() {
		File dir = new File(Environment.getExternalStorageDirectory(), "Android/data/com.rcythr.masq/files/");
		dir.mkdirs();
		return new File(dir, KEYSTORE);
		
	}
	
	private FileInputStream getAssociatedInFileStream(Context context) throws FileNotFoundException {
		if(internalStorage) {
			return context.openFileInput(KEYSTORE);
		} else {
			return new FileInputStream(getSDCardFile());
		}
	}
	
	private FileOutputStream getAssociatedOutFileStream(Context context) throws FileNotFoundException {
		if(internalStorage) {
			return context.openFileOutput(KEYSTORE, Context.MODE_WORLD_WRITEABLE);
		} else {
			return new FileOutputStream(getSDCardFile());
		}
	}
	
	public void load(Context context) throws IOException, InvalidCipherTextException {
		DataInputStream stream = null;
		if(passwordProtected) {
			byte[] data = AES.handle(false, IOUtils.toByteArray(getAssociatedInFileStream(context)), keyStoneKey);
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
	}
	
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
		writer.writeUTF("RCYTHR1");
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
			finalOut.write(AES.handle(true, output.toByteArray(), keyStoneKey));
			finalOut.close();
		}
		
		writer.close();
	}

	public void delete(Context context) {
		if(internalStorage) {
			context.deleteFile(KEYSTORE);
		} else {
			getSDCardFile().delete();
		}
		
		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		preferences.edit().clear().commit();
	}
	
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
		return keyStoneKey;
	}

	/**
	 * @param keyStoneKey the keyStoneKey to set
	 */
	public void setKeyStoneKey(byte[] keyStoneKey) {
		this.keyStoneKey = keyStoneKey;
	}
	
	
}
