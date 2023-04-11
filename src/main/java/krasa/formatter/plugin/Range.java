package krasa.formatter.plugin;

/**
 * @author Vojtech Krasa
 */
public class Range {

	private int startOffset;
	private int endOffset;
	private boolean wholeFile;

	public Range(int startOffset, int endOffset, boolean wholeFile) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.wholeFile = wholeFile;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	public boolean isWholeFile() {
		return wholeFile;
	}

	public void setWholeFile(boolean wholeFile) {
		this.wholeFile = wholeFile;
	}
}
