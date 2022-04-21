package at.ac.tuwien.big.momot.examples.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.AbstractDisturber;
import at.ac.tuwien.big.momot.reactive.error.Disturbance;
import at.ac.tuwien.big.momot.reactive.error.ErrorUtils;
import at.ac.tuwien.big.momot.reactive.error.IRangeDisturber;

public class RangeDisturber extends AbstractDisturber implements IRangeDisturber {
   public static class RangeDisturberBuilder extends AbstractDisturberBuilder {

      @Override
      public RangeDisturber build() {
         final RangeDisturber d = new RangeDisturber(this);

         if(eType == null || eOccurence == null || maxNrOfDisturbances == 0 || errorsPerDisturbance == 0) {
            throw new RuntimeException(
                  "Failed building disturber component: Must define error type and error occurence!");
         }
         return d;
      }

   }

   private int disturbanceIndex;

   private RangeDisturber(final RangeDisturberBuilder builder) {
      super(builder);
      this.disturbanceIndex = -1;
   }

   /**
    * Check for any occuring disturbance; Will occur based
    * on disturber settings (error type, probability, first/second half of planned execution).
    *
    * @param curExecutionNr
    *           .. Nr. of current rule excution
    * @param plannedExecutions
    *           .. Nr. of executions of current plan
    * @param nextExecution
    *           .. Next variable to execute, relevant for introducing specific error
    * @return .. null if no disturbance occured, otherwise disturbance object for information
    */
   @Override
   public Disturbance pollForDisturbance(final int curExecutionNr, final int plannedExecutions,
         final ITransformationVariable nextExecution) {

      // Respect setting of max. number of disturbances for this instance (one disturber instance per run)
      if(this.maxNrOfDisturbances > 0 && this.nrOfObservedDisturbances >= this.maxNrOfDisturbances) {
         return null;
      }

      if(disturbanceIndex == curExecutionNr) {
         this.disturb();
         nrOfObservedDisturbances++;
         return Disturbance.of(eType, curExecutionNr, plannedExecutions);
      }
      return null;
   }

   @Override
   public void setup(final int planLength) {
      this.disturbanceIndex = ErrorUtils.getIndexForErrorRange(eOccurence, planLength);
   }

   @Override
   public String toString() {
      return String.format("DISTURBER@%s_%s_errPerDisturbance-%d", eType, eOccurence, errorsPerDisturbance);
   }

}
