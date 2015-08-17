package br.com.mercadoanalitico.pentaho.fastsync.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Output {

	private String message;
	private Boolean error;
	private String error_message;
	
	public Output() {
		setMessage("");
		setError(false);
		setError_message("");
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
