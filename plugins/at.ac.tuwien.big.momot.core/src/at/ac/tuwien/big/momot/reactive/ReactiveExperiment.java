package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.error.Disturbance;
import at.ac.tuwien.big.momot.reactive.error.IRangeDisturber;
import at.ac.tuwien.big.momot.reactive.planningstrategy.Planning;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PlanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.ReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.SearchConfiguration;
import at.ac.tuwien.big.momot.reactive.result.ReactiveExperimentResult;
import at.ac.tuwien.big.momot.reactive.result.ReactiveRunResult;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.emf.henshin.interpreter.EGraph;

public class ReactiveExperiment {

   private final EGraph initialGraph;
   private final int nr_runs;
   private final AbstractDisturber disturber;
   private final Executor executor;
   private final List<Planning> plannings;
   private final IReactiveUtilities utils;
   private final int solutionLength;
   private final int populationSize;
   private final String evalObjectiveName;

   private final boolean verbose;
   private final Printer p;
   private final Planner planner;

   public Map<String, ReactiveExperimentResult> resultMap;

   // final PredictiveRunResult prr = predictiveReplanning(MomotUtil.copy(runtimeGraph), expName, run, pps,
   // curPlan, it.nextIndex(), calculateObjectiveOnModel(utils.getFitnessFunction(),
   // evalObjectiveName, graphOfLastPlanning, new ArrayList<>(mre.getExecutedUnits())));

   public ReactiveExperiment(final EGraph initialGraph, final List<Planning> plannings, final IReactiveUtilities utils,
         final Planner planner, final AbstractDisturber disturber, final Executor executor,
         final String evalObjectiveName, final String selectingObjectiveName, final int runs, final int solutionLength,
         final int populationSize, final Printer p, final boolean verbose) {
      this.initialGraph = initialGraph;
      this.planner = planner;
      this.plannings = plannings;
      this.utils = utils;
      this.disturber = disturber;
      this.evalObjectiveName = evalObjectiveName;
      this.nr_runs = runs;
      this.solutionLength = solutionLength;
      this.populationSize = populationSize;
      this.p = p;
      this.verbose = verbose;
      this.executor = executor;
      SearchConfiguration.setExecutor(executor);

   }

   private String getExperimentName(final Planning planning, final int id) {
      String name = String.format("%s_%s", planning.getPlanningStrategy().getInitialSearchAlgorithm(),
            planning.getReplanningStrategy().toString());
      final ReplanningStrategy rs = planning.getReplanningStrategy();
      if(rs.getPredictiveReplanningStrategy() != null) {
         name += "_" + rs.getPredictiveReplanningStrategy().toString();
      }
      name += "_" + id;
      return name;
   }

   // private double getObjectiveFromTS(final TransformationSolution ts, final String objectiveName) {
   // return ts.getObjective(utils.getFitnessFunction().getObjectiveIndex(objectiveName));
   // }
   // runResult.addPredictiveRunResult(prr);

   // private Map<Integer, Double> getObjectiveThresholdsFromCondition(final ConditionReplanningStrategy crs) {
   // return new HashMap<>(crs.getTerminationCondition().getThresholds());
   // }

   // private TransformationSolution getOptimalSolutionFromReplanning(final ReplanningStrategy ps,
   // final SearchResult res) {
   // switch(ps.getRepairStrategy()) {
   // case REPLAN_FOR_CONDITION:
   // return res.getOptimalSolutionForObjective(
   // this.getObjectiveThresholdsFromCondition((ConditionReplanningStrategy) ps),
   // utils.getFitnessFunction().getObjectiveIndex(selectingObjectiveName));
   //
   // case REPLAN_FOR_EVALUATIONS:
   // return res.getOptimalSolutionForObjective(utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName));
   // default:
   // throw new RuntimeException("Repair strategy not implemented!");
   // }
   // }

   // private Solution getSolutionSatisfyingCondition(final Population p, final MinimumObjectiveCondition moc) {
   // for(final Solution s : p) {
   // if(moc.satisfiesCriteria(s)) {
   // return s;
   // }
   // }
   // throw new RuntimeException("No solution satisfying the condition after conditioned search!");
   // }

