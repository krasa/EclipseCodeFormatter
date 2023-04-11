package krasa.formatter.common;

import com.intellij.util.PathUtil;
import krasa.formatter.exception.FileDoesNotExistsException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class ModifiableFile extends File {

	public ModifiableFile(String pathToConfigFileJava) {
		super(pathToConfigFileJava);
	}

	public boolean wasChanged(Monitor lastState) {
		checkIfExists();
		return this.lastModified() != lastState.getLastStateTime();
	}

	public void checkIfExists() throws FileDoesNotExistsException {
		if (!this.exists()) {
			throw new FileDoesNotExistsException(this);
		}
	}

	public Monitor getModifiedMonitor() {
		return new Monitor(this);
	}

	@NotNull
	public String getSystemIndependentPath() {
		return PathUtil.toSystemIndependentName(getAbsolutePath());
	}

	/**
	 * @author Vojtech Krasa
	 */
	public static class Monitor {
		private long lastStateTime;

		public Monitor(File file) {
			lastStateTime = file.lastModified();
		}

		public long getLastStateTime() {
			return lastStateTime;
		}

		public boolean wasModified(File l) {
			return l.lastModified() > lastStateTime;
		}
	}
}
