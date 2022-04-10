package at.ac.tuwien.big.momot.examples.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.AbstractDisturber;
import at.ac.tuwien.big.momot.reactive.error.Disturbance;
import at.ac.tuwien.big.momot.reactive.error.ErrorOccurence;

public class ProbabilityDisturber extends AbstractDisturber {
   public static class ProbabilityDisturberBuilder extends AbstractDisturberBuilder {

      @Override
      public ProbabilityDisturber build() {
         final ProbabilityDisturber d = new ProbabilityDisturber(this);

         if(eType == null || eOccurence == null || eProbability < 0 || eProbability > 1 || errorsPerDisturbance == 0
               || maxNrOfDisturbances == 0) {
            throw new RuntimeException(
                  "Failed building disturber component: Must define error type, error occurence, and error probability (1 >= p >= 0");
         }
         return d;
      }

   }

   private final float eProbability;

   private ProbabilityDisturber(final ProbabilityDisturberBuilder builder) {
      super(builder);
      this.eProbability = builder.eProbability;

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

      // Exclude disturbances in first or second half of execution plan according to setting
      if(eOccurence == ErrorOccurence.FIRST_HALF && curExecutionNr > plannedExecutions / 2.0
            || eOccurence == ErrorOccurence.SECOND_HALF && curExecutionNr < plannedExecutions / 2.0 + 1) {
         return null;
      }

      // Disturb by probability
      if(rand.nextFloat() < eProbability) {
         this.disturb();
         nrOfObservedDisturbances++;
         return Disturbance.of(eType, curExecutionNr, plannedExecutions);
      }
      return null;
   }

   @Override
   public String toString() {
      return String.format("DISTURBER@p%.2f_%s_%s", eProbability, eType, eOccurence);
   }

}
