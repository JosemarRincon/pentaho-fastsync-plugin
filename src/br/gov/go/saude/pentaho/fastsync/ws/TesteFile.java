package br.gov.go.saude.pentaho.fastsync.ws;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import br.gov.go.saude.pentaho.fastsync.util.FileSystem;
import br.gov.go.saude.pentaho.fastsync.util.Repository;
import br.gov.go.saude.pentaho.fastsync.util.Zip;

public class TesteFile {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// String fileSystem =
		// "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/pentaho-solutions/siconti7/paineis/boletim.cdfde";
		// String jcr =
		// "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/siconti7/siconti7/paineis/boletim.cdfde";

		String fileSystem = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/pentaho-solutions/siconti7/cargas/CtrlCarga.kjb";
		String jcr = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/siconti7/siconti7/cargas/CtrlCarga.kjb";
		String dstTaget = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/siconti7/";
		String dstCPFull = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/siconti7/siconti7//siconti7/";
		String dstTagetFull = "/home/josemar/data_science/pentaho/cbfl/dist-packages/8/biserver-ce/tomcat/temp/siconti7/siconti7/";

		// ArrayList<String> lista = new ArrayList<>();
		// lista.toString();
		// System.out.println("\n-----> file to be lista: " + lista.isEmpty() +
		// "\n");

