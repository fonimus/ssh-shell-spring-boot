package com.github.fonimus.ssh.shell.handler;

/**
 * Pretty json object
 */
public class PrettyJson<T> {

	private T object;

	private boolean prettify = true;

	public PrettyJson(T object) {
		this.object = object;
	}

	public PrettyJson(T object, boolean prettify) {
		this.object = object;
		this.prettify = prettify;
	}

	public T getObject() {
		return object;
	}

	public boolean isPrettify() {
		return prettify;
	}
}
