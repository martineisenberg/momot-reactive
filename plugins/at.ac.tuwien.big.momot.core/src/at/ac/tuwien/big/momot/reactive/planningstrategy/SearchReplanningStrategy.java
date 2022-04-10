package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.domain.Heuristic;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;

public abstract class SearchReplanningStrategy extends ReplanningStrategy {

   public enum PredictiveReplanningType {
      TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED
   }

   protected String replanningAlgorithm;
   protected double reusePortion;
   protected List<Integer> predictivePlanningAfterXSteps;
   protected SearchReplanningStrategy predictiveReplanningStrategy;
   protected PredictiveReplanningType predictiveReplanningType;

   protected Heuristic heuristic;
   protected double heuristicPortion;

   protected SearchReplanningStrategy(final RepairStrategy repairStrategy, final String replanningAlgorithm,
         final Heuristic h) {
      super(repairStrategy);
      this.replanningAlgorithm = replanningAlgorithm;
      this.reusePortion = 0;
      this.predictivePlanningAfterXSteps = null;
      this.predictiveReplanningStrategy = null;
      this.heuristic = h;
   }

   public Heuristic getHeuristic() {
      return this.heuristic;
   }

   public double getHeuristicPortion() {
      return heuristicPortion;
   }

   public List<Integer> getPredictivePlanningAfterXSteps() {
      return this.predictivePlanningAfterXSteps;
   }

   public SearchReplanningStrategy getPredictivePlanningStrategy() {
      return this.predictiveReplanningStrategy;
   }

   public PredictiveReplanningType getPredictiveReplanningType() {
      return this.predictiveReplanningType;
   }

   public String getReplanningAlgorithm() {
      return replanningAlgorithm;
   }

   public double getReusePortion() {
      return this.reusePortion;
   }

   public boolean isPredictivePlanningEnabled() {
      return this.predictivePlanningAfterXSteps != null && this.predictiveReplanningStrategy != null;
   }

   public abstract SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
         final String experimentName, final int run, final int solutionLength, final int populationSize,
         final List<List<ITransformationVariable>> reinitSeed, final double reinitBestObj,
         final boolean recordBestObjective);

   // public SearchReplanningStrategy reusePortion(final float portion) {
   // this.reusePortion = portion;
   // return this;
   // }

   public boolean withHeuristic() {
      return this.heuristic != null;
   }

   public SearchReplanningStrategy withHeuristic(final Heuristic h, final double portion) {
      this.heuristic = h;
      this.heuristicPortion = portion;
      return this;
   }

   public SearchReplanningStrategy withPlanReuse(final double portion) {
      this.reusePortion = portion;
      return this;
   }

   public SearchReplanningStrategy withPredictivePlanning(final PredictiveReplanningType prt,
         final List<Integer> planAfterXStepsList, final String algorithm, final Heuristic h,
         final double heuristicPortion) {
      this.predictivePlanningAfterXSteps = planAfterXStepsList;
      this.predictiveReplanningType = prt;
      this.predictiveReplanningStrategy = ConditionReplanningStrategy.create(algorithm, null);
      this.predictiveReplanningStrategy.heuristic = h;
      this.predictiveReplanningStrategy.heuristicPortion = heuristicPortion;
      return this;
   }
}
