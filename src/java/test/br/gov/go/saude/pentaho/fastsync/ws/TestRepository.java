package br.gov.go.saude.pentaho.fastsync.ws;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import br.gov.go.saude.pentaho.fastsync.util.FileSystem;
import br.gov.go.saude.pentaho.fastsync.util.Repository;

public class TestRepository {
	@Ignore
	@Test
	public void fileDiff() {
		String jcr= "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/sim_sinasc2/sim_sinasc/cargas/sim/03_etl_est_vitais.ktr";
		String fs="/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/pentaho-solutions/sim_sinasc/cargas/sim/03_etl_est_vitais.ktr";
		try {
			Repository.SYNC ="jcr";
			assertEquals(true, FileSystem.isDiffForTypeFile(jcr,fs));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

}
