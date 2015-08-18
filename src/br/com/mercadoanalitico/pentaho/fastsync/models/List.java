package br.com.mercadoanalitico.pentaho.fastsync.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class List {

	private ArrayList<String> update; 
	private ArrayList<String> create;
	private ArrayList<String> delete;

	private String message;
	private Boolean error;
	private String error_message;
	
	public List() {
		setMessage("");
		setError(false);
		setError_message("");

		update = new ArrayList<String>(); 
		create = new ArrayList<String>();
		delete = new ArrayList<String>();
	}
	
	public ArrayList<String> getUpdate() {
		return update;
	}
	
	public void setUpdate(ArrayList<String> update) {
		this.update = update;
	}
	
	public ArrayList<String> getCreate() {
		return create;
	}
	
	public void setCreate(ArrayList<String> create) {
		this.create = create;
	}
	
	public ArrayList<String> getDelete() {
		return delete;
	}
	
	public void setDelete(ArrayList<String> delete) {
		this.delete = delete;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public String getError_message() {
		return error_message;
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}
	
}
