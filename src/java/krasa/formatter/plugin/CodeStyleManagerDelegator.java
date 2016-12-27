package krasa.formatter.plugin;

import com.intellij.psi.codeStyle.CodeStyleManager;
import org.mockito.cglib.proxy.Callback;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.cglib.proxy.MethodProxy;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.StringJoiner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CodeStyleManagerDelegator implements MethodInterceptor, Callback {
    private static final com.intellij.openapi.diagnostic.Logger log = com.intellij.openapi.diagnostic.Logger.getInstance(CodeStyleManagerDelegator.class.getName());

    private final CodeStyleManager delegatedObject;
    private final EclipseCodeStyleManager overridingObject;

    public <T> CodeStyleManagerDelegator(CodeStyleManager delegatedObject, EclipseCodeStyleManager overridingObject) {
        this.delegatedObject = delegatedObject;
        this.overridingObject = overridingObject;
    }

    public CodeStyleManager getDelegatedObject() {
        return delegatedObject;
    }

    @Override
    public Object intercept(Object obj, Method invokedMethod, Object[] rawArguments, MethodProxy proxy) throws Throwable {
        try {
            Method delegateMethod = getDelegateMethod(invokedMethod);

            if (!compatibleReturnTypes(invokedMethod.getReturnType(), delegateMethod.getReturnType())) {
                delegatedMethodHasWrongReturnType(invokedMethod, delegateMethod, obj, delegatedObject);
            }

            log.debug("invoking overriding {}({})", invokedMethod.getName(), Arrays.toString(rawArguments));
            return delegateMethod.invoke(overridingObject, rawArguments);
        } catch (NoSuchMethodException e) {
            log.debug("invoking original {}({})", invokedMethod.getName(), Arrays.toString(rawArguments));
            return PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(invokedMethod, rawArguments);
        } catch (InvocationTargetException e) {
            // propagate the original exception from the delegate
            throw e.getCause();
        }
    }

    private Object PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(Method invokedMethod, Object[] rawArguments) throws IllegalAccessException, InvocationTargetException {
        return invokedMethod.invoke(delegatedObject, rawArguments);
    }

    public void delegatedMethodHasWrongReturnType(Method mockMethod, Method delegateMethod, Object mock, Object delegate) {
        throw new IllegalStateException(StringJoiner.join(new Object[]{"IntelliJ API changed, install proper/updated version of Eclipse Formatter plugin.", "Incompatible return types when calling: " + mockMethod + " on: " + (new MockUtil()).getMockName(mock), "return type should be: " + mockMethod.getReturnType().getSimpleName() + ", but was: " + delegateMethod.getReturnType().getSimpleName(), "(delegate instance had type: " + delegate.getClass().getSimpleName() + ")"}));
    }

    private Method getDelegateMethod(Method mockMethod) throws NoSuchMethodException {
        return overridingObject.getClass().getMethod(mockMethod.getName(), mockMethod.getParameterTypes());
    }

    private static boolean compatibleReturnTypes(Class<?> superType, Class<?> subType) {
        return superType.equals(subType) || superType.isAssignableFrom(subType);
    }


}
