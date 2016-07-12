package krasa.formatter.plugin;

import java.util.Comparator;

import krasa.formatter.utils.StringUtils;

class ImportsComparator implements Comparator<String> {
	public static final ImportsComparator IMPORTS_COMPARATOR = new ImportsComparator();

	@Override
	public int compare(String o1, String o2) {

		String containerName1 = StringUtils.getQualifier(o1);
		String simpleName1 = StringUtils.getSimpleName(o1);

		String containerName2 = StringUtils.getQualifier(o2);
		String simpleName2 = StringUtils.getSimpleName(o2);
		int i = containerName1.compareTo(containerName2);

		if (i == 0) {
			i = simpleName1.compareTo(simpleName2);
		}
		return i;
	}

}
