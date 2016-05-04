package bar.foo;

import aaa.XAAA;
import com.google.common.collect.SetMultimap;
import foo.bar.XFOO;
import org.easymock.IMocksControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static aaa.XAAA.aaa;
import static com.google.common.collect.Multimaps.forMap;
import static foo.bar.XFOO.foo;
import static org.easymock.EasyMock.createControl;

public class XBAR {

	public static void bar() {
		new XAAA();
		aaa();
		new XFOO();
		foo();

		SetMultimap<Object, Object> objectObjectSetMultimap = forMap(new HashMap<Object, Object>());
		IMocksControl control = createControl();
		List s = new ArrayList();

	}
}
