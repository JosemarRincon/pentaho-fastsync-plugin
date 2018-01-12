package br.gov.go.saude.pentaho.fastsync.util;

import br.gov.go.saude.pentaho.fastsync.engine.PluginConfig;
import br.gov.go.saude.pentaho.fastsync.models.Repo;
import br.gov.go.saude.pentaho.fastsync.models.ReturnFileList;

import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.RepositoryImportResource;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

public class Repository {
	private static FileService fileService = null;
	public static final String PATH_SEPARATOR = "/";
	public static final String ENCODED_PATH_SEPARATOR = ":";
	public static String TEMP_DIR = "";
	public static String FILE_NAME = "";
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

	public static void importFileToJcr(String location, String zipFile, String zipName) throws Exception {
		String logLevel = Repository.DEBUG ? "Verbose" : "INFO";
		String teste = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/teste/siconti.zip";
		InputStream input = new FileInputStream(zipFile);
		// String solution = File.separator+location;


		try {
			FormDataContentDisposition fileInfo = ((FormDataContentDisposition.FormDataContentDispositionBuilder) FormDataContentDisposition
					.name(FilenameUtils.getName(zipFile)).fileName(FilenameUtils.getName(zipFile))).build();

			RepositoryImportResource repositoryImporter = new RepositoryImportResource();
			if (Repository.DEBUG) {
				System.out.println("\n----->  fileInfo: " + fileInfo + "\n");
				System.out.println("\n----->  fileInfo name: " + fileInfo.getFileName() + "\n");
				System.out.println("\n----->  zipFile for import: " + zipFile + "\n");
				System.out.println("\n----->  solution: " + location + "\n");
				System.out.println("\n----->  logLevel: " + logLevel + "\n");

			}

			/**
			 * 
			 * @param importDir
			 *            JCR Directory to which the zip structure or single
			 *            file will be uploaded to.
			 * @param fileUpload
			 *            Input stream for the file.
			 * @param overwriteFile
			 *            The flag indicates ability to overwrite existing file.
			 * @param overwriteAclPermissions
			 *            The flag indicates ability to overwrite Acl
			 *            permissions.
			 * @param applyAclPermissions
			 *            The flag indicates ability to apply Acl permissions.
			 * @param retainOwnership
			 *            The flag indicates ability to retain ownership.
			 * @param charSet
			 *            The charset for imported file.
			 * @param logLevel
			 *            The level of logging.
			 * @param fileNameOverride
			 *            If present and the content represents a single file,
			 *            this parameter contains the filename to use when
			 *            storing the file in the repository. If not present,
			 *            the fileInfo.getFileName will be used. Note that the
			 *            later cannot reliably handle foreign character sets.
			 * 
			 */

			Response ret = repositoryImporter.doPostImport("/siconti", input, "true", "true", "true", "true",
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

		FileSystem.writeToFile(out, tmpDir, filename);

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

	public static void exportFileToFs2(String folder, String location, ReturnFileList listFiles)
			throws Exception, IOException {

		if (Repository.DEBUG) {
			System.out.println("\n-----> unpack file: " + folder + "\n");
			System.out.println("\n-----> location: " + location + "\n");
		}
		if (!listFiles.isEmpty()) {
			ZipUtil.unpack(new File(TEMP_DIR + "/" + FILE_NAME), new File(folder + location));
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

	public static RepositoryFileDto getJcrPathProperties(String location) throws FileNotFoundException {
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

			throw new FileNotFoundException("JCR path '" + location + "' not found.");
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

	public static void getFilesFromJcr(String userAgent, String location, String tmpDir, String withManifest)
			throws Exception, IOException {
		if (fileService == null) {
			fileService = new FileService();
		}

		FileService.DownloadFileWrapper wrapper = null;

		try {
			wrapper = fileService.doGetFileOrDirAsDownload(userAgent, location, withManifest);
		} catch (Throwable e) {
			if ((e instanceof FileNotFoundException)) {
				throw new Exception("JCR path '" + location.replaceAll(":","") + "' not found.");
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wrapper.getOutputStream().write(out);

		String filename = wrapper.getEncodedFileName();

		FileSystem.writeToFile(out, tmpDir, filename);

		String zipFile = tmpDir + "/" + filename;

		TEMP_DIR = tmpDir;
		FILE_NAME = filename;
		if(Repository.SYNC.equals("fs")){
			ZipUtil.unpack(new File(zipFile), new File(tmpDir));
		}
		
		if (Repository.DEBUG) {
			System.out.println("\n-----> zipFile gerado from JCR: " + zipFile + "\n");
			System.out.println("\n-----> tmpDir: " + tmpDir + "\n");
			System.out.println("\n-----> filename: " + filename + "\n");
			System.out.println("\n-----> location: " + location + "\n");
		}

	}

	public static Collection<String> addFilesModifidied(Collection<String> fileList, String solutionPath, String tmpDir,
			Map<String, Date> repoList, String base) throws Throwable {

		Collection<String> updateList = new ArrayList<String>();
		Iterator<String> i = fileList.iterator();

		while (i.hasNext()) {
			String fsFile = (String) i.next();
			// Date jcrTimestamp = (Date) repoList.get(base + fsFile);
			String _file = (solutionPath + "/" + fsFile.replaceAll(":", "/")).replaceAll("\\\\+", "/").replaceAll("/+",
					"/");
			String _jcrFilePath = (tmpDir + "/" + fsFile.replaceAll(":", "/")).replaceAll("\\\\+", "/").replaceAll("/+",
					"/");

			File file = new File(_file);
			if ((!file.isDirectory())) {

				try {


					Date jcrTimestamp = (Date) repoList.get(base + fsFile);
					if (FileSystem.isFilesDiffs(_file, _jcrFilePath)) {
						if (Repository.SYNC.equals("fs")) {

							if (jcrTimestamp.after(new Date(file.lastModified()))) {
								if (Repository.DEBUG) {
									System.out.println("\n-----> jcrTimestamp: " + jcrTimestamp);
									System.out.println("\n-----> fsTimestamp: " + new Date(file.lastModified()));
									System.out.println(
											"\n-----> file to be updated: " + fsFile.replaceAll(":", "/") + "\n");
								}

								updateList.add(fsFile.replaceAll(":", "/"));
								i.remove();
							}

						} else if (new Date(file.lastModified()).after(jcrTimestamp)) {
							if (Repository.DEBUG) {
								System.out.println("\n-----> jcrTimestamp: " + jcrTimestamp);
								System.out.println("\n-----> fsTimestamp: " + new Date(file.lastModified()));
								System.out
										.println("\n-----> file to be updated: " + fsFile.replaceAll(":", "/") + "\n");
							}

							updateList.add(fsFile.replaceAll(":", "/"));
							i.remove();
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

		File file = new File(TEMP_DIR + item);

		if ((file.isFile()) || ((file.isDirectory()) && (file.list().length == 0))) {
			FileSystem.deleteFile(file);
		}

	}

	public static void newZipForUpdate() {

		String solution = FileSystem.splitFileName(FILE_NAME)[0];

		ZipUtil.pack(new File(TEMP_DIR + "/" + solution), new File(TEMP_DIR + "/" + FILE_NAME), new NameMapper() {

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

			String _file = (solutionPath + "/" + StringUtils.removeStart(item, path)).replaceAll("/+", "/");
			File file = new File(_file);

			if ((!file.isDirectory()) && (FileUtils.isFileOlder(file, jcrTimestamp))) {
				preserveList.add(item.replaceAll(":", "/"));
				i.remove();
			}
		}

		return preserveList;
	}

}
