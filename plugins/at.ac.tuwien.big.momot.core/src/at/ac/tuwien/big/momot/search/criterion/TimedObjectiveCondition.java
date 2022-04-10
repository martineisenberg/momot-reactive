package at.ac.tuwien.big.momot.search.criterion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.TerminationCondition;

public class TimedObjectiveCondition implements TerminationCondition, ThresholdCondition {

   public static TimedObjectiveCondition create(final Map<Integer, Double> objectiveThresholds,
         final int objectiveOptimalityIndex) {
      return new TimedObjectiveCondition(objectiveThresholds, objectiveOptimalityIndex);
   }

   private Map<Integer, Double> objectiveThresholds;
   private long maxNanosIfObjectiveSatisfied;
   private long startTime;
   private long timeTilObjectivesSatisfied;
   private boolean areObjectivesSatisfised;
   private long terminatedAfterNanos;

   private TimedObjectiveCondition(final Map<Integer, Double> objectiveThresholds, final int objectiveOptimalityIndex) {
      this.objectiveThresholds = objectiveThresholds;
      // this.maxNanosIfObjectiveSatisfied = (long) (maxSecondsIfObjectiveSatisfied * Math.pow(10, 9));
      this.areObjectivesSatisfised = false;
      this.terminatedAfterNanos = 0;
      this.timeTilObjectivesSatisfied = 0;
      this.maxNanosIfObjectiveSatisfied = 0;
   }

   public long getMaxNanosIfObjectiveSatisfied() {
      return maxNanosIfObjectiveSatisfied;
   }

   @Override
   public double[] getObjectiveThresholds() {
      final double[] v = new double[objectiveThresholds.values().size()];
      int i = 0;
      for(final double n : objectiveThresholds.values()) {
         v[i++] = n;
      }
      return v;
   }

   public long getStartTime() {
      return startTime;
   }

   public long getTerminatedAfterNanos() {
      return terminatedAfterNanos;
   }

   @Override
   public Map<Integer, Double> getThresholds() {
      return this.objectiveThresholds;
   }

   public long getTimeTilObjectivesSatisfied() {
      return timeTilObjectivesSatisfied;
   }

   @Override
   public void initialize(final Algorithm algorithm) {
      this.startTime = System.nanoTime();
   }

   public boolean isAreObjectivesSatisfised() {
      return areObjectivesSatisfised;
   }

   public boolean satisfiesCriteria(final Solution s) {
      final double[] o = s.getObjectives();
      for(final Entry<Integer, Double> e : objectiveThresholds.entrySet()) {

         if(o[e.getKey()] >= e.getValue()) {
            return false;
         }
      }
      return true;
   }

   public void setMaxSecondsIfObjectiveSatisfied(final long timeInNanos) {
      this.maxNanosIfObjectiveSatisfied = timeInNanos;
   }

   @Override
   public void setThreshold(final int conditionIndex, final double val) {
      this.objectiveThresholds = new HashMap<>(this.objectiveThresholds);
      this.objectiveThresholds.put(conditionIndex, val);
   }

   @Override
   public boolean shouldTerminate(final Algorithm algorithm) {

      final long elapsedTime = System.nanoTime() - this.startTime;
      final NondominatedPopulation p = algorithm.getResult();
      double minObj = Double.POSITIVE_INFINITY;
      // Solution minSol = null;
      for(final Solution s : p) {
         final double curObj = s.getObjective(0);
         if(curObj < minObj) {
            minObj = curObj;
         }
      }
      System.out.println(minObj);
      if(!areObjectivesSatisfised) {

         // double minObj = Double.POSITIVE_INFINITY;
         // for(final Solution s : p) {
         // final double curObj = s.getObjective(0);
         // if(curObj < minObj) {
         // minObj = curObj;
         // }
         // }
         // System.out.println(minObj);

         for(final Solution s : p) {
            if(satisfiesCriteria(s)) {
               this.areObjectivesSatisfised = true;
               this.timeTilObjectivesSatisfied = elapsedTime;
               System.out.println("Found after " + algorithm.getNumberOfEvaluations() + " evaluations");

            }
         }
      }
      if(areObjectivesSatisfised && this.maxNanosIfObjectiveSatisfied <= elapsedTime) {
         this.terminatedAfterNanos = elapsedTime;
         final List<Solution> fitSolutions = new ArrayList<>();
         p.forEach(s -> {
            if(satisfiesCriteria(s)) {
               fitSolutions.add(s);
            }
         });
         // Solution optimalSolution = fitSolutions.stream().min((s1, s2) -> s1.getObjective(objectiveOptimalityIndex) >
         // s2.getObjective(objectiveOptimalityIndex) ? 1 : -1).get();
         System.out.println("Terminated after " + elapsedTime / Math.pow(10, 9) + "s");

         return true;
      }

      return false;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();
      for(final Entry<Integer, Double> entry : objectiveThresholds.entrySet()) {
         sb.append(String.format("_%s-%s", entry.getKey().toString(), entry.getValue().toString()));
      }
      return sb.toString();
   }

}
