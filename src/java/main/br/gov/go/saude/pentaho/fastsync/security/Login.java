package br.gov.go.saude.pentaho.fastsync.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class Login {
	public static Map<String, Object> doLogin(HttpServletRequest request, HttpServletResponse response, UriInfo info,
			String myType, String myToken, String myUrlEncoded) throws PluginBeanException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		IPluginManager plugMan = PentahoSystem.get(IPluginManager.class,
				PentahoSessionHolder.getSession());

		Object authBean = plugMan.getBean("integrator.auth");
		Method authenticate = authBean.getClass().getMethod("authenticate", new Class[] { HttpServletRequest.class,
				HttpServletResponse.class, UriInfo.class, String.class, String.class, String.class });

		@SuppressWarnings("unchecked")
		Map<String, Object> ret = (Map<String, Object>) authenticate.invoke(authBean,
				new Object[] { request, response, info, myType, myToken, myUrlEncoded });

		return ret;
	}
}
