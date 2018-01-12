package br.gov.go.saude.pentaho.fastsync.util;

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

	public static File[] searchFileFilter(String location, String fileName, String sortType) {

		File[] files = searchFileFilter(location, fileName);

		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return ((String) f1.getName()).compareTo((String) f2.getName());
			}
		});

		if ("desc".equalsIgnoreCase(sortType)) {
			Collections.reverse(Arrays.asList(files));
		}

		return files;
	}

	public static Collection<File> searchFileRecursive(String location, String[] extensions, boolean recursive) {
		File root = new File(location);
		try {
			return FileUtils.listFiles(root, extensions, recursive);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Collection<File> searchFileAndDirsRecursive(String location) {
		File root = new File(location);
		if (!root.exists()) {
			root.mkdir();
		}
		try {
			return FileUtils.listFilesAndDirs(root, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Collection<File> searchFileAndDirsRecursive(File root) {
		try {
			return FileUtils.listFilesAndDirs(root, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
