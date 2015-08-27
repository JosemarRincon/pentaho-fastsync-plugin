package br.com.mercadoanalitico.pentaho.fastsync.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.RepositoryImportResource;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.zeroturnaround.zip.ZipUtil;

import br.com.mercadoanalitico.pentaho.fastsync.engine.PluginConfig;
import br.com.mercadoanalitico.pentaho.fastsync.models.Repo;

import com.sun.jersey.core.header.FormDataContentDisposition;

public class Repository {
	
	private static FileService fileService = null;
	
	// Remove from the firstList all elements of secondList
	public static Collection<String> getDiff(Collection<String> firstList, Collection<String> secondList)	
	{
		Collection<String> _firstList = new ArrayList<String>(firstList);
		_firstList.removeAll(secondList);
		
		return _firstList;
	}

	// Get folders and file from JCR
	public static Repo getRepoFiles(String location) 
	{
		location = location.replaceAll("/+", ":").replaceAll("\\\\+", ":") + ":";
		location = location.replaceAll(":+", ":");

		if ( fileService == null) 
			fileService = new FileService();

		// Folders and files list
		Collection<String> items = new ArrayList<String>();
		
		// Hashmap for folders/list's id
		Map<String,String> ids = new HashMap<String,String>();

		// Get list from JCR
		List<RepositoryFileDto> repoFiles = fileService.doGetChildren(location, null, true, false );
		
		for (RepositoryFileDto repoFile : repoFiles) 
		{
			String item = repoFile.getPath().replaceAll("/+", ":").replaceAll("\\\\+", ":");
			
			items.add( item );
			ids.put( item, repoFile.getId() );
			
			if ( repoFile.isFolder() )
			{
				getRepoFiles(repoFile.getPath(), items, ids, fileService);
			}

		}
		
		Repo repo = new Repo();
		
		repo.setItemsList(items);
		repo.setIdsList(ids);
		
		return repo;
	}


	private static void getRepoFiles(String location, Collection<String> ret, Map<String, String> ids, FileService fileService)
	{
		location = location.replaceAll("/+", ":").replaceAll("\\\\+", ":") + ":";
				
		// Get list from JCR
		List<RepositoryFileDto> repoFiles = fileService.doGetChildren(location, null, true, false );
		
		for (RepositoryFileDto repoFile : repoFiles) 
		{
			String item = repoFile.getPath().replaceAll("/+", ":").replaceAll("\\\\+", ":");
			
			ret.add( item );
			ids.put( item, repoFile.getId() );

			if ( repoFile.isFolder() )
			{
				getRepoFiles(repoFile.getPath(), ret, ids, fileService);
			}

		}
	}
	
	// Get folders and files from filesystem
	public static Collection<String> getLocalFiles(String location) throws Exception
	{
		Collection<String> ret = new ArrayList<String>();
		
		Collection<File> localFiles = Search.searchFileAndDirsRecursive(location);
		
		if (localFiles == null) throw new Exception("Solution not found in the pentaho-solution folder.");
		
		for (File localFile : localFiles) 
		{
			String[] parts = localFile.getPath().split("pentaho-solutions");
			ret.add( parts[1].replaceAll("/+", ":").replaceAll("\\\\+", ":") );
		}
		
		return ret;
	}
	
	// Get a list of files and folders to be delete in the JCR
	public static String getDeleteList(String root, String location, String solutionFullPath, boolean debug) throws Exception
	{
		// Get Repository items and ids
		Repo repo = getRepoFiles(location);

		// Get list of files and folders from JCR
		Collection<String> repoFiles = repo.getItemsList();
		
		// Get list of files and folders from Filesystem
		Collection<String> localFiles = getLocalFiles(solutionFullPath);
		Collection<String> _localFiles = addPrefix(root, localFiles);
		
		if (debug) System.out.println("\n----->  repoFiles: " + repoFiles + "\n");
		if (debug) System.out.println("\n-----> localFiles: " + _localFiles + "\n");

		List<String> deleteList = new ArrayList<String>();
		
		Collection<String> diffList = getDiff(repoFiles, _localFiles);
		
		if (debug) System.out.println("\n-----> diffList: " + diffList + "\n");

		for (String key : diffList) {
			
			String value = repo.getIdsList().get(key);
			deleteList.add(value);
			
			if (debug)  System.out.println("-----> " + key + " -> " + value);
		}
		
		// Reverse order to delete files before their parent folder
		Collections.reverse(deleteList);
		
		return StringUtils.join(deleteList.toArray(), ",");
	}

