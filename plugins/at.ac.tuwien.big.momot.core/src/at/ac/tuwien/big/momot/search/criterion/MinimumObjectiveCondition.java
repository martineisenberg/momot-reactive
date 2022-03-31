package at.ac.tuwien.big.momot.search.criterion;

import java.util.Map;
import java.util.Map.Entry;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.TerminationCondition;

public class MinimumObjectiveCondition implements TerminationCondition {

   public static MinimumObjectiveCondition create(final Map<Integer, Double> objectiveThresholds) {
      return new MinimumObjectiveCondition(objectiveThresholds);
   }

   private final Map<Integer, Double> objectiveThresholds;

   private MinimumObjectiveCondition(final Map<Integer, Double> objectiveThresholds) {
      this.objectiveThresholds = objectiveThresholds;
   }

   @Override
   public void initialize(final Algorithm algorithm) {
      // No initialization required; Early stopping only if objective threshold reached

   }

   private boolean satisfiesCriteria(final Solution s) {
      final double[] o = s.getObjectives();
      for(final Entry<Integer, Double> e : objectiveThresholds.entrySet()) {

         if(o[e.getKey()] >= e.getValue()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public boolean shouldTerminate(final Algorithm algorithm) {
      final NondominatedPopulation p = algorithm.getResult();
      for(final Solution s : p) {
         System.out.println(s.getObjective(0));
         if(satisfiesCriteria(s)) {

            System.out.println("Finished after " + algorithm.getNumberOfEvaluations());
            return true;

         }

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
