/*
 * Eclipse Code Formatter Copyright (c) 2007-2009 Esko Luontola, www.orfjackal.net Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package krasa.formatter;

import java.net.URL;

/**
 * Paths to program icons and other resources.
 * 
 * @author Esko Luontola
 * @since 5.12.2007
 */
public class Resources {

	public static final URL PROGRAM_LOGO_16 = Resources.class.getResource("icons/logo-16.png");
	public static final URL PROGRAM_LOGO_32 = Resources.class.getResource("icons/logo-32.png");

	static {
		assert PROGRAM_LOGO_16 != null;
		assert PROGRAM_LOGO_32 != null;
	}

	private Resources() {
	}
}
