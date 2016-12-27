package krasa.formatter.plugin;

import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import org.mockito.cglib.proxy.Callback;
import org.mockito.cglib.proxy.Factory;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.internal.creation.jmock.ClassImposterizer;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.util.reflection.LenientCopyTool;

public class ProxyUtils {

    public static CodeStyleManager createProxy(CodeStyleManager manager, EclipseCodeStyleManager overridingObject) {
        CreationSettings<CodeStyleManager> creationSettings = new CreationSettings<CodeStyleManager>();
        creationSettings.setTypeToMock(CodeStyleManager.class);
        MethodInterceptor mockHandler = new CodeStyleManagerDelegator(manager, overridingObject);

        CodeStyleManager mock = ClassImposterizer.INSTANCE.imposterise(mockHandler, CodeStyleManagerImpl.class, new Class<?>[0]);

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
