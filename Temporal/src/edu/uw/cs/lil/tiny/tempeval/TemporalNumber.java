package edu.uw.cs.lil.tiny.tempeval;

public class TemporalNumber extends TemporalISO {
	boolean ordinal;
	public TemporalNumber(int n, boolean o) {
		super("num", n);
		ordinal = o;
	}

	public boolean stringInFields(String f) {
		return f.equals("num");
	}
	
	public boolean isOrdinal(){
		return ordinal;
	}
	
	public int getNum(){
		return TemporalISO.getValueFromDate(this, "num");
	}
}