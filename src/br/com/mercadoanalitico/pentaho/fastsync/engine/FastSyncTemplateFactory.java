package br.com.mercadoanalitico.pentaho.fastsync.engine;

/**
 * 
 * @author Kleyson Rios<br>
 *         Secretaria de Saude do Estado de Goias<br>
 *         www.saude.go.gov.br
 *
 */

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class FastSyncTemplateFactory implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException 
	{
		initPluginProperties();
	}
	
	private void initPluginProperties() 
	{
		PluginConfig.getInstance().init();	
	}

	
}
