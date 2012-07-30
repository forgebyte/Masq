package com.rcythr.secretsms.keymanagement;

import java.io.Serializable;
import java.util.LinkedList;

public class KeyQueue extends KeyProvider implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private LinkedList<Key> keys;
	
	public KeyQueue(String name, String contact) {
		super(name, contact);
		this.keys = new LinkedList<Key>();
	}
	
	public void addKey(Key newKey) {
		keys.add(newKey);
	}
	
	public Key getNextKey() {
		return keys.poll();
	}
	
	public boolean hasKey() {
		return !keys.isEmpty();
	}
}
