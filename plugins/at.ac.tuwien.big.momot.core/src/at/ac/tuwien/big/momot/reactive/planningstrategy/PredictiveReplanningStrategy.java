package at.ac.tuwien.big.momot.reactive.planningstrategy;

import java.util.List;

public class PredictiveReplanningStrategy extends PlanningStrategy {

   public enum PredictiveReplanningType {
      TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED
   }

   public static PredictiveReplanningStrategy create(final String algorithm, final int prioritizedObjectiveForSelection,
         final List<Integer> planAfterXSteps, final PredictiveReplanningType type, final int secondsPerExecutionStep) {
      return new PredictiveReplanningStrategy(algorithm, prioritizedObjectiveForSelection, planAfterXSteps, type,
            secondsPerExecutionStep);
   }

   protected List<Integer> predictivePlanningAfterXSteps;

   protected PredictiveReplanningType predictiveReplanningType;
   private final int secondsPerExecutionStep;
   private double reseedingPortion;

   private PredictiveReplanningStrategy(final String algorithm, final int prioritizedObjectiveForSelection,
         final List<Integer> planAfterXSteps, final PredictiveReplanningType type, final int secondsPerExecutionStep) {
      super(algorithm, prioritizedObjectiveForSelection);
      this.predictivePlanningAfterXSteps = planAfterXSteps;
      this.predictiveReplanningType = type;
      this.secondsPerExecutionStep = secondsPerExecutionStep;
   }

   public List<Integer> getPredictivePlanningAfterXSteps() {
      return predictivePlanningAfterXSteps;
   }

   public PredictiveReplanningType getPredictiveReplanningType() {
      return predictiveReplanningType;
   }

   public double getReseedingPortion() {
      return reseedingPortion;
   }

   public int getSecondsPerExecutionStep() {
      return secondsPerExecutionStep;
   }

   public boolean isReseedingPopulationEnabled() {
      return this.reseedingPortion > 0;
   }

   @Override
   public String toString() {
      return super.toString() + "-PredictivePlanning";
   }

   public PredictiveReplanningStrategy withReseedingInitialization(final double portion) {
      this.reseedingPortion = portion;
      return this;
   }

}
