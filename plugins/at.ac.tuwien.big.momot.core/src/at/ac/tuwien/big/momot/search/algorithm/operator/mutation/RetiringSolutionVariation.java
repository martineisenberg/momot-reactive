package at.ac.tuwien.big.momot.search.algorithm.operator.mutation;

import at.ac.tuwien.big.momot.search.solution.executor.SearchHelper;

import java.util.List;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;

public class RetiringSolutionVariation extends AbstractPopulationVariation {

   private final int afterGenerations;
   private final SearchHelper searchHelper;
   private final int replaceSolutions;
   private int invocations;

   public RetiringSolutionVariation(final SearchHelper searchHelper, final int afterGenerations,
         final int populationSize, final int replaceSolutions) {
      this.arity = replaceSolutions;
      this.afterGenerations = afterGenerations;
      this.searchHelper = searchHelper;
      this.replaceSolutions = replaceSolutions;
      this.invocations = 0;
   }

   @Override
   public Solution[] evolve(final Solution[] parents) {
      if(this.invocations > 0 && this.invocations % afterGenerations == 0) {
         final NondominatedPopulation ndp = new NondominatedPopulation(List.of(parents));

         int replaced = 0;
         PRNG.shuffle(parents);
         for(int i = 0; i < parents.length && replaced < this.replaceSolutions; i++) {
            if(!ndp.contains(parents[i])) {
               parents[i] = getSearchHelper().createRandomTransformationSolution();
               replaced++;
            }
         }
         System.out.println("Replaced " + replaced + " solutions!");

      }
      this.invocations++;
      return parents;
   }

   protected SearchHelper getSearchHelper() {
      return searchHelper;
   }

}
