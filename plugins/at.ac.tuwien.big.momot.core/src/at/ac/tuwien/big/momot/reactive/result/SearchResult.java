package at.ac.tuwien.big.momot.reactive.result;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;

import java.util.List;

import org.moeaframework.core.Population;

public class SearchResult {
   private final List<ITransformationVariable> optimalPlan;
   private final TransformationSolution optimalSolution;
   private final Population solutions;
   private final double executionTime;
   private final int executionEvaluations;

   public SearchResult(final Population solutions, final TransformationSolution optimalSolution,
         final List<ITransformationVariable> optimalPlan, final double executionTime, final int executionEvaluations) {
      this.solutions = solutions;
      this.optimalSolution = optimalSolution;
      this.optimalPlan = optimalPlan;
      this.executionTime = executionTime;
      this.executionEvaluations = executionEvaluations;
   }

   public int getExecutionEvaluations() {
      return this.executionEvaluations;
   }

   public double getExecutionTime() {
      return this.executionTime;
   }

   public List<ITransformationVariable> getOptimalPlan() {
      return this.optimalPlan;
   }

   public TransformationSolution getOptimalSolution() {
      return this.optimalSolution;
   }
}