   //
   // TODO Change terminaton according to set flag
   //
   // private PredictiveRunResult predictiveReplanning(final EGraph runtimeGraph, final String expName, final int run,
   // final SearchReplanningStrategy srStrategy, final List<ITransformationVariable> curPlan, final int curPlanPos,
   // final double objOfIdleComputedPlan, final int stepsOfIdleComputedPlan, final double curBestObj) {
   //
   // final Executor simExecutor = executor.copy();
   // final ModelRuntimeEnvironment simMre = new ModelRuntimeEnvironment(runtimeGraph);
   // simExecutor.setModelRuntimeEnvironment(simMre);
   //
   // // Set objective to beat and max. steps for it
   // ThresholdCondition moc = null;
   //
   // switch(srStrategy.getPredictiveReplanningType()) {
   // case TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED:
   // moc = TimedObjectiveCondition.create(
   // Map.of(utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName), objOfIdleComputedPlan),
   // utils.getFitnessFunction().getObjectiveIndex("SolutionLength"));
   //
   // ((ConditionReplanningStrategy) srStrategy.getPredictivePlanningStrategy()).setTerminationCondition(moc);
   // break;re
   // default:
   // throw new RuntimeException("predictive replanning type not implemented");
   //
   // }
   //
   // final PredictiveRunResult prr = new PredictiveRunResult(moc.getObjectiveThresholds());
   //
   // final Iterator<Integer> simulateStepsIterator = srStrategy.getPredictivePlanningAfterXSteps().iterator();
   // final ListIterator<ITransformationVariable> planIterator = curPlan.subList(curPlanPos, curPlan.size())
   // .listIterator();
   //
   // while(simulateStepsIterator.hasNext()) {
   // final int afterSteps = simulateStepsIterator.next();
   // while(planIterator.hasNext() && simMre.getExecutedUnits().size() < afterSteps) {
   // final ITransformationVariable var = planIterator.next();
   // final boolean success = simExecutor.execute(var);
   // if(success) {
   // simMre.addExecutedUnit(var);
   // }
   // }
   //
   // // simulate replanning after the steps only if plan not finished
   // if(planIterator.hasNext() && afterSteps == simMre.getExecutedUnits().size()) {
   //
   // switch(srStrategy.getPredictiveReplanningType()) {
   // case TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED:
   // final TimedObjectiveCondition toc = (TimedObjectiveCondition) moc;
   // toc.setMaxSecondsIfObjectiveSatisfied(
   // (long) (UNIT_EXECUTION_TIME_SECONDS * afterSteps * Math.pow(10, 9)));
   //
   // ((ConditionReplanningStrategy) srStrategy.getPredictivePlanningStrategy())
   // .setTerminationCondition(toc);
   // break;
   // default:
   // throw new RuntimeException("predictive replanning type does not exist");
   //
   // }
   //
   // // if(srStrategy.getPredictivePlanningStrategy() instanceof ConditionReplanningStrategy) {
   // // // moc = MinimumObjectiveCondition
   // // // .create(Map.of(utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName),
   // // // objOfIdleComputedPlan, utils.getFitnessFunction().getObjectiveIndex("SolutionLength"),
   // // // (double) stepsOfIdleComputedPlan - afterSteps));
   // //
   // // final TerminationCondition tc = ((ConditionReplanningStrategy) srStrategy
   // // .getPredictivePlanningStrategy()).getTerminationCondition();
   // // if(tc instanceof ThresholdCondition) {
   // // final ThresholdCondition thc = (ThresholdCondition) tc;
   // // thc.setThreshold(utils.getFitnessFunction().getObjectiveIndex("SolutionLength"),
   // // (double) stepsOfIdleComputedPlan - afterSteps);
   // // }
   // //
   // // // ((ConditionReplanningStrategy)
   // // // srStrategy.getPredictivePlanningStrategy()).setTerminationCondition(moc);
   // // }
   //
   // final SearchResult res = replanningBySearch(runtimeGraph, expName, run, curPlan, planIterator.nextIndex(),
   // srStrategy.getPredictivePlanningStrategy(), curBestObj);
   //
   // final Solution s = res.getOptimalSolutionForObjective(
   // Map.of(utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName), objOfIdleComputedPlan),
   // utils.getFitnessFunction().getObjectiveIndex("SolutionLength"));
   //
   // switch(srStrategy.getPredictiveReplanningType()) {
   // case TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED:
   // final TimedObjectiveCondition toc = (TimedObjectiveCondition) moc;
   // prr.addPredictiveRunResult(afterSteps, s.getObjectives(), res.getExecutionEvaluations(),
   // res.getExecutionTime(), toc.getTimeTilObjectivesSatisfied() / Math.pow(10, 9),
   // toc.getMaxNanosIfObjectiveSatisfied() / Math.pow(10, 9));
   // break;
   // default:
   // prr.addPredictiveRunResult(afterSteps, s.getObjectives(), res.getExecutionEvaluations(),
   // res.getExecutionTime(), -1, -1);
   //
   // }
   //
   // // runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations());
   // // p.subheader("Predictive Replanning", verbose);
   // p.str(String.format(
   // "Pred. Replanning (%d steps): Thresholds: %s -> Found %s after %d iterations (%f seconds)",
   // afterSteps, moc.toString(), Arrays.toString(s.getObjectives()), res.getExecutionEvaluations(),
   // res.getExecutionTime()));
   //
   // }
   // }
   // return prr;
   //
   // }

