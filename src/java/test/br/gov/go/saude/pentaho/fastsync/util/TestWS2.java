package br.gov.go.saude.pentaho.fastsync.util;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TestWS2 {
	@Ignore
	//@Test
	public void testRequest()  {

		
		String serverAdress = "http://localhost:8080";
		String URI = serverAdress + "/SCSEAWebservice/rest/Imoveis/RegistroDiario";

		// execute:executeuser
		String auth = "Basic teste";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		queryParams.add("DtInicio", "01/07/2018");
		queryParams.add("DtFim", "03/07/2018");
		//queryParams.add("paramarquivo", "/opt/home/pentaho/INFLU.dbf");
		//queryParams.add("paramarquivo", "/home/josemar/data_science/temp/file_siconti/INFLUO2017-2.dbf");

		ExecuteETL.addParametros(queryParams);
		ExecuteETL.setURI(URI);
		ExecuteETL.setAuth(auth);
		ExecuteETL.executaRequisicao2();
		

	}
	
	

	

}
