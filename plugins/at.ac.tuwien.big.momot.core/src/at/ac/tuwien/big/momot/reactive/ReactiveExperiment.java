package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.error.Disturbance;
import at.ac.tuwien.big.momot.reactive.error.ErrorUtils;
import at.ac.tuwien.big.momot.reactive.error.IRangeDisturber;
import at.ac.tuwien.big.momot.reactive.planningstrategy.ConditionReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PlanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.ReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.SearchReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.result.PredictiveRunResult;
import at.ac.tuwien.big.momot.reactive.result.ReactiveExperimentResult;
import at.ac.tuwien.big.momot.reactive.result.ReactiveRunResult;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;
import at.ac.tuwien.big.momot.search.criterion.MinimumObjectiveCondition;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

public class ReactiveExperiment {

   private final EGraph initialGraph;
   private final int nr_runs;
   private final AbstractDisturber disturber;
   private final Executor executor;
   private final List<PlanningStrategy> planningStrategies;
   private final IReactiveUtilities utils;
   private final int solutionLength;
   private final int populationSize;
   private final IReactiveSearch searchInstance;
   private final String evalObjectiveName;
   private final boolean verbose;
   private final Printer p;

   public Map<String, ReactiveExperimentResult> resultMap;

   // final PredictiveRunResult prr = predictiveReplanning(MomotUtil.copy(runtimeGraph), expName, run, pps,
   // curPlan, it.nextIndex(), calculateObjectiveOnModel(utils.getFitnessFunction(),
   // evalObjectiveName, graphOfLastPlanning, new ArrayList<>(mre.getExecutedUnits())));

   public ReactiveExperiment(final EGraph initialGraph, final List<PlanningStrategy> planningStrategies,
         final IReactiveSearch searchInstance, final IReactiveUtilities utils, final AbstractDisturber disturber,
         final Executor executor, final String evalObjectiveName, final int runs, final int solutionLength,
         final int populationSize, final Printer p, final boolean verbose) {
      this.initialGraph = initialGraph;
      this.planningStrategies = planningStrategies;
      this.searchInstance = searchInstance;
      this.utils = utils;
      this.disturber = disturber;
      this.evalObjectiveName = evalObjectiveName;
      this.nr_runs = runs;
      this.solutionLength = solutionLength;
      this.populationSize = populationSize;
      this.p = p;
      this.verbose = verbose;
      this.executor = executor;

   }

   private double calculateObjectiveOnModel(final IEGraphMultiDimensionalFitnessFunction f, final String objectiveName,
         final EGraph g, final List<ITransformationVariable> executedVars) {

      final TransformationSolution ts = new TransformationSolution(MomotUtil.copy(g), executedVars,
            f.evaluatesNrObjectives());
      f.evaluate(ts);
      return ts.getObjective(f.getObjectiveIndex(objectiveName));

   }

   private String getExperimentName(final PlanningStrategy strategy, final int id) {
      String name = String.format("%s_%s", strategy.getInitialSearchAlgorithm(), strategy.getReplanningStrategy());
      final ReplanningStrategy strat = strategy.getReplanningStrategy();
      if(strat instanceof SearchReplanningStrategy) {
         name += ((SearchReplanningStrategy) strat).getReusePortion() > 0 ? "_withSeed" : "";
      }
      name += "_" + id;
      return name;
   }

   private double getObjectiveFromTS(final TransformationSolution ts, final String objectiveName) {
      return ts.getObjective(utils.getFitnessFunction().getObjectiveIndex(objectiveName));
   }
   // runResult.addPredictiveRunResult(prr);

   private Solution getSolutionSatisfyingCondition(final Population p, final MinimumObjectiveCondition moc) {
      for(final Solution s : p) {
         if(moc.satisfiesCriteria(s)) {
            return s;
         }
      }
      throw new RuntimeException("No solution satisfying the condition after conditioned search!");
   }

