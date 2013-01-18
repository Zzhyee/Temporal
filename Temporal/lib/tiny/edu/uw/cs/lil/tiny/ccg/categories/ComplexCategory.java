package edu.uw.cs.lil.tiny.ccg.categories;

import edu.uw.cs.lil.tiny.ccg.categories.syntax.ComplexSyntax;
import edu.uw.cs.lil.tiny.ccg.categories.syntax.Slash;

/**
 * Complex syntactic category.
 */
public class ComplexCategory<Y> extends Category<Y> {
	private final boolean		fromLeftComp;
	private final boolean		fromRightComp;
	
	private final ComplexSyntax	syntax;
	
	public ComplexCategory(ComplexSyntax syntax, Y semantics) {
		this(syntax, semantics, false, false);
	}
	
	public ComplexCategory(ComplexSyntax syntax, Y semantics,
			boolean fromLeftComp, boolean fromRightComp) {
		super(semantics);
		this.syntax = syntax;
		this.fromLeftComp = fromLeftComp;
		this.fromRightComp = fromRightComp;
	}
	
	@Override
	public Category<Y> cloneWithNewSemantics(Y newSemantics) {
		return new ComplexCategory<Y>(syntax, newSemantics);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ComplexCategory)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final ComplexCategory cc = (ComplexCategory) other;
		if (!equalsNoSem(other)) {
			return false;
		}
		if (getSem() != null && cc.getSem() != null
				&& !getSem().equals(cc.getSem())) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean equalsNoSem(Object other) {
		if (!(other instanceof ComplexCategory)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final ComplexCategory cc = (ComplexCategory) other;
		if (!syntax.equals(cc.syntax)) {
			return false;
		}
		
		return true;
	}
	
	public Slash getSlash() {
		return syntax.getSlash();
	}
	
	@Override
	public ComplexSyntax getSyntax() {
		return syntax;
	}
	
	/**
	 * 'true' iff the slash is semantically equal to the given one.
	 */
	public boolean hasSlash(Slash s) {
		return syntax.getSlash() == Slash.VERTICAL || s == syntax.getSlash()
				|| s == Slash.VERTICAL;
	}
	
	public boolean isFromLeftComp() {
		return fromLeftComp;
	}
	
	public boolean isFromRightComp() {
		return fromRightComp;
	}
	
	@Override
	public boolean matches(Category<Y> other) {
		if (!(other instanceof ComplexCategory)) {
			return false;
		}
		final ComplexCategory<Y> cc = (ComplexCategory<Y>) other;
		if (cc.syntax.getSlash() != syntax.getSlash()
				&& syntax.getSlash() != Slash.VERTICAL
				&& cc.syntax.getSlash() != Slash.VERTICAL) {
			return false;
		}
		if (!matchesNoSem(other)) {
			return false;
		}
		if (getSem() != null && other.getSem() != null
				&& !getSem().equals(other.getSem())) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean matchesNoSem(Category<Y> other) {
		if (!(other instanceof ComplexCategory)) {
			return false;
		}
		return syntax.equals(other.getSyntax());
	}
	
	@Override
	public int numSlashes() {
		return syntax.numSlashes();
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(syntax.toString());
		if (getSem() != null) {
			result.append(" : ").append(getSem().toString());
		}
		return result.toString();
	}
	
	@Override
	protected int syntaxHash() {
		return syntax.hashCode();
	}
}
