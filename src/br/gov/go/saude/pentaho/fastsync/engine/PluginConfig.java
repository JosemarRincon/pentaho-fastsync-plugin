package br.gov.go.saude.pentaho.fastsync.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PluginConfig {
	public static final String PLUGIN_NAME = "fastsync";
	public static final String PLUGIN_PATH = PentahoSystem.getApplicationContext()
			.getSolutionPath("system" + File.separator + "fastsync");
	public static final Properties props = new Properties();

	static Log logger = LogFactory.getLog(PluginConfig.class);

	private static PluginConfig _instance;

	public static synchronized PluginConfig getInstance() {
		if (_instance == null) {
			_instance = new PluginConfig();
		}
		return _instance;
	}

	public void init() {
		loadPluginProperties();
	}

	private void loadPluginProperties() {
		try {
			InputStream in = new FileInputStream(PLUGIN_PATH + File.separator + "fastsync.properties");
			props.load(in);
		} catch (IOException e) {
			System.out.println("FASTSYNC_PLUGIN: Missing fastsync.properties file.");
			e.printStackTrace();
		}
	}
}
