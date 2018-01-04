package br.gov.go.saude.pentaho.fastsync.models;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;

public class Repo {
	private Collection<String> itemsList;
	private Map<String, String> idsList;
	private Map<String, Date> modifiedDateList;
	private List<RepositoryFileDto> repoFiles;

	public Collection<String> getItemsList() {
		return this.itemsList;
	}

	public void setItemsList(Collection<String> itemsList) {
		this.itemsList = itemsList;
	}

	public Map<String, String> getIdsList() {
		return this.idsList;
	}

	public void setIdsList(Map<String, String> idsList) {
		this.idsList = idsList;
	}

	public Map<String, Date> getModifiedDateList() {
		return this.modifiedDateList;
	}

	public void setModifiedDateList(Map<String, Date> modifiedDateList) {
		this.modifiedDateList = modifiedDateList;
	}

	public List<RepositoryFileDto> getRepoFiles() {
		return repoFiles;
	}

	public void setRepoFiles(List<RepositoryFileDto> repoFiles) {
		this.repoFiles = repoFiles;
	}
	
}
