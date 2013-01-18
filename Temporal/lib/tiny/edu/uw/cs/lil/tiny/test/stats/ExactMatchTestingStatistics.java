package edu.uw.cs.lil.tiny.test.stats;

import java.util.List;

import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.utils.log.ILogger;
import edu.uw.cs.utils.log.LoggerFactory;

/**
 * Testing statistics for the exact match metric.
 * 
 * @author Yoav Artzi
 * @param <X>
 * @param <Y>
 */
public class ExactMatchTestingStatistics<X, Y> extends
		AbstractTestingStatistics<X, Y> {
	private static final String		DEFAULT_METRIC_NAME		= "EXACT";
	
	private static final ILogger	LOG						= LoggerFactory
																	.create(ExactMatchTestingStatistics.class);
	
	/**
	 * The number of correct parses.
	 */
	private int						correctParses			= 0;
	
	/**
	 * The number of parses that provided no single best parse.
	 */
	private int						noParses				= 0;
	
	/**
	 * Total number of parses recorded.
	 */
	private int						numParses				= 0;
	
	/**
	 * The number of correct parses. With word skipping.
	 */
	private int						skippingCorrectParses	= 0;
	
	/**
	 * The number of parses that provided no single best parse. With word
	 * skipping.
	 */
	private int						skippingNoParses		= 0;
	
	/**
	 * The number of single best wrong parses. With word skipping.
	 */
	private int						skippingWrongParses		= 0;
	
	/**
	 * The number of single best wrong parses.
	 */
	private int						wrongParses				= 0;
	
	public ExactMatchTestingStatistics() {
		super(DEFAULT_METRIC_NAME);
	}
	
	public ExactMatchTestingStatistics(String prefix) {
		super(prefix, DEFAULT_METRIC_NAME);
	}
	
	public ExactMatchTestingStatistics(String prefix, String metricName) {
		super(prefix, metricName);
	}
	
	@Override
	public void recordNoParse(IDataItem<X> dataItem, Y gold) {
		LOG.info("%s stats -- recording no parse", getMericName());
		numParses++;
		noParses++;
	}
	
	@Override
	public void recordNoParseWithSkipping(IDataItem<X> dataItem, Y gold) {
		LOG.info("%s stats -- recording no parse with skipping", getMericName());
		skippingNoParses++;
	}
	
	@Override
	public void recordParse(IDataItem<X> dataItem, Y gold, Y label) {
		numParses++;
		if (gold.equals(label)) {
			LOG.info("%s stats -- recording correct parse: %s", getMericName(),
					label);
			correctParses++;
		} else {
			LOG.info("%s stats -- recording wrong parse: %s", getMericName(),
					label);
			wrongParses++;
		}
	}
	
	@Override
	public void recordParses(IDataItem<X> dataItem, Y gold, List<Y> labels) {
		recordNoParse(dataItem, gold);
	}
	
	@Override
	public void recordParsesWithSkipping(IDataItem<X> dataItem, Y gold,
			List<Y> labels) {
		recordNoParseWithSkipping(dataItem, gold);
	}
	
	@Override
	public void recordParseWithSkipping(IDataItem<X> dataItem, Y gold, Y label) {
		if (gold.equals(label)) {
			LOG.info("%s stats -- recording correct parse with skipping: %s",
					getMericName(), label);
			skippingCorrectParses++;
		} else {
			LOG.info("%s stats -- recording wrong parse with skipping: %s",
					getMericName(), label);
			skippingWrongParses++;
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder("=== ").append(
				getMericName()).append(" statistics:\n");
		ret.append("Recall: ").append(correctParses).append('/')
				.append(numParses).append(" = ").append(recall()).append('\n');
		ret.append("Precision: ").append(correctParses).append('/')
				.append(numParses - noParses).append(" = ").append(precision())
				.append('\n');
		ret.append("F1: ").append(f1()).append('\n');
		ret.append("SKIP Recall: ")
				.append(skippingCorrectParses + correctParses).append('/')
				.append(numParses).append(" = ").append(skippingRecall())
				.append('\n');
		ret.append("SKIP Precision: ")
				.append(skippingCorrectParses + correctParses).append('/')
				.append(numParses - skippingNoParses).append(" = ")
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
	
	private double precision() {
		return (numParses - noParses) == 0.0 ? 0.0
				: ((double) correctParses / (numParses - noParses));
	}
	
	private double recall() {
		return numParses == 0.0 ? 0.0 : (double) correctParses / numParses;
	}
	
	private double skippingF1() {
		return (skippingPrecision() + skippingRecall()) == 0.0 ? 0.0
				: (2 * skippingPrecision() * skippingRecall())
						/ (skippingPrecision() + skippingRecall());
	}
	
	private double skippingPrecision() {
		return (numParses - skippingNoParses) == 0.0 ? 0.0
				: (double) (skippingCorrectParses + correctParses)
						/ (numParses - skippingNoParses);
	}
	
	private double skippingRecall() {
		return numParses == 0.0 ? 0.0
				: (double) (skippingCorrectParses + correctParses) / numParses;
	}
	
}
