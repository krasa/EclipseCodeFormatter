package krasa.formatter.plugin;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.FormattingModeAwareIndentAdjuster;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProxyUtils {
	private static final Logger LOG = Logger.getInstance(ProxyUtils.class.getName());

	public static CodeStyleManager createProxy(CodeStyleManager manager, EclipseCodeStyleManager overridingObject) {
		return (CodeStyleManager) net.sf.cglib.proxy.Enhancer.create(CodeStyleManager.class,
				getInterfaces(manager),
				new ProxyCodeStyleManagerDelegator(manager, overridingObject));
	}

	@NotNull
	private static Class[] getInterfaces(CodeStyleManager manager) {
		try {
			List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(manager.getClass());
			LOG.debug("Proxy interfaces " + allInterfaces);
			return allInterfaces.toArray(new Class[0]);
		} catch (Throwable e) {
			//old API < IJ ~2017
			return new Class[]{};
		}
	}


}
