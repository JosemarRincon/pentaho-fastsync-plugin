package br.gov.go.saude.pentaho.fastsync.engine;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class FastSyncTemplateFactory implements BeanFactoryPostProcessor {

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		initPluginProperties();
	}

	private void initPluginProperties() {
		PluginConfig.getInstance().init();
	}
}
