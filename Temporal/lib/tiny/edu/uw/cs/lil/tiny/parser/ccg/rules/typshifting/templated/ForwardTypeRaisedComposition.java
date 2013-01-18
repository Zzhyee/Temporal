package edu.uw.cs.lil.tiny.parser.ccg.rules.typshifting.templated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.uw.cs.lil.tiny.ccg.categories.Category;
import edu.uw.cs.lil.tiny.ccg.categories.ComplexCategory;
import edu.uw.cs.lil.tiny.ccg.categories.ICategoryServices;
import edu.uw.cs.lil.tiny.ccg.categories.syntax.ComplexSyntax;
import edu.uw.cs.lil.tiny.ccg.categories.syntax.Slash;
import edu.uw.cs.lil.tiny.ccg.categories.syntax.Syntax;
import edu.uw.cs.lil.tiny.mr.lambda.Lambda;
import edu.uw.cs.lil.tiny.mr.lambda.Literal;
import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.mr.lambda.Variable;
import edu.uw.cs.lil.tiny.mr.language.type.Type;
import edu.uw.cs.lil.tiny.parser.ccg.rules.ParseRuleResult;
import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.AbstractComposition;

/**
 * A rule for first doing a type raising of the form:
 * <ul>
 * X -> T/(T\X)
 * </ul>
 * for the left input. This new category is then used in a composition of the
 * form
 * <ul>
 * <li>T/(T\X) (T\X)/Z => T/Z</li>
 * </ul>
 * We do this form of just in time type raising to avoid computing the left half
 * when it will not combine with anything on the right.
 * 
 * @author Luke Zettlemoyer
 */
public class ForwardTypeRaisedComposition extends
		AbstractComposition<LogicalExpression> {
	private static final String	RULE_NAME	= "ftrcomp";
	
	public ForwardTypeRaisedComposition(
			ICategoryServices<LogicalExpression> categoryServices) {
		super(RULE_NAME, categoryServices);
	}
	
	public ForwardTypeRaisedComposition(
			ICategoryServices<LogicalExpression> categoryServices,
			boolean useEisnerNormalForm) {
		super(RULE_NAME, categoryServices, useEisnerNormalForm);
	}
	
	@Override
	public Collection<ParseRuleResult<LogicalExpression>> apply(
			Category<LogicalExpression> left,
			Category<LogicalExpression> right, boolean isCompleteSentence) {
		
		if (!(right instanceof ComplexCategory<?>)) {
			return Collections.emptyList();
		}
		// make sure the right is a complex category, so we have some chance of
		// doing the composition
		final ComplexCategory<LogicalExpression> rightComp = (ComplexCategory<LogicalExpression>) right;
		
		// make sure right side is a forward slash
		if (!rightComp.hasSlash(Slash.FORWARD)) {
			return Collections.emptyList();
		}
		
		// make sure the Xs (see comment above) match
		if (!(rightComp.getSyntax().getLeft() instanceof ComplexSyntax)) {
			return Collections.emptyList();
		}
		// this will be (T\X) above, if everything matches up correctly
		final ComplexSyntax TX = (ComplexSyntax) rightComp.getSyntax()
				.getLeft();
		if (!TX.getSlash().equals(Slash.BACKWARD)) {
			return Collections.emptyList();
		}
		final Syntax T = TX.getLeft();
		final Syntax X = TX.getRight();
		if (!X.equals(left.getSyntax())) {
			return Collections.emptyList();
		}
		
		final Type newVarType = LogicLanguageServices.getTypeRepository()
				.generalizeType(rightComp.getSem().getType().getRange());
		if (newVarType == null
				|| !left.getSem().getType()
						.isExtendingOrExtendedBy(newVarType.getDomain())) {
			return Collections.emptyList();
		}
		
		// it all matches!!! make new category and do composition!!!
		// first, make the T/(T\X) including the new logical expression
		final ComplexSyntax newSyntax = new ComplexSyntax(T, TX, Slash.FORWARD);
		final Variable newVar = new Variable(newVarType);
		final List<LogicalExpression> args = new ArrayList<LogicalExpression>(1);
		args.add(left.getSem());
		final Literal application = new Literal(newVar, args,
				LogicLanguageServices.getTypeComparator(),
				LogicLanguageServices.getTypeRepository());
		final Lambda newSem = new Lambda(newVar, application,
				LogicLanguageServices.getTypeRepository());
		final ComplexCategory<LogicalExpression> newLeft = new ComplexCategory<LogicalExpression>(
				newSyntax, newSem);
		return doComposition(newLeft, right, false);
	}
	
	@Override
	public boolean isOverLoadable() {
		return false;
	}
}
