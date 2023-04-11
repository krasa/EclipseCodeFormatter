package krasa.formatter.plugin;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProxyUtils {
	private static final Logger LOG = Logger.getInstance(ProxyUtils.class.getName());

	public static CodeStyleManager createProxy(CodeStyleManager original, EclipseCodeStyleManager overriding) {
		return (CodeStyleManager) net.sf.cglib.proxy.Enhancer.create(CodeStyleManager.class,
				getInterfaces(original),
				new ProxyCodeStyleManagerDelegator(original, overriding));
	}

	@NotNull
	private static Class[] getInterfaces(CodeStyleManager manager) {
		List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(manager.getClass());
		LOG.debug("Proxy interfaces " + allInterfaces);
		return allInterfaces.toArray(new Class[0]);
	}


}
