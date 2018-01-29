package br.gov.go.saude.pentaho.fastsync.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Output {
	private String message;
	private Boolean error;
	private String error_message;

	public Output() {
		setMessage("");
		setError(Boolean.valueOf(false));
		setError_message("");
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
}