	// Get a list of files and folders to be delete in the FS
	public static String getDeleteFsList(String root, String location, String solutionFullPath, boolean debug) throws Exception
	{
		String base = root.replaceAll("/+", ":").replaceAll("\\\\+", ":").replaceAll(":+", ":");
		
		// Get Repository items and ids
		Repo repo = getRepoFiles(location);

		// Get list of files and folders from JCR
		Collection<String> _repoFiles = repo.getItemsList();
		Collection<String> repoFiles = new ArrayList<String>() ;

		for (String item : _repoFiles) 
		{
			repoFiles.add( StringUtils.removeStart(item, base) );
		}
		
		// Get list of files and folders from Filesystem
		Collection<String> localFiles = getLocalFiles(solutionFullPath);
		
		localFiles.remove( StringUtils.removeStart(location, base) );
		
		if (debug) System.out.println("\n----->  repoFiles: " + repoFiles + "\n");
		if (debug) System.out.println("\n-----> localFiles: " + localFiles + "\n");

		List<String> deleteList = new ArrayList<String>();
		
		Collection<String> diffList = getDiff(localFiles, repoFiles);
		
		if (debug) System.out.println("\n-----> diffList: " + diffList + "\n");

		for (String item : diffList) 
		{
			deleteList.add(item);
		}
		
		// Reverse order to delete files before their parent folder
		Collections.reverse(deleteList);
		
		// Get excluded items
		Collection<String> excludeList = excludeByRegex( deleteList, PluginConfig.props.getProperty("import.exclude.list") );

		return StringUtils.join( getDiff(deleteList, excludeList).toArray(), "," );
	}

	public static Collection<String> addPrefix(String root, Collection<String> collection) 
	{
		Collection<String> ret = new ArrayList<String>();
		
		for (String item : collection) 
		{
			ret.add( (":" + root + item).replaceAll("/+", ":").replaceAll("\\\\+", ":").replaceAll(":+", ":") );
		}
		
		return ret;
	}

	public static void deleteItems(String deleteList, boolean perm) throws Exception
	{
		if ( fileService == null) 
			fileService = new FileService();

		if (perm) fileService.doDeleteFilesPermanent(deleteList);
		else fileService.doDeleteFiles(deleteList);
		
	}

	public static void deleteItemsFs(String solutionPath, String deleteList) throws IOException
	{
		for (String token : deleteList.split(",") ) 
		{ 
			File item = new File(solutionPath + "/" + token.replaceAll(":", "/") );
			
			if ( item.isFile() )
				FileSystem.deleteFile(item);
			else
				FileSystem.deleteFolder(item);
			
		}
	}

	public static void importFileToJcr(String location, String zipFile, String debug) throws Exception 
 	{
		String logLevel = ("True".equalsIgnoreCase(debug) ? "DEBUG" : "INFO");
 		
		InputStream input = new FileInputStream(zipFile);

		try 
		{
			FormDataContentDisposition fileInfo = FormDataContentDisposition.name(FilenameUtils.getName(zipFile)).fileName(FilenameUtils.getName(zipFile)).build();
			
			RepositoryImportResource repositoryImporter = new RepositoryImportResource();
			
			Response ret = repositoryImporter.doPostImport(location, input, "true", "true", "true", "true", "UTF-8", logLevel, fileInfo, null);
			
			if (ret.getStatus() == 403) 
			{
				throw new UnifiedRepositoryAccessDeniedException("FORBIDDEN");
			}
			else if (ret.getStatus() != 200) 
			{
				String msg = ret.getEntity().toString();
				
				if ( msg.contains("PentahoAccessControlException") )
					throw new PentahoAccessControlException("Access Denied");
				else
					throw new Exception(msg);
			}

		} 
		finally 
		{
			input.close();
		}
 	}
	
	public static void exportFileToFs(String userAgent, String location, String withManifest, String tmpDir, String folder) throws Throwable 
	{
		if ( fileService == null) 
			fileService = new FileService();

		// Get zip file from JCR
		FileService.DownloadFileWrapper wrapper = null;
			wrapper = fileService.doGetFileOrDirAsDownload( userAgent, location, withManifest );
	
		// Convert StreamingOutput to ByteArrayOutputStream
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wrapper.getOutputStream().write(out);

		// Save zip file to filesystem
		String filename = wrapper.getEncodedFileName();
		FileSystem.writeToFile( out, tmpDir, filename );

		// Unzip File
		String zipFile = (tmpDir + "/" + filename);
		ZipUtil.unpack(new File(zipFile), new File(folder));
	}

	public static Collection<String> excludeByRegex( Collection<String> list, String regexFilterList) {
		
		Collection<String> excludeList = new ArrayList<String>();
		
		String[] values = regexFilterList.split(",");
		final Set<Pattern> hashSet = new HashSet<>();
		for( String p : values ) {
			hashSet.add( Pattern.compile(p) );
		}

		for (String item : list) 
		{
			for (Pattern pattern : hashSet) 
			{
				if ( pattern.matcher( item ).matches() ) 
				{
					excludeList.add(item);
					break;
				} 
			}
		}

		return excludeList;
	}
}
