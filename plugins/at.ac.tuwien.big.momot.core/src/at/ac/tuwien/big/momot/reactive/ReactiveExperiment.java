package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.error.Disturbance;
import at.ac.tuwien.big.momot.reactive.error.ErrorUtils;
import at.ac.tuwien.big.momot.reactive.error.IRangeDisturber;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PlanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PredictivePlanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.ReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.SearchReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.result.PredictiveRunResult;
import at.ac.tuwien.big.momot.reactive.result.ReactiveExperimentResult;
import at.ac.tuwien.big.momot.reactive.result.ReactiveRunResult;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.emf.henshin.interpreter.EGraph;

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

   private float calculateObjectiveOnFinalModel(final IEGraphMultiDimensionalFitnessFunction f,
         final String objectiveName, final EGraph g, final List<ITransformationVariable> executedVars) {

      final TransformationSolution ts = new TransformationSolution(MomotUtil.copy(g), executedVars,
            f.evaluatesNrObjectives());
      f.evaluate(ts);
      return (float) ts.getObjective(f.getObjectiveIndex(objectiveName));

   }

   private String getExperimentName(final PlanningStrategy strategy, final int id) {
      String name = String.format("%s_%s", strategy.getInitialSearchAlgorithm(), strategy.getReplanningStrategy());
      final ReplanningStrategy strat = strategy.getReplanningStrategy();
      if(strat instanceof SearchReplanningStrategy) {
         name += ((SearchReplanningStrategy) strat).isDoReusePreviousPlan() ? "_withReinit" : "";
      }
      name += "_" + id;
      return name;
   }

   private PredictiveRunResult predictiveReplanning(final EGraph runtimeGraph, final String expName, final int run,
         final PredictivePlanningStrategy pps, final List<ITransformationVariable> curPlan, final int curPlanPos,
         final float curBestObj) {
      final Executor simExecutor = executor.copy();
      final ModelRuntimeEnvironment simMre = new ModelRuntimeEnvironment(runtimeGraph);
      simExecutor.setModelRuntimeEnvironment(simMre);

      final PredictiveRunResult prr = new PredictiveRunResult();

      final Iterator<Integer> simulateStepsIterator = pps.getListReplanAfterAdditionalSteps().iterator();
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

         if(afterSteps == simMre.getExecutedUnits().size()) {

            final SearchResult res = pps.replan(searchInstance, runtimeGraph, pps.getReplanningAlgorithm(), expName,
                  run, solutionLength - curPlanPos, populationSize,
                  pps.isDoReusePreviousPlan() ? curPlan.subList(curPlanPos, curPlan.size()) : null,
                  pps.getReusePortion(), curBestObj, true);

            prr.addPredictiveRunResult(afterSteps, res.getOptimalSolution().getObjectives(),
                  res.getExecutionEvaluations(), res.getExecutionTime());
            // runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations());
            p.subheader("Predictive Replanning", verbose);

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
            res = ((SearchReplanningStrategy) ps).replan(searchInstance, runtimeGraph,
                  srStrategy.getReplanningAlgorithm(), experimentName, run, solutionLength - curPlanPos, populationSize,
                  srStrategy.isDoReusePreviousPlan() ? curPlan.subList(curPlanPos, curPlan.size()) : null,
                  srStrategy.getReusePortion(), curBestObj, true);

            runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations());
            p.subheader("New plan", verbose).plan(res.getOptimalSolution(), verbose);
            return res.getOptimalPlan();

         case REPLAN_FOR_EVALUATIONS:
            srStrategy = (SearchReplanningStrategy) ps;
            res = ((SearchReplanningStrategy) ps).replan(searchInstance, runtimeGraph,
                  srStrategy.getReplanningAlgorithm(), experimentName, run, solutionLength - curPlanPos, populationSize,
                  srStrategy.isDoReusePreviousPlan() ? curPlan.subList(curPlanPos, curPlan.size()) : null,
                  srStrategy.getReusePortion(), curBestObj, true);

            runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations());
            p.subheader("New plan", verbose).plan(res.getOptimalSolution(), verbose);
            return res.getOptimalPlan();
         case NAIVE:
            return curPlan.subList(curPlanPos, curPlan.size());
         default:
            throw new RuntimeException("Repair strategy not implemented!");
      }
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
      final SearchResult res = searchInstance.performSearch(runtimeGraph, ps.getInitialSearchAlgorithm(), expName, run,
            ps.getInitialSearchEvaluations(), ps.getTerminationCriterion(), solutionLength, populationSize, null, 0,
            0.0f, false);
      runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations());

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
               // change occured, use repairstrategy

               curPlan = replanning(MomotUtil.copy(runtimeGraph), expName, run, curPlan, it.nextIndex(),
                     ps.getReplanningStrategy(), runResult, calculateObjectiveOnFinalModel(utils.getFitnessFunction(),
                           evalObjectiveName, graphOfLastPlanning, new ArrayList<>(mre.getExecutedUnits())));

               if(ps.getReplanningStrategy() instanceof PredictivePlanningStrategy) {
                  final PredictivePlanningStrategy pps = (PredictivePlanningStrategy) ps.getReplanningStrategy();
                  final PredictiveRunResult prr = predictiveReplanning(MomotUtil.copy(runtimeGraph), expName, run, pps,
                        curPlan, it.nextIndex(), calculateObjectiveOnFinalModel(utils.getFitnessFunction(),
                              evalObjectiveName, graphOfLastPlanning, new ArrayList<>(mre.getExecutedUnits())));
                  runResult.addPredictiveRunResult(prr);
                  // execute simulated runs, get plans, evaluate and return results
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
      runResult.setFinalObjective(calculateObjectiveOnFinalModel(utils.getFitnessFunction(), evalObjectiveName,
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
