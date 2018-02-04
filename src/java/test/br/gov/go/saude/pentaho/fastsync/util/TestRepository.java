package br.gov.go.saude.pentaho.fastsync.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import br.gov.go.saude.pentaho.fastsync.util.FileSystem;
import br.gov.go.saude.pentaho.fastsync.util.Repository;

public class TestRepository {
	String jcr = "C:\\\\developer\\\\pentaho\\\\8\\\\pentaho-server\\\\tomcat\\\\temp\\\\siconti\\\\paineis\\\\boletim.cdfde";
	String fs = "C:\\developer\\pentaho\\8\\pentaho-server\\pentaho-solutions\\siconti\\paineis\\boletim.cdfde";

	@Ignore
	@Test
	public void fileDiff() {

		try {
			Repository.SYNC = "fs";
			assertEquals(true, FileSystem.isDiffForTypeFile(jcr, fs));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Ignore
	@Test
	public void fileDiff2() {

		try {
			Repository.SYNC = "fs";
			assertEquals(true, FileSystem.gFileFileDiff(jcr, fs));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void zipFileTest() {
		String local = "C:\\developer\\pentaho\\8\\pentaho-server\\tomcat\\temp\\indicadores";

		Repository.SYNC = "fs";
		Zip.transformEntry(local, "painel.cda");

	}

}
