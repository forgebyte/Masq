package com.rcythr.secretsms.keymanagement;

public class PermanentKey extends KeyProvider {

	private Key key;
	
	public PermanentKey(String name, String contact) {
		super(name, contact);
	}

	@Override
	public void addKey(Key newKey) {
		this.key = newKey;
	}

	@Override
	public Key getNextKey() {
		return key;
	}

	@Override
	public boolean hasKey() {
		return true;
	}

}
