package edu.uw.cs.lil.tiny.tempeval.readdata;

public class Timex implements java.io.Serializable{
	private static final long serialVersionUID = -5859852309847402300L;
	private String type;
	private String value;
	private Timex anchor;
	private int tokenStart;
	private int tokenEnd; //inclusive-exclusive
	
	// Temporary variables used during preprocessing
	private int offset; //character offset
	private String text;
	
	public Timex(String type, String value, Timex anchor, int offset) {
		this.type = type;
		this.value = value;
		this.anchor = anchor;
		this.offset = offset;
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
	
	public String toString() {
		return "[" + tokenStart + "-" + tokenEnd + "]";
	}
}