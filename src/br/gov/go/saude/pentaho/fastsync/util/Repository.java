package br.gov.go.saude.pentaho.fastsync.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.RepositoryImportResource;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;
import br.gov.go.saude.pentaho.fastsync.models.Output;
import br.gov.go.saude.pentaho.fastsync.engine.PluginConfig;
import br.gov.go.saude.pentaho.fastsync.models.Repo;
import br.gov.go.saude.pentaho.fastsync.models.ReturnFileList;

public class Repository {
	private static FileService fileService = null;
	public static final String PATH_SEPARATOR = "/";
	public static final String ENCODED_PATH_SEPARATOR = ":";
	public static String TEMP_DIR = "";
	public static String ZIP_FILE_NAME = "";
	public static String SOLUTION = "";
	public static boolean DEBUG = false;
	public static String SYNC = "jcr";

	public static Collection<String> getDiff(Collection<String> firstList, Collection<String> secondList) {
		Collection<String> _firstList = new ArrayList<String>(firstList);
		_firstList.removeAll(secondList);

		return _firstList;
	}

	public static Repo getRepoFiles(String location) {
		if (fileService == null) {
			fileService = new FileService();
		}

		Collection<String> items = new ArrayList<String>();

		Map<String, String> ids = new HashMap<String, String>();

		Map<String, Date> mds = new HashMap<String, Date>();

		List<RepositoryFileDto> repoFiles = fileService.doGetChildren(location, null, Boolean.valueOf(true),
				Boolean.valueOf(false));

		for (RepositoryFileDto repoFile : repoFiles) {
			String item = repoFile.getPath();

			items.add(item);
			ids.put(item, repoFile.getId());

			if (!repoFile.isFolder()) {
				mds.put(item, repoFile.getLastModifiedDate());
			}
			if (repoFile.isFolder()) {
				mds.put(item, repoFile.getCreatedDate());
				getRepoFiles(repoFile.getPath().replaceAll("/+", ":"), items, ids, mds, fileService);
			}
		}

		Repo repo = new Repo();

		repo.setItemsList(items);
		repo.setIdsList(ids);
		repo.setModifiedDateList(mds);
		repo.setRepoFiles(repoFiles);

		return repo;
	}

	private static void getRepoFiles(String location, Collection<String> ret, Map<String, String> ids,
			Map<String, Date> mds, FileService fileService) {
		if (!StringUtils.endsWith(location, ":")) {
			location = location + ":";
		}

		List<RepositoryFileDto> repoFiles = fileService.doGetChildren(location, null, Boolean.valueOf(true),
				Boolean.valueOf(false));

		for (RepositoryFileDto repoFile : repoFiles) {
			String item = repoFile.getPath();

			ret.add(item);
			ids.put(item, repoFile.getId());

			if (!repoFile.isFolder()) {
				mds.put(item, repoFile.getLastModifiedDate());
			}
			if (repoFile.isFolder()) {
				mds.put(item, repoFile.getCreatedDate());
				getRepoFiles(repoFile.getPath().replaceAll("/+", ":"), ret, ids, mds, fileService);
			}
		}
	}

	public static Collection<String> getLocalFiles(String location) throws Exception {
		Collection<String> ret = new ArrayList<String>();

		Collection<File> localFiles = Search.searchFileAndDirsRecursive(location);

		if (localFiles == null) {
			throw new Exception("Solution " + location + " not found in the pentaho-solution folder.");
		}
		for (File localFile : localFiles) {
			String[] parts = localFile.getPath().split("pentaho-solutions");
			ret.add(parts[1].replaceAll("/+", "/").replaceAll("\\\\+", "/"));
		}

		return ret;
	}

	public static String getDeleteList(String root, String location, String solutionFullPath) throws Exception {
		Repo repo = getRepoFiles(location);

		Collection<String> repoFiles = repo.getItemsList();

		Collection<String> localFiles = getLocalFiles(solutionFullPath);
		Collection<String> _localFiles = addPrefix(root, localFiles);

		List<String> deleteList = new ArrayList<String>();

		Collection<String> diffList = getDiff(repoFiles, _localFiles);

		if (Repository.DEBUG) {
			System.out.println("\n-----> localFiles: " + _localFiles + "\n");
			System.out.println("\n-----> repoFiles: " + repoFiles + "\n");
			System.out.println("\n-----> diffList: " + diffList + "\n");
		}
		for (String key : diffList) {
			String value = (String) repo.getIdsList().get(key);
			deleteList.add(value);
			if (Repository.DEBUG) {
				System.out.println("\n-----> " + key + " -> " + value);
			}
		}

		Collections.reverse(deleteList);

		return StringUtils.join(deleteList.toArray(), ",");
	}

