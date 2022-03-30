package at.ac.tuwien.big.momot.reactive.planningstrategy;

import org.moeaframework.core.TerminationCondition;

public class PlanningStrategy {

   public static PlanningStrategy create(final String initialSearchAlgorithm, final int initialSearchEvaluations,
         final ReplanningStrategy replanningStrategy) {
      return new PlanningStrategy(initialSearchAlgorithm, initialSearchEvaluations, null, replanningStrategy);
   }

   public static PlanningStrategy create(final String initialSearchAlgorithm,
         final TerminationCondition terminationCondition, final ReplanningStrategy replanningStrategy) {
      return new PlanningStrategy(initialSearchAlgorithm, 0, terminationCondition, replanningStrategy);
   }

   private final ReplanningStrategy replanningStrategy;
   private final String initialSearchAlgorithm;
   private final TerminationCondition terminationCriterion;
   private final int initialSearchEvaluations;

   private PlanningStrategy(final String initialSearchAlgorithm, final int initialSearchEvaluations,
         final TerminationCondition terminationCondition, final ReplanningStrategy replanningStrategy) {
      this.initialSearchAlgorithm = initialSearchAlgorithm;
      this.initialSearchEvaluations = initialSearchEvaluations;
      this.replanningStrategy = replanningStrategy;
      this.terminationCriterion = terminationCondition;
   }

   public String getInitialSearchAlgorithm() {
      return initialSearchAlgorithm;
   }

   public int getInitialSearchEvaluations() {
      return initialSearchEvaluations;
   }

   public ReplanningStrategy getReplanningStrategy() {
      return replanningStrategy;
   }

   public TerminationCondition getTerminationCriterion() {
      return terminationCriterion;
   }

}
