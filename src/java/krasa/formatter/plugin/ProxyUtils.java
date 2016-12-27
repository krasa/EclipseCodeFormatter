package krasa.formatter.plugin;

import org.mockito.cglib.proxy.Callback;
import org.mockito.cglib.proxy.Factory;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.internal.creation.jmock.ClassImposterizer;

import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;

public class ProxyUtils {
	public static CodeStyleManager createProxy(CodeStyleManagerImpl manager, EclipseCodeStyleManager overridingObject) {
		MethodInterceptor interceptor = new CodeStyleManagerDelegator(manager, overridingObject);
		return ClassImposterizer.INSTANCE.imposterise(interceptor, CodeStyleManagerImpl.class, new Class<?>[0]);
    }

    public static boolean isMyProxy(Object mock) {
        return mock != null && getDelegate(mock) != null;
    }

    public static CodeStyleManager getDelegate(Object mock) {
        if (!(mock instanceof Factory)) {
            return null;
        }
        Factory factory = (Factory) mock;
        Callback callback = factory.getCallback(0);
        if (!(callback instanceof CodeStyleManagerDelegator)) {
            return null;
        }
        return ((CodeStyleManagerDelegator) callback).getDelegatedObject();
    }

}
