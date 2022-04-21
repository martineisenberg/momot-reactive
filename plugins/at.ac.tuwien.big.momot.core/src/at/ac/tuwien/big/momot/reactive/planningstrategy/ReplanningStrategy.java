package at.ac.tuwien.big.momot.reactive.planningstrategy;

public class ReplanningStrategy extends PlanningStrategy {
   // public enum RepairStrategy {
   // NAIVE, REPLAN_FOR_EVALUATIONS, REPLAN_FOR_CONDITION, MIXED, REPLAN_PREDICTIVE
   // }

   // protected RepairStrategy repairStrategy;

   public static ReplanningStrategy create(final String algorithm, final int prioritizedObjectiveForSelection) {
      return new ReplanningStrategy(algorithm, prioritizedObjectiveForSelection);
   }

   public static ReplanningStrategy naive() {
      return new ReplanningStrategy(true);
   }

   private PredictiveReplanningStrategy predictiveReplanningStrategy;
   private double reseedingPortion;
   private final boolean isNaive;

   private ReplanningStrategy(final boolean isNaive) {
      super(null, 0);
      this.isNaive = true;
   }

   private ReplanningStrategy(final String algorithm, final int prioritizedObjectiveForSelection) {
      super(algorithm, prioritizedObjectiveForSelection);
      this.isNaive = false;
   }

   public PredictiveReplanningStrategy getPredictiveReplanningStrategy() {
      return predictiveReplanningStrategy;
   }

   public double getReseedingPortion() {
      return reseedingPortion;
   }

   public boolean hasPredictivePlanningStrategy() {
      return this.predictiveReplanningStrategy != null;
   }

   public boolean isNaive() {
      return isNaive;
   }

   public boolean isReseedingPopulationEnabled() {
      return this.reseedingPortion > 0;
   }

   public void setPredictiveReplanningStrategy(final PredictiveReplanningStrategy predictiveReplanningStrategy) {
      this.predictiveReplanningStrategy = predictiveReplanningStrategy;
   }

   public void setReusePortion(final double reusePortion) {
      this.reseedingPortion = reusePortion;
   }

   @Override
   public String toString() {
      String rv = super.toString() + "-ReplanningStrategy";
      if(reseedingPortion > 0) {
         rv += String.format("_reuse%.2f", reseedingPortion);
      }
      if(this.isNaive) {
         rv += String.format("_naive");
      }
      return rv;
   }

   public ReplanningStrategy withPredictivePlanning(final PredictiveReplanningStrategy pps) {
      this.predictiveReplanningStrategy = pps;
      return this;
   }

   public ReplanningStrategy withReseedingInitialization(final double portion) {
      this.reseedingPortion = portion;
      return this;
   }

}
