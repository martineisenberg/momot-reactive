package at.ac.tuwien.big.momot.search.algorithm.operator.mutation;

import org.moeaframework.core.Variation;

public abstract class AbstractPopulationVariation implements Variation {

   protected int arity;

   @Override
   public int getArity() {
      return this.arity;
   }

}
