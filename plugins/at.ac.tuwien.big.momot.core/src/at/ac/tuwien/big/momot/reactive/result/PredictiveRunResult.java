package at.ac.tuwien.big.momot.reactive.result;

import java.util.HashMap;
import java.util.Map;

public class PredictiveRunResult {
   class PredictiveRunPlanningStats {

      private final int evaluations;
      private final double[] objectives;

      private final double runtime;

      public PredictiveRunPlanningStats(final double objectives[], final int evaluations, final double runtime) {
         this.objectives = objectives;
         this.evaluations = evaluations;
         this.runtime = runtime;
      }
   }

   Map<Integer, PredictiveRunPlanningStats> stepsToResult;

   public PredictiveRunResult() {
      this.stepsToResult = new HashMap<>();
   }

   public void addPredictiveRunResult(final int predictiveSteps, final double[] objectives, final int evaluations,
         final double runtime) {
      this.stepsToResult.put(predictiveSteps, new PredictiveRunPlanningStats(objectives, evaluations, runtime));
   }

}
