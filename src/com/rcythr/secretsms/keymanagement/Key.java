package com.rcythr.secretsms.keymanagement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Key {
	public String displayName;
	public byte[] permanentKey;
	
	public Key() {
		permanentKey = null;
	}
	
	public static Key readData(DataInputStream stream) throws IOException {
		
		Key result = new Key();
		
		//Read display name
		result.displayName = stream.readUTF();
		
		//Read in permanent key
		if(stream.readBoolean()) {
			result.permanentKey = new byte[32];
			stream.read(result.permanentKey);
		} else {
			result.permanentKey = null;
		}
		
		return result;
	}
	
	public void writeData(DataOutputStream stream) throws IOException {
		
		stream.writeUTF(displayName);
		
		//Write out the permanent key
		if(permanentKey != null) {
			stream.writeBoolean(true);
			stream.write(permanentKey);
		} else {
			stream.writeBoolean(false);
		}
	}
}
