package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.error.ErrorOccurence;
import at.ac.tuwien.big.momot.reactive.error.ErrorType;

import java.util.Random;

public abstract class AbstractDisturber {

   public static abstract class AbstractDisturberBuilder {
      protected ErrorType eType;

      protected ErrorOccurence eOccurence;
      protected float eProbability;
      protected ModelRuntimeEnvironment mre;
      protected int maxSteps;
      protected int maxNrOfDisturbances;

      public abstract AbstractDisturber build();

      public AbstractDisturberBuilder maxNrOfDisturbances(final int maxNrOfDisturbances) {
         this.maxNrOfDisturbances = maxNrOfDisturbances;
         return this;
      }

      public AbstractDisturberBuilder maxPlanLength(final int maxLength) {
         this.maxSteps = maxLength;
         return this;
      }

      public AbstractDisturberBuilder occurence(final ErrorOccurence eo) {
         this.eOccurence = eo;
         return this;
      }

      public AbstractDisturberBuilder probability(final float p) {
         this.eProbability = p;
         return this;
      }

      public AbstractDisturberBuilder runtimeModelEnvironment(final ModelRuntimeEnvironment mre) {
         this.mre = mre;
         return this;
      }

      public AbstractDisturberBuilder type(final ErrorType et) {
         this.eType = et;
         return this;
      }
   }

   protected final ErrorType eType;

   protected final ErrorOccurence eOccurence;
   protected final float eProbability;
   protected ModelRuntimeEnvironment mre;
   protected int nrOfObservedDisturbances;
   protected final int maxNrOfDisturbances;
   protected final Random rand;

   protected AbstractDisturber(final AbstractDisturberBuilder builder) {
      this.eType = builder.eType;
      this.eOccurence = builder.eOccurence;
      this.eProbability = builder.eProbability;
      this.mre = builder.mre;
      this.maxNrOfDisturbances = builder.maxNrOfDisturbances;
      this.rand = new Random();
      this.nrOfObservedDisturbances = 0;
   }

   protected abstract void disturb(final ITransformationVariable nextStep);

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
    * @return .. whether disturbance occured in terms of a change to runtime model
    */
   public boolean pollForDisturbance(final int curExecutionNr, final int plannedExecutions,
         final ITransformationVariable nextExecution) {

      // Respect setting of max. number of disturbances for this instance (one disturber instance per run)
      if(this.maxNrOfDisturbances > 0 && this.nrOfObservedDisturbances >= this.maxNrOfDisturbances) {
         return false;
      }

      // Exclude disturbances in first or second half of execution plan according to setting
      if(eOccurence == ErrorOccurence.FIRST_HALF && curExecutionNr > plannedExecutions / 2.0
            || eOccurence == ErrorOccurence.SECOND_HALF && curExecutionNr < plannedExecutions / 2.0 + 1) {
         return false;
      }

      // Disturb by probability
      if(rand.nextFloat() < eProbability) {
         this.disturb(nextExecution);
         nrOfObservedDisturbances++;
         return true;
      }
      return false;
   }

   public void reset() {
      this.mre = null;
      this.nrOfObservedDisturbances = 0;
   }

   public void setModelRuntimeEnvironment(final ModelRuntimeEnvironment mre) {
      this.mre = mre;
   }
}
