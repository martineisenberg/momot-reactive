package at.ac.tuwien.big.momot.reactive.result;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;

import java.util.List;

import org.moeaframework.core.Population;

public class SearchResult {
   private final List<ITransformationVariable> optimalPlan;
   private final TransformationSolution optimalSolution;
   private final Population solutions;

   public SearchResult(final Population solutions, final TransformationSolution optimalSolution,
         final List<ITransformationVariable> optimalPlan) {
      this.solutions = solutions;
      this.optimalSolution = optimalSolution;
      this.optimalPlan = optimalPlan;
   }

   public List<ITransformationVariable> getOptimalPlan() {
      return this.optimalPlan;
   }

   public TransformationSolution getOptimalSolution() {
      return this.optimalSolution;
   }
}
