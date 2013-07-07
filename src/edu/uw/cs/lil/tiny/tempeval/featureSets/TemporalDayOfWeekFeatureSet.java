package edu.uw.cs.lil.tiny.tempeval.featureSets;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.joint.model.IJointFeatureSet;
import edu.uw.cs.lil.tiny.tempeval.TemporalJoda;
import edu.uw.cs.lil.tiny.tempeval.types.TemporalDate;
import edu.uw.cs.lil.tiny.utils.hashvector.HashVectorFactory;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVectorImmutable;
import edu.uw.cs.lil.tiny.utils.hashvector.KeyArgs;
import edu.uw.cs.utils.composites.Pair;
import edu.uw.cs.utils.composites.Triplet;

public class TemporalDayOfWeekFeatureSet implements IJointFeatureSet<Sentence, 
String[], LogicalExpression, LogicalExpression>{
	private static final String	FEATURE_TAG	= "TEMPORAL_WEEKDAY_";

	@Override
	public List<Triplet<KeyArgs, Double, String>> getFeatureWeights(
			IHashVector theta) {
		// TODO throw error
		final List<Triplet<KeyArgs, Double, String>> weights = new LinkedList<Triplet<KeyArgs, Double, String>>();
		for (final Pair<KeyArgs, Double> feature : theta.getAll(FEATURE_TAG)) {
			weights.add(Triplet.of(feature.first(), feature.second(),
					(String) null));
		}
		return weights;
	}

	
	private IHashVectorImmutable setTemporalFeats(LogicalExpression logic, IHashVector feats, IDataItem<Pair<Sentence, String[]>> dataItem) {
		List<String> weekdays = Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday");
		for (String s : weekdays){
			if (logic.toString().contains(s)){
				TemporalDate ref_time = TemporalDate.readDocumentDate(dataItem.getSample().second()[2]);
				if (weekdays.get((TemporalJoda.convertISOToLocalDate(ref_time).dayOfWeek().get())-1).equals(s))
					feats.set(FEATURE_TAG + "sameDay_" + getOuterPred(logic.toString()), 1);
				else
					feats.set(FEATURE_TAG + "notSameDay" + getOuterPred(logic.toString()), 1);
			}
		}
		
		return feats;
	}
	
	private String getOuterPred(String l){
		if (l.startsWith("(previous:<")){
			return "_previous";
		} else if (l.startsWith("(this:<")){
			return "_this";
		} else if (l.startsWith("(next:<")){
			return "_next";
		} else if (l.startsWith("(temporal_ref:<")){
			return "temporal_ref";
		} else {
			return "_none";
		}
	}

	@Override
	public double score(LogicalExpression executionStep, IHashVector theta,
			IDataItem<Pair<Sentence, String[]>> dataItem) {
		return setTemporalFeats(executionStep, HashVectorFactory.create(), dataItem)
				.vectorMultiply(theta);
	}


	@Override
	public void setFeats(LogicalExpression executionStep, IHashVector feats,
			IDataItem<Pair<Sentence, String[]>> dataItem) {
		setTemporalFeats(executionStep, feats, dataItem);		
	}



}
