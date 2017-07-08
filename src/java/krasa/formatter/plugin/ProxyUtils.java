package krasa.formatter.plugin;


import com.intellij.psi.codeStyle.CodeStyleManager;

public class ProxyUtils {
	public static CodeStyleManager createProxy(CodeStyleManager manager, EclipseCodeStyleManager overridingObject) {
		return (CodeStyleManager) net.sf.cglib.proxy.Enhancer.create(CodeStyleManager.class, new ProxyCodeStyleManagerDelegator(manager, overridingObject));
    }


}
