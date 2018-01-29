package br.gov.go.saude.pentaho.fastsync.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.core.header.FormDataContentDisposition;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.security.PentahoLogoutHandler;
import org.springframework.transaction.CannotCreateTransactionException;
import br.gov.go.saude.pentaho.fastsync.models.Output;
import br.gov.go.saude.pentaho.fastsync.models.ReturnFileList;
import br.gov.go.saude.pentaho.fastsync.security.Login;
import br.gov.go.saude.pentaho.fastsync.util.FileSystem;
import br.gov.go.saude.pentaho.fastsync.util.PublishUtil;
import br.gov.go.saude.pentaho.fastsync.util.Repository;

@Path("/fastsync/api/")
public class FastSyncREST {
	@Context
	private HttpServletRequest request;
	@Context
	private HttpServletResponse response;

	@GET
	@Path("/publish")
	@Produces({ "application/json" })
	public Output publishSchema(@Context UriInfo info) {
		this.response.setHeader("Access-Control-Allow-Origin", "*");
		this.response.setHeader("Access-Control-Allow-Credentials", "true");

		Output output = new Output();

		Map<String, Object> ret = null;

		String solution = (String) info.getQueryParameters().getFirst("solution");
		if (StringUtils.isBlank(solution)) {
			output.setError(Boolean.valueOf(true));
			output.setError_message("FastSync: Missing parameter.");
			output.setMessage("Parameter solution not defined.");
			return output;
		}

		String path = (String) info.getQueryParameters().getFirst("path");

		String schema = (String) info.getQueryParameters().getFirst("schema");
		if (StringUtils.isBlank(schema)) {
			output.setError(Boolean.valueOf(true));
			output.setError_message("FastSync: Missing parameter.");
			output.setMessage("Parameter schema not defined.");
			return output;
		}

		String datasourceName = (String) info.getQueryParameters().getFirst("datasourceName");
		if (StringUtils.isBlank(datasourceName)) {
			output.setError(Boolean.valueOf(true));
			output.setError_message("FastSync: Missing parameter.");
			output.setMessage("Parameter datasourceName not defined.");
			return output;
		}

		String xmlaEnabledFlag = (String) info.getQueryParameters().getFirst("xmlaEnabledFlag");
		if (StringUtils.isBlank(xmlaEnabledFlag)) {
			xmlaEnabledFlag = "false";
		}

		try {
			String myType = "";
			myType = (String) info.getQueryParameters().getFirst("type");

			String myToken = "";
			myToken = (String) info.getQueryParameters().getFirst("token");

			String myUrlEncoded = "";
			myUrlEncoded = (String) info.getQueryParameters().getFirst("urlEncoded");

			if ((!StringUtils.isBlank(myType)) && (!"undefined".equalsIgnoreCase(myType))
					&& (!StringUtils.isBlank(myToken)) && (!"undefined".equalsIgnoreCase(myToken))
					&& (!StringUtils.isBlank(myUrlEncoded)) && (!"undefined".equalsIgnoreCase(myUrlEncoded))) {
				ret = Login.doLogin(this.request, this.response, info, myType, myToken, myUrlEncoded);

				if (!((Boolean) ret.get("ok")).booleanValue()) {
					output.setMessage("Authentication failed.");
					output.setError(Boolean.valueOf(true));
					output.setError_message((String) ret.get("message"));
					//PentahoLogoutHandler pentahoLogoutHandler;
					return output;
				}
			}

			String _PATH = solution + "/";
			if ((!"".equalsIgnoreCase(path)) && (path != null)) {
				_PATH = _PATH + path + "/";
			}
			_PATH = _PATH + schema;
			String schemaPath = PentahoSystem.getApplicationContext().getSolutionPath(_PATH).replaceAll("\\\\+", "/")
					.replaceAll("/+", "/");

			InputStream dataInputStream = new FileInputStream(schemaPath);

			FormDataContentDisposition schemaFileInfo = ((FormDataContentDisposition.FormDataContentDispositionBuilder) FormDataContentDisposition
					.name("uploadAnalysis").fileName(schema)).build();

			String catalogName = PublishUtil.determineDomainCatalogName(schemaPath, schema);

			if (!PublishUtil.validateName(catalogName)) {
				dataInputStream.close();
				output.setError(Boolean.valueOf(true));
				output.setError_message("FastSync: Invalid catalog name.");
				output.setMessage("Illegal character on the catalog name.");
				//PentahoLogoutHandler pentahoLogoutHandler;
				return output;
			}

			AnalysisService service = new AnalysisService();

			boolean xmlaEnabled = "True".equalsIgnoreCase(xmlaEnabledFlag);

			service.putMondrianSchema(dataInputStream, schemaFileInfo, catalogName, null, null, true, xmlaEnabled,
					"Datasource=" + datasourceName, null);
			dataInputStream.close();
			output.setError(Boolean.valueOf(false));
			output.setMessage(catalogName + " published successful.");
		} catch (FileNotFoundException e) {

			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Schema file not found.");
			e.printStackTrace();
		} catch (CannotCreateTransactionException e) {

			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied for anonymous user.");
			e.printStackTrace();
		} catch (PentahoAccessControlException e) {

			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Pentaho Access Control");
			e.printStackTrace();
		} catch (Exception e) {

			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		} finally {

			if (ret != null) {
				PentahoLogoutHandler pentahoLogoutHandler = new PentahoLogoutHandler();
				pentahoLogoutHandler.logout(this.request, this.response, null);
			}
		}

		return output;
	}

