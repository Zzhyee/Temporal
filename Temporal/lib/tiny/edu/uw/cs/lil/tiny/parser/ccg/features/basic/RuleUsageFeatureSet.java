package edu.uw.cs.lil.tiny.parser.ccg.features.basic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.lil.tiny.parser.ccg.IParseStep;
import edu.uw.cs.lil.tiny.parser.ccg.model.parse.IParseFeatureSet;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.AbstractDecoderIntoFile;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.IDecoder;
import edu.uw.cs.lil.tiny.utils.hashvector.HashVectorFactory;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVectorImmutable;
import edu.uw.cs.lil.tiny.utils.hashvector.KeyArgs;
import edu.uw.cs.utils.composites.Pair;
import edu.uw.cs.utils.composites.Triplet;

public class RuleUsageFeatureSet<X, Y> implements IParseFeatureSet<X, Y> {
	
	private static final String	FEATURE_TAG	= "RULE";
	private final double		scale;
	
	public RuleUsageFeatureSet(double scale) {
		this.scale = scale;
	}
	
	public static <X, Y> IDecoder<RuleUsageFeatureSet<X, Y>> getDecoder() {
		return new Decoder<X, Y>();
	}
	
	@Override
	public List<Triplet<KeyArgs, Double, String>> getFeatureWeights(
			IHashVector theta) {
		final List<Triplet<KeyArgs, Double, String>> weights = new LinkedList<Triplet<KeyArgs, Double, String>>();
		for (final Pair<KeyArgs, Double> feature : theta.getAll(FEATURE_TAG)) {
			weights.add(Triplet.of(feature.first(), feature.second(),
					(String) null));
		}
		return weights;
	}
	
	@Override
	public double score(IParseStep<Y> obj, IHashVector theta,
			IDataItem<X> dataItem) {
		return setFeats(obj.getRuleName(), HashVectorFactory.create())
				.vectorMultiply(theta);
		
	}
	
	@Override
	public void setFeats(IParseStep<Y> obj, IHashVector feats,
			IDataItem<X> dataItem) {
		setFeats(obj.getRuleName(), feats);
		
	}
	
	private IHashVectorImmutable setFeats(String ruleName, IHashVector features) {
		if (ruleName.startsWith("shift")) {
			features.set(FEATURE_TAG, ruleName,
					features.get(FEATURE_TAG, ruleName) + 1.0 * scale);
		}
		return features;
	}
	
	private static class Decoder<X, Y> extends
			AbstractDecoderIntoFile<RuleUsageFeatureSet<X, Y>> {
		private static final int	VERSION	= 1;
		
		public Decoder() {
			super(LogicalExpressionCoordinationFeatureSet.class);
		}
		
		@Override
		public int getVersion() {
			return VERSION;
		}
		
		@Override
		protected Map<String, String> createAttributesMap(
				RuleUsageFeatureSet<X, Y> object) {
			final HashMap<String, String> attributes = new HashMap<String, String>();
			attributes.put("scale", Double.toString(object.scale));
			return attributes;
		}
		
		@Override
		protected RuleUsageFeatureSet<X, Y> doDecode(
				Map<String, String> attributes,
				Map<String, File> dependentFiles, BufferedReader reader)
				throws IOException {
			return new RuleUsageFeatureSet<X, Y>(Double.valueOf(attributes
					.get("scale")));
		}
		
		@Override
		protected void doEncode(RuleUsageFeatureSet<X, Y> object,
				BufferedWriter writer) throws IOException {
			// Nothing to do here
		}
		
		@Override
		protected Map<String, File> encodeDependentFiles(
				RuleUsageFeatureSet<X, Y> object, File directory,
				File parentFile) throws IOException {
			// No dependent files
			return new HashMap<String, File>();
		}
		
	}
	
}