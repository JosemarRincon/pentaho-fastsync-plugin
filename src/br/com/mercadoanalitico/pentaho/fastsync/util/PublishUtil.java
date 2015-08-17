package br.com.mercadoanalitico.pentaho.fastsync.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PublishUtil {

	protected static String reservedChars = "/\\\t\r\n";
	
	private static Pattern containsReservedCharsPattern = makePattern(reservedChars);

	  /**
	   * helper method to calculate the domain id from the file name, or pass catalog
	   * @param schemaPath full path of schema file on filesystem
	   * @param fileName name of schema file on filesystem
	   * @return Look up name from XML otherwise use file name
	   */
	  public static String determineDomainCatalogName(String schemaPath, String fileName) 
	  {
	    String domainId  = "";
	    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	    try {
	    	InputStream dataInputStream = new FileInputStream(schemaPath);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(dataInputStream);
			NodeList schemas = document.getElementsByTagName("Schema");
			Node schema = schemas.item(0);
			Node name = schema.getAttributes().getNamedItem("name");
			domainId = name.getTextContent();
			dataInputStream.reset();
	    } 
	    catch (Exception e) {
	      //LOG.fine("Problem occurred when trying to get schema name from document. Using filename instead.");
	    }

	    if("".equals(domainId)){
	      domainId = fileName;
	    }

	    return domainId;
	  }
	  
	  /**
	   * Checks for presence of black listed chars as well as illegal permutations of legal chars.
	   */
	  public static boolean validateName(final String name)
	  {
	    return !StringUtils.isEmpty( name ) &&
	        name.trim().equals( name ) && // no leading or trailing whitespace
	        !containsReservedCharsPattern.matcher( name ).matches() && // no reserved characters
	        !".".equals( name ) && // no . //$NON-NLS-1$
	        !"..".equals( name ) ; // no .. //$NON-NLS-1$
	  }	  
	  
	  private static Pattern makePattern(String reservedChars)
	  {
	    // escape all reserved characters as they may have special meaning to regex engine
	    StringBuilder buf = new StringBuilder();
	    buf.append(".*["); //$NON-NLS-1$
	    for (int i=0;i<reservedChars.length();i++)
	    {
	      buf.append( "\\" ); //$NON-NLS-1$
	      buf.append(reservedChars.substring(i, i + 1));
	    }
	    buf.append("]+.*"); //$NON-NLS-1$
	    return Pattern.compile(buf.toString());
	  }

}