	@GET
	@Path("/sync/{id}")
	@Produces({ "application/json" })
	public Output syncSolution(@Context UriInfo info, @HeaderParam("user-agent") String userAgent,
			@PathParam("id") String id) {
		this.response.setHeader("Access-Control-Allow-Origin", "*");
		this.response.setHeader("Access-Control-Allow-Credentials", "true");

		Output output = new Output();

		String solution = "";
		String path = "";
		String withManifest = "";

		Map<String, Object> ret = null;

		boolean keepNewerFlag = "true".equalsIgnoreCase((String) info.getQueryParameters().getFirst("keep"));

		try {
			Repository.SYNC = id;
			String delete = (String) info.getQueryParameters().getFirst("delete");
			String deletePerm = (String) info.getQueryParameters().getFirst("deletePerm");
			Repository.DEBUG = "true".equalsIgnoreCase((String) info.getQueryParameters().getFirst("debug"));

			String _withManifest = (String) info.getQueryParameters().getFirst("withManifest");
			withManifest = "true".equalsIgnoreCase(_withManifest) ? "true" : "false";

			String myType = "";
			myType = (String) info.getQueryParameters().getFirst("type");

			String myToken = "";
			myToken = (String) info.getQueryParameters().getFirst("token");

			String myUrlEncoded = "";
			myUrlEncoded = (String) info.getQueryParameters().getFirst("urlEncoded");

			if ((!StringUtils.isBlank(myType)) && (!"undefined".equalsIgnoreCase(myType))
					&& (!StringUtils.isBlank(myToken)) && (!"undefined".equalsIgnoreCase(myToken))
					&& (!StringUtils.isBlank(myUrlEncoded)) && (!"undefined".equalsIgnoreCase(myUrlEncoded))) {
				ret = Login.doLogin(this.request, this.response, info, myType, myToken, myUrlEncoded);

				if (!((Boolean) ret.get("ok")).booleanValue()) {
					output.setMessage("Authentication failed.");
					output.setError(Boolean.valueOf(true));
					output.setError_message((String) ret.get("message"));
					// PentahoLogoutHandler pentahoLogoutHandler;
					return output;
				}
			}

			solution = ((String) info.getQueryParameters().getFirst("solution")).replaceAll("/+", "")
					.replaceAll(":+", "").replaceAll("\\\\+", "");

			if (StringUtils.isBlank(solution)) {
				output.setError(Boolean.valueOf(true));
				output.setError_message("FastSync: Missing parameter.");
				output.setMessage("Parameter solution not defined.");
				// PentahoLogoutHandler pentahoLogoutHandler;
				return output;
			}
			if ("system".equalsIgnoreCase(solution)) {
				output.setError(Boolean.valueOf(true));
				output.setError_message("FastSync: Invalid solution.");
				output.setMessage("System folder can not be synchronized.");

				return output;
			}

			path = ((String) info.getQueryParameters().getFirst("path")).replaceAll("/+", "/").replaceAll(":+", "/")
					.replaceAll("\\\\+", "/");

			if (path != null) {
				path = path.toLowerCase();
			}
			if (StringUtils.isBlank(path)) {
				path = "public";
			}

			StringUtils.startsWith(path, "/");
			StringUtils.removeEnd(path, "/");

			Repository.TEMP_DIR = FileSystem.getTmpDir(solution).replaceAll("\\\\+", "/").replaceAll("/+", "/");
			Repository.SOLUTION = solution;
			if (Repository.DEBUG) {
				System.out.println("\n-----> tmpDir sync: " + Repository.TEMP_DIR + "\n");
				System.out.println("\n-----> solution: " + Repository.SOLUTION + "\n");
				System.out.println("\n-----> Sync: " + Repository.SYNC + "\n");

			}

			if ("jcr".equalsIgnoreCase(Repository.SYNC)) {
				Repository.syncJcr(solution, path, delete, deletePerm, output, keepNewerFlag, userAgent, withManifest);
			} else if ("fs".equalsIgnoreCase(Repository.SYNC)) {
				Repository.syncFs(solution, path, delete, output, Repository.TEMP_DIR, userAgent, withManifest,
						keepNewerFlag);
			} else {
				output.setError(Boolean.valueOf(true));
				output.setError_message("FastSync: Invalid URL.");
				output.setMessage("/sync/" + id + " not defined.");
			}

			return output;
		} catch (PentahoAccessControlException e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage(
					"FastSync: Access denied. You do not have role permission to create and publish content.");
			e.printStackTrace();
		} catch (UnifiedRepositoryAccessDeniedException e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied. Check folder permissions.");
			e.printStackTrace();
		} catch (PlatformImportException e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: You do not have permission to create this folder.");
			e.printStackTrace();
		} catch (CannotCreateTransactionException e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied for anonymous user.");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		} catch (Exception e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		} catch (Throwable e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();

		} finally {
			try {

				 FileSystem.deleteFolder(new File(Repository.TEMP_DIR));

				if (Repository.DEBUG) {
					System.out.println("\n-----> Delete dir : " + Repository.TEMP_DIR + "\n");
				}
				if (ret != null) {
					PentahoLogoutHandler pentahoLogoutHandler = new PentahoLogoutHandler();
					pentahoLogoutHandler.logout(request, response, null);
				}

			} catch (Throwable e) {
				output.setError(Boolean.valueOf(true));
				output.setError_message(e.getMessage());
				output.setMessage("FastSync: Internal Server Error");
				e.printStackTrace();
			}
		}
		return output;
	}