   private PredictiveRunResult predictiveReplanning(final EGraph runtimeGraph, final String expName, final int run,
         final SearchReplanningStrategy srStrategy, final MinimumObjectiveCondition moc,
         final List<ITransformationVariable> curPlan, final int curPlanPos, final double curBestObj) {
      final Executor simExecutor = executor.copy();
      final ModelRuntimeEnvironment simMre = new ModelRuntimeEnvironment(runtimeGraph);
      simExecutor.setModelRuntimeEnvironment(simMre);

      final PredictiveRunResult prr = new PredictiveRunResult(moc.getObjectiveThresholds());

      final Iterator<Integer> simulateStepsIterator = srStrategy.getPredictivePlanningAfterXSteps().iterator();
      final Iterator<ITransformationVariable> planIterator = curPlan.subList(curPlanPos, curPlan.size()).iterator();

      while(simulateStepsIterator.hasNext()) {
         final int afterSteps = simulateStepsIterator.next();
         while(planIterator.hasNext() && simMre.getExecutedUnits().size() < afterSteps) {
            final ITransformationVariable var = planIterator.next();
            final boolean success = simExecutor.execute(var);
            if(success) {
               simMre.addExecutedUnit(var);
            }
         }

         // simulate replanning after the steps only if plan not finished
         if(planIterator.hasNext() && afterSteps == simMre.getExecutedUnits().size()) {

            final SearchResult res = replanningBySearch(runtimeGraph, expName, run, curPlan, curPlanPos,
                  srStrategy.getPredictivePlanningStrategy(), curBestObj);

            final Solution s = getSolutionSatisfyingCondition(res.getPopulation(), moc);

            prr.addPredictiveRunResult(afterSteps, s.getObjectives(), res.getExecutionEvaluations(),
                  res.getExecutionTime());

            // runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations());
            // p.subheader("Predictive Replanning", verbose);
            p.str(String.format(
                  "Pred. Replanning (%d steps): Thresholds: %s -> Found %s after %d iterations (%f seconds)",
                  afterSteps, ((ConditionReplanningStrategy) srStrategy).getTerminationCondition().toString(),
                  Arrays.toString(s.getObjectives()), res.getExecutionEvaluations(), res.getExecutionTime()));

         }
      }
      return prr;

   }

