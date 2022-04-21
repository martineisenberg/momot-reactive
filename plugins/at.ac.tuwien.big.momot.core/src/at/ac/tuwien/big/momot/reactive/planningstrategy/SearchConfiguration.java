package at.ac.tuwien.big.momot.reactive.planningstrategy;

import at.ac.tuwien.big.momot.domain.Heuristic;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.Executor;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.TerminationCondition;

public class SearchConfiguration {
   private static Executor executor;

   public static Executor getExecutor() {
      return executor;
   }

   public static void setExecutor(final Executor e) {
      SearchConfiguration.executor = e.copy();
   }

   private final EGraph startingState;
   private final String algorithmName;
   private final int maxEvaluations;
   private TerminationCondition terminationCondition;
   private final int solutionLength;
   private final int populationSize;
   private double bestObjSoFarVal;
   private int bestObjSoFarIndex;

   private final List<List<ITransformationVariable>> initialPopulation;

   public SearchConfiguration(final EGraph startingState, final PlanningStrategy ps, final int solutionLength,
         final int populationSize) {
      this.startingState = MomotUtil.copy(startingState);
      this.algorithmName = ps.getInitialSearchAlgorithm();
      this.maxEvaluations = ps.getInitialSearchEvaluations();
      this.terminationCondition = ps.getTerminationCriterion();
      this.solutionLength = solutionLength;
      this.populationSize = populationSize;

      this.initialPopulation = new ArrayList<>();
      if(ps.getHeuristic() != null && ps.getHeuristicPortion() > 0) {
         this.initPopulationWithHeuristic(ps.getHeuristic(), ps.getHeuristicPortion());
      }

   }

   public SearchConfiguration(final EGraph state, final PlanningStrategy ps, final int solutionLength,
         final int populationSize, final double bestObjSoFarVal, final int bestObjSoFarIndex) {
      this(state, ps, solutionLength, populationSize);
      this.bestObjSoFarIndex = bestObjSoFarIndex;
      this.bestObjSoFarVal = bestObjSoFarVal;
   }

   public void addSeedToPopulation(final List<ITransformationVariable> seed, final double reseedingPortion) {
      this.initialPopulation.addAll(Stream.generate(() -> new ArrayList<>(seed))
            .limit((long) (this.populationSize * reseedingPortion)).collect(Collectors.toList()));
   }

   public String getAlgoritmName() {
      return algorithmName;
   }

   public int getBestObjSoFarIndex() {
      return bestObjSoFarIndex;
   }

   public double getBestObjSoFarVal() {
      return bestObjSoFarVal;
   }

   public List<List<ITransformationVariable>> getInitialPopulation() {
      return initialPopulation;
   }

   public int getMaxEvaluations() {
      return maxEvaluations;
   }

   public int getPopulationSize() {
      return populationSize;
   }

   public int getSolutionLength() {
      return solutionLength;
   }

   public EGraph getStartingState() {
      return startingState;
   }

   public TerminationCondition getTerminationCondition() {
      return terminationCondition;
   }

   private void initPopulationWithHeuristic(final Heuristic h, final double portion) {

      final List<ITransformationVariable> heuristicSeq = h.getInitialPopulationTs(this.startingState, executor,
            solutionLength);
      this.initialPopulation.addAll(Stream.generate(() -> new ArrayList<>(heuristicSeq))
            .limit((long) (portion * populationSize)).collect(Collectors.toList()));

   }

   public void setTerminationCondition(final TerminationCondition tc) {
      this.terminationCondition = tc;
   }

}