   // private List<ITransformationVariable> replanning(final EGraph runtimeGraph, final String experimentName,
   // final int run, final List<ITransformationVariable> curPlan, final int curPlanPos, final ReplanningStrategy ps,
   // final ReactiveRunResult runResult, final double curBestObj) {
   // SearchReplanningStrategy srStrategy = null;
   // SearchResult res = null;
   // TransformationSolution bestSolution = null;
   // List<ITransformationVariable> returnPlan = null;
   // switch(ps.getRepairStrategy()) {
   // case REPLAN_FOR_CONDITION:
   // srStrategy = (SearchReplanningStrategy) ps;
   // res = replanningBySearch(runtimeGraph, experimentName, run, new ArrayList<>(curPlan), curPlanPos,
   // srStrategy, curBestObj);
   //
   // bestSolution = getOptimalSolutionFromReplanning(ps, res);
   //
   // runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations(),
   // getObjectiveFromTS(bestSolution, evalObjectiveName));
   // p.subheader("New plan", verbose).plan(bestSolution, verbose);
   //
   // returnPlan = bestSolution.getVariablesAsList();
   // break;
   // case REPLAN_FOR_EVALUATIONS:
   // srStrategy = (SearchReplanningStrategy) ps;
   // res = replanningBySearch(runtimeGraph, experimentName, run, new ArrayList<>(curPlan), curPlanPos,
   // srStrategy, curBestObj);
   //
   // bestSolution = getOptimalSolutionFromReplanning(ps, res);
   //
   // runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations(),
   // getObjectiveFromTS(bestSolution, evalObjectiveName));
   // p.subheader("New plan", verbose).plan(bestSolution, verbose);
   // returnPlan = bestSolution.getVariablesAsList();
   // break;
   // case NAIVE:
   // returnPlan = curPlan.subList(curPlanPos, curPlan.size());
   // break;
   // default:
   // throw new RuntimeException("Repair strategy not implemented!");
   // }
   //
   // // predictive replanning
   // if(ps instanceof SearchReplanningStrategy) {
   // final SearchReplanningStrategy srs = (SearchReplanningStrategy) ps;
   // if(srs.isPredictivePlanningEnabled()) {
   //
   // final PredictiveRunResult prr = predictiveReplanning(MomotUtil.copy(runtimeGraph), experimentName, run, srs,
   // new ArrayList<>(curPlan), curPlanPos, calculateObjectiveOnModel(evalObjectiveName,
   // MomotUtil.copy(runtimeGraph), new ArrayList<>(returnPlan)),
   // curPlan.size(), curBestObj);
   // runResult.addPredictiveRunResult(prr);
   // // execute simulated runs, get plans, evaluate and return results
   // }
   // }
   // return returnPlan;
   //
   // }

