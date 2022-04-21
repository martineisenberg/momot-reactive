package at.ac.tuwien.big.momot.reactive.result;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;

import java.util.ArrayList;
import java.util.List;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

public class SolutionAnalyzer {

   // public List<ITransformationVariable> getOptimalPlanForObjective(final Map<Integer, Double> objectiveThresholds,
   // final int optimalObjIndex) {
   //
   // final TransformationSolution optimalSolution = getOptimalSolution(objectiveThresholds, optimalObjIndex);
   //
   // return optimalSolution.getVariablesAsList();
   // }

   // private TransformationSolution getOptimalSolution(final Map<Integer, Double> objectiveThresholds,
   // final int optimalObjIndex) {
   // final List<Solution> fitSolutions = new ArrayList<>();
   // this.solutions.forEach(s -> {
   // if(satisfiesCriteria(s, objectiveThresholds)) {
   // fitSolutions.add(s);
   // }
   // });
   //
   // final TransformationSolution optimalSolution = (TransformationSolution) fitSolutions.stream()
   // .min((s1, s2) -> s1.getObjective(optimalObjIndex) > s2.getObjective(optimalObjIndex) ? 1 : -1).get();
   //
   // return optimalSolution;
   // }

   public static TransformationSolution getOptimalSolution(final Population p,
         final int selectSolutionByObjectiveIndex) {

      final List<Solution> solutions = new ArrayList<>();
      p.forEach(s -> {
         solutions.add(s);

      });

      final TransformationSolution optimalSolution = (TransformationSolution) solutions.stream().min((s1,
            s2) -> s1.getObjective(selectSolutionByObjectiveIndex) > s2.getObjective(selectSolutionByObjectiveIndex) ? 1
                  : -1)
            .get();

      return optimalSolution;
   }

   // public TransformationSolution getOptimalSolutionForObjective(final Map<Integer, Double> objectiveThresholds,
   // final int optimalObjIndex) {
   //
   // final TransformationSolution optimalSolution = getOptimalSolution(objectiveThresholds, optimalObjIndex);
   //
   // return optimalSolution;
   // }
   //
   // public Population getPopulation() {
   // return this.solutions;
   // }
   //
   // private boolean satisfiesCriteria(final Solution s, final Map<Integer, Double> objectiveThresholds) {
   // final double[] o = s.getObjectives();
   // for(final Entry<Integer, Double> e : objectiveThresholds.entrySet()) {
   //
   // if(o[e.getKey()] >= e.getValue()) {
   // return false;
   // }
   // }
   // return true;
   // }
}
