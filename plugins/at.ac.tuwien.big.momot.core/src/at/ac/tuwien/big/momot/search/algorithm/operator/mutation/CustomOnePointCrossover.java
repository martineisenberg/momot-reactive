package at.ac.tuwien.big.momot.search.algorithm.operator.mutation;

import at.ac.tuwien.big.moea.search.algorithm.operator.crossover.AbstractParentalCrossoverVariation;
import at.ac.tuwien.big.momot.search.solution.executor.SearchHelper;
import at.ac.tuwien.big.momot.util.MomotUtil;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;

public class CustomOnePointCrossover extends AbstractParentalCrossoverVariation {

   private final SearchHelper searchHelper;
   private final boolean removeBoth;

   /**
    * Constructs a one-point crossover operator with the specified probability
    * of applying this operator to solutions.
    *
    * @param probability
    *           the probability of applying this operator to solutions
    */
   public CustomOnePointCrossover(final double probability, final SearchHelper searchHelper, final boolean removeBoth) {
      super(probability);
      this.searchHelper = searchHelper;
      this.removeBoth = removeBoth;
   }

   @Override
   public Solution[] doEvolve(final Solution[] parents) {
      Solution result1 = parents[0].copy();
      Solution result2 = parents[1].copy();

      // final List<Variable> v1 = new ArrayList<>();
      //
      // for(int i = 0; i < parents[0].getNumberOfVariables(); i++) {
      // v1.add(parents[0].getVariable(i));
      // }
      // Math.max(, probability)

      final float equality = MomotUtil.getOverlappingVarProportion(result1, result2);

      if(equality == 1) {
         System.out.println("Equ:" + equality);
         if(removeBoth) {
            result1 = getSearchHelper().createRandomTransformationSolution();
            result2 = getSearchHelper().createRandomTransformationSolution();
         } else {
            if(result1.getNumberOfVariables() > 1) {
               final int crossoverPoint = PRNG.nextInt(result1.getNumberOfVariables() - 1);

               for(int i = 0; i <= crossoverPoint; i++) {
                  result1.setVariable(i, result2.getVariable(i));
               }
            }
            result2 = getSearchHelper().createRandomTransformationSolution();
         }
      } else {

         if(PRNG.nextDouble() <= getProbability() && result1.getNumberOfVariables() > 1) {
            final int crossoverPoint = PRNG.nextInt(result1.getNumberOfVariables() - 1);

            for(int i = 0; i <= crossoverPoint; i++) {
               final Variable temp = result1.getVariable(i);
               result1.setVariable(i, result2.getVariable(i));
               result2.setVariable(i, temp);
            }
         }
      }

      return new Solution[] { result1, result2 };
   }

   protected SearchHelper getSearchHelper() {
      return searchHelper;
   }

}
