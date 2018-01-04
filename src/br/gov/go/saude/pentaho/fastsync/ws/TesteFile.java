package br.gov.go.saude.pentaho.fastsync.ws;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TesteFile {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		final Path firstFile = Paths.get("/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/pentaho-solutions/siconti/cargas/carregaDimClassifFinal.ktr");
		final Path secondFile = Paths.get("/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/pentaho-solutions/siconti/cargas/carregaDimClassifFinal.ktr");
		final List<String> firstFileContent = Files.readAllLines(firstFile, Charset.defaultCharset());
		final List<String> secondFileContent = Files.readAllLines(secondFile, Charset.defaultCharset());

		System.out.println(diffFiles(firstFileContent, secondFileContent));
		System.out.println(diffFiles(secondFileContent, firstFileContent));
		System.out.println( System.getProperty("java.io.tmpdir"));

	}

	private static boolean diffFiles(final List<String> firstFileContent, final List<String> secondFileContent) {
		final List<String> diff = new ArrayList<String>();
		boolean filesDiff = false;
		for (final String line : firstFileContent) {
			if (!secondFileContent.contains(line)) {
				//diff.add(line);
				filesDiff=true;
				break;
			}
		}
		return filesDiff;
	}

}
