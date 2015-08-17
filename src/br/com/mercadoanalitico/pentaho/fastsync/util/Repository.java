package br.com.mercadoanalitico.pentaho.fastsync.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import br.com.mercadoanalitico.pentaho.fastsync.models.Repo;
import br.com.mercadoanalitico.pentaho.fastsync.pentaho.ArchiveLoader;

public class Repository {
	
	// Remove from the firstList all elements of secondList
	public static Collection<String> getDiff(Collection<String> firstList, Collection<String> secondList)	
	{
		firstList.removeAll(secondList);
		
		return firstList;
	}

	// Get folders and file from JCR
	public static Repo getRepoFiles(String location) 
	{
		FileService fileService = new FileService();

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
	public static Collection<String> getLocalFiles(String location) 
	{
		Collection<String> ret = new ArrayList<String>();
		
		Collection<File> localFiles = Search.searchFileAndDirsRecursive(location);
		
		for (File localFile : localFiles) 
		{
			String[] parts = localFile.getPath().split("pentaho-solutions");
			ret.add( parts[1].replaceAll("/+", ":").replaceAll("\\\\+", ":") );
		}
		
		return ret;
	}
	
	// Get a list of files and folders to be delete in the JCR
	public static String getDeleteList(String solution, String solutionFullPath, boolean debug)
	{
		// Get Repository items and ids
		Repo repo = getRepoFiles( ":" + solution + ":" );

		// Get list of files and folders from JCR
		Collection<String> repoFiles = repo.getItemsList();
		
		// Get list of files and folders from Filesystem
		Collection<String> localFiles = getLocalFiles(solutionFullPath);
		
		if (debug) System.out.println("----->  repoFiles: " + repoFiles);
		if (debug) System.out.println("-----> localFiles: " + localFiles);

		List<String> deleteList = new ArrayList<String>();
		
		Collection<String> diffList = getDiff(repoFiles, localFiles);
		
		if (debug) System.out.println("diffList: " + diffList);

		for (String key : diffList) {
			
			String value = repo.getIdsList().get(key);
			deleteList.add(value);
			
			if (debug)  System.out.println("-----> " + key + " -> " + value);
		}
		
		// Reverse order to delete files before their parent folder
		Collections.reverse(deleteList);
		
		return StringUtils.join(deleteList.toArray(), ",");
	}

	public static void deleteItems(String deleteList, boolean perm) throws Exception
	{
		FileService fileService = new FileService();

		if (perm) fileService.doDeleteFilesPermanent(deleteList);
		else fileService.doDeleteFiles(deleteList);
		
	}

	public static void loadFileToJcr(String location) throws FileNotFoundException, PlatformImportException
	{
		File directory = new File(location);
		
        // Instantiate the importer
        IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
        ArchiveLoader archiveLoader = new ArchiveLoader( importer );
        archiveLoader.loadAll( directory, ArchiveLoader.ZIPS_FILTER );
	}

}
