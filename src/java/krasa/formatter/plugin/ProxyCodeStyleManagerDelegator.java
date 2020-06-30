package krasa.formatter.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.codeStyle.CodeStyleManager;
import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProxyCodeStyleManagerDelegator implements InvocationHandler {
	private static final Logger log = Logger.getInstance(ProxyCodeStyleManagerDelegator.class.getName());

	private final CodeStyleManager original;
	private final EclipseCodeStyleManager overridingObject;
	private final Set<Method> notOverriddenMethods = new HashSet<Method>();

	public ProxyCodeStyleManagerDelegator(CodeStyleManager original, EclipseCodeStyleManager overriding) {
		this.original = original;
		this.overridingObject = overriding;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] rawArguments) throws Throwable {
		if (!overridingObject.isEnabled() || notOverriddenMethods.contains(method)) {
			return PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(method, rawArguments);
		} else {
			try {
				Method overridingMethod = getOverridingMethod(method);

				if (!compatibleReturnTypes(method.getReturnType(), overridingMethod.getReturnType())) {
					log.error("IntelliJ API changed, install proper/updated version of Eclipse Formatter plugin. " + "Incompatible return types when calling: " + method
							+ " on: " + proxy.getClass().getSimpleName() + " return type should be: " + method.getReturnType().getSimpleName() + ", but was: "
							+ overridingMethod.getReturnType().getSimpleName() + " (delegate instance had type: " + ((Object) overridingObject).getClass().getSimpleName() + ")");
					return PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(method, rawArguments);
				}
				if (log.isDebugEnabled()) {
					log.debug("invoking overriding {}({})", method.getName(), Arrays.toString(rawArguments));
				}
				return overridingMethod.invoke(overridingObject, rawArguments);
			} catch (NoSuchMethodException e) {
				notOverriddenMethods.add(method);
				if (log.isDebugEnabled()) {
					log.debug("invoking original {}({})", method.getName(), Arrays.toString(rawArguments));
				}

				return PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(method, rawArguments);
			} catch (InvocationTargetException e) {
				// propagate the original exception from the delegate
				throw e.getCause();
			}
		}
	}

	private Object PLEASE_REPORT_BUGS_TO_JETBRAINS_IF_IT_FAILS_HERE____ORIGINAL_INTELLIJ_FORMATTER_WAS_USED(Method invokedMethod, Object[] rawArguments)
			throws Throwable {
		try {
			return invokedMethod.invoke(original, rawArguments);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private Method getOverridingMethod(Method mockMethod) throws NoSuchMethodException {
		return overridingObject.getClass().getMethod(mockMethod.getName(), mockMethod.getParameterTypes());
	}

	private static boolean compatibleReturnTypes(Class<?> required, Class<?> overriding) {
		return required.equals(overriding) || required.isAssignableFrom(overriding);
	}

}
