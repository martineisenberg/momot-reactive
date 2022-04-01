package at.ac.tuwien.big.momot.reactive.result;

import java.util.HashMap;
import java.util.Map;

public class PredictiveRunResult {
   public class PredictiveRunPlanningStats {

      private final int evaluations;
      private final double[] objectives;
      private final double runtime;

      public PredictiveRunPlanningStats(final double objectives[], final int evaluations, final double runtime) {
         this.objectives = objectives;
         this.evaluations = evaluations;
         this.runtime = runtime;
      }

      public int getEvaluations() {
         return evaluations;
      }

      public double[] getObjectives() {
         return objectives;
      }

      public double getRuntime() {
         return runtime;
      }

   }

   Map<Integer, PredictiveRunPlanningStats> stepsToResult;
   double[] idlePlanningObjectives;

   public PredictiveRunResult(final double[] idlePlanningObjectives) {
      this.idlePlanningObjectives = idlePlanningObjectives;
      this.stepsToResult = new HashMap<>();

   }

   public void addPredictiveRunResult(final int predictiveSteps, final double[] objectives, final int evaluations,
         final double runtime) {
      this.stepsToResult.put(predictiveSteps, new PredictiveRunPlanningStats(objectives, evaluations, runtime));
   }

   public double[] getIdlePlanningObjectives() {
      return this.idlePlanningObjectives;
   }

   public Map<Integer, PredictiveRunPlanningStats> getStepsToResult() {
      return this.stepsToResult;
   }

}
