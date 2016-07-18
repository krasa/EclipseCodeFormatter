package krasa.formatter.plugin;

import java.util.*;

import krasa.formatter.utils.StringUtils;

import com.intellij.openapi.util.MultiValuesMap;

/*not thread safe*/
class ImportsSorter450 implements ImportsSorter {

	private List<String> template = new ArrayList<String>();
	private MultiValuesMap<String, String> matchingImports = new MultiValuesMap<String, String>();
	private ArrayList<String> notMatching = new ArrayList<String>();
	private Set<String> allImportOrderItems = new HashSet<String>();
	private Comparator<String> comparator;

	static List<String> sort(List<String> imports, List<String> importsOrder) {
		ImportsSorter importsSorter = new ImportsSorter450(importsOrder);
		return importsSorter.sort(imports);
	}

	@Override
	public List<String> sort(List<String> imports) {
		filterMatchingImports(imports);
		mergeNotMatchingItems(false);
		mergeNotMatchingItems(true);
		mergeMatchingItems();

		return getResult();
	}

	public ImportsSorter450(List<String> importOrder) {
		List<String> importOrderCopy = new ArrayList<String>(importOrder);
		normalizeStaticOrderItems(importOrderCopy);
		putStaticItemIfNotExists(importOrderCopy);
		template.addAll(importOrderCopy);
		this.allImportOrderItems.addAll(importOrderCopy);
		comparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String containerName1 = allImportOrderItems.contains(o1) ? o1 : StringUtils.getPackage(o1);
				String simpleName1 = allImportOrderItems.contains(o1) ? "" : StringUtils.getSimpleName(o1);

				String containerName2 = allImportOrderItems.contains(o2) ? o2 : StringUtils.getPackage(o2);
				String simpleName2 = allImportOrderItems.contains(o2) ? "" : StringUtils.getSimpleName(o2);
				int i = containerName1.compareTo(containerName2);

				if (i == 0) {
					i = simpleName1.compareTo(simpleName2);
				}
				return i;
			}
		};
	}

	private void putStaticItemIfNotExists(List<String> allImportOrderItems) {
		boolean contains = false;
		int indexOfFirstStatic = 0;
		for (int i = 0; i < allImportOrderItems.size(); i++) {
			String allImportOrderItem = allImportOrderItems.get(i);
			if (allImportOrderItem.equals("static ")) {
				contains = true;
			}
			if (allImportOrderItem.startsWith("static ")) {
				indexOfFirstStatic = i;
			}
		}
		if (!contains) {
			allImportOrderItems.add(indexOfFirstStatic, "static ");
		}
	}

	private void normalizeStaticOrderItems(List<String> allImportOrderItems) {
		for (int i = 0; i < allImportOrderItems.size(); i++) {
			String s = allImportOrderItems.get(i);
			if (s.startsWith("\\#") || s.startsWith("#")) {
				allImportOrderItems.set(i, s.replace("\\#", "static ").replace("#", "static "));
			}
		}
	}

	/**
	 * returns not matching items and initializes internal state
	 */
	private void filterMatchingImports(List<String> imports) {
		for (String anImport : imports) {
			String orderItem = getBestMatchingImportOrderItem(anImport);
			if (orderItem != null) {
				matchingImports.put(orderItem, anImport);
			} else {
				notMatching.add(anImport);
			}
		}
		notMatching.addAll(allImportOrderItems);
	}

	private String getBestMatchingImportOrderItem(String anImport) {
		String matchingImport = null;
		for (String orderItem : allImportOrderItems) {
			if (anImport.startsWith(orderItem)) {
				if (matchingImport == null) {
					matchingImport = orderItem;
				} else {
					matchingImport = StringUtils.betterMatching(matchingImport, orderItem, anImport);
				}
			}
		}
		return matchingImport;
	}

	/**
	 * not matching means it does not match any order item, so it will be appended before or after order items
	 */
	private void mergeNotMatchingItems(boolean staticItems) {
		Collections.sort(notMatching, comparator);

		int firstIndexOfOrderItem = getFirstIndexOfOrderItem(notMatching, staticItems);
		int indexOfOrderItem = 0;
		for (int i = 0; i < notMatching.size(); i++) {
			String notMatchingItem = notMatching.get(i);
			if (!matchesStatic(staticItems, notMatchingItem)) {
				continue;
			}
			boolean isOrderItem = isOrderItem(notMatchingItem, staticItems);
			if (isOrderItem) {
				indexOfOrderItem = template.indexOf(notMatchingItem);
			} else {
				if (indexOfOrderItem == 0 && firstIndexOfOrderItem != 0) {
					// insert before alphabetically first order item
					template.add(firstIndexOfOrderItem, notMatchingItem);
					firstIndexOfOrderItem++;
				} else if (firstIndexOfOrderItem == 0) {
					// no order is specified
					if (template.size() > 0 && (template.get(template.size() - 1).startsWith("static"))) {
						// insert N after last static import
						template.add(ImportSorterAdapter.N);
					}
					template.add(notMatchingItem);
				} else {
					// insert after the previous order item
					template.add(indexOfOrderItem + 1, notMatchingItem);
					indexOfOrderItem++;
				}
			}
		}
	}

	private boolean isOrderItem(String notMatchingItem, boolean staticItems) {
		boolean contains = allImportOrderItems.contains(notMatchingItem);
		return contains && matchesStatic(staticItems, notMatchingItem);
	}

	/**
	 * gets first order item from sorted input list, and finds out it's index in template.
	 */
	private int getFirstIndexOfOrderItem(List<String> notMatching, boolean staticItems) {
		int firstIndexOfOrderItem = 0;
		for (int i = 0; i < notMatching.size(); i++) {
			String notMatchingItem = notMatching.get(i);
			if (!matchesStatic(staticItems, notMatchingItem)) {
				continue;
			}
			boolean isOrderItem = isOrderItem(notMatchingItem, staticItems);
			if (isOrderItem) {
				firstIndexOfOrderItem = template.indexOf(notMatchingItem);
				break;
			}
		}
		return firstIndexOfOrderItem;
	}

	private boolean matchesStatic(boolean staticItems, String notMatchingItem) {
		boolean isStatic = notMatchingItem.startsWith("static ");
		return (isStatic && staticItems) || (!isStatic && !staticItems);
	}

	private void mergeMatchingItems() {
		for (int i = 0; i < template.size(); i++) {
			String item = template.get(i);
			if (allImportOrderItems.contains(item)) {
				// find matching items for order item
				Collection<String> strings = matchingImports.get(item);
				if (strings == null || strings.isEmpty()) {
					// if there is none, just remove order item
					template.remove(i);
					i--;
					continue;
				}
				ArrayList<String> matchingItems = new ArrayList<String>(strings);
				Collections.sort(matchingItems, comparator);

				// replace order item by matching import statements
				// this is a mess and it is only a luck that it works :-]
				template.remove(i);
				if (i != 0 && !template.get(i - 1).equals(ImportSorterAdapter.N)) {
					template.add(i, ImportSorterAdapter.N);
					i++;
				}
				if (i < template.size() && !template.get(i).equals(ImportSorterAdapter.N)
						&& !template.get(i).equals(ImportSorterAdapter.N)) {
					template.add(i, ImportSorterAdapter.N);
				}
				template.addAll(i, matchingItems);
			}
		}
		// if there is \n on the end, remove it
		if (template.size() > 0 && template.get(template.size() - 1).equals(ImportSorterAdapter.N)) {
			template.remove(template.size() - 1);
		}
	}

	private List<String> getResult() {
		ArrayList<String> strings = new ArrayList<String>();

		for (String s : template) {
			if (s.equals(ImportSorterAdapter.N)) {
				strings.add(s);
			} else {
				strings.add("import " + s + ";" + ImportSorterAdapter.N);
			}
		}
		return strings;
	}

}
