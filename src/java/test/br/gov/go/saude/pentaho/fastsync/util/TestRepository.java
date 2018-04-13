package br.gov.go.saude.pentaho.fastsync.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import br.gov.go.saude.pentaho.fastsync.util.FileSystem;
import br.gov.go.saude.pentaho.fastsync.util.Repository;

public class TestRepository {
	String jcr = "/home/josemar/data_science/pentaho/cbfl/dist-packages/pentaho-server/pentaho-solutions/sim_sinasc/cargas/sinasc/base_fato_para_host_db.kjb";
	String fs = "/home/josemar/data_science/pentaho/cbfl/dist-packages/pentaho-server/pentaho-solutions/sim_sinasc/cargas/sinasc/base_fato_para_host_db.kjb";

	@Ignore
	@Test
	public void fileDiff() {

		try {
			Repository.SYNC = "fs";
			Repository.DEBUG = true;
			assertEquals(false, FileSystem.isDiffForTypeFile(jcr, fs));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	@Ignore
	@Test
	public void zipFileTest() {
		String local = "";

		Repository.SYNC = "fs";
		Zip.transformEntry(local, "painel.cda");

	}

}
