package at.ac.tuwien.big.moea.search.algorithm.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * Construct a variation operator applying one or more variations sequentially.
 * This construct is used to support mixed-type decision variables; however,
 * this requires that the variation operators are type safe. Type safe variation
 * operates only on supported types and ignores unsupported types.
 * <p>
 * {@code CompoundVariation} provides the following behavior:
 * <ol>
 * <li>If the previous operator produced {@code K} offspring and the current
 * operator requires {@code K} parents, the current operator is applied
 * normally. The current operator may produce any number of offspring.
 * <li>If the previous operator produced {@code K} offspring and the current
 * operator requires {@code 1} parent, the current operator is applied to each
 * offspring individually. The current operator may produce any number of
 * offspring, but only the first offspring will be retained.
 * <li>Otherwise, an exception is thrown.
 * </ol>
 */
public class CustomCompoundVariation implements Variation {

   /**
    * The variation operators in the order they are applied.
    */
   private final List<Variation> operators;

   /**
    * The name of this variation operator.
    */
   private String name;

   /**
    * Constructs a compound variation operator with no variation operators.
    */
   public CustomCompoundVariation() {
      super();

      operators = new ArrayList<>();
   }

   /**
    * Constructs a compound variation operator with the specified variation
    * operators.
    *
    * @param operators
    *           the variation operators in the order they are applied
    */
   public CustomCompoundVariation(final Variation... operators) {
      this();

      for(final Variation operator : operators) {
         appendOperator(operator);
      }
   }

   /**
    * Appends the specified variation operator to this compound operator.
    *
    * @param variation
    *           the variation operator to append
    */
   public void appendOperator(final Variation variation) {
      operators.add(variation);
   }

   @Override
   public Solution[] evolve(final Solution[] parents) {
      Solution[] result = Arrays.copyOf(parents, parents.length);

      for(final Variation operator : operators) {
         if(result.length == operator.getArity()) {
            result = operator.evolve(result);
         } else if(operator.getArity() == 1) {
            for(int j = 0; j < result.length; j++) {
               result[j] = operator.evolve(new Solution[] { result[j] })[0];
            }
         } else {
            throw new FrameworkException("invalid number of parents");
         }
      }

      return result;
   }

   @Override
   public int getArity() {
      return operators.get(0).getArity();
   }

   /**
    * Returns the name of this variation operator. If no name has been
    * assigned through {@link #setName(String)}, a name is generated which
    * identifies the underlying operators.
    *
    * @return the name of this variation operator
    */
   public String getName() {
      if(name == null) {
         final StringBuilder sb = new StringBuilder();

         for(final Variation operator : operators) {
            if(sb.length() > 0) {
               sb.append('+');
            }

            sb.append(operator.getClass().getSimpleName());
         }

         return sb.toString();
      } else {
         return name;
      }
   }

   public List<Variation> getVariationOperators() {
      return this.operators;
   }

   /**
    * Sets the name of this variation operator.
    *
    * @param name
    *           the name of this variation operator
    */
   public void setName(final String name) {
      this.name = name;
   }

}
