package br.gov.go.saude.pentaho.fastsync.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.web.http.api.resources.services.FileService.RepositoryFileToStreamWrapper;

public class FileSystem {
	public static boolean renameFile(String oldName, String newName) {
		String fileTemp = null;

		File fileOld = new File(oldName);

		File fileNew = new File(newName);

		if (fileNew.exists()) {
			fileTemp = newName + ".tmp";

			if (!renameFile(newName, fileTemp)) {
				return false;
			}
		}

		boolean success = fileOld.renameTo(fileNew);
		if (!success) {
			return false;
		}

		if (fileTemp != null) {
			deleteFile(newName + ".tmp");
		}

		return true;
	}

	public static boolean deleteFile(String fileName) {

		boolean success = new File(fileName).delete();

		if (!success) {
			return false;
		}

		return true;
	}

	public static boolean deleteFile(File fileName) {
		boolean success = fileName.delete();

		if (!success) {
			return false;
		}

		return true;
	}

	public static void deleteFolder(File directory) throws IOException {
		FileUtils.deleteDirectory(directory);
	}

	public static void copyDirectory(File from, File to) throws IOException {
		FileUtils.copyDirectory(from, to);
	}

	/*
	 * Copiar diretorio para outro local
	 * 
	 * @param src
	 * 
	 * @param dst
	 * 
	 * @param exclude regex list
	 * 
	 * @return void
	 * 
	 * @throws IOException
	 */
	public static void copyDirectory(File from, File to, String regexFilterList) throws IOException {

		String[] values = regexFilterList.split(",");
		final Set<Pattern> hashSet = new HashSet<>();
		for (String p : values) {
			hashSet.add(Pattern.compile(p));
		}

		FileUtils.copyDirectory(from, to, new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				for (Pattern pattern : hashSet) {
					if (pattern.matcher(FilenameUtils.separatorsToUnix(pathname.getPath())).matches()) {
						return false;
					}
				}
				return true;
			}
		});

	}

	public static boolean appendTextToFile(String fileName, String texto) {
		FileWriter out = null;
		try {
			out = new FileWriter(fileName, true);
			out.write(texto);

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public static String lerArquivoTexto(String nomeArquivo) {
		File arquivo = new File(nomeArquivo);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(arquivo);

			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);

			String conteudo = new String(bytes).replaceAll("\r", "");

			return conteudo;

		} catch (FileNotFoundException e) {
			System.out.println("Class lerArquivo: File not Found!");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static String[] splitFileName(String nomeArquivo) {
		String[] conteudo = new String[2];

		conteudo[0] = nomeArquivo.substring(0, nomeArquivo.lastIndexOf("."));
		conteudo[1] = nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1);

		return conteudo;
	}

	public static void writeToFile(InputStream inputStream, String directory, String fileName) throws Exception {
		OutputStream out = null;

		try {
			File dir = new File(directory);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(directory + File.separator + fileName);
			file.createNewFile();

			out = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} finally {
			out.flush();
			out.close();
		}
	}

	public static void writeToFile(ByteArrayOutputStream outputStream, String fileName) throws IOException {
		FileOutputStream fop = null;

		try {
			File file = new File(Repository.TEMP_DIR + File.separator + fileName);
			File folder = new File(Repository.TEMP_DIR);

			if (!folder.exists()) {
				folder.mkdir();
			}

			if (!file.exists()) {
				file.createNewFile();
			}

			fop = new FileOutputStream(file);

			byte[] contentInBytes = outputStream.toByteArray();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			if (Repository.DEBUG) {
				System.out.println("\n file write : \n" + Repository.TEMP_DIR + File.separator + fileName);
				System.out.println("\n ***** Download from JCR is complete ****** \n ");

			}
			
		} finally {
			if (fop != null) {
				fop.close();
			}
		}
	}

	public static String getTmpDir(String location) {
		String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + location;

		File dir = new File(tmpDir);

		if (!dir.exists()) {
			dir.mkdir();
		}
		return tmpDir;
	}

	public static boolean isDirectoryEmpty(File file) {
		return (file.isDirectory()) && (file.list().length == 0);
	}

	public static boolean isDirectoryEmpty(String file) {
		File _file = new File(file);

		return (_file.isDirectory()) && (_file.list().length == 0);
	}

	public static boolean isFileExists(String path) {
		File file = new File(path);
		return file.exists();
	}

	public static boolean isDiffForTypeFile(String file1, String file2) throws IOException {
		String ext = file1.substring(file1.length() - 3);
		if (ext.equalsIgnoreCase("ktr") || ext.equalsIgnoreCase("kjb")) {
			if (Repository.SYNC.equals("fs")) {
				return diffFiles2(file2, file1);
			} else {
				return diffFiles2(file1, file2);
			}

		} else {
			return isFilesDiffs(file2, file1);
		}

		// return isFilesDiffs(file1, file2);

	}

	public static boolean isFilesDiffs(String file1, String file2) throws IOException, FileNotFoundException {
		File f1 = new File(file1);
		File f2 = new File(file2);
		boolean diff = false;
		if (f1.length() != f2.length()) {
			InputStream isf1 = null;
			InputStream isf2 = null;
			isf1 = new FileInputStream(f1);
			isf2 = new FileInputStream(f2);
			try {
				diff = compareStreams(isf1, isf2);
				if (!diff) {
					diff = compareStreams(isf2, isf1);
				}
			} catch (FileNotFoundException ex) {
				throw new IOException(ex);
			}
		}
		return diff; // arquivos iguais
	}

	public static boolean compareStreams(InputStream isf1, InputStream isf2) throws IOException {
		int len;
		byte[] f1_buf = new byte[1048576];
		byte[] f2_buf = new byte[1048576];
		while (isf1.read(f1_buf) >= 0) {
			len = isf2.read(f2_buf);
			for (int j = 0; j < len; j++) {
				if (f1_buf[j] != f2_buf[j]) { // tamanho diferente e conteudo diferente
					if (Repository.DEBUG) {
						System.out.println("\n-----> inputStrem 1: " + f1_buf[j]);
						System.out.println("\n-----> inputStrem 2: " + f2_buf[j]);
					}
					return true;
				}
			}
		}
		isf1.close();
		isf2.close();
		return false;
	}

	public static boolean compareEspecificFiles(String file1, String file2) throws IOException {
		boolean filesDiff = false;
		final Path _f1 = Paths.get(file1.toString());
		final Path _f2 = Paths.get(file2.toString());
		List<String> jcr = Files.readAllLines(_f1, Charset.forName("UTF-8"));
		List<String> fileSystem = Files.readAllLines(_f2, Charset.forName("UTF-8"));
		int i = 0;
		if (file1.length() != file2.length()) {
			for (String line : jcr) {
				if (!fileSystem.contains(line)) {
					if (line.contains("<directory>") || line.contains("<directory />")
							|| line.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
							|| line.contains("<modified_date>") || line.contains("partitionschema")
							|| line.contains("dynamic") || line.contains("partitions_per_slave")
							|| line.contains("slaveserver") || line.contains("master") || line.contains("hostname")
							|| line.contains("port") || line.contains("key_for_session_key")) {
						filesDiff = false;
						continue;
					}
					if (Repository.DEBUG) {
						System.out.println("\n-----> conteudo line: " + line.trim());
						System.out.println("\n-----> conteudo : " + fileSystem);
						System.out.println("\n-----> index : " + i);
					}
					filesDiff = true;
					break;
				}
				i++;
			}
		}
		return filesDiff;

	}

	public static boolean diffFiles2(String f1, String f2) throws IOException {
		boolean filesDiff = false;
		filesDiff = compareEspecificFiles(f1, f2);
		if (!filesDiff) {
			filesDiff = compareEspecificFiles(f2, f1);
		}

		return filesDiff;
	}
}