	public static String getDeleteFsList(String root, String location, String solutionFullPath) throws Exception {
		String base = "/".equals(root) ? "" : root;

		Repo repo = getRepoFiles(location);

		Collection<String> _repoFiles = repo.getItemsList();
		Collection<String> repoFiles = new ArrayList<String>();

		for (String item : _repoFiles) {
			repoFiles.add(StringUtils.removeStart(item, base));
		}

		Collection<String> localFiles = getLocalFiles(solutionFullPath);

		localFiles.remove(StringUtils.removeStart(location, base));

		List<String> deleteList = new ArrayList<String>();

		Collection<String> diffList = getDiff(localFiles, repoFiles);

		if (Repository.DEBUG) {
			System.out.println("\n-----> localFiles: " + localFiles + "\n");
			System.out.println("\n-----> repoFiles: " + repoFiles + "\n");
			System.out.println("\n-----> diffList: " + diffList + "\n");
		}
		for (String item : diffList) {
			deleteList.add(item);
		}

		Collections.reverse(deleteList);

		Collection<String> excludeList = excludeByRegex(deleteList,
				PluginConfig.props.getProperty("import.exclude.list"));

		return StringUtils.join(getDiff(deleteList, excludeList).toArray(), ",");
	}

	public static Collection<String> addPrefix(String root, Collection<String> collection) {
		Collection<String> ret = new ArrayList<String>();

		for (String item : collection) {
			ret.add((root + item).replaceAll("/+", "/"));
		}

		return ret;
	}

	public static void deleteItems(String deleteList, boolean perm) throws Exception {
		if (fileService == null) {
			fileService = new FileService();
		}
		if (perm)
			fileService.doDeleteFilesPermanent(deleteList);
		else {
			fileService.doDeleteFiles(deleteList);
		}
	}

	public static void deleteItemsFs(String solutionPath, String deleteList) throws IOException {
		for (String token : deleteList.split(",")) {
			File item = new File(solutionPath + "/" + token.replaceAll(":", "/"));

			if (item.isFile()) {
				FileSystem.deleteFile(item);
			} else {
				FileSystem.deleteFolder(item);
			}
		}
	}

	public static void importFileToJcr(String zipFile, String zipName) throws Exception {
		String logLevel = Repository.DEBUG ? "DEBUG" : "INFO";
		InputStream input = new FileInputStream(zipFile);
		// String solution = File.separator+location;

		try {
			FormDataContentDisposition fileInfo = ((FormDataContentDisposition.FormDataContentDispositionBuilder) FormDataContentDisposition
					.name(FilenameUtils.getName(zipFile)).fileName(FilenameUtils.getName(zipFile))).build();

			RepositoryImportResource repositoryImporter = new RepositoryImportResource();
			if (Repository.DEBUG) {
				System.out.println("\n----->  zipFile for import: " + zipFile + "\n");
				System.out.println("\n----->  solution: " + SOLUTION + "\n");
				System.out.println("\n----->  logLevel: " + logLevel + "\n");
				System.out.println("\n----->  file separador: " + File.separator + "\n");
			}

			Response ret = repositoryImporter.doPostImport("/" + SOLUTION, input, "true", "true", "true", "true",
					"UTF-8", logLevel, fileInfo, null);

			if (ret.getStatus() == 403) {
				throw new UnifiedRepositoryAccessDeniedException("FORBIDDEN");
			}
			if (ret.getStatus() != 200) {
				String msg = ret.getEntity().toString();

				if (msg.contains("PentahoAccessControlException")) {
					throw new PentahoAccessControlException("Access Denied");
				}
				throw new Exception(msg);
			}

		} finally {
			input.close();
		}
	}

