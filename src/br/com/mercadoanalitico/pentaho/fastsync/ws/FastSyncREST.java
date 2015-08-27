package br.com.mercadoanalitico.pentaho.fastsync.ws;

/**
 * 
 * @author Kleyson Rios<br>
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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

import br.com.mercadoanalitico.pentaho.fastsync.engine.PluginConfig;
import br.com.mercadoanalitico.pentaho.fastsync.models.List;
import br.com.mercadoanalitico.pentaho.fastsync.models.Output;
import br.com.mercadoanalitico.pentaho.fastsync.security.Login;
import br.com.mercadoanalitico.pentaho.fastsync.util.FileSystem;
import br.com.mercadoanalitico.pentaho.fastsync.util.PublishUtil;
import br.com.mercadoanalitico.pentaho.fastsync.util.Repository;
import br.com.mercadoanalitico.pentaho.fastsync.util.Zip;

import com.sun.jersey.core.header.FormDataContentDisposition;


@Path("/fastsync/api")
public class FastSyncREST {
	
	@Context private HttpServletRequest request;
	@Context private HttpServletResponse response;
	
	/*
	 * 
	 * Permissions needed to publish a new schema:
	 * 		- Create Content
	 * 		- Publish Content or Administer Security
	 * 
	 */
	@GET
	@Path("/publish")
	@Produces("application/json")
	public Output publishSchema ( @Context UriInfo info ) {

		// For simple CORS requests, the server only needs to add these 2 header parameters that allow access to any client.
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		Output output = new Output();			

		Map<String, Object> ret = null;

		// Parameters to publish a schema file
		String solution = info.getQueryParameters().getFirst("solution");	// In relation to the pentaho-solution folder
		if ( "".equalsIgnoreCase(solution) || solution == null )
		{
			output.setError(true);
			output.setError_message("FastSync: Missing parameter.");
			output.setMessage("Parameter solution not defined.");
			return output;
		}

		String path = info.getQueryParameters().getFirst("path"); // Path for the schema file in relation to the pentaho-solution/solution folder
		
		String schema = info.getQueryParameters().getFirst("schema"); // Schema filename
		if ( "".equalsIgnoreCase(schema) || schema == null )
		{
			output.setError(true);
			output.setError_message("FastSync: Missing parameter.");
			output.setMessage("Parameter schema not defined.");
			return output;
		}
		
		String datasourceName = info.getQueryParameters().getFirst("datasourceName");
		if ( "".equalsIgnoreCase(datasourceName) || datasourceName == null )
		{
			output.setError(true);
			output.setError_message("FastSync: Missing parameter.");
			output.setMessage("Parameter datasourceName not defined.");
			return output;
		}
		
		String xmlaEnabledFlag = info.getQueryParameters().getFirst("xmlaEnabledFlag");
		if ( "".equalsIgnoreCase(xmlaEnabledFlag) || xmlaEnabledFlag == null )
		{
			xmlaEnabledFlag = "false";
		}
			
		//IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
		//String username = roleDao.getUser(null, "admin").getUsername();
		//String password = roleDao.getUser(null, "admin").getPassword();

		try {
		    	
			// Get Auth Query Parameters
			String myType = "";
			myType = info.getQueryParameters().getFirst("type");
	
			String myToken = "";
			myToken = info.getQueryParameters().getFirst("token");
			
			String myUrlEncoded = "";
			myUrlEncoded = info.getQueryParameters().getFirst("urlEncoded");

			// Try to do login using Integrator if parameters are defined
			if ( !("".equalsIgnoreCase(myType)) && !(myType == null) && !("undefined".equalsIgnoreCase(myType)) && !("".equalsIgnoreCase(myToken)) && !(myToken == null) && !("undefined".equalsIgnoreCase(myToken)) && !("".equalsIgnoreCase(myUrlEncoded)) && !(myUrlEncoded == null) && !("undefined".equalsIgnoreCase(myUrlEncoded)) )
			{				
				ret = Login.doLogin(request, response, info, myType, myToken, myUrlEncoded);
				// Authentication Success
				if (!((boolean) ret.get("ok"))) 
				{
					output.setMessage("Authentication failed.");
					output.setError(true);
					output.setError_message((String) ret.get("message"));
					
					return output;
				}
			}

			// Define full schema path
			String _PATH = solution + File.separator;
			if ( !("".equalsIgnoreCase(path)) && path != null )
			{
				_PATH = _PATH + path + File.separator;
			}
			_PATH = _PATH + schema;
			String schemaPath = PentahoSystem.getApplicationContext().getSolutionPath(_PATH);
			
			// Get schema file from the filesystem
			InputStream dataInputStream = new FileInputStream(schemaPath);

			FormDataContentDisposition schemaFileInfo = FormDataContentDisposition.name("uploadAnalysis").fileName(schema).build();
		    	
			// Try to get schema name from xml, otherwise use filename
			String catalogName = PublishUtil.determineDomainCatalogName(schemaPath, schema);

			// If the schema name or the schema filename contain reserved characters, do not attempt to publish.
			if ( !PublishUtil.validateName( catalogName ) )
			{
				output.setError(true);
				output.setError_message("FastSync: Invalid catalog name.");
				output.setMessage("Illegal character on the catalog name.");
				return output;
			}
		        
			AnalysisService service = new AnalysisService();

			boolean xmlaEnabled = "True".equalsIgnoreCase( xmlaEnabledFlag ) ? true : false;
		        
			/**
			 * service.putMondrianSchema (
			 * 		dataInputStream
			 * 		schemaFileInfo
			 * 		catalogName
			 * 		origCatalogName
			 * 		datasourceName
			 * 		overWriteInRepository
			 * 		xmlaEnabled
			 * 		parameters
			 * 		acl
			 * );
			 */
			service.putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, null, null, true, xmlaEnabled, "Datasource=" + datasourceName, null );
		          
			output.setError(false);
			output.setMessage(catalogName + " published successful.");
						
		} catch ( FileNotFoundException e ) {
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Schema file not found.");
			e.printStackTrace();
		} catch ( CannotCreateTransactionException e ) {
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied for anonymous user.");
			e.printStackTrace();
		} catch ( PentahoAccessControlException e ) {
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Pentaho Access Control");
			e.printStackTrace();
		} catch ( Exception e ) {
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		} finally {
			
			// If authentication was done by Integrator, finish the user session.
			if (ret != null) 
			{
				PentahoLogoutHandler pentahoLogoutHandler = new PentahoLogoutHandler();
				pentahoLogoutHandler.logout(request, response, null);
			}

		}
		
			
		return output;
			
	}

	public void syncJcr(String solution, String path, String delete, String deletePerm, String debug, Output output, String tmpDir) throws Exception 
	{
		File dstDir;
		
		// Define full solution path
		String _PATH = solution + File.separator;
		String solutionFullPath = PentahoSystem.getApplicationContext().getSolutionPath(_PATH);
		
		if ( "True".equalsIgnoreCase(delete) ) 
		{
			String location = path + File.separator + solution;
			String deleteList = Repository.getDeleteList(path, location, solutionFullPath, "True".equalsIgnoreCase(debug) );
			
			if (deleteList.length() > 0 ) 
				Repository.deleteItems( deleteList, "True".equalsIgnoreCase(deletePerm) );
		}

		// Copy solution to tmpFolder
        dstDir = new File(tmpDir + File.separator + solution + File.separator + path + File.separator + solution + File.separator);
        
        FileSystem.copyDirectory( new File(solutionFullPath), dstDir, PluginConfig.props.getProperty("import.exclude.list") );
		
        // Pack solution
        Zip zipPack = new Zip();

        tmpDir = FileSystem.getTmpDir(solution);
        String fullZipName = tmpDir + File.separator + solution + ".zip";
        
        zipPack.setFullPathZipFileName(fullZipName);
        zipPack.setPackDirectoryPath((tmpDir + File.separator + solution).replaceAll("\\\\+", "/").replaceAll("/+", "/"));
        zipPack.packDirectory();

        // Load solution zip file to JCR
        Repository.importFileToJcr(tmpDir, fullZipName, debug);
        
		output.setError(false);
		output.setMessage("Successful synchronize to JCR from FileSystem.");
		
	}
	
	public void syncFs(String solution, String path, String delete, String debug, Output output, String tmpDir, String userAgent, String withManifest) throws Throwable 
	{
		// Define Solution Path
		String solutionPath = ( PentahoSystem.getApplicationContext().getSolutionPath("") ).replaceAll("\\\\+", "/").replaceAll("/+", "/");

		// Define JCR base path
		String location = (":" + path + ":" + solution).replaceAll("/+", ":").replaceAll("\\\\+", ":").replaceAll(":+", ":");
		location = StringUtils.removeEnd(location, ":");

		// Delete files/folder from FileSystem
		if ( "True".equalsIgnoreCase(delete) ) 
		{
			String deleteList = Repository.getDeleteFsList(path, location, (solutionPath + "/" + solution), "True".equalsIgnoreCase(debug) );
			
			if (deleteList.length() > 0 ) 
				Repository.deleteItemsFs( solutionPath, deleteList );
		}

		// Save solution to filesystem
		Repository.exportFileToFs( userAgent, location, withManifest, tmpDir, solutionPath);
		
		output.setError(false);
		output.setMessage("Successful synchronize to FileSystem from JCR.");
	}
	
	/*
	 * 
	 * Permissions needed to sync a solution
	 * 		- Create Content
	 * 		- Publish Content or Administer Security
	 *		- Folders Write Permission
	 *  
	 */
	@GET
	@Path("/sync/{id}")
	@Produces("application/json")
	public Output syncSolution ( @Context UriInfo info, @HeaderParam ( "user-agent" ) String userAgent, @PathParam ( "id" ) String id ) 
	{
		// For simple CORS requests, the server only needs to add these 2 header parameters that allow access to any client.
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		Output output = new Output();
		
		String tmpDir = "";
		
		Map<String, Object> ret = null;

		try
		{
			String delete = info.getQueryParameters().getFirst("delete");			// Delete files on JCR/FileSystem that do not exist on FileSystem/JCR
			String deletePerm = info.getQueryParameters().getFirst("deletePerm");	// Permanently deletes the selected list of files from the repository
			String debug = info.getQueryParameters().getFirst("debug");

			String _withManifest = info.getQueryParameters().getFirst("withManifest");
			String withManifest = ( "true".equalsIgnoreCase(_withManifest) ? "true" : "false" );
			
			// Get Auth Query Parameters
			String myType = "";
			myType = info.getQueryParameters().getFirst("type");
	
			String myToken = "";
			myToken = info.getQueryParameters().getFirst("token");
			
			String myUrlEncoded = "";
			myUrlEncoded = info.getQueryParameters().getFirst("urlEncoded");

			// Try to do login using Integrator if parameters are defined
			if ( !("".equalsIgnoreCase(myType)) && !(myType == null) && !("undefined".equalsIgnoreCase(myType)) && !("".equalsIgnoreCase(myToken)) && !(myToken == null) && !("undefined".equalsIgnoreCase(myToken)) && !("".equalsIgnoreCase(myUrlEncoded)) && !(myUrlEncoded == null) && !("undefined".equalsIgnoreCase(myUrlEncoded)) )
			{				
				ret = Login.doLogin(request, response, info, myType, myToken, myUrlEncoded);
				// Authentication Success
				if (!((boolean) ret.get("ok"))) 
				{
					output.setMessage("Authentication failed.");
					output.setError(true);
					output.setError_message((String) ret.get("message"));
					
					return output;
				}
			}
			
			// Parameters to synchronize a solution
			String solution = info.getQueryParameters().getFirst("solution");	// Relative to the pentaho-solution folder
			if ( StringUtils.isBlank(solution) )
			{
				output.setError(true);
				output.setError_message("FastSync: Missing parameter.");
				output.setMessage("Parameter solution not defined.");
				return output;
			} 
			else if ( "system".equalsIgnoreCase(solution) ) 
			{
				output.setError(true);
				output.setError_message("FastSync: Invalid solution.");
				output.setMessage("System folder can not be synchronized.");
				return output;
			}

			// Get path parameter
			String path = info.getQueryParameters().getFirst("path");	// Relative to the PUC Browser files
			
			if (path != null)
				path = path.toLowerCase();
			
			if ( StringUtils.isBlank(path) )
			{
				path = "public";
			} 
			
			// Define tmpDir
	        tmpDir = (FileSystem.getTmpDir(solution)).replaceAll("\\\\+", "/").replaceAll("/+", "/");
			
	        // Call sync
	        if ( "jcr".equalsIgnoreCase(id) )
	        {
	        	syncJcr(solution, path, delete, deletePerm, debug, output, tmpDir);
	        }
	        else if ( "fs".equalsIgnoreCase(id) )
	        {
	        	syncFs(solution, path, delete, debug, output, tmpDir, userAgent, withManifest);
	        }
	        else
	        {
				output.setError(true);
				output.setError_message("FastSync: Invalid URL.");
				output.setMessage("/sync/" + id + " not defined.");
	        }
	        
		}
    	catch( PentahoAccessControlException e ) {
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied. You do not have role permission to create and publish content.");
			e.printStackTrace();
    	}
    	catch( UnifiedRepositoryAccessDeniedException e ) {
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied. Check folder permissions.");
			e.printStackTrace();
    	}
		catch ( PlatformImportException e ) 
		{
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: You do not have permission to create this folder.");
			e.printStackTrace();
		} 
		catch ( CannotCreateTransactionException e ) 
		{
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Access denied for anonymous user.");
			e.printStackTrace();
		} 
		catch (FileNotFoundException e)
		{
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		}
		catch (Exception e)
		{
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		}
		catch (Throwable e)
		{
			output.setError(true);
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		}
		finally 
		{
            try 
            {
                // Delete tmpDir
				FileSystem.deleteFolder(new File(tmpDir));
				
				// If authentication was done by Integrator, finish the user session.
				if (ret != null) 
				{
					PentahoLogoutHandler pentahoLogoutHandler = new PentahoLogoutHandler();
					pentahoLogoutHandler.logout(request, response, null);
				}
			} 
            catch (IOException e) 
			{
				output.setError(true);
				output.setError_message(e.getMessage());
				output.setMessage("FastSync: Internal Server Error");
				e.printStackTrace();
			}
		}
		
		return output;
	}
	
	@GET
	@Path("/sync/list/jcr")
	@Produces("application/json")
	public List listSyncJcr ( @Context UriInfo info ) 
	{
		List returnList = new List();
		
		// Parameters to synchronize a solution
		String solution = info.getQueryParameters().getFirst("solution");	// Relative to the pentaho-solution folder
		if ( "".equalsIgnoreCase(solution) || solution == null )
		{
			returnList.setError(true);
			returnList.setError_message("FastSync: Missing parameter.");
			returnList.setMessage("Parameter solution not defined.");
			return returnList;
		} 
		else if ( "system".equalsIgnoreCase(solution) ) 
		{
			returnList.setError(true);
			returnList.setError_message("FastSync: Invalid solution.");
			returnList.setMessage("System folder can not be synchronized.");
			return returnList;
		}

		// Get path parameter
		String path = info.getQueryParameters().getFirst("path").toLowerCase();	// Relative to the PUC Browser files
		if ( "".equalsIgnoreCase(path) || path == null )
		{
			path = "public";
		} 
		
		try
		{
			// Define full solution path
			String _PATH = solution + File.separator;
			String solutionFullPath = PentahoSystem.getApplicationContext().getSolutionPath(_PATH);
			
			// Get list of files and folders from JCR
			String location = (":" + path + File.separator + solution + ":").replaceAll("/+", ":").replaceAll("\\\\+", ":").replaceAll(":+", ":");
			Collection<String> repoFiles = Repository.getRepoFiles(location).getItemsList();
	
			// Get list of files and folders from Filesystem
			Collection<String> localFiles = Repository.getLocalFiles(solutionFullPath);
			Collection<String> _localFiles = Repository.addPrefix(path, localFiles);
	
			// Get excluded list
			Collection<String> excludeList = Repository.excludeByRegex( _localFiles, PluginConfig.props.getProperty("import.exclude.list") );
			for (String item : excludeList) {
				
				returnList.getExclude().add(item);
				
			}
			
			// Get list of files to be deleted
			Collection<String> deleteList = Repository.getDiff(repoFiles, _localFiles);
			
			for (String item : deleteList) 
			{
				returnList.getDelete().add(item);
			}
			
			// Get list of files to be updated
			Collection<String> updateList = Repository.getDiff(repoFiles, deleteList);
			
			for (String item : updateList) 
			{
				returnList.getUpdate().add(item);
			}
	
			// Get list of files to be created
			updateList.add(location.substring(0,location.length()-1));
			Collection<String> createList = Repository.getDiff( Repository.getDiff(_localFiles, excludeList), updateList );
	
			for (String item : createList) 
			{
				returnList.getCreate().add(item);
			}
		
			returnList.setError(false);
			returnList.setMessage("Synchronize to JCR from FileSystem.");
	
		}
    	catch( UnifiedRepositoryAccessDeniedException e ) {
    		returnList.setError(true);
    		returnList.setError_message(e.getMessage());
    		returnList.setMessage("FastSync: Access denied. Check folder permissions.");
			e.printStackTrace();
    	}
		catch ( CannotCreateTransactionException e ) 
		{
			returnList.setError(true);
			returnList.setError_message(e.getMessage());
			returnList.setMessage("FastSync: Access denied for anonymous user.");
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			returnList.setError(true);
			returnList.setError_message(e.getMessage());
			returnList.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();
		}
		
		return returnList;
	}

}

