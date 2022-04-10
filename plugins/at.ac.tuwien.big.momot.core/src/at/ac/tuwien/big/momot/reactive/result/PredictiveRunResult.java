package at.ac.tuwien.big.momot.reactive.result;

import java.util.HashMap;
import java.util.Map;

public class PredictiveRunResult {
   public class PredictiveRunPlanningStats {

      private final int evaluations;
      private final double[] objectives;
      private final double runtime;
      private final double maxRuntimeIfObjectivesSatisfied;
      private final double runtimeTilObjectivesSatisfised;

      public PredictiveRunPlanningStats(final double objectives[], final int evaluations, final double runtime,
            final double runtimeTilObjSatisfied, final double maxRuntimeIfObjSatisfied) {
         this.objectives = objectives;
         this.evaluations = evaluations;
         this.runtime = runtime;
         this.maxRuntimeIfObjectivesSatisfied = maxRuntimeIfObjSatisfied;
         this.runtimeTilObjectivesSatisfised = runtimeTilObjSatisfied;
      }

      public int getEvaluations() {
         return evaluations;
      }

      public double getMaxRuntimeIfObjectivesSatisfied() {
         return maxRuntimeIfObjectivesSatisfied;
      }

      public double[] getObjectives() {
         return objectives;
      }

      public double getRuntime() {
         return runtime;
      }

      public double getRuntimeTilObjectivesSatisfised() {
         return runtimeTilObjectivesSatisfised;
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
      this.stepsToResult.put(predictiveSteps, new PredictiveRunPlanningStats(objectives, evaluations, runtime, -1, -1));
   }

   public void addPredictiveRunResult(final int predictiveSteps, final double[] objectives, final int evaluations,
         final double runtime, final double runtimeTilObjSatisfied, final double maxRuntimeIfObjSatisfied) {
      this.stepsToResult.put(predictiveSteps, new PredictiveRunPlanningStats(objectives, evaluations, runtime,
            runtimeTilObjSatisfied, maxRuntimeIfObjSatisfied));
   }

   public double[] getIdlePlanningObjectives() {
      return this.idlePlanningObjectives;
   }

   public Map<Integer, PredictiveRunPlanningStats> getStepsToResult() {
      return this.stepsToResult;
   }

}
