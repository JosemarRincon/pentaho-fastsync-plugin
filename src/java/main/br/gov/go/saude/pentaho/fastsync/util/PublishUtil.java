package br.gov.go.saude.pentaho.fastsync.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PublishUtil {
	protected static String reservedChars = "/\\\t\r\n";
	private static Pattern containsReservedCharsPattern = makePattern(reservedChars);
	public static String determineDomainCatalogName(String schemaPath, String fileName) {
		String domainId = "";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			InputStream dataInputStream = new FileInputStream(schemaPath);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(dataInputStream);
			NodeList schemas = document.getElementsByTagName("Schema");
			Node schema = schemas.item(0);
			Node name = schema.getAttributes().getNamedItem("name");
			domainId = name.getTextContent();
			dataInputStream.reset();
		} catch (Exception e) {
		}

		if ("".equals(domainId)) {
			domainId = fileName;
		}

		return domainId;
	}

	public static boolean validateName(String name) {
		return (!StringUtils.isEmpty(name)) && (name.trim().equals(name))
				&& (!containsReservedCharsPattern.matcher(name).matches()) && (!".".equals(name))
				&& (!"..".equals(name));
	}

	private static Pattern makePattern(String reservedChars) {
		StringBuilder buf = new StringBuilder();
		buf.append(".*[");
		for (int i = 0; i < reservedChars.length(); i++) {
			buf.append("\\");
			buf.append(reservedChars.substring(i, i + 1));
		}
		buf.append("]+.*");
		return Pattern.compile(buf.toString());
	}
}
