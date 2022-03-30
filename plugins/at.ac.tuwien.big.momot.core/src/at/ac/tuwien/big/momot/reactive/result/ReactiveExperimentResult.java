package at.ac.tuwien.big.momot.reactive.result;

import at.ac.tuwien.big.momot.reactive.error.Disturbance;

import java.util.ArrayList;
import java.util.List;

public class ReactiveExperimentResult {
   private final List<Float> finalObjectives;
   private final List<List<Double>> runtimes;
   private final List<List<Number>> evaluations;
   private final List<List<Disturbance>> disturbances;
   private final List<Integer> finalFailedExecutions;

   public ReactiveExperimentResult() {
      this.finalObjectives = new ArrayList<>();
      this.runtimes = new ArrayList<>();
      this.evaluations = new ArrayList<>();
      this.disturbances = new ArrayList<>();
      this.finalFailedExecutions = new ArrayList<>();
   }

   public void addRunResult(final ReactiveRunResult reactiveRunRes) {
      this.runtimes.add(reactiveRunRes.getRuntimes());
      this.evaluations.add(reactiveRunRes.getEvaluations());
      this.disturbances.add(reactiveRunRes.getDisturbances());
      this.finalObjectives.add(reactiveRunRes.getFinalObjective());
      this.finalFailedExecutions.add(reactiveRunRes.getFailedExecutions());
   }

   public List<List<Disturbance>> getDisturbances() {
      return this.disturbances;
   }

   public List<List<Number>> getEvaluations() {
      return this.evaluations;
   }

   public List<Integer> getFinalFailedExecutions() {
      return this.finalFailedExecutions;
   }

   public List<Float> getFinalObjectives() {
      return this.finalObjectives;
   }

   public List<List<Double>> getRuntimes() {
      return this.runtimes;
   }
}
