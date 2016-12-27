package krasa.formatter.plugin;

import org.mockito.cglib.proxy.Callback;
import org.mockito.cglib.proxy.Factory;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.internal.creation.jmock.ClassImposterizer;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.util.reflection.LenientCopyTool;

import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;

public class ProxyUtils {
	public static CodeStyleManager createProxy(CodeStyleManagerImpl manager, EclipseCodeStyleManager overridingObject) {
		CreationSettings<CodeStyleManagerImpl> creationSettings = new CreationSettings<CodeStyleManagerImpl>();
		creationSettings.setTypeToMock(CodeStyleManagerImpl.class);
        MethodInterceptor mockHandler = new CodeStyleManagerDelegator(manager, overridingObject);

		CodeStyleManagerImpl mock = ClassImposterizer.INSTANCE.imposterise(mockHandler, CodeStyleManagerImpl.class,
				new Class<?>[0]);

        Object spiedInstance = creationSettings.getSpiedInstance();
        if (spiedInstance != null) {
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }

        return mock;
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
