package edu.uw.cs.lil.tiny.tempeval.util;

public class TemporalStatistics {
	private int correct, gold, predicted;

	public TemporalStatistics() {
		correct = gold = predicted = 0;
	}

	public void addCorrect(int newCorrect) {
		correct += newCorrect;
	}

	public void addGold(int newGold) {
		gold += newGold;
	}

	public void addPredicted(int newPredicted) {
		predicted += newPredicted;
	}

	public double getRecall() {
		return ((double) correct)/gold;
	}

	public double getPrecision() {
		return ((double) correct)/predicted;
	}

	public double getF1() {
		double r = getRecall();
		double p = getPrecision();
		return 2*r*p/(r+p);
	}

	public String toString() {
		String s = "";
		s += String.format("Recall: %.2f (%d/%d)\n", 100*getRecall(), correct, gold);
		s += String.format("Precision: %.2f (%d/%d)\n", 100*getPrecision(), correct, predicted);
		s += String.format("F1: %.2f\n", 100*getF1());
		return s;
	}
}