package br.gov.go.saude.pentaho.fastsync.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.transform.StringZipEntryTransformer;

public class Zip {
	private String packFilePath;
	private String packDirectoryPath;
	private String fullPathZipFileName;

	public void packFile() throws IOException {
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		FileInputStream fis = null;

		try {
			String outputFile = this.fullPathZipFileName;

			fos = new FileOutputStream(outputFile);

			zos = new ZipOutputStream(fos);

			ZipEntry ze = new ZipEntry(new File(this.packFilePath).getName());
			zos.putNextEntry(ze);

			fis = new FileInputStream(this.packFilePath);

			byte[] bytesRead = new byte[512];

			int bytesNum;

			while ((bytesNum = fis.read(bytesRead)) > 0) {
				zos.write(bytesRead, 0, bytesNum);
			}

		} finally {
			fis.close();
			zos.closeEntry();
			zos.close();
			fos.close();
		}
	}

	public void packDirectory() throws IOException {
		FileOutputStream fos = null;
		ZipOutputStream zos = null;

		try {
			String outputFile = this.fullPathZipFileName;

			fos = new FileOutputStream(outputFile);
			zos = new ZipOutputStream(fos);

			packCurrentDirectoryContents(this.packDirectoryPath, zos);

		} finally {

			zos.closeEntry();
			zos.close();
			fos.close();
		}
	}

	private void packCurrentDirectoryContents(String directoryPath, ZipOutputStream zos) throws IOException {
		File dir = new File(directoryPath);
		String[] dirElements = dir.list();

		if ((dirElements.length == 0) && (dir.isDirectory())) {
			ZipEntry ze = new ZipEntry(directoryPath.replaceAll(this.packDirectoryPath, "") + "/");
			zos.putNextEntry(ze);
		}

		for (String dirElement : dirElements) {

			String dirElementPath = directoryPath + "/" + dirElement;

			if (new File(dirElementPath).isDirectory()) {
				packCurrentDirectoryContents(dirElementPath, zos);

			} else {

				ZipEntry ze = new ZipEntry(dirElementPath.replaceAll(this.packDirectoryPath, ""));
				zos.putNextEntry(ze);
				FileInputStream fis = new FileInputStream(dirElementPath);

				byte[] bytesRead = new byte[512];

				int bytesNum;

				while ((bytesNum = fis.read(bytesRead)) > 0) {
					zos.write(bytesRead, 0, bytesNum);
				}

				fis.close();
			}
		}
	}

	public void setPackFilePath(String packFilePath) {
		this.packFilePath = packFilePath;
	}

	public void setPackDirectoryPath(String packDirectoryPath) {
		this.packDirectoryPath = packDirectoryPath;
	}

	public void setFullPathZipFileName(String fullPathZipFileName) {
		this.fullPathZipFileName = fullPathZipFileName;
	}

	public static void transformEntry(String local,String filename) {
		ZipUtil.transformEntry(new File(local), filename, new StringZipEntryTransformer() {
			protected String transform(ZipEntry zipEntry, String input) throws IOException {
				return input.toUpperCase();
			}
		}, new File(local+"/"+"teste.zip"));
	}

}
