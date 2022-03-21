package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.problem.solution.variable.TransformationPlaceholderVariable;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PlanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.ReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.SearchReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.result.ReactiveResult;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

   public Map<String, ReactiveResult> resultMap;

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

   private List<TransformationSolution> getBestSolutionNTimes(final EGraph g, final int n,
         final List<ITransformationVariable> trafoVars, final int fillWithPlaceholdersUntil) {
      final List<TransformationSolution> solutions = new ArrayList<>();
      final TransformationSolution[] solutionArr = new TransformationSolution[n];

      trafoVars.addAll(Stream.generate(TransformationPlaceholderVariable::new)
            .limit(fillWithPlaceholdersUntil - trafoVars.size()).collect(Collectors.toList()));

      Arrays.fill(solutionArr, new TransformationSolution(MomotUtil.copy(g), trafoVars,
            utils.getFitnessFunction().evaluatesNrObjectives()));

      return List.of(solutionArr);

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

   private List<ITransformationVariable> replanning(final EGraph runtimeGraph, final EGraph graphOfLastPlanning,
         final List<ITransformationVariable> curPlan, final int curPlanPos, final ReplanningStrategy ps) {
      switch(ps.getRepairStrategy()) {
         case REPLAN_FOR_EVALUATIONS:
            final SearchReplanningStrategy srStrategy = (SearchReplanningStrategy) ps;
            final SearchResult res = ((SearchReplanningStrategy) ps).replan(searchInstance, runtimeGraph,
                  srStrategy.getReplanningAlgorithm(), solutionLength, populationSize,
                  srStrategy.isDoReusePreviousPlan()
                        ? getBestSolutionNTimes(graphOfLastPlanning,
                              (int) (srStrategy.getReusePortion() * populationSize),
                              curPlan.subList(curPlanPos, curPlan.size()), solutionLength)
                        : null);

            p.subheader("New plan", verbose).plan(res.getOptimalSolution(), verbose);
            return res.getOptimalPlan();
         case NAIVE:
            return curPlan.subList(curPlanPos, curPlan.size());
         default:
            throw new RuntimeException("Repair strategy not implemented!");
      }
   }

   private float run(final EGraph runtimeGraph, final PlanningStrategy ps) {

      final ModelRuntimeEnvironment mre = new ModelRuntimeEnvironment(runtimeGraph);

      disturber.reset();
      disturber.setModelRuntimeEnvironment(mre);
      executor.setModelRuntimeEnvironment(mre);
      // Create initial plan

      p.header2("INITIAL PLAN", verbose);
      final SearchResult res = searchInstance.performSearch(runtimeGraph, ps.getInitialSearchAlgorithm(),
            ps.getInitialSearchEvaluations(), ps.getTerminationCriterion(), solutionLength, populationSize, null);

      p.plan(res.getOptimalSolution(), verbose);

      List<ITransformationVariable> curPlan = res.getOptimalPlan();
      EGraph graphOfLastPlanning = MomotUtil.copy(runtimeGraph);

      for(ListIterator<ITransformationVariable> it = curPlan.listIterator(); it.hasNext();) {

         // Execute plan iteratively
         final ITransformationVariable nextStep = it.next();
         final boolean success = executor.execute(nextStep);
         if(success) {
            mre.addExecutedUnit(nextStep);
         }

         if(it.hasNext()) {
            final ITransformationVariable nextToExecute = curPlan.get(it.nextIndex());
            if(disturber.pollForDisturbance(it.nextIndex(), curPlan.size(), nextToExecute)) {

               p.printChangeDetails(it.nextIndex(), curPlan.size(), curPlan.subList(0, it.nextIndex()), verbose)
                     .str(this.utils.getReprFromEGraph(mre.getGraph()), verbose)
                     .header2(String.format("Using planning strategy (%s)...", ps.getReplanningStrategy()), verbose);

               // change occured, use repairstrategy
               curPlan = replanning(MomotUtil.copy(runtimeGraph), graphOfLastPlanning, curPlan, it.nextIndex(),
                     ps.getReplanningStrategy());

               // Reset to first plan step
               it = curPlan.listIterator();

               // Remember last planning state and clear runtime units
               graphOfLastPlanning = MomotUtil.copy(runtimeGraph);
               mre.getExecutedUnits().clear();
            }
         }
      }

      // final ModelRuntimeSnapshot lastSnapshot = runtimeSnapshots.get(runtimeSnapshots.size() - 1);
      p.header2("Final model", verbose).str(utils.getReprFromEGraph(mre.getGraph()), verbose);

      return calculateObjectiveOnFinalModel(utils.getFitnessFunction(), evalObjectiveName, graphOfLastPlanning,
            mre.getExecutedUnits());
   }

   public Map<String, ReactiveResult> runExperiment() {

      final Map<String, ReactiveResult> resultPerExperiment = new HashMap<>();

      int experimentId = 0;
      for(final PlanningStrategy strategy : planningStrategies) {
         final String expName = getExperimentName(strategy, experimentId++);
         resultPerExperiment.put(expName, new ReactiveResult());
         for(int j = 0; j < nr_runs; j++) {
            p.property("Configuration", String.format("%s (Strategy: %s) -> run %d/%d",
                  strategy.getInitialSearchAlgorithm(), strategy.getReplanningStrategy(), j + 1, nr_runs));
            resultPerExperiment.get(expName).addFinalObjective(run(MomotUtil.copy(initialGraph), strategy));
         }

      }
      return resultPerExperiment;
   }

}
