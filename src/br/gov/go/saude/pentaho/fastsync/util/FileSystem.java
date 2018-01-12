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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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

	public static void writeToFile(ByteArrayOutputStream outputStream, String directory, String fileName)
			throws Exception {
		FileOutputStream fop = null;

		try {
			File file = new File(directory + File.separator + fileName);
			fop = new FileOutputStream(file);

			if (!file.exists()) {
				file.createNewFile();
			}

			byte[] contentInBytes = outputStream.toByteArray();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			System.out.println("\n file write : \n"+directory + File.separator + fileName);
			System.out.println("\n *****Done****** \n ");
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
	
	public static boolean isFilesDiffs(String file1, String file2) {
        File f1 = new File(file1);
        File f2 = new File(file2);
        byte[] f1_buf = new byte[1048576];
        byte[] f2_buf = new byte[1048576];
        int len;
        if (f1.length() != f2.length()) {
            try {
                InputStream isf1 = new FileInputStream(f1);
                InputStream isf2 = new FileInputStream(f2);
                try {
                    while (isf1.read(f1_buf) >= 0) {
                        len=isf2.read(f2_buf);
                        for (int j = 0; j < len; j++) {
                            if (f1_buf[j] != f2_buf[j]) {
                                return true; // tamanho diferente e  conteudo diferente
                            }
                        }
                    }
                } catch (IOException e) {
                }
            } catch (FileNotFoundException e) {
            }
        } 
        return false; // arquivos iguais
    }

	@SuppressWarnings("unused")
	public static boolean diffContentFiles(final List<String> firstFileContent, final List<String> secondFileContent) {
		final List<String> diff = new ArrayList<String>();
		boolean filesDiff = false;
		int i =0;

		for (final String line : secondFileContent) {
			//&& !line.contentEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
			if (!firstFileContent.contains(line)  ) {
				// diff.add(line);
				if (Repository.DEBUG) {
					System.out.println("\n-----> line firstFileContent Diff: " + firstFileContent+"\n ------>"+firstFileContent.size()+"firstFileContent size \n");
					System.out.println("\n-----> line secondFileContent Diff: " + secondFileContent+"\n ------>"+secondFileContent.size()+"secondFileContent size \n");
					System.out.println("\n-----> line fileSystem Diff: " + line);
					System.out.println("\n-----> line index: " + i);
				}
				filesDiff = true;
				break;
			}
			i++;
		}
		
		
		return filesDiff;
	}
}
