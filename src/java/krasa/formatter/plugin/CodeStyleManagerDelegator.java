package krasa.formatter.plugin;

import com.intellij.psi.codeStyle.CodeStyleManager;
import org.mockito.cglib.proxy.Callback;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.cglib.proxy.MethodProxy;
import org.mockito.internal.util.StringJoiner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CodeStyleManagerDelegator implements MethodInterceptor, Callback {
	private static final com.intellij.openapi.diagnostic.Logger log = com.intellij.openapi.diagnostic.Logger
	 .getInstance(CodeStyleManagerDelegator.class.getName());

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
		if (!overridingObject.isEnabled()) {
			return PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(invokedMethod, rawArguments);
		} else {
			try {
				Method overridingMethod = getOverridingMethod(invokedMethod);

				if (!compatibleReturnTypes(invokedMethod.getReturnType(), overridingMethod.getReturnType())) {
					overridingMethodHasWrongReturnType(invokedMethod, overridingMethod, obj, overridingObject);
				}
				if (log.isDebugEnabled()) {
					log.debug("invoking overriding {}({})", invokedMethod.getName(), Arrays.toString(rawArguments));
				}
				return overridingMethod.invoke(overridingObject, rawArguments);
			} catch (NoSuchMethodException e) {
				if (log.isDebugEnabled()) {
					log.debug("invoking original {}({})", invokedMethod.getName(), Arrays.toString(rawArguments));
				}

				return PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(invokedMethod, rawArguments);
			} catch (InvocationTargetException e) {
				// propagate the original exception from the delegate
				throw e.getCause();
			}
		}
	}

	private Object PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(Method invokedMethod, Object[] rawArguments) throws Throwable {
		try {
			return invokedMethod.invoke(delegatedObject, rawArguments);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public void overridingMethodHasWrongReturnType(Method mockMethod, Method overridingMethod, Object mock,
												   Object overridingObject) {
		throw new IllegalStateException(StringJoiner.join(new Object[]{
		 "IntelliJ API changed, install proper/updated version of Eclipse Formatter plugin.",
		 "Incompatible return types when calling: " + mockMethod + " on: " + mock.getClass().getSimpleName(),
		 "return type should be: " + mockMethod.getReturnType().getSimpleName() + ", but was: "
		  + overridingMethod.getReturnType().getSimpleName(),
		 "(delegate instance had type: " + overridingObject.getClass().getSimpleName() + ")"}));
	}

	private Method getOverridingMethod(Method mockMethod) throws NoSuchMethodException {
		return overridingObject.getClass().getMethod(mockMethod.getName(), mockMethod.getParameterTypes());
	}

	private static boolean compatibleReturnTypes(Class<?> superType, Class<?> subType) {
		return superType.equals(subType) || superType.isAssignableFrom(subType);
	}

}
