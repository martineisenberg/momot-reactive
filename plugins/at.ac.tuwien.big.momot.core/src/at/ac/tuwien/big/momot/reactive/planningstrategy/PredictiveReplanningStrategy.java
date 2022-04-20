// package at.ac.tuwien.big.momot.reactive.planningstrategy;
//
// import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
// import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
// import at.ac.tuwien.big.momot.reactive.result.SearchResult;
//
// import java.util.List;
//
// import org.eclipse.emf.henshin.interpreter.EGraph;
//
// public class PredictivePlanningStrategy extends SearchReplanningStrategy {
//
// private final EvaluationReplanningStrategy erStrategy;
// private final ConditionReplanningStrategy crStrategy;
// private final List<Integer> replanAfterAdditionalSteps;
//
// private PredictivePlanningStrategy(final String algorithm, final List<Integer> replanAfterSteps,
// final EvaluationReplanningStrategy erStrategy, final ConditionReplanningStrategy crStrategy) {
// super(RepairStrategy.REPLAN_PREDICTIVE, algorithm, false, 0.0f);
// this.crStrategy = crStrategy;
// this.erStrategy = erStrategy;
// this.replanAfterAdditionalSteps = replanAfterSteps;
//
// }
//
// public PredictivePlanningStrategy create(final String algorithm, final List<Integer> replanAfterSteps,
// final ConditionReplanningStrategy crStrategy) {
// return new PredictivePlanningStrategy(algorithm, replanAfterSteps, null, crStrategy);
// }
//
// public PredictivePlanningStrategy create(final String algorithm, final List<Integer> replanAfterSteps,
// final EvaluationReplanningStrategy erStrategy) {
// return new PredictivePlanningStrategy(algorithm, replanAfterSteps, erStrategy, null);
// }
//
// public ConditionReplanningStrategy getConditionReplanningStrategy() {
// return this.crStrategy;
// }
//
// // public SearchResult replan(final Executor e, final ModelRuntimeEnvironment mre,
// // final List<ITransformationVariable> remainingPlan, final IReactiveSearch search, final String algorithmName,
// // final String experimentName, final int run, final int solutionLength, final int populationSize,
// // final List<ITransformationVariable> reinitSeed, final float reinitPortion, final double reinitBestObj,
// // final boolean recordBestObjective) {
// //
// // e.setModelRuntimeEnvironment(mre);
// //
// // final Iterator<Integer> simulateStepsIterator = replanAfterAdditionalSteps.iterator();
// // final Iterator<ITransformationVariable> planIterator = remainingPlan.iterator();
// //
// // while(simulateStepsIterator.hasNext()) {
// // final int afterSteps = simulateStepsIterator.next();
// // while(planIterator.hasNext() && mre.getExecutedUnits().size() < afterSteps) {
// // final ITransformationVariable var = planIterator.next();
// // final boolean success = e.execute(var);
// // if(success) {
// // mre.addExecutedUnit(var);
// // }
// // }
// //
// // if(afterSteps == mre.getExecutedUnits().size()) {
// // search.performSearch(mre.getGraph(), algorithmName, experimentName, run,
// // this.erStrategy != null ? this.erStrategy.getEvaluations() : 0,
// // this.crStrategy != null ? this.crStrategy.getTerminationCondition() : null, solutionLength,
// // populationSize, reinitSeed, reinitBestObj, reinitPortion, recordBestObjective);
// // }
// // }
// //
// // }
//
// public List<Integer> getListReplanAfterAdditionalSteps() {
// return this.replanAfterAdditionalSteps;
// }
//
// @Override
// public SearchResult replan(final IReactiveSearch search, final EGraph graph, final String algorithmName,
// final String experimentName, final int run, final int solutionLength, final int populationSize,
// final List<ITransformationVariable> reinitSeed, final float reinitPortion, final double reinitBestObj,
// final boolean recordBestObjective) {
//
// return search.performSearch(graph, algorithmName, experimentName, run,
// this.erStrategy != null ? this.erStrategy.getEvaluations() : 0,
// this.crStrategy != null ? this.crStrategy.getTerminationCondition() : null, solutionLength, populationSize,
// reinitSeed, reinitBestObj, reinitPortion, recordBestObjective);
// }
//
// @Override
// public String toString() {
// return "PredictivePlanningStrategy-" + erStrategy.toString() + crStrategy
// + (this.doReusePreviousPlan ? "reusePortion=" + reusePortion : "");
// }
//
// }
