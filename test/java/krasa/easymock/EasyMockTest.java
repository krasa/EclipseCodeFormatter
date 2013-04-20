package krasa.easymock;

import static org.easymock.EasyMock.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

public class EasyMockTest {

	private List<Object> mocks = new ArrayList<Object>();

	@Before
	public void setUp() throws Exception {
		List<Field> mockFields = MockAnnotationUtil.findFieldsThatAreMarkedForMocking(this);
		makeFieldsAccessibleForMocking(mockFields);
		defineMocks(mockFields, this);
	}

	@After
	public void tearDown() throws Exception {
		verify(mocks.toArray());
	}

	public void replayAll() {
		replay(mocks.toArray());
	}

	private void defineMocks(List<Field> mockedFields, Object testInstance) throws Exception {
		for (Field field : mockedFields) {
			String fieldName = field.getName();
			Class<?> type = field.getType();
			Object mock = createMock(fieldName, type);
			mocks.add(mock);
			field.set(testInstance, mock);
		}
	}

	private void makeFieldsAccessibleForMocking(List<Field> mockFields) {
		for (Field f : mockFields) {
			f.setAccessible(true);
		}
	}
}
