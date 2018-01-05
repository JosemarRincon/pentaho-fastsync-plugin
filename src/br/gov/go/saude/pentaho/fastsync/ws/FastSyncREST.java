package br.gov.go.saude.pentaho.fastsync.ws;

import br.gov.go.saude.pentaho.fastsync.engine.PluginConfig;
import br.gov.go.saude.pentaho.fastsync.models.Output;
import br.gov.go.saude.pentaho.fastsync.models.Repo;
import br.gov.go.saude.pentaho.fastsync.models.ReturnFileList;
import br.gov.go.saude.pentaho.fastsync.security.Login;
import br.gov.go.saude.pentaho.fastsync.util.FileSystem;
import br.gov.go.saude.pentaho.fastsync.util.PublishUtil;
import br.gov.go.saude.pentaho.fastsync.util.Repository;
import br.gov.go.saude.pentaho.fastsync.util.Search;
import br.gov.go.saude.pentaho.fastsync.util.Zip;
import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.apache.commons.lang.StringUtils;

import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.security.PentahoLogoutHandler;
import org.springframework.transaction.CannotCreateTransactionException;

@Path("/fastsync/api")
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
					PentahoLogoutHandler pentahoLogoutHandler;
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
				output.setError(Boolean.valueOf(true));
				output.setError_message("FastSync: Invalid catalog name.");
				output.setMessage("Illegal character on the catalog name.");
				PentahoLogoutHandler pentahoLogoutHandler;
				return output;
			}

			AnalysisService service = new AnalysisService();

			boolean xmlaEnabled = "True".equalsIgnoreCase(xmlaEnabledFlag);

			service.putMondrianSchema(dataInputStream, schemaFileInfo, catalogName, null, null, true, xmlaEnabled,
					"Datasource=" + datasourceName, null);

			output.setError(Boolean.valueOf(false));
			output.setMessage(catalogName + " published successful.");
		} catch (FileNotFoundException e) {
			PentahoLogoutHandler pentahoLogoutHandler;
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Schema file not found.");
			e.printStackTrace();
		} catch (CannotCreateTransactionException e) {
			PentahoLogoutHandler pentahoLogoutHandler;
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied for anonymous user.");
			e.printStackTrace();
		} catch (PentahoAccessControlException e) {
			PentahoLogoutHandler pentahoLogoutHandler;
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Pentaho Access Control");
			e.printStackTrace();
		} catch (Exception e) {
			PentahoLogoutHandler pentahoLogoutHandler;
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

	public void syncJcr(String solution, String path, String delete, String deletePerm, Output output, String tmpDir,
			boolean keepNewerFlag) throws Exception {
		String solutionFullPath = PentahoSystem.getApplicationContext().getSolutionPath(solution)
				.replaceAll("\\\\+", "/").replaceAll("/+", "/");

		if ("True".equalsIgnoreCase(delete)) {
			String location = (path + "/" + solution).replaceAll("/+", "/");

			String deleteList = Repository.getDeleteList(path, location, solutionFullPath);

			if (deleteList.length() > 0) {
				Repository.deleteItems(deleteList, "True".equalsIgnoreCase(deletePerm));
			}
		}

		File dstDir = new File(tmpDir + File.separator + solution + File.separator + path + File.separator + solution
				+ File.separator);

		FileSystem.copyDirectory(new File(solutionFullPath), dstDir,
				PluginConfig.props.getProperty("import.exclude.list"));

		ReturnFileList listFiles = new ReturnFileList();

		if (keepNewerFlag) {
			listJcr(solution, path, listFiles, keepNewerFlag);
		}
		for (String item : listFiles.getPreserve()) {
			File file = new File(tmpDir + "/" + solution + item);

			if ((file.isFile()) || (FileSystem.isDirectoryEmpty(file))) {
				FileSystem.deleteFile(file);
			}
		}

		Collection<File> stageFolderFiles = Search.searchFileAndDirsRecursive(tmpDir + "/" + solution);

		stageFolderFiles.remove(new File(tmpDir + "/" + solution));

		Collections.reverse((List<File>) stageFolderFiles);

		for (File item : stageFolderFiles) {
			if (FileSystem.isDirectoryEmpty(item)) {
				FileSystem.deleteFile(item);
			}
		}
		if (!FileSystem.isDirectoryEmpty(tmpDir + "/" + solution)) {

			Zip zipPack = new Zip();

			tmpDir = FileSystem.getTmpDir(solution);
			String fullZipName = tmpDir + File.separator + solution + ".zip";

			zipPack.setFullPathZipFileName(fullZipName);
			zipPack.setPackDirectoryPath(
					(tmpDir + File.separator + solution).replaceAll("\\\\+", "/").replaceAll("/+", "/"));
			zipPack.packDirectory();
			if (Repository.DEBUG) {
				System.out.println("\n----->  tmpDir: " + tmpDir + "\n");
				System.out.println("\n----->  fullZipName: " + fullZipName + "\n");
				System.out.println("\n----->  solution: " + solution + "\n");
			}
			Repository.importFileToJcr(tmpDir, fullZipName);
		}

		output.setError(Boolean.valueOf(false));
		output.setMessage("Successful synchronize to JCR from FileSystem.");
	}

	public void syncFs(String solution, String path, String delete, Output output, String tmpDir, String userAgent,
			String withManifest, boolean keepNewerFlag) throws Throwable {

		String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath("").replaceAll("\\\\+", "/")
				.replaceAll("/+", "/");

		String location = (path + "/" + solution).replaceAll("/+", "/");

		if ("True".equalsIgnoreCase(delete)) {
			String deleteList = Repository.getDeleteFsList(path, location, solutionPath + "/" + solution);

			if (deleteList.length() > 0) {
				Repository.deleteItemsFs(solutionPath, deleteList);
			}
		}

		ReturnFileList listFiles = new ReturnFileList();

		// if (keepNewerFlag) {
		listFs(solution, path, listFiles, keepNewerFlag, tmpDir, withManifest,userAgent);
		// }

		// Repository.exportFileToFs(userAgent, location, withManifest, tmpDir,
		// solutionPath, listFiles.getPreserve());
		Repository.exportFileToFs2(solutionPath, location, listFiles);

		output.setError(Boolean.valueOf(false));
		output.setMessage("Successful synchronize to FileSystem from JCR.");
	}

	@GET
	@Path("/sync/{id}")
	@Produces({ "application/json" })
	public Output syncSolution(@Context UriInfo info, @HeaderParam("user-agent") String userAgent,
			@PathParam("id") String id) {
		this.response.setHeader("Access-Control-Allow-Origin", "*");
		this.response.setHeader("Access-Control-Allow-Credentials", "true");

		Output output = new Output();

		String tmpDir = "";

		Map<String, Object> ret = null;

		boolean keepNewerFlag = "true".equalsIgnoreCase((String) info.getQueryParameters().getFirst("keepNewerFlag"));

		try {
			String delete = (String) info.getQueryParameters().getFirst("delete");
			String deletePerm = (String) info.getQueryParameters().getFirst("deletePerm");
			Repository.DEBUG = "True".equalsIgnoreCase((String) info.getQueryParameters().getFirst("debug"));

			String _withManifest = (String) info.getQueryParameters().getFirst("withManifest");
			String withManifest = "true".equalsIgnoreCase(_withManifest) ? "true" : "false";

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
					PentahoLogoutHandler pentahoLogoutHandler;
					return output;
				}
			}

			String solution = ((String) info.getQueryParameters().getFirst("solution")).replaceAll("/+", "")
					.replaceAll(":+", "").replaceAll("\\\\+", "");

			if (StringUtils.isBlank(solution)) {
				output.setError(Boolean.valueOf(true));
				output.setError_message("FastSync: Missing parameter.");
				output.setMessage("Parameter solution not defined.");
				PentahoLogoutHandler pentahoLogoutHandler;
				return output;
			}
			if ("system".equalsIgnoreCase(solution)) {
				output.setError(Boolean.valueOf(true));
				output.setError_message("FastSync: Invalid solution.");
				output.setMessage("System folder can not be synchronized.");
				PentahoLogoutHandler pentahoLogoutHandler;
				return output;
			}

			String path = ((String) info.getQueryParameters().getFirst("path")).replaceAll("/+", "/")
					.replaceAll(":+", "/").replaceAll("\\\\+", "/");

			if (path != null) {
				path = path.toLowerCase();
			}
			if (StringUtils.isBlank(path)) {
				path = "public";
			}

			StringUtils.startsWith(path, "/");
			StringUtils.removeEnd(path, "/");

			tmpDir = FileSystem.getTmpDir(solution).replaceAll("\\\\+", "/").replaceAll("/+", "/");

			if ("jcr".equalsIgnoreCase(id)) {
				syncJcr(solution, path, delete, deletePerm, output, tmpDir, keepNewerFlag);
			} else if ("fs".equalsIgnoreCase(id)) {
				syncFs(solution, path, delete, output, tmpDir, userAgent, withManifest, keepNewerFlag);
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
				FileSystem.deleteFolder(new File(tmpDir));
				System.out.println("\n----->  Delete dir : " + tmpDir + "\n");

				if (ret != null) {
					PentahoLogoutHandler pentahoLogoutHandler = new PentahoLogoutHandler();
					pentahoLogoutHandler.logout(request, response, null);
				}
			} catch (IOException e) {
				output.setError(Boolean.valueOf(true));
				output.setError_message(e.getMessage());
				output.setMessage("FastSync: Internal Server Error");
				e.printStackTrace();
			}
		}
		return output;
	}

	public void listJcr(String solution, String path, ReturnFileList returnList, boolean keepNewerFlag)
			throws Exception {
		String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath("").replaceAll("\\\\+", "/")
				.replaceAll("/+", "/");

		String solutionFullPath = PentahoSystem.getApplicationContext().getSolutionPath(solution)
				.replaceAll("\\\\+", "/").replaceAll("/+", "/");

		String location = (path + "/" + solution + "/").replaceAll("/+", "/");

		Repo repoMaps = Repository.getRepoFiles(location.replaceAll("/", ":"));

		Collection<String> repoFiles = repoMaps.getItemsList();

		Collection<String> localFiles = Repository.getLocalFiles(solutionFullPath);
		Collection<String> _localFiles = Repository.addPrefix(path, localFiles);

		_localFiles.remove(location.substring(0, location.length() - 1));

		Collection<String> excludeList = Repository.excludeByRegex(_localFiles,
				PluginConfig.props.getProperty("import.exclude.list"));
		for (String item : excludeList) {
			returnList.getExclude().add(item);
		}

		Collection<String> deleteList = Repository.getDiff(repoFiles, _localFiles);

		for (String item : deleteList) {
			returnList.getDelete().add(item);
		}

		Collection<String> _createList = Repository.getDiff(Repository.getDiff(_localFiles, repoFiles), excludeList);
		Collection<String> createList = Repository.getDiff(_createList, returnList.getPreserve());

		for (String item : createList) {
			returnList.getCreate().add(item);
		}

		Collection<String> updateList = Repository.getDiff(Repository.getDiff(repoFiles, deleteList), excludeList);

		if (keepNewerFlag) {
			Collection<String> preserveList = Repository.removeNewerJcrFiles(updateList, repoMaps.getModifiedDateList(),
					solutionPath, path);

			for (String item : preserveList) {
				if (!Repository.getJcrPathProperties(item.replaceAll("/", ":")).isFolder()) {
					returnList.getPreserve().add(item);
				}
			}
		}
		for (String item : updateList) {
			if (!Repository.getJcrPathProperties(item).isFolder()) {
				returnList.getUpdate().add(item);
			}
		}
	}

	public void listFs(String solution, String path, ReturnFileList returnList, boolean keepNewerFlag, String tmpDir,
			String withManifest,String userAgent) throws Throwable {
		String base = "/".equals(path) ? "" : path;

		String solutionFullPath = PentahoSystem.getApplicationContext().getSolutionPath(solution)
				.replaceAll("\\\\+", "/").replaceAll("/+", "/");

		String location = (path + "/" + solution).replaceAll("/+", "/");

		Repository.getJcrPathProperties(location.replaceAll("/", ":") + ":");

		Repo repoMaps = Repository.getRepoFiles(location.replaceAll("/", ":") + ":");

		Collection<String> _repoFiles = repoMaps.getItemsList();
		if (Repository.DEBUG) {
			System.out.println("\n-----> repoFiles and folders zize: " + _repoFiles.size() + "\n");
		}
		Collection<String> repoFiles = new ArrayList<String>();

		for (String item : _repoFiles) {

			repoFiles.add(StringUtils.removeStart(item, base));
		}

		Collection<String> localFiles = Repository.getLocalFiles(solutionFullPath);

		localFiles.remove(StringUtils.removeStart(location, base));

		Collection<String> _deleteList = Repository.getDiff(localFiles, repoFiles);

		Collection<String> excludeList = Repository.excludeByRegex(localFiles,
				PluginConfig.props.getProperty("import.exclude.list"));
		for (String item : excludeList) {
			returnList.getExclude().add(item);
		}

		Collection<String> deleteList = Repository.getDiff(_deleteList, excludeList);
		for (String item : deleteList) {
			returnList.getDelete().add(item);
		}

		Collection<String> createList = Repository.getDiff(repoFiles, localFiles);
		for (String item : createList) {
			returnList.getCreate().add(item);
		}

		if (Repository.DEBUG) {
			System.out.println("\n-----> base: " + base + "\n");
			System.out.println("\n-----> tmpDir: " + tmpDir + "\n");
			System.out.println("\n-----> userAgent: " + userAgent + "\n");
		}

		Repository.getFilesFromJcr(userAgent, location, tmpDir, withManifest);
		Collection<String> updateList = Repository.addFilesModifidied(Repository.getDiff(localFiles, excludeList),
				PentahoSystem.getApplicationContext().getSolutionPath(""), base, tmpDir);

		if (keepNewerFlag) {
			Collection<String> preserveList = Repository.getDiff(Repository.getDiff(localFiles, excludeList),
					updateList);
			for (String item : preserveList) {
				if (!Repository.getJcrPathProperties(base + item).isFolder()) {
					returnList.getPreserve().add(item);
					Repository.removeFilePreservedList(item);
				}
			}
			Repository.newZipForUpdate();

		} else {
			updateList = Repository.getDiff(localFiles, excludeList);
		}

		if (Repository.DEBUG) {
			System.out.println("\n-----> updateList: " + updateList + "\n");
			System.out.println("\n-----> updateList size: " + updateList.size() + "\n");
		}

		for (String item : updateList) {
			if (!Repository.getJcrPathProperties(base + item).isFolder()) {
				returnList.getUpdate().add(item);
			}
		}

		
	}

	@GET
	@Path("/sync/list/{id}")
	@Produces({ "application/json" })
	public ReturnFileList listSyncJcr(@Context UriInfo info, @HeaderParam("user-agent") String userAgent, @PathParam("id") String id) {
		ReturnFileList returnList = new ReturnFileList();

		String solution = ((String) info.getQueryParameters().getFirst("solution")).replaceAll("/+", "")
				.replaceAll(":+", "").replaceAll("\\\\+", "");

		String _withManifest = (String) info.getQueryParameters().getFirst("withManifest");
		String withManifest = "true".equalsIgnoreCase(_withManifest) ? "true" : "false";
		String tmpDir = FileSystem.getTmpDir(solution).replaceAll("\\\\+", "/").replaceAll("/+", "/");

		Repository.DEBUG = "True".equalsIgnoreCase((String) info.getQueryParameters().getFirst("debug"));

		System.out.println("\n-----> debug: " + (String) info.getQueryParameters().getFirst("debug") + "\n");

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

		if (path != null) {
			path = path.toLowerCase();
		}
		if (StringUtils.isBlank(path)) {
			path = "public";
		}

		StringUtils.startsWith(path, "/");
		StringUtils.removeEnd(path, "/");

		boolean keepNewerFlag = "true".equalsIgnoreCase((String) info.getQueryParameters().getFirst("keepNewerFlag"));

		try {
			if ("jcr".equalsIgnoreCase(id)) {
				listJcr(solution, path, returnList, keepNewerFlag);
			} else if ("fs".equalsIgnoreCase(id)) {
				listFs(solution, path, returnList, keepNewerFlag, tmpDir, withManifest,userAgent);
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
