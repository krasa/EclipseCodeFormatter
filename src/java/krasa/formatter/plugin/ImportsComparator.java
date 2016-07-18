package krasa.formatter.plugin;

import java.util.Comparator;

import krasa.formatter.utils.StringUtils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.GlobalSearchScope;

class ImportsComparator implements Comparator<String> {

	private JavaPsiFacade javaPsiFacade;
	private GlobalSearchScope scope;

	public ImportsComparator(Project project) {
		javaPsiFacade = JavaPsiFacadeImpl.getInstance(project);
		scope = GlobalSearchScope.allScope(project);
	}

	public ImportsComparator() {
	}

	@Override
	public int compare(String o1, String o2) {
		String simpleName1 = simpleName(o1);
		String containerName1 = getPackage(o1, simpleName1);

		String simpleName2 = simpleName(o2);
		String containerName2 = getPackage(o2, simpleName2);


		int i = containerName1.compareTo(containerName2);

		if (i == 0) {
			i = simpleName1.compareTo(simpleName2);
		}
		return i;
	}

	private String getPackage(String qualified, String simple) {
		String substring;
		if (qualified.length() > simple.length()) {
			substring = qualified.substring(0, qualified.length() - simple.length() - 1);
		} else {
			substring = StringUtils.getPackage(qualified);
		}
		return substring;
	}

	private String simpleName(String qualified) {
		String simpleName;

		PsiClass aClass = getPsiClass(qualified);
		if (aClass != null) {
			PsiClass containingClass = aClass;
			simpleName = aClass.getName();
			while (containingClass != null && containingClass.getContainingClass() != null) {
				containingClass = containingClass.getContainingClass();
				if (containingClass != null) {
					simpleName = containingClass.getName() + "." + simpleName;
				}
			}
		} else {
			simpleName = StringUtils.getSimpleName(qualified);
		}
		return simpleName;
	}

	protected PsiClass getPsiClass(String qualified) {
		return javaPsiFacade.findClass(qualified, scope);
	}

}
