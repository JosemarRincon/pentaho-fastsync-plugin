package br.gov.go.saude.pentaho.fastsync.util;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TestWS {
	@Ignore
	@Test
	public void testExecKTR()  {

		String serverAdress = "http://pentaho8-homolog.saude-go.net";
		String URI = serverAdress + "/pentaho/plugin/cda/api/doQuery";
		String auth = "Basic YWRtaW46cGFzc3dvcmQ=";
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		queryParams.add("path", "/home/admin/paineis/boletim.cda");
		queryParams.add("dataAccessId", "cargaDM");
		queryParams.add("paramarquivo", "/opt/home/pentaho/INFLU.dbf");

		ExecuteETL.addParametros(queryParams);
		ExecuteETL.setURI(URI);
		ExecuteETL.setAuth(auth);
		ExecuteETL.executaRequisicao();
		ExecuteETL result = ExecuteETL.getResultadoRequesicao();

		System.out.println(result);

	}

	

}
