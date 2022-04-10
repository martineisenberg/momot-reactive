package at.ac.tuwien.big.momot.search.criterion;

import java.util.Map;

import org.moeaframework.core.TerminationCondition;

public interface ThresholdCondition extends TerminationCondition {
   public double[] getObjectiveThresholds();

   public Map<Integer, Double> getThresholds();

   public void setThreshold(int conditionIndex, double val);
}
