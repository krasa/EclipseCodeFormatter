package krasa.formatter.plugin;

import java.util.*;

import krasa.formatter.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.MultiValuesMap;

/*not thread safe*/
@SuppressWarnings("Duplicates")
class ImportsSorter452 implements ImportsSorter {

	private List<String> template = new ArrayList<String>();
	private MultiValuesMap<String, String> matchingImports = new MultiValuesMap<String, String>();
	private ArrayList<String> notMatching = new ArrayList<String>();
	private Set<String> allImportOrderItems = new HashSet<String>();
	private Comparator<? super String> importsComparator;

	public ImportsSorter452(List<String> importOrder, ImportsComparator comparator) {
		importsComparator = comparator;
		List<String> importOrderCopy = new ArrayList<String>(importOrder);
		normalizeStaticOrderItems(importOrderCopy);
		putStaticItemIfNotExists(importOrderCopy);
		template.addAll(importOrderCopy);
		this.allImportOrderItems.addAll(importOrderCopy);
	}

	@Override
	public List<String> sort(List<String> imports) {
		filterMatchingImports(imports);
		mergeMatchingItems();
		mergeNotMatchingItems();
		removeNewLines();
		return getResult();
	}

	private void removeNewLines() {
		List<String> temp = new ArrayList<String>();

		boolean previousWasNewLine = false;
		boolean anyContent = false;
		for (int i = 0; i < template.size(); i++) {
			String s = template.get(i);
			if (!anyContent && s.equals(ImportSorterAdapter.N)) {
				continue;
			}
			if (s.equals(ImportSorterAdapter.N)) {
				if (previousWasNewLine) {
					continue;
				} else {
					temp.add(s);
				}
				previousWasNewLine = true;
			} else {
				previousWasNewLine = false;
				anyContent = true;
				temp.add(s);
			}
		}


		Collections.reverse(temp);
		List<String> temp2 = trimNewLines(temp);
		Collections.reverse(temp2);

		template = temp2;
	}


	@NotNull
	private List<String> trimNewLines(List<String> temp) {
		List<String> temp2 = new ArrayList<String>();
		boolean anyContent = false;
		for (int i = 0; i < temp.size(); i++) {
			String s = temp.get(i);
			if (!anyContent && s.equals(ImportSorterAdapter.N)) {
				continue;
			}
			anyContent = true;
			temp2.add(s);
		}
		return temp2;
	}

	private void putStaticItemIfNotExists(List<String> allImportOrderItems) {
		boolean contains = false;
		for (int i = 0; i < allImportOrderItems.size(); i++) {
			String allImportOrderItem = allImportOrderItems.get(i);
			if (allImportOrderItem.equals("static ")) {
				contains = true;
			}
		}
		if (!contains) {
			allImportOrderItems.add(0, "static ");
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
			if (anImport.startsWith(
					// 4.5.1+ matches exact package name
					orderItem.equals("static ") || orderItem.equals("") ? orderItem : orderItem + ".")) {
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
	private void mergeNotMatchingItems() {
		Collections.sort(notMatching, importsComparator);

		template.add(ImportSorterAdapter.N);
		for (int i = 0; i < notMatching.size(); i++) {
			String notMatchingItem = notMatching.get(i);
			if (!matchesStatic(false, notMatchingItem)) {
				continue;
			}
			boolean isOrderItem = isOrderItem(notMatchingItem, false);
			if (!isOrderItem) {
				template.add(notMatchingItem);
			}
		}
		template.add(ImportSorterAdapter.N);
	}

	private boolean isOrderItem(String notMatchingItem, boolean staticItems) {
		boolean contains = allImportOrderItems.contains(notMatchingItem);
		return contains && matchesStatic(staticItems, notMatchingItem);
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
				Collections.sort(matchingItems, importsComparator);

				// replace order item by matching import statements
				// this is a mess and it is only a luck that it works :-]
				template.remove(i);
				if (i != 0 && !template.get(i - 1).equals(ImportSorterAdapter.N)) {
					template.add(i, ImportSorterAdapter.N);
					i++;
				}
				if (i + 1 < template.size() && !template.get(i + 1).equals(ImportSorterAdapter.N)
						&& !template.get(i).equals(ImportSorterAdapter.N)) {
					template.add(i, ImportSorterAdapter.N);
				}
				template.addAll(i, matchingItems);
				if (i != 0 && !template.get(i - 1).equals(ImportSorterAdapter.N)) {
					template.add(i, ImportSorterAdapter.N);
				}

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
