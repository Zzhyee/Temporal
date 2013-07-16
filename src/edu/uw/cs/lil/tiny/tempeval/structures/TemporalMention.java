package edu.uw.cs.lil.tiny.tempeval.structures;

import edu.uw.cs.lil.tiny.tempeval.TemporalMain;

public class TemporalMention implements java.io.Serializable{
	private static final long serialVersionUID = -5859852309847402300L;
	private String type;
	private String value;
	private int tokenStart;
	private int tokenEnd; //inclusive-exclusive

	// Temporary variables used only during preprocessing
	private int offset; //character offset
	private String text;

	// Used during preprocessing
	public TemporalMention(String type, String value, int offset) {
		this.type = type;
		this.value = value;
		this.offset = offset;
	}

	// Used during detection
	public TemporalMention(int tokenStart, int tokenEnd, String text) {
		this.text = text;
		setTokenRange(tokenStart, tokenEnd);
	}

	public void setTokenRange(int tokenStart, int tokenEnd) {
		this.tokenStart = tokenStart;
		this.tokenEnd = tokenEnd;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public int getStartChar() {
		return offset;
	}

	public int getEndChar() {
		return offset + text.length();
	}

	public int getStartToken() {
		return tokenStart;
	}

	public int getEndToken() {
		return tokenEnd;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String toString() {
		return text + (value == null ? "" : "(" + value + ")");
	}

	public String prettyString() {
		return "[" + tokenStart + "-" + tokenEnd + "]";
	}

	public boolean matches(TemporalMention other) {
		if (TemporalMain.STRICT_MATCHING)
			return this.tokenStart == other.tokenStart && this.tokenEnd == other.tokenEnd;
		else
			return overlapsWith(other);
	}

	public void mergeWith(TemporalMention gm) {
		this.type = gm.type;
		this.value = gm.value;
	}

	public boolean overlapsWith(TemporalMention other) {
		return this.tokenEnd >= other.tokenStart && this.tokenStart <= other.tokenEnd;
	}

}