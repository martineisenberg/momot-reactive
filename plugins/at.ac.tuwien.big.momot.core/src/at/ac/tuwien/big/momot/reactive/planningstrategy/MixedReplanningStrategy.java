// package at.ac.tuwien.big.momot.reactive.planningstrategy;
//
// import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
// import at.ac.tuwien.big.momot.reactive.IReactiveSearchInstance;
// import at.ac.tuwien.big.momot.reactive.result.SearchResult;
//
// import java.util.List;
//
// import org.eclipse.emf.henshin.interpreter.EGraph;
//
// public class MixedReplanningStrategy extends SearchReplanningStrategy {
//
// private final EvaluationReplanningStrategy erStrategy;
// private final ConditionReplanningStrategy crStrategy;
//
// protected MixedReplanningStrategy(final String replanningAlgorithm, final EvaluationReplanningStrategy erStrategy,
// final ConditionReplanningStrategy crStrategy) {
// super(RepairStrategy.MIXED, replanningAlgorithm, null);
// this.crStrategy = crStrategy;
// this.erStrategy = erStrategy;
// }
//
// @Override
// public SearchResult replan(final IReactiveSearchInstance search, final EGraph graph, final String algorithmName,
// final String experimentName, final int run, final int solutionLength, final int populationSize,
// final List<List<ITransformationVariable>> reinitSeed, final double reinitBestObj,
// final boolean recordBestObjective) {
//
// return search.performSearch(graph, algorithmName, experimentName, run,
// this.erStrategy != null ? this.erStrategy.getEvaluations() : 0,
// this.crStrategy != null ? this.crStrategy.getTerminationCondition() : null, solutionLength, populationSize,
// reinitSeed, reinitBestObj, recordBestObjective);
// }
//
// @Override
// public String toString() {
// return "MixedReplanningStrategy-" + erStrategy.toString() + "_" + crStrategy.toString()
// + (this.reusePortion > 0 ? "reusePortion=" + reusePortion : "");
// }
//
// }
