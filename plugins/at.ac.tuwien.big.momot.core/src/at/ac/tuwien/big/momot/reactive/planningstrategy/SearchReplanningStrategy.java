package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;

public abstract class SearchReplanningStrategy extends ReplanningStrategy {

   protected String replanningAlgorithm;
   protected boolean doReusePreviousPlan;
   protected float reusePortion;

   protected SearchReplanningStrategy(final String replanningAlgorithm, final boolean reusePreviousPlan,
         final float reusePortion) {
      super(RepairStrategy.REPLAN_FOR_EVALUATIONS);
      this.replanningAlgorithm = replanningAlgorithm;
      this.doReusePreviousPlan = reusePreviousPlan;
      this.reusePortion = reusePortion;
   }

   public String getReplanningAlgorithm() {
      return replanningAlgorithm;
   }

   public float getReusePortion() {
      return this.reusePortion;
   }

   public boolean isDoReusePreviousPlan() {
      return doReusePreviousPlan;
   }

   public abstract SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
         final int solutionLength, final int populationSize, final List<TransformationSolution> reinitSolutions);
}