   // private SearchResult replanningBySearch(final EGraph runtimeGraph, final String experimentName, final int run,
   // final List<ITransformationVariable> curPlan, final int curPlanPos, final SearchReplanningStrategy srStrategy,
   // final double curBestObj) {
   //
   // final List<List<ITransformationVariable>> initialPopulationSeq = new ArrayList<>();
   // if(srStrategy.getHeuristic() != null) {
   // final List<ITransformationVariable> heuristicSeq = srStrategy.getHeuristic()
   // .getInitialPopulationTs(MomotUtil.copy(runtimeGraph), executor.copy(), solutionLength);
   // initialPopulationSeq.addAll(Stream.generate(() -> new ArrayList<>(heuristicSeq))
   // .limit((long) (srStrategy.getHeuristicPortion() * populationSize)).collect(Collectors.toList()));
   // }
   //
   // if(srStrategy.getReseedingPortion() > 0) {
   // initialPopulationSeq.addAll(Stream.generate(() -> new ArrayList<>(curPlan.subList(curPlanPos, curPlan.size())))
   // .limit((long) (this.populationSize * srStrategy.getReseedingPortion())).collect(Collectors.toList()));
   // }
   //
   // return srStrategy.replan(searchInstance, runtimeGraph, srStrategy.getReplanningAlgorithm(), experimentName, run,
   // solutionLength - curPlanPos, populationSize, initialPopulationSeq.isEmpty() ? null : initialPopulationSeq,
   // curBestObj, true);
   // }

