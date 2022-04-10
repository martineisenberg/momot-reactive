package at.ac.tuwien.big.momot.reactive.result;

import at.ac.tuwien.big.momot.reactive.error.Disturbance;

import java.util.ArrayList;
import java.util.List;

public class ReactiveExperimentResult {
   private final List<Double> finalObjectives;
   private final List<List<PredictiveRunResult>> predictiveRunResults;
   private final List<List<Double>> postDisturbanceObjectives;
   private final List<List<Double>> preDisturbanceObjectives;

   private final List<List<Double>> plannedObjectives;

   private final List<List<Double>> runtimes;
   private final List<List<Number>> evaluations;
   private final List<List<Disturbance>> disturbances;
   private final List<Integer> finalFailedExecutions;

   public ReactiveExperimentResult() {
      this.finalObjectives = new ArrayList<>();
      this.runtimes = new ArrayList<>();
      this.postDisturbanceObjectives = new ArrayList<>();
      this.preDisturbanceObjectives = new ArrayList<>();
      this.plannedObjectives = new ArrayList<>();
      this.predictiveRunResults = new ArrayList<>();
      this.evaluations = new ArrayList<>();
      this.disturbances = new ArrayList<>();
      this.finalFailedExecutions = new ArrayList<>();
   }

   public void addRunResult(final ReactiveRunResult reactiveRunRes) {
      this.predictiveRunResults.add(reactiveRunRes.getPredictiveRunResults());
      this.runtimes.add(reactiveRunRes.getRuntimes());
      this.evaluations.add(reactiveRunRes.getEvaluations());
      this.postDisturbanceObjectives.add(reactiveRunRes.getPostDisturbanceObjectives());
      this.preDisturbanceObjectives.add(reactiveRunRes.getPreDisturbanceObjectives());
      this.plannedObjectives.add(reactiveRunRes.getPlannedObjectives());
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

   public List<Double> getFinalObjectives() {
      return this.finalObjectives;
   }

   public List<List<Double>> getPlannedObjectives() {
      return this.plannedObjectives;
   }

   public List<List<Double>> getPostDisturbanceObjectives() {
      return this.postDisturbanceObjectives;
   }

   public List<List<PredictiveRunResult>> getPredictiveRunResults() {
      return this.predictiveRunResults;
   }

   public List<List<Double>> getPreDisturbanceObjectives() {
      return this.preDisturbanceObjectives;
   }

   public List<List<Double>> getRuntimes() {
      return this.runtimes;
   }
}
