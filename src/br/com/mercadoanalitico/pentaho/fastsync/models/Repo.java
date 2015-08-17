package br.com.mercadoanalitico.pentaho.fastsync.models;

import java.util.Collection;
import java.util.Map;

public class Repo {

	private Collection<String> itemsList;
	private Map<String,String> idsList;
	
	public Collection<String> getItemsList() {
		return itemsList;
	}
	
	public void setItemsList(Collection<String> itemsList) {
		this.itemsList = itemsList;
	}
	
	public Map<String, String> getIdsList() {
		return idsList;
	}
	
	public void setIdsList(Map<String, String> idsList) {
		this.idsList = idsList;
	}
	
	
}
