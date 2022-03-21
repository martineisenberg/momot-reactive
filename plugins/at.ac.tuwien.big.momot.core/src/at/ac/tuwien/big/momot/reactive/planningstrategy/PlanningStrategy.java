package at.ac.tuwien.big.momot.reactive.planningstrategy;

import org.moeaframework.core.TerminationCondition;

public class PlanningStrategy {
   public static class PlanningStrategyBuilder {

      private final ReplanningStrategy replanningStrategy;
      private final String initialSearchAlgorithm;
      private int initialSearchEvaluations;
      private TerminationCondition terminationCondition;

      public PlanningStrategyBuilder(final String algorithm, final ReplanningStrategy strategy) {
         this.initialSearchAlgorithm = algorithm;
         this.replanningStrategy = strategy;
      }

      public PlanningStrategy build() {
         final PlanningStrategy p = new PlanningStrategy(this);

         if(initialSearchEvaluations == 0 && terminationCondition == null) {
            throw new RuntimeException(
                  "Forgot setting of replanning strategy, search algorithm, or either max. evaluations or termination condition!");
         }

         return p;
      }

      public PlanningStrategyBuilder maxEvaluations(final int maxEvaluations) {
         this.initialSearchEvaluations = maxEvaluations;
         return this;
      }

      public PlanningStrategyBuilder terminationCondition(final TerminationCondition condition) {
         this.terminationCondition = condition;
         return this;
      }
   }

   // public static PlanningStrategy create(final String initialSearchAlgorithm, final int initialSearchEvaluations,
   // final ReplanningStrategy replanningStrategy) {
   // return new PlanningStrategy(initialSearchAlgorithm, initialSearchEvaluations, replanningStrategy);
   // }

   private final ReplanningStrategy replanningStrategy;
   private final String initialSearchAlgorithm;
   private final TerminationCondition terminationCriterion;
   private final int initialSearchEvaluations;

   private PlanningStrategy(final PlanningStrategyBuilder builder) {
      this.initialSearchAlgorithm = builder.initialSearchAlgorithm;
      this.initialSearchEvaluations = builder.initialSearchEvaluations;
      this.replanningStrategy = builder.replanningStrategy;
      this.terminationCriterion = builder.terminationCondition;
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
