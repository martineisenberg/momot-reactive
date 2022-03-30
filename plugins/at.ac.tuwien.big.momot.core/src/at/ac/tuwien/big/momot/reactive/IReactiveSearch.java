package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.TerminationCondition;

public interface IReactiveSearch {

   public SearchResult performSearch(final EGraph graph, final String algorithmName, String experimentName, int run,
         final int evaluations, final TerminationCondition terminationCondition, final int solutionLength,
         final int populationSize, final List<ITransformationVariable> reinitSeed, final double reinistBestObj,
         float reinitPortion, boolean recordBestObjective);

}