   private List<ITransformationVariable> replanning(final EGraph runtimeGraph, final String experimentName,
         final int run, final List<ITransformationVariable> curPlan, final int curPlanPos, final ReplanningStrategy ps,
         final ReactiveRunResult runResult, final double curBestObj) {
      SearchReplanningStrategy srStrategy = null;
      SearchResult res = null;
      switch(ps.getRepairStrategy()) {
         case REPLAN_FOR_CONDITION:
            srStrategy = (SearchReplanningStrategy) ps;
            res = replanningBySearch(runtimeGraph, experimentName, run, curPlan, curPlanPos, srStrategy, curBestObj);

            runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations(),
                  getObjectiveFromTS(res.getOptimalSolution(), evalObjectiveName));
            p.subheader("New plan", verbose).plan(res.getOptimalSolution(), verbose);
            return res.getOptimalPlan();

         case REPLAN_FOR_EVALUATIONS:
            srStrategy = (SearchReplanningStrategy) ps;
            res = replanningBySearch(runtimeGraph, experimentName, run, curPlan, curPlanPos, srStrategy, curBestObj);

            runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations(),
                  getObjectiveFromTS(res.getOptimalSolution(), evalObjectiveName));
            p.subheader("New plan", verbose).plan(res.getOptimalSolution(), verbose);
            return res.getOptimalPlan();
         case NAIVE:
            return curPlan.subList(curPlanPos, curPlan.size());
         default:
            throw new RuntimeException("Repair strategy not implemented!");
      }
   }

   private SearchResult replanningBySearch(final EGraph runtimeGraph, final String experimentName, final int run,
         final List<ITransformationVariable> curPlan, final int curPlanPos, final SearchReplanningStrategy srStrategy,
         final double curBestObj) {

      return srStrategy.replan(searchInstance, runtimeGraph, srStrategy.getReplanningAlgorithm(), experimentName, run,
            solutionLength - curPlanPos, populationSize,
            srStrategy.getReusePortion() > 0 ? curPlan.subList(curPlanPos, curPlan.size()) : null,
            srStrategy.getReusePortion(), curBestObj, true);
   }

   private ReactiveRunResult run(final EGraph runtimeGraph, final PlanningStrategy ps, final String expName,
         final int run) {

      final ModelRuntimeEnvironment mre = new ModelRuntimeEnvironment(runtimeGraph);

      final ReactiveRunResult runResult = new ReactiveRunResult();
      int failedExecutions = 0;

      disturber.reset();
      disturber.setModelRuntimeEnvironment(mre);
      executor.setModelRuntimeEnvironment(mre);
      // Create initial plan

      p.header2("INITIAL PLAN", verbose);
      final SearchResult res = searchInstance.performSearch(MomotUtil.copy(runtimeGraph),
            ps.getInitialSearchAlgorithm(), expName, run, ps.getInitialSearchEvaluations(),
            ps.getTerminationCriterion(), solutionLength, populationSize, null, 0, 0.0f, false);
      runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations(),
            getObjectiveFromTS(res.getOptimalSolution(), evalObjectiveName));

      p.plan(res.getOptimalSolution(), verbose);

      List<ITransformationVariable> curPlan = res.getOptimalPlan();
      if(disturber instanceof IRangeDisturber) {
         ((IRangeDisturber) disturber)
               .setDisturbanceIndex(ErrorUtils.getIndexForErrorRange(disturber.eOccurence, curPlan.size()));
      }
      EGraph graphOfLastPlanning = MomotUtil.copy(runtimeGraph);

      for(ListIterator<ITransformationVariable> it = curPlan.listIterator(); it.hasNext();) {

         // Execute plan iteratively
         final ITransformationVariable nextStep = it.next();
         final boolean success = executor.execute(nextStep);
         if(success) {
            mre.addExecutedUnit(nextStep);
         } else {
            failedExecutions++;
         }

         if(it.hasNext()) {
            final ITransformationVariable nextToExecute = curPlan.get(it.nextIndex());
            final Disturbance d = disturber.pollForDisturbance(it.nextIndex() - 1, curPlan.size(), nextToExecute);

            // If no disturbance occured, d = null
            if(d != null) {

               p.printChangeDetails(it.nextIndex(), curPlan.size(), curPlan.subList(0, it.nextIndex()), verbose)
                     .str(this.utils.getReprFromEGraph(mre.getGraph()), verbose)
                     .header2(String.format("Using planning strategy (%s)...", ps.getReplanningStrategy()), verbose);
               runResult.addDisturbance(d);

               runResult.addPostDisturbanceObjective(calculateObjectiveOnModel(utils.getFitnessFunction(),
                     evalObjectiveName, mre.getGraph(), new ArrayList<>()));

               // change occured, use repairstrategy
               final List<ITransformationVariable> savePlan = new ArrayList<>(curPlan);
               curPlan = replanning(MomotUtil.copy(runtimeGraph), expName, run, curPlan, it.nextIndex(),
                     ps.getReplanningStrategy(), runResult, calculateObjectiveOnModel(utils.getFitnessFunction(),
                           evalObjectiveName, graphOfLastPlanning, new ArrayList<>(mre.getExecutedUnits())));

               // predictive replanning
               if(ps.getReplanningStrategy() instanceof SearchReplanningStrategy) {
                  final SearchReplanningStrategy srs = (SearchReplanningStrategy) ps.getReplanningStrategy();
                  if(srs.isPredictivePlanningEnabled()) {
                     // Set objective to beat and max. steps for it
                     MinimumObjectiveCondition moc = null;
                     if(srs.getPredictivePlanningStrategy() instanceof ConditionReplanningStrategy) {
                        moc = MinimumObjectiveCondition.create(Map.of(
                              utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName),
                              calculateObjectiveOnModel(utils.getFitnessFunction(), evalObjectiveName, runtimeGraph,
                                    curPlan),
                              utils.getFitnessFunction().getObjectiveIndex("SolutionLength"), (double) curPlan.size()));

                        ((ConditionReplanningStrategy) srs.getPredictivePlanningStrategy())
                              .setTerminationCondition(moc);
                     }

                     final PredictiveRunResult prr = predictiveReplanning(MomotUtil.copy(runtimeGraph), expName, run,
                           srs, moc, savePlan, it.nextIndex(), calculateObjectiveOnModel(utils.getFitnessFunction(),
                                 evalObjectiveName, graphOfLastPlanning, new ArrayList<>(mre.getExecutedUnits())));
                     runResult.addPredictiveRunResult(prr);
                     // execute simulated runs, get plans, evaluate and return results
                  }
               }

               // Reset to first plan step
               it = curPlan.listIterator();

               // Remember last planning state and clear runtime units
               graphOfLastPlanning = MomotUtil.copy(runtimeGraph);
               mre.getExecutedUnits().clear();
            }
         }
      }

      if(runResult.getDisturbances().isEmpty()) {
         System.out.println("REACHED");
      }
      // final ModelRuntimeSnapshot lastSnapshot = runtimeSnapshots.get(runtimeSnapshots.size() - 1);
      p.header2("Final model", verbose).str(utils.getReprFromEGraph(mre.getGraph()), verbose);

      runResult.setFailedExecutions(failedExecutions);
      runResult.setFinalObjective(calculateObjectiveOnModel(utils.getFitnessFunction(), evalObjectiveName,
            graphOfLastPlanning, mre.getExecutedUnits()));
      return runResult;
   }

   public Map<String, ReactiveExperimentResult> runExperiment() {

      final Map<String, ReactiveExperimentResult> resultPerExperiment = new HashMap<>();

      int experimentId = 0;
      for(final PlanningStrategy strategy : planningStrategies) {
         final String expName = getExperimentName(strategy, experimentId++);
         // resultPerExperiment.put(expName, new ReactiveExperimentResult());

         final ReactiveExperimentResult experimentStats = new ReactiveExperimentResult();
         for(int j = 0; j < nr_runs; j++) {
            p.property("Configuration", String.format("%s (Strategy: %s) -> run %d/%d",
                  strategy.getInitialSearchAlgorithm(), strategy.getReplanningStrategy(), j + 1, nr_runs));
            final ReactiveRunResult reactiveRunRes = run(MomotUtil.copy(initialGraph), strategy, expName, j);

            experimentStats.addRunResult(reactiveRunRes);
         }
         resultPerExperiment.put(expName, experimentStats);

      }
      return resultPerExperiment;
   }

}