	@GET
	@Path("/sync/list/{id}")
	@Produces({ "application/json" })
	public ReturnFileList listSync(@Context UriInfo info, @HeaderParam("user-agent") String userAgent,
			@PathParam("id") String id) {
		ReturnFileList returnList = new ReturnFileList();

		String solution = ((String) info.getQueryParameters().getFirst("solution")).replaceAll("/+", "")
				.replaceAll(":+", "").replaceAll("\\\\+", "");

		String withManifest = (String) info.getQueryParameters().getFirst("withManifest");
		;
		String tmpDir = FileSystem.getTmpDir(solution).replaceAll("\\\\+", "/").replaceAll("/+", "/");

		Repository.DEBUG = "true".equalsIgnoreCase((String) info.getQueryParameters().getFirst("debug"));

		System.out.println("\n-----> debug: " + Repository.DEBUG + "\n");

		if (StringUtils.isBlank(solution)) {
			returnList.setError(Boolean.valueOf(true));
			returnList.setError_message("FastSync: Missing parameter.");
			returnList.setMessage("Parameter solution not defined.");
			return returnList;
		}
		if ("system".equalsIgnoreCase(solution)) {
			returnList.setError(Boolean.valueOf(true));
			returnList.setError_message("FastSync: Invalid solution.");
			returnList.setMessage("System folder can not be synchronized.");
			return returnList;
		}

		String path = ((String) info.getQueryParameters().getFirst("path")).replaceAll("/+", "/").replaceAll(":+", "/")
				.replaceAll("\\\\+", "/");
		Repository.SYNC = id;

		if (path != null) {
			path = path.toLowerCase();
		}
		if (StringUtils.isBlank(path)) {
			path = "public";
		}

		StringUtils.startsWith(path, "/");
		StringUtils.removeEnd(path, "/");

		boolean keepNewerFlag = "true".equalsIgnoreCase((String) info.getQueryParameters().getFirst("keep"));

		Repository.TEMP_DIR = FileSystem.getTmpDir(solution);
//		Repository.SOLUTION = File.separator + solution;
		Repository.SOLUTION =  "/"+solution;
		if (Repository.DEBUG) {
			System.out.println("\n-----> tmpDir list: " + Repository.TEMP_DIR + "\n");
			System.out.println("\n-----> Sync: " + Repository.SYNC + "\n");
			System.out.println("\n-----> Solution: " + Repository.SOLUTION + "\n");
		}

		try {
			if ("jcr".equalsIgnoreCase(Repository.SYNC)) {
				Repository.listJcr(solution, path, returnList, keepNewerFlag, withManifest, userAgent);
			} else if ("fs".equalsIgnoreCase(Repository.SYNC)) {
				Repository.listFs(solution, path, returnList, keepNewerFlag, tmpDir, withManifest, userAgent);
			} else {
				returnList.setError(Boolean.valueOf(true));
				returnList.setError_message("FastSync: Invalid URL.");
				returnList.setMessage("/sync/list/" + id + " not defined.");
			}

			returnList.setError(Boolean.valueOf(false));
			returnList.setMessage("Synchronize to JCR from FileSystem.");
		} catch (UnifiedRepositoryAccessDeniedException e) {
			returnList.setError(Boolean.valueOf(true));
			returnList.setError_message(e.getMessage());
			returnList.setMessage("FastSync: Access denied. Check folder permissions.");
			e.printStackTrace();
		} catch (CannotCreateTransactionException e) {
			returnList.setError(Boolean.valueOf(true));
			returnList.setError_message(e.getMessage());
			returnList.setMessage("FastSync: Access denied for anonymous user.");
			e.printStackTrace();
		} catch (Exception e) {
			returnList.setError(Boolean.valueOf(true));
			returnList.setError_message(e.getMessage());
			returnList.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		} catch (Throwable e) {
			returnList.setError(Boolean.valueOf(true));
			returnList.setError_message(e.getMessage());
			returnList.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();

		}

		return returnList;
	}
}
