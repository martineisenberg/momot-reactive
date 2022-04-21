package at.ac.tuwien.big.momot.reactive.result;

import org.moeaframework.core.Population;

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

   public Population getPopulation() {
      return this.solutions;
   }

}
