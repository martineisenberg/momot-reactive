package at.ac.tuwien.big.moea.search.algorithm.reinforcement;

import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.ApplicationState;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.IEnvironment;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.utils.FileManager;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.utils.IRLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.moeaframework.algorithm.AbstractAlgorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;

public abstract class AbstractTabularRLAgent<S extends Solution> extends AbstractAlgorithm {

   protected final String savePath;
   protected long startTime;
   protected final List<Double> framesList;
   protected final List<Double> timePassedList;
   protected S currentSolution;
   protected int iterations = 0;
   protected int epochCount = 0;
   protected IRLUtils<S> utils;
   protected int recordInterval;
   protected int epochSteps;
   protected int terminateAfterEpisodes;
   protected String qTableIn;
   protected String qTableOut;
   protected Random rng = null;
   protected boolean verbose;
   protected NondominatedPopulation population;

   protected AbstractTabularRLAgent(final Problem problem, final IEnvironment<S> environment, final String savePath,
         final int recordInterval, final int terminateAfterEpisodes, final String qTableOut, final boolean verbose) {
      super(problem);

      this.terminateAfterEpisodes = terminateAfterEpisodes;

      java.lang.reflect.Method evaluateFunc = null;
      try {
         evaluateFunc = this.getClass().getMethod("evaluate", Solution.class);
      } catch(NoSuchMethodException | SecurityException e) {
         e.printStackTrace();
      }

      environment.setEvaluationMethod(this, evaluateFunc);

      this.framesList = new ArrayList<>();
      this.timePassedList = new ArrayList<>();

      this.verbose = verbose;

      this.rng = new Random();
      this.savePath = savePath;
      this.recordInterval = recordInterval;

      this.population = new NondominatedPopulation();

      this.startTime = 0;

      FileManager.createDirsIfNonNullAndNotExists(savePath);

      this.currentSolution = environment.reset();
      evaluate(this.currentSolution);

      this.population.add(this.currentSolution);

      this.utils = environment.getRLUtils();

      this.qTableOut = qTableOut;

   }

   public void addSolutionIfImprovement(final Solution s) {
      if(!isDominatedByAnySolutionInParetoFront(s)) {
         this.population.add(s);
      }
   }

   public abstract List<ApplicationState> epsGreedyDecision();

   public boolean isDominatedByAnySolutionInParetoFront(final Solution s) {
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
}
