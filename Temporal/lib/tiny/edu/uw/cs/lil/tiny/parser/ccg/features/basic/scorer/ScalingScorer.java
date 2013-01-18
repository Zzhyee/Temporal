package edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.uw.cs.lil.tiny.parser.ccg.model.storage.AbstractDecoderIntoFile;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.DecoderHelper;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.DecoderServices;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.IDecoder;
import edu.uw.cs.utils.collections.IScorer;

/**
 * Scorer to scale an existing base scorer.
 * 
 * @author Yoav Artzi
 * @param <Y>
 */
public class ScalingScorer<Y> implements IScorer<Y> {
	
	private final IScorer<Y>	baseScorer;
	private final double		scale;
	
	public ScalingScorer(double scale, IScorer<Y> baseScorer) {
		this.scale = scale;
		this.baseScorer = baseScorer;
	}
	
	public static <Y> IDecoder<ScalingScorer<Y>> getDecoder(
			DecoderHelper<Y> decoderHelper) {
		return new Decoder<Y>(decoderHelper);
	}
	
	@Override
	public double score(Y lex) {
		return scale * baseScorer.score(lex);
	}
	
	private static class Decoder<Y> extends
			AbstractDecoderIntoFile<ScalingScorer<Y>> {
		private static final int		VERSION	= 1;
		private final DecoderHelper<Y>	decoderHelper;
		
		public Decoder(DecoderHelper<Y> decoderHelper) {
			super(ScalingScorer.class);
			this.decoderHelper = decoderHelper;
		}
		
		@Override
		public int getVersion() {
			return VERSION;
		}
		
		@Override
		protected Map<String, String> createAttributesMap(
				ScalingScorer<Y> object) {
			final Map<String, String> attributes = new HashMap<String, String>();
			
			attributes.put("scale", Double.toString(object.scale));
			
			return attributes;
		}
		
		@Override
		protected ScalingScorer<Y> doDecode(Map<String, String> attributes,
				Map<String, File> dependentFiles, BufferedReader reader)
				throws IOException {
			
			final double scale = Double.valueOf(attributes.get("scale"));
			
			// Get base scorer
			final IScorer<Y> baseScorer = DecoderServices.decode(
					dependentFiles.get("baseScorer"), decoderHelper);
			
			return new ScalingScorer<Y>(scale, baseScorer);
		}
		
		@Override
		protected void doEncode(ScalingScorer<Y> object, BufferedWriter writer)
				throws IOException {
			// Nothing to do here
		}
		
		@Override
		protected Map<String, File> encodeDependentFiles(
				ScalingScorer<Y> object, File directory, File parentFile)
				throws IOException {
			final Map<String, File> files = new HashMap<String, File>();
			
			// Encode base scorer
			final File defaultScorerFile = new File(directory,
					parentFile.getName() + ".baseScorer");
			DecoderServices.encode(object.baseScorer, defaultScorerFile,
					decoderHelper);
			files.put("baseScorer", defaultScorerFile);
			
			return files;
		}
	}
	
}
