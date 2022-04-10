package at.ac.tuwien.big.momot.reactive.result;

import at.ac.tuwien.big.momot.reactive.error.Disturbance;

import java.util.ArrayList;
import java.util.List;

public class ReactiveRunResult {
   private double finalObjective;
   private final List<Double> postDisturbanceObjectives;
   private final List<Double> preDisturbanceObjectives;
   private final List<Double> plannedObjectives;

   private final List<Double> runtimes;
   private final List<Number> evaluations;
   private final List<Disturbance> disturbances;
   private final List<PredictiveRunResult> predictiveRunResults;
   private int failedExecutions;

   public ReactiveRunResult() {
      this.postDisturbanceObjectives = new ArrayList<>();
      this.preDisturbanceObjectives = new ArrayList<>();

      this.plannedObjectives = new ArrayList<>();

      this.runtimes = new ArrayList<>();
      this.evaluations = new ArrayList<>();
      this.disturbances = new ArrayList<>();
      this.failedExecutions = 0;
      this.predictiveRunResults = new ArrayList<>();
   }

   public void addDisturbance(final Disturbance d) {
      this.disturbances.add(d);
   }

   public void addPlanningStats(final double runtime, final double evaluations, final double objective) {
      this.runtimes.add(runtime);
      this.evaluations.add(evaluations);
      this.plannedObjectives.add(objective);
   }

   public void addPostDisturbanceObjective(final double objectiveValue) {
      this.postDisturbanceObjectives.add(objectiveValue);

   }

   public void addPredictiveRunResult(final PredictiveRunResult prr) {
      this.predictiveRunResults.add(prr);
   }

   public void addPreDisturbanceObjective(final double objectiveValue) {
      this.preDisturbanceObjectives.add(objectiveValue);
   }

   public List<Disturbance> getDisturbances() {
      return this.disturbances;
   }

   public List<Number> getEvaluations() {
      return this.evaluations;
   }

   public int getFailedExecutions() {
      return this.failedExecutions;
   }

   public double getFinalObjective() {
      return this.finalObjective;
   }

   public List<Double> getPlannedObjectives() {
      return this.plannedObjectives;
   }

   public List<Double> getPostDisturbanceObjectives() {
      return this.postDisturbanceObjectives;
   }

   public List<PredictiveRunResult> getPredictiveRunResults() {
      return this.predictiveRunResults;
   }

   public List<Double> getPreDisturbanceObjectives() {
      return this.preDisturbanceObjectives;
   }

   public List<Double> getRuntimes() {
      return this.runtimes;
   }

   public void setFailedExecutions(final int failedExecutions) {
      this.failedExecutions = failedExecutions;
   }

   public void setFinalObjective(final double finalObjective) {
      this.finalObjective = finalObjective;
   }

}
