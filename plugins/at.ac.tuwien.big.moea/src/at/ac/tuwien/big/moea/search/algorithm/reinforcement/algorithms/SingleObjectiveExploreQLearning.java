// package at.ac.tuwien.big.moea.search.algorithm.reinforcement.algorithms;
//
// import at.ac.tuwien.big.moea.search.algorithm.reinforcement.AbstractSOValueBasedReinforcementAlgorithm;
// import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.ApplicationState;
// import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.DoneStatus;
// import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.ISOEnvironment;
// import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.SOEnvResponse;
// import at.ac.tuwien.big.moea.search.algorithm.reinforcement.utils.LocalSearchStrategy;
//
// import java.util.List;
//
// import org.moeaframework.core.Problem;
// import org.moeaframework.core.Solution;
//
// public class SingleObjectiveExploreQLearning<S extends Solution> extends
// AbstractSOValueBasedReinforcementAlgorithm<S> {
//
// private final double gamma;
// private double eps;
// private final double epsDecay;
// private final double epsMinimum;
// private final boolean withEpsDecay;
// private final long startTime;
//
// private final int explorationSteps;
//
// public SingleObjectiveExploreQLearning(final int explorationSteps, final double gamma, final double eps,
// final boolean withEpsDecay, final double epsDecay, final double epsMinimum, final Problem problem,
// final ISOEnvironment<S> environment, final String savePath, final int recordInterval,
// final int terminateAfterEpisodes, final String qTableIn, final String qTableOut, final boolean verbose) {
// super(problem, environment, savePath, recordInterval, terminateAfterEpisodes, qTableIn, qTableOut, verbose);
//
// this.gamma = gamma;
// this.eps = eps;
// this.epsDecay = epsDecay;
// this.epsMinimum = epsMinimum;
// this.explorationSteps = explorationSteps;
// this.withEpsDecay = withEpsDecay;
// startTime = System.currentTimeMillis();
//
// java.lang.reflect.Method evaluateFunc = null;
// try {
// evaluateFunc = this.getClass().getMethod("evaluate", Solution.class);
// } catch(NoSuchMethodException | SecurityException e) {
// e.printStackTrace();
// }
//
// this.environment.setEvaluationMethod(this, evaluateFunc);
// }
//
// @Override
// public List<ApplicationState> epsGreedyDecision() {
//
// List<ApplicationState> nextAction = null;
//
// if(rng.nextDouble() >= this.eps) {
// // Pick best transformation (max. benefit) for current state
// nextAction = this.qTable.getMaxRewardAction(utils.getApplicationStates(currentSolution));
// } else if(withEpsDecay && eps >= epsMinimum) { // nextAction = null => explore and decrease eps if above threshold
// eps -= epsDecay;
// }
// return nextAction;
// }
//
// @Override
// protected void iterate() {
// epochSteps++;
//
// final List<ApplicationState> nextAction = epsGreedyDecision();
//
// final SOEnvResponse<S> response = (SOEnvResponse<S>) environment.step(LocalSearchStrategy.GREEDY,
// nextAction, this.explorationSteps);
//
// final DoneStatus doneStatus = response.getDoneStatus();
// final double reward = response.getReward();
// final S nextState = response.getState();
//
// if(doneStatus != DoneStatus.FINAL_STATE_REACHED) {
// cumReward += reward;
// updateQ(utils.getApplicationStates(currentSolution),
// utils.getApplicationStatesDiff(currentSolution, nextState), utils.getApplicationStates(nextState),
// reward);
//
// addSolutionIfImprovement(nextState);
//
// iterations++;
// if(verbose) {
// System.out.println("Iteration: " + iterations);
// }
// }
//
// if(doneStatus == DoneStatus.MAX_LENGTH_REACHED || doneStatus == DoneStatus.FINAL_STATE_REACHED) {
//
// if(this.recordInterval > 0) {
// rewardEarned.add(cumReward);
// framesList.add((double) iterations);
// timePassedList.add((double) (System.currentTimeMillis() - startTime));
// meanRewardEarned.add(cumReward / epochSteps);
//
// if(epochCount % this.recordInterval == 0) {
// saveRewards(savePath, framesList, rewardEarned, timePassedList, meanRewardEarned, epochCount);
// }
// }
//
// epochCount++;
// cumReward = 0;
// epochSteps = 0;
// currentSolution = environment.reset();
// } else {
// currentSolution = nextState;
// }
//
// }
//
// private void updateQ(final List<ApplicationState> state, final List<ApplicationState> action,
// final List<ApplicationState> nextState, final double reward) {
//
// final double transitionReward = this.qTable.getTransitionReward(state, action);
//
// this.qTable.addStateIfNotExists(nextState);
//
// final double qUpdateValue = transitionReward
// + (reward + gamma * this.qTable.getMaxRewardValue(nextState) - transitionReward);
//
// this.qTable.update(state, action, qUpdateValue);
// }
//
// }
