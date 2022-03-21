package at.ac.tuwien.big.moea.search.algorithm.reinforcement.algorithms;

import at.ac.tuwien.big.moea.search.algorithm.reinforcement.AbstractMOTabularRLAgent;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.ApplicationState;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.IParetoQTableAccessor;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.ParetoQState;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.DoneStatus;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.IMOEnvironment;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.MOEnvResponse;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.utils.EvaluationStrategy;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.utils.LocalSearchStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class ParetoQLearning<S extends Solution> extends AbstractMOTabularRLAgent<S> {

   private final IParetoQTableAccessor<List<ApplicationState>, List<ApplicationState>> qTable;
   private final Map<List<ApplicationState>, NondominatedPopulation> qStateNDP;

   private final double gamma; // Eagerness - 0 looks in the near future, 1 looks in the distant future
   private final EvaluationStrategy strategy; // Eagerness - 0 looks in the near future, 1 looks in the distant future

   private double eps;
   private final double epsDecay;
   private final double epsMinimum;
   private final boolean withEpsDecay;
   private final int exploreSteps;
   private final LocalSearchStrategy localSearchStrategy;

   public ParetoQLearning(final LocalSearchStrategy localSearchStrategy, final int exploreSteps, final double gamma,
         final EvaluationStrategy strategy, final double eps, final boolean withEpsDecay, final double epsDecay,
         final double epsMinimum, final Problem problem, final IMOEnvironment<S> environment, final String savePath,
         final int recordInterval, final int terminateAfterEpisodes,
         final IParetoQTableAccessor<List<ApplicationState>, List<ApplicationState>> qTableIn, final String qTableOut,
         final boolean verbose) {
      super(problem, environment, savePath, recordInterval, terminateAfterEpisodes, qTableOut, verbose);

      this.qStateNDP = new HashMap<>();
      this.gamma = gamma;
      this.eps = eps;
      this.epsDecay = epsDecay;
      this.epsMinimum = epsMinimum;
      this.withEpsDecay = withEpsDecay;
      this.strategy = strategy;
      startTime = System.currentTimeMillis();
      this.exploreSteps = exploreSteps;
      this.localSearchStrategy = localSearchStrategy;

      if(this.qTableIn != null) {
         this.qTable = qTableIn;
      } else {
         this.qTable = this.utils.initParetoQTable(environment.getUnitMapping());
         this.qTable.addStateIfNotExists(new ArrayList<>());
         this.qStateNDP.put(new ArrayList<>(), new NondominatedPopulation());

      }
   }

   @Override
   public List<ApplicationState> epsGreedyDecision() {

      List<ApplicationState> nextAction = null;

      // Strategies
      if(rng.nextDouble() >= this.eps) {
         final NondominatedPopulation stateQNDP = qStateNDP.get(utils.getApplicationStates(currentSolution));

         if(stateQNDP.isEmpty()) {
            return null;
         } else if(stateQNDP.size() == 1) {
            qTable.getActionForCurrentState(stateQNDP, this.utils.getApplicationStates(currentSolution));

         }

         nextAction = this.qTable.getMaxRewardAction(this.strategy, utils.getApplicationStates(currentSolution),
               this.problem, stateQNDP);

      } else if(withEpsDecay && eps >= epsMinimum)

      { // nextAction = null => explore and decrease eps if above threshold
         eps -= epsDecay;
      }

      return nextAction;
   }

   // private List<Solution> determineQSet(final List<Assignment> state, final List<Assignment> action) {
   // final Collection<Solution> syncList = Collections.synchronizedCollection(new ArrayList<>());
   //
   // final ParetoQState pQState = qTable.get(utils.getAssignments(currentSolution)).get(action);
   //
   // final List<Solution> objList = new ArrayList<>();
   // pQState.ndObjectives.forEach(o -> objList.add(o));
   //
   // objList.parallelStream().forEach(s -> {
   // final INDArray sObjectives = Nd4j.create(s.getObjectives());
   // syncList.add(new Solution(pQState.immediateR.add(sObjectives).toDoubleVector()));
   // });
   //
   // // final NondominatedPopulation qSets = new NondominatedPopulation(syncList);
   //
   // // for(final Solution s : pQState.ndObjectives) {
   // // final INDArray sObjectives = Nd4j.create(s.getObjectives());
   // // qSets.add(new Solution(pQState.immediateR.add(sObjectives).toDoubleVector()));
   // // }
   // return new ArrayList<>(syncList);
   // }

   // private NondominatedPopulation determineQSets(final List<Assignment> state) {
   //
   // final Collection<Solution> syncList = Collections.synchronizedCollection(new ArrayList<>());
   //
   // final Map<List<Assignment>, ParetoQLearning<S>.ParetoQState> actiontoQST = qTable
   // .get(utils.getAssignments(currentSolution));
   //
   // actiontoQST.entrySet().parallelStream().forEach(e -> {
   // final ParetoQState curQST = e.getValue();
   // final INDArray immR = curQST.immediateR;
   // if(curQST.ndObjectives.size() > 0) {
   // for(final Solution s : curQST.ndObjectives) {
   // final INDArray sObjectives = Nd4j.create(s.getObjectives());
   // // qSets.add(new Solution(immR.add(sObjectives.mul(gamma)).toDoubleVector()));
   // syncList.add(new Solution(immR.add(sObjectives.mul(gamma)).toDoubleVector()));
   // }
   // } else {
   // // qSets.add(new Solution(immR.toDoubleVector()));
   // syncList.add(new Solution(immR.toDoubleVector()));
   //
   // }
   //
   // });
   // final NondominatedPopulation qSets = new NondominatedPopulation(syncList);
   //
   // // for(final Entry<List<Assignment>, ParetoQLearning<S>.ParetoQState> entry : actiontoQST.entrySet()) {
   // // final ParetoQState curQST = entry.getValue();
   // // final INDArray immR = curQST.immediateR;
   // // if(curQST.ndObjectives.size() > 0) {
   // // for(final Solution s : curQST.ndObjectives) {
   // // final INDArray sObjectives = Nd4j.create(s.getObjectives());
   // // qSets.add(new Solution(immR.add(sObjectives.mul(gamma)).toDoubleVector()));
   // // }
   // // } else {
   // // qSets.add(new Solution(immR.toDoubleVector()));
   // // }
   // //
   // // }
   // return qSets;
   // // final NondominatedPopulation ndp = new NondominatedPopulation();
   // // final Map<List<Assignment>, ParetoQState> stateNonDomSetMap = qTable.get(nextState);
   // // for(final Map.Entry<List<Assignment>, ParetoQState> entry : stateNonDomSetMap.entrySet()) {
   // // final INDArray curR = entry.getValue().immediateR;
   // // for(final Solution s : entry.getValue().ndPop) {
   // // final double[] lReward = curR.add(Nd4j.create(s.getObjectives()).mul(gamma)).toDoubleVector();
   // // ndp.add(new Solution(lReward));
   // // }
   // // }
   // // return ndp;
   // }

   @Override
   protected void iterate() {
      if(this.startTime == 0) {
         this.startTime = System.currentTimeMillis();
      }

      epochSteps++;
      final List<ApplicationState> nextAction = epsGreedyDecision();

      MOEnvResponse<S> response = null;

      response = (MOEnvResponse<S>) environment.step(localSearchStrategy, nextAction, exploreSteps);

      final double[] rewards = response.getRewards();
      final S nextState = response.getState();
      final DoneStatus doneStatus = response.getDoneStatus();

      if(doneStatus != DoneStatus.FINAL_STATE_REACHED) {
         updateQSets(utils.getApplicationStates(currentSolution),
               utils.getApplicationStatesDiff(currentSolution, nextState), utils.getApplicationStates(nextState),
               rewards);

         addSolutionIfImprovement(nextState);

         iterations++;

         printIfVerboseMode("Iteration: " + iterations);

      }

      // if(iterations % 1000 == 0) {
      // System.out.println(iterations);
      // }

      if(doneStatus == DoneStatus.MAX_LENGTH_REACHED || doneStatus == DoneStatus.FINAL_STATE_REACHED) {

         if(this.recordInterval > 0) {

            framesList.add((double) iterations);
            timePassedList.add((double) (System.currentTimeMillis() - startTime));

            if(savePath != null && epochCount > 0 && epochCount % this.recordInterval == 0) {
               saveRewards(savePath, framesList, this.environment.getFunctionNames(), rewardEarnedLists, timePassedList,
                     meanRewardEarnedLists);
            }

         }

         epochCount++;
         epochSteps = 0;
         currentSolution = environment.reset();
      } else {
         currentSolution = nextState;
      }

   }

   // private final Map<List<Assignment>, Map<List<Assignment>, ParetoQState>> qTable;

   private void updateQSets(final List<ApplicationState> state, final List<ApplicationState> action,
         final List<ApplicationState> nextState, final double[] rewards) {

      // update z* if necessary, max. function values are returned with *-1 (to maximize, here minimize)
      // for(int i = 0; i < z.length; i++) {
      // if(z[i] < rewards[i] * -1) {
      // final double oldZ = z[i];
      // z[i] = rewards[i] * -1;
      // // System.out.format("Obj. %d: %f -> %f\n", i, oldZ, z[i]);
      // }
      // }

      final NondominatedPopulation sPool = qStateNDP.get(state);
      ParetoQState pQState = qTable.getParetoQState(state, action);

      if(pQState == null) {
         pQState = new ParetoQState(problem.getNumberOfObjectives());
      }

      final boolean hasAdded = this.qTable.addStateIfNotExists(nextState);
      if(hasAdded) {
         this.qStateNDP.put(nextState, new NondominatedPopulation());
      }

      // final NondominatedPopulation qSets = determineQSets(nextState);

      // Determine old action set solutions, remove from state pool

      sPool.removeAll(pQState.getAdvantages());
      pQState.getAdvantages().clear();

      pQState.setNDObjectives(qStateNDP.get(nextState));

      final INDArray rewardsArr = Nd4j.create(rewards);

      pQState.setImmediateR(
            pQState.getImmediateR().add(rewardsArr.sub(pQState.getImmediateR()).div(pQState.incUpdates())));

      for(final Solution s : pQState.getNdObjectives()) {
         final INDArray sObjectives = Nd4j.create(s.getObjectives());
         // qSets.add(new Solution(immR.add(sObjectives.mul(gamma)).toDoubleVector()));
         pQState.getAdvantages()
               .add(new Solution(pQState.getImmediateR().add(sObjectives.mul(gamma)).toDoubleVector()));
      }

      if(pQState.getNdObjectives().size() == 0) {
         pQState.getAdvantages().add(new Solution(pQState.getImmediateR().toDoubleVector()));
      }

      sPool.addAll(pQState.getAdvantages());

      // qTable.get(state).put(action, pQState);
      qTable.update(state, action, pQState);
   }

}
