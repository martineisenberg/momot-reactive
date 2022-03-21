package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.TerminationCondition;

public class EvaluationReplanningStrategy extends SearchReplanningStrategy {

   public static class EvaluationReplanningStrategyBuilder {
      private int maxEvaluations;
      private TerminationCondition terminationCondition;
      private final String algorithm;
      private boolean reusePreviousPlan;
      private float reusePortion;

      public EvaluationReplanningStrategyBuilder(final String algorithm) {
         this.algorithm = algorithm;
      }

      public EvaluationReplanningStrategy build() {
         final EvaluationReplanningStrategy ers = new EvaluationReplanningStrategy(this);

         if(maxEvaluations == 0 && terminationCondition == null) {
            throw new RuntimeException("Forgot setting max evaluations or algorithm for replanning strategy!");
         }

         return ers;
      }
      // final boolean reusePreviousPlan, final float reusePortion

      public EvaluationReplanningStrategyBuilder maxEvaluations(final int maxEvaluations) {
         this.maxEvaluations = maxEvaluations;
         return this;
      }

      public EvaluationReplanningStrategyBuilder reusePreviousPlan(final boolean reuse, final float reusePortion) {
         this.reusePreviousPlan = reuse;
         this.reusePortion = reusePortion;
         return this;
      }

      public EvaluationReplanningStrategyBuilder terminationCondition(final TerminationCondition condition) {
         this.terminationCondition = condition;
         return this;
      }
   }

   // public static EvaluationReplanningStrategy create(final String algorithm, final int nrOfEvaluations,
   // final boolean reusePreviousPlan, final float reusePortion) {
   // return new EvaluationReplanningStrategy(algorithm, nrOfEvaluations, reusePreviousPlan, reusePortion);
   // }

   private final int evaluations;
   private final TerminationCondition terminationCondition;

   private EvaluationReplanningStrategy(final EvaluationReplanningStrategyBuilder builder) {
      super(builder.algorithm, builder.reusePreviousPlan, builder.reusePortion);
      this.evaluations = builder.maxEvaluations;
      this.terminationCondition = builder.terminationCondition;
   }

   @Override
   public SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
         final int solutionLength, final int populationSize, final List<TransformationSolution> reinitSolutions) {

      return search.performSearch(graph, algorithmName, evaluations, terminationCondition, solutionLength,
            populationSize, reinitSolutions);
   }

   @Override
   public String toString() {
      return "EvaluationReplanningStrategy-" + evaluations + "reusePortion=" + reusePortion;
   }

}
