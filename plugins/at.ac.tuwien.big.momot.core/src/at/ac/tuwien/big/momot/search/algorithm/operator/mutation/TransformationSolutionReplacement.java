package at.ac.tuwien.big.momot.search.algorithm.operator.mutation;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.search.solution.executor.SearchHelper;

public class TransformationSolutionReplacement extends AbstractTransformationMutation {
   private final SearchHelper searchHelper;
   private int nrOfReplaced;

   public TransformationSolutionReplacement(final SearchHelper searchHelper) {
      this.searchHelper = searchHelper;
      this.nrOfReplaced = 0;
   }

   public TransformationSolutionReplacement(final SearchHelper searchHelper, final double probability) {
      super(probability);
      this.nrOfReplaced = 0;
      this.searchHelper = searchHelper;
   }

   protected SearchHelper getSearchHelper() {
      return searchHelper;
   }

   @Override
   protected TransformationSolution mutate(final TransformationSolution mutant) {
      System.out.println("Replaced " + (++nrOfReplaced));
      return getSearchHelper().createRandomTransformationSolution();
   }

}
