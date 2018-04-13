package br.gov.go.saude.pentaho.fastsync.util;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TestWS {
	//@Ignore
	@Test
	public void testExecKTR()  {

		String serverAdress = "http://pentaho8-homolog.saude-go.net";
		//String serverAdress = "http://localhost:8080";
		String URI = serverAdress + "/pentaho/plugin/cda/api/doQuery";
		// execute:executeuser
		String auth = "Basic ZXhlY3V0ZTpleGVjdXRldXNlcg==";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		queryParams.add("path", "/siconti/paineis/boletim.cda");
		queryParams.add("dataAccessId", "cargaDM");
		//queryParams.add("paramarquivo", "/opt/home/pentaho/INFLU.dbf");
		//queryParams.add("paramarquivo", "/home/josemar/data_science/temp/file_siconti/INFLUO2017-2.dbf");

		ExecuteETL.addParametros(queryParams);
		ExecuteETL.setURI(URI);
		ExecuteETL.setAuth(auth);
		ExecuteETL.executaRequisicao();
		ExecuteETL result = ExecuteETL.getResultadoRequesicao();

		System.out.println(result);

	}

	

}
