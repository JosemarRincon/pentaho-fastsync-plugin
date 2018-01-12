package br.gov.go.saude.pentaho.fastsync.ws;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import br.gov.go.saude.pentaho.fastsync.util.FileSystem;
import br.gov.go.saude.pentaho.fastsync.util.Repository;

public class TesteFile {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fileSystem = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/pentaho-solutions/siconti/cargas/carregaDimClassifFinal.ktr";
		String jcr = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/siconti/siconti/cargas/carregaDimClassifFinal.ktr";

		if (TesteFile.diffFiles(fileSystem, jcr)) {

			System.out.println("\n-----> file to be updated: " + fileSystem.replaceAll(":", "/") + "\n");
		}

	}

	private static boolean diffFiles(String f1, String f2) throws IOException {
//		final List<String> diff = new ArrayList<String>();
//		boolean filesDiff = false;
//		final Path _f1 = Paths.get(f1.toString());
//		final Path _f2 = Paths.get(f2.toString());
//		List<String> fileSystem = Files.readAllLines(_f1);
//		List<String> jcr = Files.readAllLines(_f2);
//		final String SYNC = "fs";
//		int i = 0;
//		if (SYNC.equals("fs")) {
//
//			for (String line : fileSystem) {
//				if (!contains(jcr, line)) {
//					// diff.add(line);
//					System.out.println("\n-----> conteudo fileSystem: " + line.trim());
//					System.out.println("\n-----> conteudo jcr index: " + jcr.get(i).trim().replaceAll(" ", ""));
//					System.out.println("\n-----> jrc: " + jcr);
//					System.out.println("\n-----> index : " + i);
//					filesDiff = true;
//					break;
//				}
//				i++;
//			}
//
//		} else {
//
//			for (String line : jcr) {
//				if (!contains(fileSystem, line)) {
//					// diff.add(line);
//					System.out.println("\n-----> conteudo jcr: " + line.trim());
//					System.out.println("\n-----> conteudo index fileSystem: " + fileSystem.get(i).trim().replaceAll(" ", ""));
//					System.out.println("\n-----> fileSystem: " + fileSystem);
//					System.out.println("\n-----> index : " + i);
//					filesDiff = true;
//					break;
//				}
//				i++;
//			}
//
//		}
		return true;
	}

	private static boolean contains(List<String> listContent, String lineFind) {
		for (String line : listContent) {
		 if (!line.trim().replaceAll(" ", "").equalsIgnoreCase(lineFind.trim().replaceAll(" ", ""))) {
				System.out.println("\n-----> line : " + line);
				System.out.println("\n-----> lineFind : " + lineFind);
				return false;
			}
		}

		return true;

	}

}
