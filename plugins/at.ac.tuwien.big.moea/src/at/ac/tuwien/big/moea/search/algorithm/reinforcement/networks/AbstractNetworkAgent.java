package at.ac.tuwien.big.moea.search.algorithm.reinforcement.networks;

import at.ac.tuwien.big.moea.search.algorithm.reinforcement.domain.IEncodingStrategy;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.ISOEnvironment;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.utils.FileManager;

import java.util.ArrayList;
import java.util.List;

import org.moeaframework.algorithm.AbstractAlgorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;

public abstract class AbstractNetworkAgent<S extends Solution> extends AbstractAlgorithm {

   protected int nrOfEpochs = 0;
   protected final int terminateAfterEpisodes;

   int framecount;
   protected final ArrayList<Double> rewardEarned;
   protected final ArrayList<Double> framesList;
   protected final ArrayList<Double> timePassedList;
   protected final ArrayList<Double> meanRewardEarned;

   protected long startTime;
   protected ISOEnvironment<S> environment;
   protected NondominatedPopulation population;
   protected int epochsPerModelSave;

   protected IEncodingStrategy<S> encoder;

   protected final int maxSolutionLength;
   protected final boolean verbose;

   protected final String scoreSavePath;

   public AbstractNetworkAgent(final Problem problem, final ISOEnvironment<S> environment, final String scoreSavePath,
         final int terminateAfterEpisodes, final int epochsPerModelSave, final boolean verbose) {
      super(problem);

      java.lang.reflect.Method evaluateFunc = null;
      try {
         evaluateFunc = this.getClass().getMethod("evaluate", Solution.class);
      } catch(NoSuchMethodException | SecurityException e) {
         e.printStackTrace();
      }

      environment.setEvaluationMethod(this, evaluateFunc);

      FileManager.createDirsIfNonNullAndNotExists(scoreSavePath);

      this.population = new NondominatedPopulation();

      this.encoder = environment.getProblemEncoder();

      this.epochsPerModelSave = epochsPerModelSave;

      this.environment = environment;
      this.rewardEarned = new ArrayList<>();
      this.framesList = new ArrayList<>();
      this.timePassedList = new ArrayList<>();
      this.meanRewardEarned = new ArrayList<>();
      this.verbose = verbose;

      this.scoreSavePath = scoreSavePath;
      this.framecount = 0;

      this.maxSolutionLength = problem.getNumberOfVariables();
      this.terminateAfterEpisodes = terminateAfterEpisodes;
   }

   protected void addSolutionIfImprovement(final Solution s) {
      if(!isDominatedByAnySolutionInParetoFront(s)) {
         this.population.add(s);
      }
   }

   @Override
   public NondominatedPopulation getResult() {
      return this.population;
   }

   protected boolean isDominatedByAnySolutionInParetoFront(final Solution s) {
      final DominanceComparator comparator = this.population.getComparator();
      for(int i = 0; i < this.population.size(); i++) {
         // if solution in pareto front dominates solution s, return true
         if(comparator.compare(s, this.population.get(i)) > 0) {
            return true;
         }
      }
      return false;
   }

   protected void printIfVerboseMode(final String str) {
      if(this.verbose) {
         System.out.println(str);
      }
   }

   public void saveRewards(final List<Double> framesList, final List<Double> rewardList,
         final List<Double> meanRewardList, final List<Double> timePassedList, final long ts) {
      final ArrayList<ArrayList<Double>> lll = new ArrayList<>();
      lll.add((ArrayList<Double>) framesList);
      lll.add((ArrayList<Double>) rewardList);
      lll.add((ArrayList<Double>) meanRewardList);
      lll.add((ArrayList<Double>) timePassedList);

      FileManager.saveBenchMark("evaluations;reward;averageReward;runtime in ms;", lll,
            scoreSavePath + "_" + FileManager.milliSecondsToFormattedDate(startTime) + ".csv");
   }
}
