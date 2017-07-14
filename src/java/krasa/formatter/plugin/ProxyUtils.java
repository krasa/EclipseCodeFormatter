package krasa.formatter.plugin;


import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.FormattingModeAwareIndentAdjuster;
import org.jetbrains.annotations.NotNull;

public class ProxyUtils {
	public static CodeStyleManager createProxy(CodeStyleManager manager, EclipseCodeStyleManager overridingObject) {
		return (CodeStyleManager) net.sf.cglib.proxy.Enhancer.create(CodeStyleManager.class,
				getInterfaces(),
				new ProxyCodeStyleManagerDelegator(manager, overridingObject));
	}

	@NotNull
	private static Class[] getInterfaces() {
		try {
			return new Class[]{FormattingModeAwareIndentAdjuster.class};
		} catch (Throwable e) {
			//old API < IJ ~2017
			return new Class[]{};
		}
	}


}