		if (TesteFile.diffFiles2(jcr,fileSystem)) {
			System.out.println("\n-----> file to be updated: " + fileSystem.replaceAll(":", "/") + "\n");
		}
		// ;
		// File dstDir = new File(dstCPFull);
		// try {
		// copyDirectory(new File(fileSystem), dstDir,
		// ".*\\.project$,.*\\.git.*,.*\\.svn.*,.*\\.locale.*,.*\\.sh.*,.*\\.md.*"
		// + "," + testeReplace());
		// } finally {
		// zip(dstTaget, dstTagetFull);
		// }

	}

	public static void zip(String dstTarget, String dstTargetFull) {

		Zip zipPack = new Zip();

		String zipName = "siconti7" + ".zip";
		String fullZipName = dstTarget + zipName;

		zipPack.setFullPathZipFileName(fullZipName);

		zipPack.setPackDirectoryPath((dstTargetFull));
		try {
			zipPack.packDirectory();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

	public static String testeReplace() {
		String lista = "[/siconti7/upload/index.xml, /siconti7/paineis/boletim.cdfde, /siconti7/paineis/boletim.cda, /siconti7/paineis/res/js/scripts.js, /siconti7/paineis/boletim.wcdf, /siconti7/paineis/index.xml, /siconti7/paineis/kettle/classificacaoCasosObitosSRAG.ktr, /siconti7/paineis/kettle/classificacaoCasosSRAG.ktr, /siconti7/paineis/kettle/tiposInfluenzaGoias.ktr, /siconti7/paineis/kettle/fatorRiscoInfluenza.ktr, /siconti7/paineis/kettle/perfilObitoSexoInfluenza.ktr, /siconti7/paineis/kettle/perfilObitoSexo.ktr, /siconti7/paineis/kettle/mapa_coef_mortalidade.ktr, /siconti7/paineis/kettle/fatorRisco.ktr, /siconti7/res/css/featureList.css, /siconti7/res/css/paragrafo.css, /siconti7/res/css/slide.css, /siconti7/res/css/print.css, /siconti7/res/css/normalize.css, /siconti7/res/css/index.xml, /siconti7/res/image/bt_register.png, /siconti7/res/image/tab_r.png, /siconti7/res/image/menu1.png, /siconti7/res/image/tab_m.png, /siconti7/res/image/tab_b.png, /siconti7/res/image/menu.png, /siconti7/res/image/bt_open.png, /siconti7/res/image/bt_close.png, /siconti7/res/image/bt_login.png, /siconti7/res/image/tab-selected.png, /siconti7/res/image/index.xml, /siconti7/res/image/tab_l.png, /siconti7/res/js/jquery.idTabs.min.js, /siconti7/res/js/jquery.dataTables.rowGrouping.js, /siconti7/res/js/slide.js, /siconti7/res/js/index.xml, /siconti7/res/js/featureList.js, /siconti7/res/index.xml, /siconti7/index.xml, /siconti7/analises/srag.xml, /siconti7/analises/index.xml, /siconti7/cargas/limpa_stg_table-deprecated.ktr, /siconti7/cargas/criterios_ficha_antiga.ktr, /siconti7/cargas/ControleSragErros.ktr, /siconti7/cargas/carregaDimTipoMetodologia.ktr, /siconti7/cargas/carregaDimVacina.ktr, /siconti7/cargas/carregaDimMunResidencia.ktr, /siconti7/cargas/carregaDimAntiviral.ktr, /siconti7/cargas/envio_email_erro_notif.ktr, /siconti7/cargas/limpaTabelaFato.ktr, /siconti7/cargas/carregaDimMacroNotific.ktr, /siconti7/cargas/carregaDimGestante.ktr, /siconti7/cargas/carregaDimInternacao.ktr, /siconti7/cargas/read_stg_srag.ktr, /siconti7/cargas/CtrlCarga.kjb, /siconti7/cargas/carregaDimMunNotific.ktr, /siconti7/cargas/RecebeArquivo.ktr, /siconti7/cargas/ControleDuplicados.ktr, /siconti7/cargas/validaCampos.ktr, /siconti7/cargas/carregaDimMicroResidencia.ktr, /siconti7/cargas/ControleDuplicados2.ktr, /siconti7/cargas/carregaDimDiagnostico.ktr, /siconti7/cargas/carregaDimMetodologia.ktr, /siconti7/cargas/carregaDimMacroResidencia.ktr, /siconti7/cargas/carregaDimMacroInternacao.ktr, /siconti7/cargas/carregaDimEvolucao.ktr, /siconti7/cargas/ControleSrag.ktr, /siconti7/cargas/carregaDimFaixaEtaria.ktr, /siconti7/cargas/carregaDimFatorRisco.ktr, /siconti7/cargas/carregaDimMunInternacao.ktr, /siconti7/cargas/carregaDimSintoma.ktr, /siconti7/cargas/deletaDuplicados.ktr, /siconti7/cargas/carregaDimRaioX.ktr, /siconti7/cargas/carregaDimSexo.ktr, /siconti7/cargas/carregaDimSuporteVentilatorio.ktr, /siconti7/cargas/executora.ktr, /siconti7/cargas/carregaDimCriterioConfirmacao.ktr, /siconti7/cargas/carregaDimSubTipoDiagnostico.ktr, /siconti7/cargas/carregaDimMicroInternacao.ktr, /siconti7/cargas/carregaDimEstabNotific.ktr, /siconti7/cargas/carregaDimUti.ktr, /siconti7/cargas/carregaTabelaFato.ktr, /siconti7/cargas/index.xml, /siconti7/cargas/carregaDimMicroNotific.ktr, /siconti7/cargas/carregaDimEstabInternacao.ktr, /siconti7/cargas/carregaDimAmostra.ktr, /siconti7/cargas/preparaCargaBISrag.ktr, /siconti7/cargas/carregaDimTempo.ktr, /siconti7/cargas/carregaDimClassifFinal.ktr]";
		String newLita = lista.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\/siconti7", ".*\\\\\\+")
				.replaceAll("\\+", "\\\\").replaceAll(" ", "");
		System.out.println("\n-----> file to be lista: " + newLita + "\n");
		return newLita;

	}

	public static boolean diffFiles2(String f1, String f2) throws IOException {
		boolean filesDiff = false;
		final Path _f1 = Paths.get(f1.toString());
		final Path _f2 = Paths.get(f2.toString());
		List<String> fileSystem = Files.readAllLines(_f1, Charset.forName("UTF-8"));
		List<String> jcr = Files.readAllLines(_f2, Charset.forName("UTF-8"));
		int i = 0;
		if (f1.length() != f2.length()) {
			for (String line : jcr) {
				if (!fileSystem.contains(line)) {
					// System.out.println("\n-----> conteudo jcr: " +
					// line.trim());
					if (line.contains("<directory>") || line.contains("<directory />") || line.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
							|| line.contains("<modified_date>") || line.contains("<xloc>") || line.contains("<yloc>")
							|| line.contains("partitionschema") || line.contains("dynamic")
							|| line.contains("partitions_per_slave") || line.contains("slaveserver")
							|| line.contains("master") || line.contains("hostname") || line.contains("port")
							|| line.contains("key_for_session_key")) {
						filesDiff = false;
						continue;
					}
					System.out.println("\n-----> conteudo line: " + line.trim());
					System.out.println("\n-----> conteudo : " + fileSystem);
					System.out.println("\n-----> index : " + i);
					filesDiff = true;
					break;
				}
				i++;
			}
		}

		return filesDiff;
	}

	public static boolean isFilesDiffs(String file1, String file2) {
		File f1 = new File(file1);
		File f2 = new File(file2);
		byte[] f1_buf = new byte[1048576];
		byte[] f2_buf = new byte[1048576];
		int len;
		if (f1.length() != f2.length()) {
			System.out.println("\n-----> entro: " + f1.length());
			System.out.println("\n-----> entro: " + f2.length());
			try {
				InputStream isf1 = new FileInputStream(f1);
				InputStream isf2 = new FileInputStream(f2);
				try {
					while (isf1.read(f1_buf) >= 0) {
						len = isf2.read(f2_buf);
						for (int j = 0; j < len; j++) {
							if (f1_buf[j] != f2_buf[j]) {

								System.out.println("\n-----> f1_buf[j]: " + f1_buf[j]);
								System.out.println("\n-----> f2_buf[j]: " + f2_buf[j]);
								return true; // tamanho diferente e conteudo
												// diferente
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

	public static String md5(String input) {
		String md5 = null;
		if (null == input)
			return null;
		try {
			// Create MessageDigest object for MD5
			MessageDigest digest = MessageDigest.getInstance("MD5");
			// Update input string in message digest
			digest.update(input.getBytes(), 0, input.length());
			// Converts message digest value in base 16 (hex)
			md5 = new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5;
	}

}
