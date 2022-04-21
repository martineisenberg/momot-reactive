package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.domain.Heuristic;
import at.ac.tuwien.big.momot.search.criterion.MinimumObjectiveCondition;

import java.util.Map;

import org.moeaframework.core.TerminationCondition;

public class PlanningStrategy {

   public static PlanningStrategy create(final String initialSearchAlgorithm,
         final int prioritizedObjectiveForSelection) {
      return new PlanningStrategy(initialSearchAlgorithm, prioritizedObjectiveForSelection);
   }

   private final String searchAlgorithm;
   private final int prioritizedObjectiveForSelection;

   private TerminationCondition terminationCriterion;
   private int maxEvaluations;
   private Heuristic heuristic;
   private double heuristicPortion;

   protected PlanningStrategy(final String initialSearchAlgorithm, final int prioritizedObjectiveForSelection) {
      this.searchAlgorithm = initialSearchAlgorithm;
      this.prioritizedObjectiveForSelection = prioritizedObjectiveForSelection;
   }

   public PredictiveReplanningStrategy castAsPredictiveReplanningStrategy() {
      if(!(this instanceof PredictiveReplanningStrategy)) {
         throw new RuntimeException("Illegal cast to PredictiveReplanningStrategy!");
      }
      return (PredictiveReplanningStrategy) this;
   }

   public ReplanningStrategy castAsReplanningStrategy() {
      if(!(this instanceof ReplanningStrategy)) {
         throw new RuntimeException("Illegal cast to ReplanningStrategy!");
      }
      return (ReplanningStrategy) this;
   }

   public Heuristic getHeuristic() {
      return this.heuristic;
   }

   public double getHeuristicPortion() {
      return this.heuristicPortion;
   }

   public String getInitialSearchAlgorithm() {
      return searchAlgorithm;
   }

   public int getInitialSearchEvaluations() {
      return maxEvaluations;
   }

   public int getPrioritizedObjectiveForSelection() {
      return prioritizedObjectiveForSelection;
   }

   public TerminationCondition getTerminationCriterion() {
      return terminationCriterion;
   }

   @Override
   public String toString() {
      return "PlanningStrategy";
   }

   public PlanningStrategy withHeuristicInitialization(final Heuristic h, final double seedingPortion) {
      this.heuristic = h;
      this.heuristicPortion = seedingPortion;
      return this;
   }

   public PlanningStrategy withMaxEvaluations(final int maxEvaluations) {
      this.maxEvaluations = maxEvaluations;
      return this;
   }

   public PlanningStrategy withObjectiveThresholds(final Map<Integer, Double> map) {
      this.terminationCriterion = MinimumObjectiveCondition.create(map);
      return this;
   }

   // public PlanningStrategy withTerminationCriterion(final TerminationCondition c) {
   // this.terminationCriterion = c;
   // return this;
   // }
}
