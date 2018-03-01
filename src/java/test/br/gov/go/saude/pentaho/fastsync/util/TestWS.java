package br.gov.go.saude.pentaho.fastsync.util;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TestWS {

	@Test
	public void testExecKTR() {

		String serverAdress = "http://localhost:8080";
		String URI = serverAdress+"/pentaho/plugin/cda/api/doQuery";
		String auth = "Basic YWRtaW46cGFzc3dvcmQ=";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		
		queryParams.add("path", "/private/siconti/paineis/boletim.cda");
		queryParams.add("dataAccessId", "cargaDM");
		queryParams.add("paramarquivo", "/home/josemar/data_science/temp/file_siconti/INFLUO2017.dbf");

		ExecuteETL.addParametros(queryParams);
		ExecuteETL.setURI(URI);
		ExecuteETL.setAuth(auth);
		ExecuteETL.executaRequisicao();
		ExecuteETL result = ExecuteETL.getResultadoRequesicao();
		
		System.out.println(result);
		
	}

}
