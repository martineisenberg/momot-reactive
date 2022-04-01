package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;

public class EvaluationReplanningStrategy extends SearchReplanningStrategy {

   public static EvaluationReplanningStrategy create(final String algorithm, final int nrOfEvaluations) {
      return new EvaluationReplanningStrategy(algorithm, nrOfEvaluations);
   }

   private final int evaluations;

   private EvaluationReplanningStrategy(final String algorithm, final int nrOfEvaluations) {
      super(RepairStrategy.REPLAN_FOR_EVALUATIONS, algorithm);
      this.evaluations = nrOfEvaluations;
   }

   public int getEvaluations() {
      return evaluations;
   }

   @Override
   public SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
         final String experimentName, final int run, final int solutionLength, final int populationSize,
         final List<ITransformationVariable> reinitSeed, final double reinitPortion, final double reinitBestObj,
         final boolean recordBestObjective) {

      return search.performSearch(graph, algorithmName, experimentName, run, evaluations, null, solutionLength,
            populationSize, reinitSeed, reinitBestObj, reinitPortion, recordBestObjective);
   }

   @Override
   public String toString() {
      return "EvaluationReplanningStrategy-" + evaluations
            + (this.reusePortion > 0 ? "reusePortion=" + reusePortion : "");
   }

}
