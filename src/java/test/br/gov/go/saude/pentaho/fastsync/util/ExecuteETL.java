package br.gov.go.saude.pentaho.fastsync.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MultivaluedMap;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class ExecuteETL {
	private Integer executionTime;
	private boolean executionResult;
	private Integer executionNrErrors;
	private Integer executionLinesRead;
	private Integer executionLinesWritten;
	private Integer executionLinesOutput;
	private Integer executionLinesRejected;
	private Integer executionLinesUpdated;
	private Integer executionLinesDeleted;
	private Integer executionFilesRetrieved;
	private Integer executionExitStatus;
	private String executionLogText;
	private String executionLogChannelId;
	private static MultivaluedMap<String, String> queryParams;
	private static String URI;
	private static String auth;
	private static JSONObject objJSON = new JSONObject();

	public static void addParametros(MultivaluedMap<String, String> queryParams) {
		ExecuteETL.queryParams = queryParams;
	}

	public static void setURI(String URI) {
		ExecuteETL.URI = URI;
	}

	public static void setAuth(String auth) {
		ExecuteETL.auth = auth;
	}

	@SuppressWarnings("unchecked")
	public static void executaRequisicao()  {
		Client c = Client.create();
		WebResource wr = c.resource(URI);

		ClientResponse response = null;
		response = wr.queryParams(queryParams).header("Content-Type", "application/json;charset=UTF-8")
				.header("Authorization", auth).get(ClientResponse.class);

		String json = response.getEntity(String.class);
		
		try{
			
			if(json.isEmpty() || json == null || json.contains("authentication")) {
				throw new Exception("Falha ao executar a consulta da API!\n"+json);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			
		}
		JsonParser parser = new JsonParser();
		JsonObject array = parser.parse(json).getAsJsonObject();

		objJSON.clear();
		for (int i = 0; i < array.getAsJsonArray("metadata").size(); i++) {
			String colName = array.getAsJsonArray("metadata").get(i).getAsJsonObject().get("colName").getAsString()
					.replaceAll("\"", "");
			JsonElement value = array.getAsJsonArray("resultset").get(0).getAsJsonArray().get(i);
			objJSON.put(colName, value);
		}
	}
	
	public static void executaRequisicao2() {
		

		System.out.println(URI);
		System.out.println(queryParams);
		System.out.println(auth);

		try {

			URL url = new URL(URI);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		  }


	}

	public static ExecuteETL getResultadoRequesicao() {
		Gson gson = new Gson();
		return gson.fromJson(objJSON.toJSONString(), ExecuteETL.class);
	}

	

	public Integer getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Integer executionTime) {
		this.executionTime = executionTime;
	}

	public boolean isExecutionResult() {
		return executionResult;
	}

	public void setExecutionResult(boolean executionResult) {
		this.executionResult = executionResult;
	}

	public Integer getExecutionNrErrors() {
		return executionNrErrors;
	}

	public void setExecutionNrErrors(Integer executionNrErrors) {
		this.executionNrErrors = executionNrErrors;
	}

	public Integer getExecutionLinesRead() {
		return executionLinesRead;
	}

	public void setExecutionLinesRead(Integer executionLinesRead) {
		this.executionLinesRead = executionLinesRead;
	}

	public Integer getExecutionLinesWritten() {
		return executionLinesWritten;
	}

	public void setExecutionLinesWritten(Integer executionLinesWritten) {
		this.executionLinesWritten = executionLinesWritten;
	}

	public Integer getExecutionLinesOutput() {
		return executionLinesOutput;
	}

	public void setExecutionLinesOutput(Integer executionLinesOutput) {
		this.executionLinesOutput = executionLinesOutput;
	}

	public Integer getExecutionLinesRejected() {
		return executionLinesRejected;
	}

	public void setExecutionLinesRejected(Integer executionLinesRejected) {
		this.executionLinesRejected = executionLinesRejected;
	}

	public Integer getExecutionLinesUpdated() {
		return executionLinesUpdated;
	}

	public void setExecutionLinesUpdated(Integer executionLinesUpdated) {
		this.executionLinesUpdated = executionLinesUpdated;
	}

	public Integer getExecutionLinesDeleted() {
		return executionLinesDeleted;
	}

	public void setExecutionLinesDeleted(Integer executionLinesDeleted) {
		this.executionLinesDeleted = executionLinesDeleted;
	}

	public Integer getExecutionFilesRetrieved() {
		return executionFilesRetrieved;
	}

	public void setExecutionFilesRetrieved(Integer executionFilesRetrieved) {
		this.executionFilesRetrieved = executionFilesRetrieved;
	}

	public Integer getExecutionExitStatus() {
		return executionExitStatus;
	}

	public void setExecutionExitStatus(Integer executionExitStatus) {
		this.executionExitStatus = executionExitStatus;
	}

	public String getExecutionLogText() {
		return executionLogText;
	}

	public void setExecutionLogText(String executionLogText) {
		this.executionLogText = executionLogText;
	}

	public String getExecutionLogChannelId() {
		return executionLogChannelId;
	}

	public void setExecutionLogChannelId(String executionLogChannelId) {
		this.executionLogChannelId = executionLogChannelId;
	}

	@Override
	public String toString() {
		return "ResultadoExecKTR [executionTime=" + executionTime + ", executionResult=" + executionResult
				+ ", executionNrErrors=" + executionNrErrors + ", executionLinesRead=" + executionLinesRead
				+ ", executionLinesWritten=" + executionLinesWritten + ", executionLinesOutput=" + executionLinesOutput
				+ ", executionLinesRejected=" + executionLinesRejected + ", executionLinesUpdated="
				+ executionLinesUpdated + ", executionLinesDeleted=" + executionLinesDeleted
				+ ", executionFilesRetrieved=" + executionFilesRetrieved + ", executionExitStatus="
				+ executionExitStatus + ", executionLogText=" + executionLogText + ", executionLogChannelId="
				+ executionLogChannelId + "]";
	}

}
