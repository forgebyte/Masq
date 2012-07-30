package com.rcythr.secretsms.keymanagement;

public abstract class KeyProvider {
	private String name;
	private String contact;
	
	public KeyProvider(String name, String contact) {
		this.name = name;
		this.contact = contact;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	public String getContact() {
		return this.contact;
	}
	
	public abstract void addKey(Key newKey);
	
	public abstract Key getNextKey();
	
	public abstract boolean hasKey();
}
