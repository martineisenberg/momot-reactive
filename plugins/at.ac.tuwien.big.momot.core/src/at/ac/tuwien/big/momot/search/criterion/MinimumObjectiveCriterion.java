package at.ac.tuwien.big.momot.search.criterion;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.TerminationCondition;

public class MinimumObjectiveCriterion implements TerminationCondition {

   public static MinimumObjectiveCriterion create(final double val, final int objectiveIndex) {
      return new MinimumObjectiveCriterion(val, objectiveIndex);
   }

   private final int objectiveIndex;

   private final double val;

   private MinimumObjectiveCriterion(final double val, final int objectiveIndex) {
      this.objectiveIndex = objectiveIndex;
      this.val = val;
   }

   @Override
   public void initialize(final Algorithm algorithm) {
      // No initialization required; Early stopping only if objective threshold reached

   }

   // public static TerminationCriterion maximumObjective(final double val, final int objectiveIndex) {
   // return new TerminationCriterion(false, objectiveIndex, val);
   // }

   @Override
   public boolean shouldTerminate(final Algorithm algorithm) {
      final NondominatedPopulation p = algorithm.getResult();
      for(final Solution s : p) {
         if(s.getObjective(objectiveIndex) <= val) {
            return true;
         }
      }
      return false;
   }

}
