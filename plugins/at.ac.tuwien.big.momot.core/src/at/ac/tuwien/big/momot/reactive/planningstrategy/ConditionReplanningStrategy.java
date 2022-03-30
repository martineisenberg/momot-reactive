package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.TerminationCondition;

public class ConditionReplanningStrategy extends SearchReplanningStrategy {

   public static ConditionReplanningStrategy create(final String algorithm,
         final TerminationCondition terminationCondition) {
      return new ConditionReplanningStrategy(algorithm, terminationCondition);
   }

   private final TerminationCondition terminationCondition;

   protected ConditionReplanningStrategy(final String algorithm, final TerminationCondition terminationCondition) {
      super(RepairStrategy.REPLAN_FOR_CONDITION, algorithm, false, 0.0f);
      this.terminationCondition = terminationCondition;
   }

   public TerminationCondition getTerminationCondition() {
      return this.terminationCondition;
   }

   @Override
   public SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
         final String experimentName, final int run, final int solutionLength, final int populationSize,
         final List<ITransformationVariable> reinitSeed, final float reinitPortion, final double reinitBestObj,
         final boolean recordBestObjective) {

      return search.performSearch(graph, algorithmName, experimentName, run, 0, terminationCondition, solutionLength,
            populationSize, reinitSeed, reinitBestObj, reinitPortion, recordBestObjective);

   }

   @Override
   public String toString() {
      return "ConditionReplanningStrategy-" + terminationCondition.toString()
            + (this.doReusePreviousPlan ? "reusePortion=" + reusePortion : "");
   }

}
