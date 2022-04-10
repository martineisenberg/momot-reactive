package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.domain.Heuristic;

import org.moeaframework.core.TerminationCondition;

public class PlanningStrategy {

   public static PlanningStrategy create(final String initialSearchAlgorithm, final int initialSearchEvaluations,
         final ReplanningStrategy replanningStrategy) {
      return new PlanningStrategy(initialSearchAlgorithm, initialSearchEvaluations, null, replanningStrategy, null, .0);
   }

   public static PlanningStrategy create(final String initialSearchAlgorithm,
         final TerminationCondition terminationCondition, final Heuristic h, final double heuristicPortion,
         final ReplanningStrategy replanningStrategy) {
      return new PlanningStrategy(initialSearchAlgorithm, 0, terminationCondition, replanningStrategy, h,
            heuristicPortion);
   }

   public static PlanningStrategy create(final String initialSearchAlgorithm,
         final TerminationCondition terminationCondition, final ReplanningStrategy replanningStrategy) {
      return new PlanningStrategy(initialSearchAlgorithm, 0, terminationCondition, replanningStrategy, null, .0);
   }

   private final ReplanningStrategy replanningStrategy;
   private final String initialSearchAlgorithm;
   private final TerminationCondition terminationCriterion;
   private final int initialSearchEvaluations;
   private final Heuristic heuristic;
   private final double heuristicPortion;

   private PlanningStrategy(final String initialSearchAlgorithm, final int initialSearchEvaluations,
         final TerminationCondition terminationCondition, final ReplanningStrategy replanningStrategy,
         final Heuristic heuristic, final double heuristicPortion) {
      this.initialSearchAlgorithm = initialSearchAlgorithm;
      this.initialSearchEvaluations = initialSearchEvaluations;
      this.replanningStrategy = replanningStrategy;
      this.terminationCriterion = terminationCondition;
      this.heuristic = heuristic;
      this.heuristicPortion = heuristicPortion;
   }

   public Heuristic getHeuristic() {
      return this.heuristic;
   }

   public double getHeuristicPortion() {
      return this.heuristicPortion;
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
