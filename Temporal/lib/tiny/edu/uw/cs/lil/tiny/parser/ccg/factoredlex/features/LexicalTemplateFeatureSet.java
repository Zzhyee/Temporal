package edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.FactoredLexicon;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.LexicalTemplate;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.UniformScorer;
import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
import edu.uw.cs.lil.tiny.parser.ccg.lexicon.Lexicon;
import edu.uw.cs.lil.tiny.parser.ccg.model.lexical.AbstractLexicalFeatureSet;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.AbstractDecoderIntoFile;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.DecoderHelper;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.DecoderServices;
import edu.uw.cs.lil.tiny.parser.ccg.model.storage.IDecoder;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
import edu.uw.cs.lil.tiny.utils.hashvector.KeyArgs;
import edu.uw.cs.utils.collections.IScorer;
import edu.uw.cs.utils.composites.Triplet;

public class LexicalTemplateFeatureSet<X> extends
		AbstractLexicalFeatureSet<X, LogicalExpression> {
	
	private final String						featureTag;
	
	private final IScorer<LexicalTemplate>		initialFixedScorer;
	
	private final IScorer<LexicalTemplate>		initialScorer;
	
	private int									nextId	= 0;
	
	private final double						scale;
	
	private final Map<LexicalTemplate, Integer>	templateIds;
	
	private LexicalTemplateFeatureSet(String featureTag,
			IScorer<LexicalTemplate> initialFixedScorer,
			IScorer<LexicalTemplate> initialScorer,
			Map<LexicalTemplate, Integer> templateIds, double scale) {
		this.featureTag = featureTag;
		this.initialFixedScorer = initialFixedScorer;
		this.initialScorer = initialScorer;
		this.templateIds = templateIds;
		this.scale = scale;
		for (final Map.Entry<LexicalTemplate, Integer> entry : this.templateIds
				.entrySet()) {
			if (entry.getValue() >= nextId) {
				nextId = entry.getValue() + 1;
			}
		}
	}
	
	public static <X> IDecoder<LexicalTemplateFeatureSet<X>> getDecoder(
			DecoderHelper<LogicalExpression> decoderHelper) {
		return new Decoder<X>(decoderHelper);
	}
	
	@Override
	public boolean addEntry(LexicalEntry<LogicalExpression> entry,
			IHashVector parametersVector) {
		final LexicalTemplate template = getTemplate(entry);
		if (template == null) {
			return false;
		}
		if (templateIds.containsKey(template)) {
			return false;
		}
		final int num = getNextId();
		templateIds.put(template, new Integer(num));
		parametersVector.set(featureTag, String.valueOf(num),
				initialScorer.score(template));
		return true;
	}
	
	@Override
	public boolean addFixedEntry(LexicalEntry<LogicalExpression> entry,
			IHashVector parametersVector) {
		final LexicalTemplate template = getTemplate(entry);
		if (template == null) {
			return false;
		}
		if (templateIds.containsKey(template)) {
			return false;
		}
		final int num = getNextId();
		templateIds.put(template, new Integer(num));
		parametersVector.set(featureTag, String.valueOf(num),
				initialFixedScorer.score(template));
		return true;
	}
	
	@Override
	public List<Triplet<KeyArgs, Double, String>> getFeatureWeights(
			IHashVector theta) {
		// Get weights relevant to this feature set and attach each of them the
		// lexical entry as comment
		final List<Triplet<KeyArgs, Double, String>> weights = new LinkedList<Triplet<KeyArgs, Double, String>>();
		
		for (final Map.Entry<LexicalTemplate, Integer> entry : templateIds
				.entrySet()) {
			final int index = entry.getValue();
			final double weight = theta.get(featureTag, String.valueOf(index));
			weights.add(Triplet.of(
					new KeyArgs(featureTag, String.valueOf(index)), weight,
					entry.getKey().toString()));
		}
		
		return weights;
	}
	
	@Override
	public double score(LexicalEntry<LogicalExpression> entry,
			IHashVector parametersVector) {
		if (entry == null) {
			return 0.0;
		}
		final LexicalTemplate template = getTemplate(entry);
		if (template == null) {
			// if the lexical item is not factored, we return 0. this is to
			// allow parsing with mixed lexical items (both factored and
			// unfactored).
			return 0.0;
		}
		final int i = indexOf(template);
		if (i >= 0) {
			return parametersVector.get(featureTag, String.valueOf(i)) * scale;
		}
		// return the weight that would be assigned if this feature were added
		return initialScorer.score(template) * scale;
	}
	
	@Override
	public void setFeats(LexicalEntry<LogicalExpression> entry,
			IHashVector features) {
		if (entry == null) {
			return;
		}
		final LexicalTemplate template = getTemplate(entry);
		if (template == null) {
			// if the lexical item is not factored, we don't set any features.
			// this is to allow parsing with mixed lexical items (both factored
			// and unfactored).
			return;
		}
		final int i = indexOf(template);
		if (i >= 0) {
			features.set(featureTag, String.valueOf(i),
					features.get(featureTag, String.valueOf(i)) + 1.0 * scale);
		}
		
	}
	
	/**
	 * Returns the next ID to use and increase the ID counter by one.
	 * 
	 * @return next free ID for the entries map.
	 */
	private int getNextId() {
		return nextId++;
	}
	
	private LexicalTemplate getTemplate(LexicalEntry<LogicalExpression> entry) {
		if (entry instanceof FactoredLexicon.FactoredLexicalEntry) {
			return ((FactoredLexicon.FactoredLexicalEntry) entry).getTemplate();
		}
		return null;
	}
	
	private int indexOf(LexicalTemplate l) {
		final Integer index = templateIds.get(l);
		if (index == null) {
			return -1;
		} else {
			return index.intValue();
		}
	}
	
	public static class Builder<X> {
		
		private String								featureTag			= "XTMP";
		
		private IScorer<LexicalTemplate>			initialFixedScorer	= new UniformScorer<LexicalTemplate>(
																				0.0);
		
		private IScorer<LexicalTemplate>			initialScorer		= new UniformScorer<LexicalTemplate>(
																				0.0);
		
		private double								scale				= 1.0;
		
		private final Map<LexicalTemplate, Integer>	templateIds			= new HashMap<LexicalTemplate, Integer>();
		
		public LexicalTemplateFeatureSet<X> build() {
			return new LexicalTemplateFeatureSet<X>(featureTag,
					initialFixedScorer, initialScorer, templateIds, scale);
		}
		
		public Builder<X> setFeatureTag(String featureTag) {
			this.featureTag = featureTag;
			return this;
		}
		
		public Builder<X> setInitialFixedScorer(
				IScorer<LexicalTemplate> initialFixedScorer) {
			this.initialFixedScorer = initialFixedScorer;
			return this;
		}
		
		public Builder<X> setInitialScorer(
				IScorer<LexicalTemplate> initialScorer) {
			this.initialScorer = initialScorer;
			return this;
		}
		
		public Builder<X> setScale(double scale) {
			this.scale = scale;
			return this;
		}
		
	}
	
	private static class Decoder<X> extends
			AbstractDecoderIntoFile<LexicalTemplateFeatureSet<X>> {
		
		private static final int						VERSION	= 1;
		
		private final DecoderHelper<LogicalExpression>	decoderHelper;
		
		public Decoder(DecoderHelper<LogicalExpression> decoderHelper) {
			super(LexicalTemplateFeatureSet.class);
			this.decoderHelper = decoderHelper;
		}
		
		@Override
		public int getVersion() {
			return VERSION;
		}
		
		@Override
		protected Map<String, String> createAttributesMap(
				LexicalTemplateFeatureSet<X> object) {
			final HashMap<String, String> attributes = new HashMap<String, String>();
			
			attributes.put("featureTag", object.featureTag);
			attributes.put("scale", Double.toString(object.scale));
			
			return attributes;
		}
		
		@Override
		protected LexicalTemplateFeatureSet<X> doDecode(
				Map<String, String> attributes,
				Map<String, File> dependentFiles, BufferedReader reader)
				throws IOException {
			final String featureTag = attributes.get("featureTag");
			final double scale = Double.valueOf(attributes.get("scale"));
			
			// Read scorers from external files
			final IScorer<LexicalTemplate> initialScorer = DecoderServices
					.decode(dependentFiles.get("initialScorer"), decoderHelper);
			final IScorer<LexicalTemplate> initialFixedScorer = DecoderServices
					.decode(dependentFiles.get("initialFixedScorer"),
							decoderHelper);
			
			// Read lexItems mapping
			final Map<LexicalTemplate, Integer> templateIds = new HashMap<LexicalTemplate, Integer>();
			// Read the header of the map
			readTextLine(reader);
			String line;
			while (!(line = readTextLine(reader))
					.equals("LEX_TEMPLATES_MAP_END")) {
				final String split[] = line.split("\t");
				final LexicalTemplate template = LexicalTemplate.parse(
						split[0], decoderHelper.getCategoryServices(),
						Lexicon.SAVED_LEXICON_ORIGIN);
				final int id = Integer.valueOf(split[1]);
				templateIds.put(template, id);
			}
			
			final LexicalTemplateFeatureSet<X> lfs = new LexicalTemplateFeatureSet<X>(
					featureTag, initialFixedScorer, initialScorer, templateIds,
					scale);
			return lfs;
		}
		
		@Override
		protected void doEncode(LexicalTemplateFeatureSet<X> object,
				BufferedWriter writer) throws IOException {
			// Store mapping of lexical templates to feature IDs
			writer.write("LEX_TEMPLATES_MAP_START\n");
			for (final Map.Entry<LexicalTemplate, Integer> entry : object.templateIds
					.entrySet()) {
				writer.write(String.format("%s\t%d\n", entry.getKey(),
						entry.getValue()));
			}
			writer.write("LEX_TEMPLATES_MAP_END\n");
		}
		
		@Override
		protected Map<String, File> encodeDependentFiles(
				LexicalTemplateFeatureSet<X> object, File directory,
				File parentFile) throws IOException {
			final Map<String, File> dependentFiles = new HashMap<String, File>();
			
			// Store scorers to separate files
			final File initialScorerFile = new File(directory,
					parentFile.getName() + ".initialScorer");
			DecoderServices.encode(object.initialScorer, initialScorerFile,
					decoderHelper);
			dependentFiles.put("initialScorer", initialScorerFile);
			
			final File initialFixedScorerFile = new File(directory,
					parentFile.getName() + ".initialFixedScorer");
			DecoderServices.encode(object.initialFixedScorer,
					initialFixedScorerFile, decoderHelper);
			dependentFiles.put("initialFixedScorer", initialFixedScorerFile);
			
			return dependentFiles;
		}
		
	}
	
}