   private ReactiveRunResult run(final EGraph runtimeGraph, final Planning planning) {

      final ModelRuntimeEnvironment mre = new ModelRuntimeEnvironment(runtimeGraph);

      final ReactiveRunResult runResult = new ReactiveRunResult();
      int failedExecutions = 0;

      disturber.reset(mre);
      executor.setModelRuntimeEnvironment(mre);
      // Create initial plan

      final PlanningStrategy ps = planning.getPlanningStrategy();
      final ReplanningStrategy rps = planning.getReplanningStrategy();

      final SearchConfiguration initialSearchConf = new SearchConfiguration(runtimeGraph, ps, solutionLength,
            populationSize);

      // final List<List<ITransformationVariable>> initialSolutionSeqs = null;
      // if(ps.getHeuristic() != null) {
      // final List<ITransformationVariable> heuristicSeq = ps.getHeuristic()
      // .getInitialPopulationTs(MomotUtil.copy(runtimeGraph), executor.copy(), solutionLength);
      //
      // initialSolutionSeqs = Stream.generate(() -> new ArrayList<>(heuristicSeq))
      // .limit((long) (ps.getHeuristicPortion() * populationSize)).collect(Collectors.toList());
      // }

      // final SearchResult res = searchInstance.performSearch(runtimeGraph,
      // ps.getInitialSearchAlgorithm(), expName, run, ps.getInitialSearchEvaluations(),
      // ps.getTerminationCriterion(), solutionLength, populationSize, initialSolutionSeqs, 0, false);

      List<ITransformationVariable> plan = planner.plan(initialSearchConf, ps, runResult);

      if(disturber instanceof IRangeDisturber) {
         ((IRangeDisturber) disturber).setup(plan.size());
      }
      // EGraph graphOfLastPlanning = MomotUtil.copy(runtimeGraph);

      for(ListIterator<ITransformationVariable> it = plan.listIterator(); it.hasNext();) {

         // Execute plan iteratively
         final ITransformationVariable nextStep = it.next();
         final boolean success = executor.execute(nextStep);
         if(success) {
            mre.addExecutedUnit(nextStep);
         } else {
            failedExecutions++;
         }

         if(it.hasNext()) {
            final ITransformationVariable nextToExecute = plan.get(it.nextIndex());
            final EGraph prePotentialDisturbanceGraph = MomotUtil.copy(mre.getGraph());
            final Disturbance d = disturber.pollForDisturbance(it.nextIndex() - 1, plan.size(), nextToExecute);

            // If no disturbance occured, d = null
            if(d != null) {

               p.printChangeDetails(it.nextIndex(), plan.size(), plan.subList(0, it.nextIndex()), verbose)
                     .str(this.utils.getReprFromEGraph(mre.getGraph()), verbose).header2(
                           String.format("Using planning strategy (%s)...", planning.getReplanningStrategy()), verbose);
               runResult.addDisturbance(d);

               runResult.addPostDisturbanceObjective(MomotUtil.calculateObjectiveOnModel(evalObjectiveName,
                     utils.getFitnessFunction(), mre.getGraph(), new ArrayList<>()));
               runResult.addPreDisturbanceObjective(MomotUtil.calculateObjectiveOnModel(evalObjectiveName,
                     utils.getFitnessFunction(), prePotentialDisturbanceGraph, new ArrayList<>()));

               final List<ITransformationVariable> remainingPlan = new ArrayList<>(
                     plan.subList(it.nextIndex(), plan.size()));

               List<ITransformationVariable> updatedPlan = null;

               if(!rps.isNaive()) {

                  final SearchConfiguration replanningConf = new SearchConfiguration(runtimeGraph, rps, solutionLength,
                        populationSize,
                        MomotUtil.calculateObjectiveOnModel(evalObjectiveName, utils.getFitnessFunction(),
                              mre.getGraph(), new ArrayList<>()),
                        this.utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName));

                  if(rps.isReseedingPopulationEnabled()) {
                     replanningConf.addSeedToPopulation(new ArrayList<>(plan.subList(it.nextIndex(), plan.size())),
                           rps.getReseedingPortion());
                  }

                  updatedPlan = planner.replan(replanningConf, rps, remainingPlan, runResult);

               }

               // planner returns null if no valid search settings, e.g., naive repair!
               plan = updatedPlan != null ? updatedPlan : remainingPlan;

               // Reset to first plan step
               it = plan.listIterator();
               // clear runtime units
               mre.getExecutedUnits().clear();
            }
         }
      }

      // final ModelRuntimeSnapshot lastSnapshot = runtimeSnapshots.get(runtimeSnapshots.size() - 1);
      p.header2("Final model", verbose).str(utils.getReprFromEGraph(mre.getGraph()), verbose);

      runResult.setFailedExecutions(failedExecutions);
      runResult.setFinalObjective(MomotUtil.calculateObjectiveOnModel(evalObjectiveName, utils.getFitnessFunction(),
            mre.getGraph(), new ArrayList<>()));
      return runResult;
   }

   public Map<String, ReactiveExperimentResult> runExperiment(final String listenerBase) {

      final Map<String, ReactiveExperimentResult> resultPerExperiment = new HashMap<>();

      int experimentId = 0;
      for(final Planning planning : plannings) {
         final String expName = getExperimentName(planning, experimentId);
         // resultPerExperiment.put(expName, new ReactiveExperimentResult());

         final ReactiveExperimentResult experimentStats = new ReactiveExperimentResult();
         for(int j = 0; j < nr_runs; j++) {
            p.property("Configuration",
                  String.format("%s (Strategy: %s) -> run %d/%d",
                        planning.getPlanningStrategy().getInitialSearchAlgorithm(), planning.getReplanningStrategy(),
                        j + 1, nr_runs));
            planner.setupEventListeners(listenerBase, expName, j + 1);

            final ReactiveRunResult reactiveRunRes = run(MomotUtil.copy(initialGraph), planning);
            experimentStats.addRunResult(reactiveRunRes);
         }
         resultPerExperiment.put(expName, experimentStats);
         experimentId++;

      }
      return resultPerExperiment;
   }

}
