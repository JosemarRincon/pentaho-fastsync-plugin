package br.com.mercadoanalitico.pentaho.fastsync.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class Search {

	// Busca lista de arquivos dentro de um diretorio. Para o nome do arquivo pode se usar wildcards.
	// Nao faz busca recursiva.
	public static File[] searchFileFilter(String location, String fileName) {
		try {
			File dir = new File(location);
			FileFilter fileFilter = new WildcardFileFilter(fileName);
			return dir.listFiles(fileFilter);
		} catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}

	// Busca lista de arquivos dentro de um diretorio. Para o nome do arquivo pode se usar wildcards.
	// Nao faz busca recursiva.
	// Opcoes de sort: asc ou desc
	public static File[] searchFileFilter(String location, String fileName, String sortType) {
			
		File[] files = searchFileFilter(location, fileName);
			
		Arrays.sort( files,  
			new Comparator<File>() {  
				public int compare(File f1, File f2 ) {  
					return ((String)f1.getName()).compareTo((String)f2.getName());
	            }  
	        }  
	    );
		
		if ("desc".equalsIgnoreCase(sortType)){
			Collections.reverse(Arrays.asList(files)); 
		}
			
		return files;
			
	}
	
    //
    // Finds files within a root directory and optionally its
    // subdirectories which match an array of extensions. When the
    // extensions is null all files will be returned.
    //
    // This method will returns matched file as java.io.File
    //
	public static Collection<File> searchFileRecursive(String location, String[] extensions, boolean recursive) {
    	File root = new File(location);
 
        try {
            Collection<File> files = FileUtils.listFiles(root, extensions, recursive);
            return files;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}

    //
    // Finds files/directories within a root directory and optionally its
    // subdirectories which match an array of extensions. When the
    // extensions is null all files will be returned.
    //
    // This method will returns matched file as java.io.File
    //
	public static Collection<File> searchFileAndDirsRecursive(String location) {
    	File root = new File(location);
 
        try {
            Collection<File> files = FileUtils.listFilesAndDirs(root, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
            return files;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}
	
}