	public static void exportFileToFs(String userAgent, String location, String withManifest, String tmpDir,
			String folder, ArrayList<String> preservedFiles) throws Exception, IOException {
		if (fileService == null) {
			fileService = new FileService();
		}

		FileService.DownloadFileWrapper wrapper = null;
		try {
			wrapper = fileService.doGetFileOrDirAsDownload(userAgent, location, withManifest);
		} catch (Throwable e) {
			if ((e instanceof FileNotFoundException)) {
				throw new Exception("JCR path '" + location + "' not found.");
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wrapper.getOutputStream().write(out);

		String filename = wrapper.getEncodedFileName();

		FileSystem.writeToFile(out, filename);

		String zipFile = tmpDir + "/" + filename;

		if (preservedFiles.size() > 0) {

			ZipUtil.unpack(new File(zipFile), new File(tmpDir));

			for (String item : preservedFiles) {
				File file = new File(tmpDir + item);

				if ((file.isFile()) || ((file.isDirectory()) && (file.list().length == 0))) {
					FileSystem.deleteFile(file);
				}
			}

			String solution = FileSystem.splitFileName(filename)[0];

			ZipUtil.pack(new File(tmpDir + "/" + solution), new File(zipFile), new NameMapper() {

				public String map(String name) {
					return name;
				}
			});
		}

		ZipUtil.unpack(new File(zipFile), new File(folder));

	}

	public static void exportFileToFs2(String solutionPath, String location, ReturnFileList listFiles)
			throws Exception, IOException {

		if (!listFiles.getCreate().isEmpty() || !listFiles.getDelete().isEmpty() || !listFiles.getUpdate().isEmpty()) {
			ZipUtil.unpack(new File(TEMP_DIR + "/" + ZIP_FILE_NAME), new File(solutionPath));

			if (Repository.DEBUG) {
				System.out.println("\n-----> name file: " + ZIP_FILE_NAME + "\n");
				System.out.println("\n----->  Exported solutionPath FS: " + solutionPath + "\n");

			}
		}

	}

	public static Collection<String> excludeByRegex(Collection<String> list, String regexFilterList) {
		Collection<String> excludeList = new ArrayList<String>();

		String[] values = regexFilterList.split(",");
		final Set<Pattern> hashSet = new HashSet<>();
		for (String p : values) {
			hashSet.add(Pattern.compile(p));
		}

		for (String item : list) {
			for (Pattern pattern : hashSet) {
				if (pattern.matcher(item).matches()) {
					excludeList.add(item);
					break;
				}
			}
		}

		return excludeList;
	}

	public static RepositoryFileDto getJcrPathProperties(String location) throws FileNotFoundException, IOException {
		location = location.replaceAll("/+", ":").replaceAll("\\\\+", ":") + ":";
		location = location.replaceAll(":+", ":");

		RepositoryFileDto repositoryFileDto = null;

		if (fileService == null) {
			fileService = new FileService();
		}
		try {
			repositoryFileDto = fileService.doGetProperties(location);

			return repositoryFileDto;
		} catch (FileNotFoundException e) {
			FileSystem.deleteFolder(new File(Repository.TEMP_DIR));
			throw new FileNotFoundException("JCR path '" + location.replaceAll(":", "") + "' not found.");
		}
	}

	public static boolean isJcrPathExists(String location) {

		if (fileService == null) {
			fileService = new FileService();
		}
		try {
			fileService.doGetProperties(location);
			return true;

		} catch (FileNotFoundException e) {
			return false;
		}
	}

	public static Collection<String> removeNewerFsFiles(Collection<String> fileList, Map<String, Date> repoList,
			String solutionPath, String base) {
		Collection<String> preserveList = new ArrayList<String>();

		Iterator<String> i = fileList.iterator();

		while (i.hasNext()) {
			String item = (String) i.next();

			Date jcrTimestamp = (Date) repoList.get(base + item);

			String _file = (solutionPath + "/" + item.replaceAll(":", "/")).replaceAll("\\\\+", "/").replaceAll("/+",
					"/");
			File file = new File(_file);

			if ((!file.isDirectory()) && (FileUtils.isFileNewer(file, jcrTimestamp))) {
				preserveList.add(item.replaceAll(":", "/"));
				i.remove();
			}
		}

		return preserveList;
	}

	public static void getFilesFromJcr(String userAgent, String withManifest) throws IOException {

		if (fileService == null) {
			fileService = new FileService();
		}

		FileService.DownloadFileWrapper wrapper = null;

		try {
			while (wrapper == null) {
				wrapper = fileService.doGetFileOrDirAsDownload(userAgent, SOLUTION, "false");
			}

		} catch (Throwable e) {
			if ((e instanceof FileNotFoundException)) {
				throw new FileNotFoundException("JCR path '" + SOLUTION + "' not found.");
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String filename = "";

		wrapper.getOutputStream().write(out);
		filename = wrapper.getEncodedFileName();
		// write file zip in temp file
		FileSystem.writeToFile(out, filename);
		String zipFile = Repository.TEMP_DIR + File.separator + filename;
		ZIP_FILE_NAME = filename;
		ZipUtil.unpack(new File(zipFile), new File(TEMP_DIR));
		if (Repository.DEBUG) {
			System.out.println("\n-----> zipFile from JCR: " + zipFile + "\n");
			System.out.println("\n-----> unpack file: " + TEMP_DIR + "\n");

		}

	}

	public static Collection<String> addFilesModifidied(Collection<String> fileList, String solutionPath,
			Map<String, Date> repoList, String base) throws Throwable {

		Collection<String> updateList = new ArrayList<String>();
		Iterator<String> i = fileList.iterator();

		while (i.hasNext()) {
			String fsFile = (String) i.next();
			// Date jcrTimestamp = (Date) repoList.get(base + fsFile);
			System.out.println("\n-----> fsFile: " + fsFile);
			
			String _file = (solutionPath + "/" + fsFile.replaceAll(":", "/")).replaceAll("\\\\+", "/").replaceAll("\\+","/");
			String _jcrFilePath = (TEMP_DIR + fsFile.replaceAll(":", "/")).replaceAll("\\\\+", "/").replaceAll("/+",
					"/");

			File file = new File(_file);
			if ((!file.isDirectory())) {
				try {

					Date jcrTimestamp = (Date) repoList.get(base + fsFile);
					Date fsTimestamp = new Date(file.lastModified());
					if (FileSystem.isDiffForTypeFile(_jcrFilePath, _file)) {
						if (Repository.SYNC.equals("fs")) {
							if (jcrTimestamp.after(new Date(file.lastModified()))) {
								if (Repository.DEBUG) {
									System.out.println("\n-----> jcrTimestamp: " + jcrTimestamp);
									System.out.println("\n-----> fsTimestamp: " + fsTimestamp);
									System.out.println("\n-----> fsfile: " + _file);
									System.out.println("\n-----> jcrFile: " + _jcrFilePath);
									System.out.println(
											"\n-----> file to be updated: " + fsFile.replaceAll(":", "/") + "\n");
								}

								updateList.add(fsFile.replaceAll(":", "/"));
								i.remove();
							}

						} else if (Repository.SYNC.equals("jcr")) {
							if (fsTimestamp.after(jcrTimestamp)) {

								if (Repository.DEBUG) {
									System.out.println("\n-----> jcrTimestamp: " + jcrTimestamp);
									System.out.println("\n-----> fsTimestamp: " + new Date(file.lastModified()));
									System.out.println("\n-----> fsfile: " + _file);
									System.out.println("\n-----> jcrFile: " + _jcrFilePath);
									System.out.println("\n-----> file to be updated: "
											+ fsFile.replaceAll(":", File.separator) + "\n");
								}

								updateList.add(fsFile.replaceAll(":", "/"));
								i.remove();

							}

						}
					}

				} catch (Exception e) {
					throw new Exception(e);
				}
			}

		} //

		return updateList;
	}

	public static void removeFilePreservedList(String item) {
		// System.out.println("\n-----> remove preserved file : " + TEMP_DIR + item);
		File file = new File(TEMP_DIR + item);

		if ((file.isFile()) || ((file.isDirectory()) && (file.list().length == 0))) {
			FileSystem.deleteFile(file);
		}

	}

	public static void newZipForUpdate() {

		String solution = FileSystem.splitFileName(ZIP_FILE_NAME)[0];
		if (Repository.DEBUG) {
			System.out.println("\n-----> zip for update: " + TEMP_DIR + File.separator + ZIP_FILE_NAME);
		}

		ZipUtil.pack(new File(TEMP_DIR + File.separator + solution),
				new File(TEMP_DIR + File.separator + ZIP_FILE_NAME), new NameMapper() {

					public String map(String name) {
						return name;
					}
				});

	}

	public static Collection<String> removeNewerJcrFiles(Collection<String> fileList, Map<String, Date> repoList,
			String solutionPath, String path) {
		Collection<String> preserveList = new ArrayList<String>();

		Iterator<String> i = fileList.iterator();

		while (i.hasNext()) {
			String item = (String) i.next();

			Date jcrTimestamp = (Date) repoList.get(item);

			String _file = (solutionPath + File.separator + StringUtils.removeStart(item, path)).replaceAll("/+",
					File.separator);
			File file = new File(_file);

			if ((!file.isDirectory()) && (FileUtils.isFileOlder(file, jcrTimestamp))) {
				preserveList.add(item.replaceAll(":", "/"));
				i.remove();
			}
		}

		return preserveList;
	}

	public static void listFs(String solution, String path, ReturnFileList returnList, boolean keepNewerFlag,
			String tmpDir, String withManifest, String userAgent) throws Throwable {
		String base = "/".equals(path) ? "" : path;
		String location = (path + "/" + solution).replaceAll("/+", "/");
		Repository.getJcrPathProperties(location.replaceAll("/", ":") + ":");

		String solutionFullPath = PentahoSystem.getApplicationContext().getSolutionPath(solution)
				.replaceAll("\\\\+", "/").replaceAll("/+", "/");

		Repo repoMaps = Repository.getRepoFiles(location.replaceAll("/", ":") + ":");

		Collection<String> _repoFiles = repoMaps.getItemsList();

		if (Repository.DEBUG) {
			System.out.println("\n-----> repoFiles and folders zize: " + _repoFiles.size() + "\n");
		}
		FileSystem.deleteFolder(new File(Repository.TEMP_DIR));

		Collection<String> repoFiles = new ArrayList<String>();

		for (String item : _repoFiles) {
			repoFiles.add(StringUtils.removeStart(item, base));
		}

		Collection<String> localFiles = Repository.getLocalFiles(solutionFullPath);

		localFiles.remove(StringUtils.removeStart(location, base));

		Collection<String> _deleteList = Repository.getDiff(localFiles, repoFiles);

		Collection<String> excludeList = Repository.excludeByRegex(localFiles,
				PluginConfig.props.getProperty("import.exclude.list"));
		try {
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

			System.out.println("\n-----> keepNewerFlag:" + keepNewerFlag + " \n\n");

			if (Repository.DEBUG) {
				System.out.println("\n-----> base: " + base + "\n");
				System.out.println("\n-----> tmpDir: " + tmpDir + "\n");
				System.out.println("\n-----> userAgent: " + userAgent + "\n");
				// System.out.println("\n-----> repoFiles: " + repoFiles + "\n\n");
				// System.out.println("\n-----> localFiles: " + localFiles + "\n");
			}
			Collection<String> updateList = new ArrayList<String>();
			System.out.println("\n-----> Repository.SOLUTION: " + Repository.SOLUTION + "\n");
			System.out.println("\n-----> isJcrPathExists: " + Repository.isJcrPathExists(Repository.SOLUTION) + "\n");
			if (Repository.isJcrPathExists(Repository.SOLUTION)) {
				Repository.getFilesFromJcr(userAgent, withManifest);
				Collection<String> listFiltered = Repository.getDiff(
						Repository.getDiff(Repository.getDiff(repoFiles, excludeList), createList), deleteList);
				updateList = Repository.addFilesModifidied(listFiltered,
						PentahoSystem.getApplicationContext().getSolutionPath(""), repoMaps.getModifiedDateList(),
						base);
				if (updateList == null) {
					updateList = new ArrayList<String>();
				}

				// if (keepNewerFlag) {
				Collection<String> preserveList = Repository.getDiff(Repository.getDiff(localFiles, excludeList),
						updateList);
				System.out.println("\n-----> preserveList: " + preserveList + "\n\n");
				for (String item : preserveList) {
					if (Repository.isJcrPathExists(item)) {
						System.out.println("\n-----> item: " + item + "\n\n");
						if (!Repository.getJcrPathProperties(base + item).isFolder()) {
							returnList.getPreserve().add(item);
							Repository.removeFilePreservedList(item);
						}
					}
				}
				Repository.newZipForUpdate();

				// } else {
				// updateList = Repository.getDiff(localFiles, excludeList);
				// }
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
		} finally {
			File folder = new File(Repository.TEMP_DIR);
			if (folder.exists()) {
				FileSystem.deleteFolder(folder);
				System.out.println("\n-----> Delete list FS tmp : " + Repository.TEMP_DIR + "\n");

			}

		}

	}

	public static void listJcr(String solution, String path, ReturnFileList returnList, boolean keepNewerFlag,
			String withManifest, String userAgent) throws Throwable {
		// String solutionPath =
		// PentahoSystem.getApplicationContext().getSolutionPath("").replaceAll("\\\\+",
		// "/")
		// .replaceAll("/+", "/");

		String base = "/".equals(path) ? "" : path;

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
		try {
			for (String item : excludeList) {
				returnList.getExclude().add(item);
			}

			Collection<String> deleteList = Repository.getDiff(repoFiles, _localFiles);

			for (String item : deleteList) {
				returnList.getDelete().add(item);
			}

			Collection<String> _createList = Repository.getDiff(Repository.getDiff(_localFiles, repoFiles),
					excludeList);
			Collection<String> createList = Repository.getDiff(_createList, returnList.getPreserve());

			for (String item : createList) {
				returnList.getCreate().add(item);
			}

			if (Repository.DEBUG) {
				System.out.println("\n-----> repoFiles: " + repoFiles + "\n");
				// System.out.println("\n-----> excludeList: " + excludeList + "\n");
				System.out.println("\n-----> createList: " + createList + "\n");
			}
			Collection<String> updateList = new ArrayList<>();

			if (Repository.isJcrPathExists(Repository.SOLUTION)) {
				Repository.getFilesFromJcr(userAgent, withManifest);
				System.out.println("\n----->  Solution sync: " + Repository.SOLUTION + "\n");
				Collection<String> listFiltered = Repository.getDiff(
						Repository.getDiff(Repository.getDiff(repoFiles, excludeList), createList), deleteList);
				updateList = Repository.addFilesModifidied(listFiltered,
						PentahoSystem.getApplicationContext().getSolutionPath(""), repoMaps.getModifiedDateList(),
						base);

				if (keepNewerFlag) {
					Collection<String> preserveList = Repository.getDiff(Repository.getDiff(localFiles, excludeList),
							updateList);
					if (Repository.DEBUG) {
						System.out.println("\n-----> preserveList: " + preserveList + "\n");
					}

					for (String item : preserveList) {
						if (Repository.isJcrPathExists(item)) {
							if (!Repository.getJcrPathProperties(item).isFolder()) {
								returnList.getPreserve().add(item);
								Repository.removeFilePreservedList(item);
							}
						}
					}

				} else {
					updateList = Repository.getDiff(localFiles, excludeList);
				}
			}

			if (Repository.DEBUG) {
				System.out.println("\n-----> updateList: " + updateList + "\n");
				System.out.println("\n-----> updateList size: " + updateList.size() + "\n");
			}

			for (String item : updateList) {
				if (!Repository.getJcrPathProperties(item).isFolder()) {
					returnList.getUpdate().add(item);
				}
			}

		} finally {
			File folder = new File(Repository.TEMP_DIR);
			if (folder.exists()) {
				FileSystem.deleteFolder(folder);
				System.out.println("\n-----> Delete list JCR tmp : " + Repository.TEMP_DIR + "\n");

			}

		}
	}

	public static void syncFs(String solution, String path, String delete, Output output, String tmpDir,
			String userAgent, String withManifest, boolean keepNewerFlag) throws Throwable {
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
		try {
			solutionPath = solutionPath + Repository.SOLUTION;
			Repository.listFs(solution, path, listFiles, keepNewerFlag, tmpDir, withManifest, userAgent);
		} finally {
			Repository.exportFileToFs2(solutionPath, location, listFiles);
			FileSystem.deleteFolder(new File(Repository.TEMP_DIR));
			if (Repository.DEBUG) {
				System.out.println("\n-----> solutionPath FS: " + solutionPath + "\n");
				System.out.println("\n-----> Delete folder tmp FS : " + Repository.TEMP_DIR + "\n");
			}

		}

		output.setError(Boolean.valueOf(false));
		output.setMessage("Successful synchronize to FileSystem from JCR.");
	}

	public static void syncJcr(String solution, String path, String delete, String deletePerm, Output output,
			boolean keepNewerFlag, String withManifest, String userAgent) throws Throwable {
		String solutionFullPath = PentahoSystem.getApplicationContext().getSolutionPath(solution)
				.replaceAll("\\\\+", "/").replaceAll("/+", "/");

		if ("true".equalsIgnoreCase(delete)) {
			String location = (path + "/" + solution).replaceAll("/+", "/");

			String deleteList = Repository.getDeleteList(path, location, solutionFullPath);

			if (deleteList.length() > 0) {
				Repository.deleteItems(deleteList, "true".equalsIgnoreCase(deletePerm));
			}
		}

		String dstCopyFull = Repository.TEMP_DIR + File.separator + Repository.SOLUTION + File.separator
				+ Repository.SOLUTION + File.separator;
		File dstDir = new File(dstCopyFull);

		if (Repository.DEBUG) {
			System.out.println("\n----->  solutionFullPath: " + solutionFullPath + "\n");
			System.out.println("\n----->  destine dir: " + dstDir + "\n");

		}

		ReturnFileList listFiles = new ReturnFileList();
		try {
			Repository.listJcr(solution, path, listFiles, keepNewerFlag, withManifest, userAgent);
		} finally {
			String listaExcludCopy = listFiles.getPreserve().isEmpty() ? ""
					: listFiles.getPreserve().toString().replaceAll("\\[", "").replaceAll("\\]", "")
							.replaceAll("\\/" + Repository.SOLUTION, ".*\\\\\\+").replaceAll("\\+", "\\\\");
			if (Repository.DEBUG) {
				System.out.println("\n----->  listaExcludCopy: " + listaExcludCopy + "\n");
			}
			FileSystem.copyDirectory(new File(solutionFullPath), dstDir,
					PluginConfig.props.getProperty("import.exclude.list"));
		}
		String dstTarget = Repository.TEMP_DIR + File.separator + Repository.SOLUTION + File.separator;
		for (String item : listFiles.getPreserve()) {
			File file = new File(dstTarget + item);
			/*
			 * if ((file.isFile()) || (FileSystem.isDirectoryEmpty(file))) {
			 * FileSystem.deleteFile(file); }
			 */
			if ((file.isFile())) {
				FileSystem.deleteFile(file);
			}
		}

		Collection<File> stageFolderFiles = Search.searchFileAndDirsRecursive(dstCopyFull);
		stageFolderFiles.remove(new File(dstCopyFull));
		Collections.reverse((List<File>) stageFolderFiles);
		for (File item : stageFolderFiles) {
			if (FileSystem.isDirectoryEmpty(item)) {
				FileSystem.deleteFile(item);
			}
		}
		try {

			if (!FileSystem.isDirectoryEmpty(dstCopyFull)) {
				Zip zipPack = new Zip();
				String zipName = Repository.SOLUTION + ".zip";
				String fullZipName = Repository.TEMP_DIR + File.separator + zipName;
				zipPack.setFullPathZipFileName(fullZipName.replaceAll("\\\\+", "/").replaceAll("/+", "/"));
				// zipPack.setPackDirectoryPath((dstTargetFull).replaceAll("\\\\+",
				// "/").replaceAll("/+", "/"));
				zipPack.setPackDirectoryPath((dstTarget).replaceAll("\\\\+", "/").replaceAll("/+", "/"));
				zipPack.packDirectory();

				if (Repository.DEBUG) {
					System.out.println("\n----->  tmpDir para JRC: "
							+ (dstCopyFull).replaceAll("\\\\+", "/").replaceAll("/+", "/") + "\n");
					System.out.println("\n----->  fullZipName para enviar pro JRC: "
							+ fullZipName.replaceAll("\\\\+", "/").replaceAll("/+", "/") + "\n");
					System.out.println("\n----->  solution: " + Repository.SOLUTION + "\n");
				}
				Repository.importFileToJcr(fullZipName.replaceAll("\\\\+", "/").replaceAll("/+", "/"), zipName);
			}

			output.setError(Boolean.valueOf(false));
			output.setMessage("Successful synchronize to JCR from FileSystem.");

		} catch (Throwable e) {
			output.setError(Boolean.valueOf(true));
			output.setError_message(e.getMessage());
			output.setMessage("FastSync: Internal Server Error");
			e.printStackTrace();

		}
	}

}