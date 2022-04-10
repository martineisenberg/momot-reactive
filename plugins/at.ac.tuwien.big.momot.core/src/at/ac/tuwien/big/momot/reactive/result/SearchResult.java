package at.ac.tuwien.big.momot.reactive.result;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

public class SearchResult {

   private final Population solutions;
   private final double executionTime;
   private final int executionEvaluations;

   public SearchResult(final Population solutions, final double executionTime, final int executionEvaluations) {
      this.solutions = solutions;

      this.executionTime = executionTime;
      this.executionEvaluations = executionEvaluations;
   }

   public int getExecutionEvaluations() {
      return this.executionEvaluations;
   }

   public double getExecutionTime() {
      return this.executionTime;
   }

   public List<ITransformationVariable> getOptimalPlanForObjective(final Map<Integer, Double> objectiveThresholds,
         final int optimalObjIndex) {

      final TransformationSolution optimalSolution = getOptimalSolution(objectiveThresholds, optimalObjIndex);

      return optimalSolution.getVariablesAsList();
   }

   private TransformationSolution getOptimalSolution(final Map<Integer, Double> objectiveThresholds,
         final int optimalObjIndex) {
      final List<Solution> fitSolutions = new ArrayList<>();
      this.solutions.forEach(s -> {
         if(satisfiesCriteria(s, objectiveThresholds)) {
            fitSolutions.add(s);
         }
      });

      final TransformationSolution optimalSolution = (TransformationSolution) fitSolutions.stream()
            .min((s1, s2) -> s1.getObjective(optimalObjIndex) > s2.getObjective(optimalObjIndex) ? 1 : -1).get();

      return optimalSolution;
   }

   public TransformationSolution getOptimalSolutionForObjective(final int optimalObjIndex) {

      final List<Solution> fitSolutions = new ArrayList<>();
      this.solutions.forEach(s -> {
         fitSolutions.add(s);
      });

      final TransformationSolution optimalSolution = (TransformationSolution) fitSolutions.stream()
            .min((s1, s2) -> s1.getObjective(optimalObjIndex) > s2.getObjective(optimalObjIndex) ? 1 : -1).get();

      return optimalSolution;
   }

   public TransformationSolution getOptimalSolutionForObjective(final Map<Integer, Double> objectiveThresholds,
         final int optimalObjIndex) {

      final TransformationSolution optimalSolution = getOptimalSolution(objectiveThresholds, optimalObjIndex);

      return optimalSolution;
   }

   public Population getPopulation() {
      return this.solutions;
   }

   private boolean satisfiesCriteria(final Solution s, final Map<Integer, Double> objectiveThresholds) {
      final double[] o = s.getObjectives();
      for(final Entry<Integer, Double> e : objectiveThresholds.entrySet()) {

         if(o[e.getKey()] >= e.getValue()) {
            return false;
         }
      }
      return true;
   }
}
