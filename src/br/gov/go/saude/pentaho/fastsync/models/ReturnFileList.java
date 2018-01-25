package br.gov.go.saude.pentaho.fastsync.models;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ReturnFileList {
	private ArrayList<String> update;
	private ArrayList<String> create;
	private ArrayList<String> delete;
	private ArrayList<String> exclude;
	private ArrayList<String> preserve;
	private String message;
	private Boolean error;
	private String error_message;

	public ReturnFileList() {
		setMessage("");
		setError(Boolean.valueOf(false));
		setError_message("");

		this.update = new ArrayList<String>();
		this.create = new ArrayList<String>();
		this.delete = new ArrayList<String>();
		this.exclude = new ArrayList<String>();
		setPreserve(new ArrayList<String>());
	}

	public ArrayList<String> getUpdate() {
		return this.update;
	}

	public void setUpdate(ArrayList<String> update) {
		this.update = update;
	}

	public ArrayList<String> getCreate() {
		return this.create;
	}

	public void setCreate(ArrayList<String> create) {
		this.create = create;
	}

	public ArrayList<String> getDelete() {
		return this.delete;
	}

	public void setDelete(ArrayList<String> delete) {
		this.delete = delete;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getError() {
		return this.error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public String getError_message() {
		return this.error_message;
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	public ArrayList<String> getExclude() {
		return this.exclude;
	}

	public void setExclude(ArrayList<String> exclude) {
		this.exclude = exclude;
	}

	public ArrayList<String> getPreserve() {
		return this.preserve;
	}

	public void setPreserve(ArrayList<String> preserve) {
		this.preserve = preserve;
	}

	public boolean isEmpty() {
		System.out.println("\n-----> update: " + update + "\n");
		System.out.println("\n-----> create: " + create + "\n");
		System.out.println("\n-----> delete: " + delete + "\n");
		
		System.out.println("\n-----> update: " + this.update.isEmpty() + "\n");
		System.out.println("\n-----> create: " + this.create.isEmpty() + "\n");
		System.out.println("\n-----> delete: " + this.delete.isEmpty() + "\n");

		if (this.update.isEmpty() && this.create.isEmpty() && this.delete.isEmpty())
			return true;

		return false;
	}
}
