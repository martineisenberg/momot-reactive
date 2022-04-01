package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;

public abstract class SearchReplanningStrategy extends ReplanningStrategy {

   protected String replanningAlgorithm;
   protected double reusePortion;
   protected List<Integer> predictivePlanningAfterXSteps;
   protected SearchReplanningStrategy predictivePlanningStrategy;

   protected SearchReplanningStrategy(final RepairStrategy repairStrategy, final String replanningAlgorithm) {
      super(repairStrategy);
      this.replanningAlgorithm = replanningAlgorithm;
      this.reusePortion = 0;
      this.predictivePlanningAfterXSteps = null;
      this.predictivePlanningStrategy = null;
   }

   public List<Integer> getPredictivePlanningAfterXSteps() {
      return this.predictivePlanningAfterXSteps;
   }

   public SearchReplanningStrategy getPredictivePlanningStrategy() {
      return this.predictivePlanningStrategy;
   }

   public String getReplanningAlgorithm() {
      return replanningAlgorithm;
   }

   public double getReusePortion() {
      return this.reusePortion;
   }

   public boolean isPredictivePlanningEnabled() {
      return this.predictivePlanningAfterXSteps != null && this.predictivePlanningStrategy != null;
   }

   public abstract SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
         final String experimentName, final int run, final int solutionLength, final int populationSize,
         final List<ITransformationVariable> reinitSeed, final double reinitPortion, final double reinitBestObj,
         final boolean recordBestObjective);

   // public SearchReplanningStrategy reusePortion(final float portion) {
   // this.reusePortion = portion;
   // return this;
   // }

   public SearchReplanningStrategy withPlanReuse(final double portion) {
      this.reusePortion = portion;
      return this;
   }

   public SearchReplanningStrategy withPredictivePlanning(final List<Integer> planAfterXStepsList,
         final String algorithm) {
      this.predictivePlanningAfterXSteps = planAfterXStepsList;
      this.predictivePlanningStrategy = ConditionReplanningStrategy.create(algorithm, null);
      return this;
   }
}
