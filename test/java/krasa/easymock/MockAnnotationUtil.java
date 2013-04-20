package krasa.easymock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MockAnnotationUtil {

	public static List<Field> findFieldsThatAreMarkedForMocking(EasyMockTest clazz) {
		List<Field> results = new ArrayList<Field>();
		Class<?> current = clazz.getClass();
		while (current != Object.class) {
			Field[] fields = current.getDeclaredFields();
			for (Field f : fields) {
				if (f.isAnnotationPresent(Mocked.class)) {
					results.add(f);
				}
			}
			current = current.getSuperclass();
		}
		return results;
	}
}
