package br.gov.go.saude.pentaho.fastsync.util;

import javax.ws.rs.core.MultivaluedMap;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ExecuteETL {
	private Integer ExecutionTime;
	private boolean ExecutionResult;
	private Integer ExecutionNrErrors;
	private Integer ExecutionLinesRead;
	private Integer ExecutionLinesWritten;
	private Integer ExecutionLinesOutput;
	private Integer ExecutionLinesRejected;
	private Integer ExecutionLinesUpdated;
	private Integer ExecutionLinesDeleted;
	private Integer ExecutionFilesRetrieved;
	private Integer ExecutionExitStatus;
	private String ExecutionLogText;
	private String ExecutionLogChannelId;
	private static MultivaluedMap<String, String> queryParams;
	private static String URI;
	private static String auth;
	private static JSONObject objJSON;

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
	public static void executaRequisicao() {
		Client c = Client.create();
		WebResource wr = c.resource(URI);

		ClientResponse response = null;
		response = wr.queryParams(queryParams).header("Content-Type", "application/json;charset=UTF-8")
				.header("Authorization", auth).get(ClientResponse.class);

		String json = response.getEntity(String.class);
		JsonParser parser = new JsonParser();
		JsonObject array = parser.parse(json).getAsJsonObject();

		objJSON = new JSONObject();
		for (int i = 0; i < array.getAsJsonArray("metadata").size(); i++) {
			String colName = array.getAsJsonArray("metadata").get(i).getAsJsonObject().get("colName").getAsString()
					.replaceAll("\"", "");
			JsonElement value = array.getAsJsonArray("resultset").get(0).getAsJsonArray().get(i);
			objJSON.put(colName, value);
		}
	}

	public static ExecuteETL getResultadoRequesicao() {
		Gson gson = new Gson();
		return gson.fromJson(objJSON.toJSONString(), ExecuteETL.class);
	}

	public Integer getExecutionTime() {
		return ExecutionTime;
	}

	public void setExecutionTime(Integer executionTime) {
		ExecutionTime = executionTime;
	}

	public boolean isExecutionResult() {
		return ExecutionResult;
	}

	public void setExecutionResult(boolean executionResult) {
		ExecutionResult = executionResult;
	}

	public Integer getExecutionNrErrors() {
		return ExecutionNrErrors;
	}

	public void setExecutionNrErrors(Integer executionNrErrors) {
		ExecutionNrErrors = executionNrErrors;
	}

	public Integer getExecutionLinesRead() {
		return ExecutionLinesRead;
	}

	public void setExecutionLinesRead(Integer executionLinesRead) {
		ExecutionLinesRead = executionLinesRead;
	}

	public Integer getExecutionLinesWritten() {
		return ExecutionLinesWritten;
	}

	public void setExecutionLinesWritten(Integer executionLinesWritten) {
		ExecutionLinesWritten = executionLinesWritten;
	}

	public Integer getExecutionLinesOutput() {
		return ExecutionLinesOutput;
	}

	public void setExecutionLinesOutput(Integer executionLinesOutput) {
		ExecutionLinesOutput = executionLinesOutput;
	}

	public Integer getExecutionLinesRejected() {
		return ExecutionLinesRejected;
	}

	public void setExecutionLinesRejected(Integer executionLinesRejected) {
		ExecutionLinesRejected = executionLinesRejected;
	}

	public Integer getExecutionLinesUpdated() {
		return ExecutionLinesUpdated;
	}

	public void setExecutionLinesUpdated(Integer executionLinesUpdated) {
		ExecutionLinesUpdated = executionLinesUpdated;
	}

	public Integer getExecutionLinesDeleted() {
		return ExecutionLinesDeleted;
	}

	public void setExecutionLinesDeleted(Integer executionLinesDeleted) {
		ExecutionLinesDeleted = executionLinesDeleted;
	}

	public Integer getExecutionFilesRetrieved() {
		return ExecutionFilesRetrieved;
	}

	public void setExecutionFilesRetrieved(Integer executionFilesRetrieved) {
		ExecutionFilesRetrieved = executionFilesRetrieved;
	}

	public Integer getExecutionExitStatus() {
		return ExecutionExitStatus;
	}

	public void setExecutionExitStatus(Integer executionExitStatus) {
		ExecutionExitStatus = executionExitStatus;
	}

	public String getExecutionLogText() {
		return ExecutionLogText;
	}

	public void setExecutionLogText(String executionLogText) {
		ExecutionLogText = executionLogText;
	}

	public String getExecutionLogChannelId() {
		return ExecutionLogChannelId;
	}

	public void setExecutionLogChannelId(String executionLogChannelId) {
		ExecutionLogChannelId = executionLogChannelId;
	}

	@Override
	public String toString() {
		return "ResultadoExecKTR [ExecutionTime=" + ExecutionTime + ", ExecutionResult=" + ExecutionResult
				+ ", ExecutionNrErrors=" + ExecutionNrErrors + ", ExecutionLinesRead=" + ExecutionLinesRead
				+ ", ExecutionLinesWritten=" + ExecutionLinesWritten + ", ExecutionLinesOutput=" + ExecutionLinesOutput
				+ ", ExecutionLinesRejected=" + ExecutionLinesRejected + ", ExecutionLinesUpdated="
				+ ExecutionLinesUpdated + ", ExecutionLinesDeleted=" + ExecutionLinesDeleted
				+ ", ExecutionFilesRetrieved=" + ExecutionFilesRetrieved + ", ExecutionExitStatus="
				+ ExecutionExitStatus + ", ExecutionLogText=" + ExecutionLogText + ", ExecutionLogChannelId="
				+ ExecutionLogChannelId + "]";
	}

}
