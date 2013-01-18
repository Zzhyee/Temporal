package edu.uw.cs.lil.tiny.test.ccg.lambda;

import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.mr.lambda.Lambda;
import edu.uw.cs.lil.tiny.mr.lambda.Literal;
import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalConstant;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.mr.lambda.Variable;
import edu.uw.cs.lil.tiny.mr.lambda.visitor.ILogicalExpressionVisitor;
import edu.uw.cs.lil.tiny.mr.lambda.visitor.IsExtendedConstant;
import edu.uw.cs.lil.tiny.test.stats.AbstractTestingStatistics;
import edu.uw.cs.utils.composites.Pair;
import edu.uw.cs.utils.log.ILogger;
import edu.uw.cs.utils.log.LoggerFactory;

public class SingleSentencePartialCreditTestingStatistics extends
		AbstractTestingStatistics<Sentence, LogicalExpression> {
	private static final String		DEFAULT_METRIC_NAME				= "PARTIAL_CREDIT";
	
	private static final ILogger	LOG								= LoggerFactory
																			.create(SingleSentencePartialCreditTestingStatistics.class);
	
	private int						numGoldPartialPairs				= 0;
	private int						numLabelPartialPairs			= 0;
	private int						numMatchedPartialPairs			= 0;
	private int						skippingNumGoldPartialPairs		= 0;
	private int						skippingNumLabelPartialPairs	= 0;
	private int						skippingNumMatchedPartialPairs	= 0;
	
	public SingleSentencePartialCreditTestingStatistics() {
		super(DEFAULT_METRIC_NAME);
	}
	
	public SingleSentencePartialCreditTestingStatistics(String prefix) {
		super(prefix, DEFAULT_METRIC_NAME);
	}
	
	public SingleSentencePartialCreditTestingStatistics(String prefix,
			String metricName) {
		super(prefix, metricName);
	}
	
	@Override
	public void recordNoParse(IDataItem<Sentence> dataItem,
			LogicalExpression gold) {
		recordParse(dataItem, gold, null);
	}
	
	@Override
	public void recordNoParseWithSkipping(IDataItem<Sentence> dataItem,
			LogicalExpression gold) {
		recordParseWithSkipping(dataItem, gold, null);
	}
	
	@Override
	public void recordParse(IDataItem<Sentence> dataItem,
			LogicalExpression gold, LogicalExpression label) {
		final PartialCreditTriplet partialCreditTriplet = partialCompare(gold,
				label);
		
		LOG.info("Partial credit: %s", partialCreditTriplet);
		numGoldPartialPairs += partialCreditTriplet.getGoldPairs();
		numLabelPartialPairs += partialCreditTriplet.getLabelPairs();
		numMatchedPartialPairs += partialCreditTriplet.getMatchedPairs();
	}
	
	@Override
	public void recordParses(IDataItem<Sentence> dataItem,
			LogicalExpression gold, List<LogicalExpression> labels) {
		recordNoParse(dataItem, gold);
	}
	
	@Override
	public void recordParsesWithSkipping(IDataItem<Sentence> dataItem,
			LogicalExpression gold, List<LogicalExpression> labels) {
		recordNoParseWithSkipping(dataItem, gold);
	}
	
	@Override
	public void recordParseWithSkipping(IDataItem<Sentence> dataItem,
			LogicalExpression gold, LogicalExpression label) {
		final PartialCreditTriplet partialCreditTriplet = partialCompare(gold,
				label);
		
		LOG.info("Empty partial credit: %s", partialCreditTriplet);
		skippingNumGoldPartialPairs += partialCreditTriplet.getGoldPairs();
		skippingNumLabelPartialPairs += partialCreditTriplet.getLabelPairs();
		skippingNumMatchedPartialPairs += partialCreditTriplet
				.getMatchedPairs();
	}
	
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder("=== ").append(
				getMericName()).append(" statistics:\n");
		ret.append("Recall: ").append(numMatchedPartialPairs).append('/')
				.append(numGoldPartialPairs).append(" = ").append(recall())
				.append('\n');
		ret.append("Precision: ").append(numMatchedPartialPairs).append('/')
				.append(numLabelPartialPairs).append(" = ").append(precision())
				.append('\n');
		ret.append("F1: ").append(f1()).append('\n');
		ret.append("SKIP Recall: ").append(skippingNumMatchedPartialPairs)
				.append('/').append(skippingNumGoldPartialPairs).append(" = ")
				.append(skippingRecall()).append('\n');
		ret.append("SKIP Precision: ").append(skippingNumMatchedPartialPairs)
				.append('/').append(skippingNumLabelPartialPairs).append(" = ")
				.append(skippingPrecision()).append('\n');
		ret.append("SKIP F1: ").append(skippingF1());
		return ret.toString();
	}
	
	@Override
	public String toTabDelimitedString() {
		final StringBuilder ret = new StringBuilder(getPrefix())
				.append("\tmetric=").append(getMericName()).append("\t");
		ret.append("recall=").append(recall()).append('\t');
		ret.append("precision=").append(precision()).append('\t');
		ret.append("f1=").append(f1()).append('\t');
		ret.append("skippingRecall=").append(skippingRecall()).append('\t');
		ret.append("skippingPrecision=").append(skippingPrecision())
				.append('\t');
		ret.append("skippingF1=").append(skippingF1());
		return ret.toString();
	}
	
	private double f1() {
		return (precision() + recall()) == 0.0 ? 0.0
				: (2 * precision() * recall()) / (precision() + recall());
	}
	
	private PartialCreditTriplet partialCompare(LogicalExpression gold,
			LogicalExpression label) {
		final Multiset<Pair<? extends LogicalExpression, ? extends LogicalExpression>> goldPairs = GetPredConstPairs
				.of(gold);
		final Multiset<Pair<? extends LogicalExpression, ? extends LogicalExpression>> labelPairs;
		if (label == null) {
			labelPairs = HashMultiset.create();
		} else {
			labelPairs = GetPredConstPairs.of(label);
		}
		
		// The "intersection" of the gold and label pair sets = the number of
		// matches
		final Multiset<Pair<? extends LogicalExpression, ? extends LogicalExpression>> intersection = HashMultiset
				.create();
		
		for (final Entry<Pair<? extends LogicalExpression, ? extends LogicalExpression>> entry : goldPairs
				.entrySet()) {
			intersection.setCount(
					entry.getElement(),
					Math.min(entry.getCount(),
							labelPairs.count(entry.getElement())));
		}
		
		return new PartialCreditTriplet(goldPairs.size(), labelPairs.size(),
				intersection.size());
	}
	
	private double precision() {
		return numLabelPartialPairs == 0.0 ? 0.0
				: (double) numMatchedPartialPairs / numLabelPartialPairs;
	}
	
	private double recall() {
		return numGoldPartialPairs == 0.0 ? 0.0
				: (double) numMatchedPartialPairs / numGoldPartialPairs;
	}
	
	private double skippingF1() {
		return (skippingPrecision() + skippingRecall()) == 0.0 ? 0.0
				: (2 * skippingPrecision() * skippingRecall())
						/ (skippingPrecision() + skippingRecall());
	}
	
	private double skippingPrecision() {
		return skippingNumLabelPartialPairs == 0.0 ? 0.0
				: (double) skippingNumMatchedPartialPairs
						/ skippingNumLabelPartialPairs;
	}
	
	private double skippingRecall() {
		return skippingNumGoldPartialPairs == 0.0 ? 0.0
				: (double) skippingNumMatchedPartialPairs
						/ skippingNumGoldPartialPairs;
	}
	
	private static class GetPredConstPairs implements ILogicalExpressionVisitor {
		private final Multiset<Pair<? extends LogicalExpression, ? extends LogicalExpression>>	predConstPairs	= HashMultiset
																														.create();
		
		/**
		 * Usage only through static 'of' method.
		 */
		private GetPredConstPairs() {
			// Nothing to do
		}
		
		public static Multiset<Pair<? extends LogicalExpression, ? extends LogicalExpression>> of(
				LogicalExpression exp) {
			final GetPredConstPairs visitor = new GetPredConstPairs();
			visitor.visit(exp);
			return visitor.getPredConstPairs();
		}
		
		public Multiset<Pair<? extends LogicalExpression, ? extends LogicalExpression>> getPredConstPairs() {
			return predConstPairs;
		}
		
		@Override
		public void visit(Lambda lambda) {
			lambda.getArgument().accept(this);
			lambda.getBody().accept(this);
		}
		
		@Override
		public void visit(Literal literal) {
			// Visit the predicate
			literal.getPredicate().accept(this);
			
			final LogicalExpression pred = literal.getPredicate();
			if (!LogicLanguageServices.isCoordinationPredicate(pred)
					&& !LogicLanguageServices.isArrayIndexPredicate(pred)
					&& !LogicLanguageServices.isArraySubPredicate(pred)
					&& literal.getPredicate() instanceof LogicalConstant) {
				if (literal.getArguments().size() == 1
						&& !(literal.getArguments().get(0) instanceof LogicalConstant)) {
					// Unary predicates
					predConstPairs.add(Pair.of(literal.getPredicate(),
							(LogicalExpression) null));
					return;
				} else if (literal.getArguments().size() == 2
						&& !(literal.getArguments().get(0) instanceof LogicalConstant)
						&& IsExtendedConstant.of(literal.getArguments().get(1))) {
					// Binary predicate
					predConstPairs.add(Pair.of(literal.getPredicate(), literal
							.getArguments().get(1)));
					return;
				}
			}
			
			// Just visit the arguments and predicate
			for (final LogicalExpression arg : literal.getArguments()) {
				arg.accept(this);
			}
		}
		
		@Override
		public void visit(LogicalConstant logicalConstant) {
			// Nothing to do
		}
		
		@Override
		public void visit(LogicalExpression logicalExpression) {
			logicalExpression.accept(this);
		}
		
		@Override
		public void visit(Variable variable) {
			// Nothing to do
		}
	}
	
	private static class PartialCreditTriplet {
		private final int	goldPairs;
		private final int	labelPairs;
		private final int	matchedPairs;
		
		public PartialCreditTriplet(int goldPairs, int labelPairs,
				int matchedPairs) {
			this.goldPairs = goldPairs;
			this.labelPairs = labelPairs;
			this.matchedPairs = matchedPairs;
		}
		
		public int getGoldPairs() {
			return goldPairs;
		}
		
		public int getLabelPairs() {
			return labelPairs;
		}
		
		public int getMatchedPairs() {
			return matchedPairs;
		}
		
		@Override
		public String toString() {
			return "PartialCreditTriplet [goldPairs=" + goldPairs
					+ ", labelPairs=" + labelPairs + ", matchedPairs="
					+ matchedPairs + "]";
		}
		
	}
}
