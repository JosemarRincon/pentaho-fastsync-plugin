package br.com.mercadoanalitico.pentaho.fastsync.util;

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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class FileSystem {

	/**
	 * Renomear um arquivo ou diretorio.
	 * 
	 * @param oldName
	 * @param newName
	 * @return true / false
	 */
	public static boolean renameFile(String oldName, String newName) {
		
		String fileTemp = null;

		// File (or directory) with old name
		File fileOld = new File(oldName);

		// File (or directory) with new name
		File fileNew = new File(newName);

		// Se o arquivo de destino ja existe, renomear para um nome temporario
		if (fileNew.exists()) {
			
			fileTemp = newName + ".tmp";

			if (!renameFile(newName, fileTemp)) {
				return false;
			}
		}

		// Rename file (or directory)
		boolean success = fileOld.renameTo(fileNew);
		if (!success) {
			return false;
			// File was not successfully renamed
		}

		// Se foi criado um arquivo temporario, apagar
		if (fileTemp != null) {
			deleteFile(newName + ".tmp");
		}

		return true;
	}

	/**
	 * Deletar um arquivo
	 * 
	 * @param fileName
	 * @return true / false
	 */
	public static boolean deleteFile(String fileName) {

		boolean success = (new File(fileName)).delete();

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

	/**
	 * Deletar um diretorio
	 * 
	 * @param folderName
	 * @return void
	 * @throws IOException 
	 */
	public static void deleteFolder(File directory) throws IOException {

		FileUtils.deleteDirectory(directory);

	}

	/**
	 * Copiar diretorio para outro local
	 * 
	 * @param src
	 * @param dst
	 * @return void
	 * @throws IOException 
	 */
	public static void copyDirectory(File from, File to) throws IOException {

		FileUtils.copyDirectory(from, to);

	}
	
	/*
	 * Copiar diretorio para outro local
	 * 
	 * @param src
	 * @param dst
	 * @param exclude regex list
	 * @return void
	 * @throws IOException 
	 */
	public static void copyDirectory(File from, File to, String regexFilterList) throws IOException {
	
		String[] values = regexFilterList.split(",");
		final Set<Pattern> hashSet = new HashSet<>();
		for( String p : values ) {
			hashSet.add( Pattern.compile(p) );
		}
	
		FileUtils.copyDirectory(from, to, new FileFilter() {
				
			@Override
			public boolean accept(File pathname) {
				for (Pattern pattern : hashSet) {
					if ( pattern.matcher( FilenameUtils.separatorsToUnix(pathname.getPath()) ).matches() ) {
						return false;
					}
				}
				return true;
			}
		});
	
	}
	

	/**
	 * Incluir um texto no final de arquivos .txt
	 * 
	 * @param fileName
	 * @param texto
	 * @return true / false
	 */
	public static boolean appendTextToFile (String fileName, String texto) {
		
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

	/**
	 * Ler o conteudo de um arquivo .txt
	 * 
	 * @param nomeArquivo
	 * @return string
	 */
	public static String lerArquivoTexto(String nomeArquivo) {

		File arquivo = new File(nomeArquivo);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(arquivo);
			
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);

			String conteudo = new String(bytes).replaceAll("\r", "");
			
			return conteudo;
		
		}
		catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("Class lerArquivo: File not Found!");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;		
	}
	
	/**
	 * Separa nome e extensao de um arquivo
	 * 
	 * @param nomeArquivo
	 * @return String[0] - nome do arquivo / String[1] - extensao do arquivo
	 */
	public static String[] splitFileName(String nomeArquivo) {

		String[] conteudo = new String[2];
		
		conteudo[0] = nomeArquivo.substring(0, nomeArquivo.lastIndexOf(".")); 
		conteudo[1] = nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1);
		
		return conteudo;
	}
	
	public static void writeToFile(InputStream inputStream, String directory, String fileName) throws Exception 
	{
		OutputStream out = null;

		try
		{
			File dir = new File(directory);
			if (!dir.exists()) dir.mkdirs();

			File file = new File(directory + File.separator + fileName);
			file.createNewFile();

			out = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) 
			{
				out.write(bytes, 0, read);
			}
			
		} finally {
			out.flush();
			out.close();
		}
	}

	public static void writeToFile(ByteArrayOutputStream outputStream, String directory, String fileName) throws Exception 
	{
		FileOutputStream fop = null;
		File file;

		try {

			file = new File(directory + File.separator + fileName);
			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			byte[] contentInBytes = outputStream.toByteArray();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

			System.out.println("Done");

		} finally {
			if (fop != null)
				fop.close();
		}
	}

	public static String getTmpDir(String location) 
	{
		String tmpDir = System.getProperty( "java.io.tmpdir" ) + File.separator + location;
		
		File dir = new File(tmpDir);
		
		if ( !dir.exists() ) dir.mkdir();
		
		return tmpDir;
	}

}
