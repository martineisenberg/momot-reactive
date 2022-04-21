// package at.ac.tuwien.big.momot.reactive.planningstrategy;
//
// import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
// import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
// import at.ac.tuwien.big.momot.reactive.result.SearchResult;
// import at.ac.tuwien.big.momot.search.criterion.ThresholdCondition;
//
// import java.util.List;
//
// import org.eclipse.emf.henshin.interpreter.EGraph;
//
// public class ConditionReplanningStrategy extends SearchReplanningStrategy {
//
// public static ConditionReplanningStrategy create(final String algorithm,
// final ThresholdCondition terminationCondition) {
// return new ConditionReplanningStrategy(algorithm, terminationCondition);
// }
//
// private ThresholdCondition terminationCondition;
//
// protected ConditionReplanningStrategy(final String algorithm, final ThresholdCondition terminationCondition) {
// super(RepairStrategy.REPLAN_FOR_CONDITION, algorithm, null);
// this.terminationCondition = terminationCondition;
// }
//
// // protected ConditionReplanningStrategy(final String algorithm, final TerminationCondition terminationCondition,
// // final Heuristic h) {
// // super(RepairStrategy.REPLAN_FOR_CONDITION, algorithm, h);
// // this.terminationCondition = terminationCondition;
// // }
//
// public ThresholdCondition getTerminationCondition() {
// return this.terminationCondition;
// }
//
// @Override
// public SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
// final String experimentName, final int run, final int solutionLength, final int populationSize,
// final List<List<ITransformationVariable>> reinitSeed, final double reinitBestObj,
// final boolean recordBestObjective) {
//
// return search.performSearch(graph, algorithmName, experimentName, run, 0, terminationCondition, solutionLength,
// populationSize, reinitSeed, reinitBestObj, recordBestObjective);
//
// }
//
// public void setTerminationCondition(final ThresholdCondition condition) {
// this.terminationCondition = condition;
// }
//
// @Override
// public String toString() {
// return "ConditionReplanningStrategy-" + terminationCondition.toString()
// + (this.reusePortion > 0 ? "reusePortion=" + reusePortion : "");
// }
//
// }
