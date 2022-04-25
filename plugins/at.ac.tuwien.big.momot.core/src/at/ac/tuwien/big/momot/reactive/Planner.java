package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.moea.experiment.executor.listener.AbstractProgressListener;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PlanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PredictiveReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PredictiveReplanningStrategy.PredictiveReplanningType;
import at.ac.tuwien.big.momot.reactive.planningstrategy.ReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.SearchConfiguration;
import at.ac.tuwien.big.momot.reactive.result.PredictiveRunResult;
import at.ac.tuwien.big.momot.reactive.result.ReactiveRunResult;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;
import at.ac.tuwien.big.momot.reactive.result.SolutionAnalyzer;
import at.ac.tuwien.big.momot.search.criterion.ThresholdCondition;
import at.ac.tuwien.big.momot.search.criterion.TimedObjectiveCondition;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.TerminationCondition;

public class Planner {

   private final IReactiveSearchInstance searchInstance;
   private final IReactiveUtilities utils;
   private final List<AbstractProgressListener> eventListeners;
   private final Printer p;
   private final String evalObjective;

   public Planner(final IReactiveSearchInstance instance, final String evalObjective, final Printer p,
         final IReactiveUtilities utils) {
      this.searchInstance = instance;
      this.p = p;
      this.eventListeners = new ArrayList<>();
      this.evalObjective = evalObjective;
      this.utils = utils;
   }

   private TerminationCondition adaptTerminationCondition(final int stepsIntoFuture, final TerminationCondition tc,
         final PredictiveReplanningStrategy pps) {
      switch(pps.getPredictiveReplanningType()) {
         case TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED:
            final TimedObjectiveCondition toc = TimedObjectiveCondition
                  .create(((ThresholdCondition) tc).getThresholds());
            toc.setMaxSecondsIfObjectiveSatisfied(
                  (long) (pps.getSecondsPerExecutionStep() * stepsIntoFuture * Math.pow(10, 9)));

            return toc;
         default:
            throw new RuntimeException("predictive replanning type not implemented");
      }
   }

   public void addEventListener(final AbstractProgressListener el) {
      this.eventListeners.add(el);
   }

   private void addPredictiveRunResult(final PredictiveReplanningStrategy prs, final TerminationCondition tc,
         final TransformationSolution selection, final int afterSteps, final SearchResult res,
         final PredictiveRunResult prr) {
      switch(prs.getPredictiveReplanningType()) {
         case TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED:
            final TimedObjectiveCondition toc = (TimedObjectiveCondition) tc;
            prr.addPredictiveRunResult(afterSteps, selection.getObjectives(), res.getExecutionEvaluations(),
                  res.getExecutionTime(), toc.getTimeTilObjectivesSatisfied() / Math.pow(10, 9),
                  toc.getMaxNanosIfObjectiveSatisfied() / Math.pow(10, 9));
            break;
         default:
            prr.addPredictiveRunResult(afterSteps, selection.getObjectives(), res.getExecutionEvaluations(),
                  res.getExecutionTime(), -1, -1);

      }
   }

   private TerminationCondition createTerminationConditionForPredictiveReplanningType(
         final PredictiveReplanningType type, final EGraph idleComputedResultGraph) {
      switch(type) {
         case TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED:
            final double objOfIdleComputedPlan = MomotUtil.calculateObjectiveOnModel(evalObjective,
                  this.utils.getFitnessFunction(), idleComputedResultGraph, new ArrayList<>());
            return TimedObjectiveCondition
                  .create(Map.of(utils.getFitnessFunction().getObjectiveIndex(evalObjective), objOfIdleComputedPlan));
         default:
            throw new RuntimeException("predictive replanning type not implemented");
      }
   }

   public IReactiveSearchInstance getSearchInstance() {
      return searchInstance;
   }

   public List<ITransformationVariable> plan(final SearchConfiguration conf, final PlanningStrategy ps,
         final ReactiveRunResult runResult) {

      final SearchResult res = searchInstance.performSearch(conf);

      // Determine best solution dep. on selection by objective
      final TransformationSolution selection = SolutionAnalyzer.getOptimalSolution(res.getPopulation(),
            ps.getPrioritizedObjectiveForSelection());

      // Add to result data
      runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations(),
            selection.getObjective(ps.getPrioritizedObjectiveForSelection()));

      // p.header2("INITIAL PLAN", verbose);
      // p.plan(res.getOptimalSolutionForObjective(utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName)),
      // verbose);

      // Return vars of best solution
      return new ArrayList<>(selection.getVariablesAsList());
   }

   public List<ITransformationVariable> replan(final SearchConfiguration conf, final ReplanningStrategy rps,
         final List<ITransformationVariable> remainingPlan, final ReactiveRunResult runResult) {

      final SearchResult res = searchInstance.performSearch(conf, this.eventListeners);

      // Determine best solution dep. on selection by objective
      final TransformationSolution selection = SolutionAnalyzer.getOptimalSolution(res.getPopulation(),
            rps.getPrioritizedObjectiveForSelection());

      // Add to result data
      runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations(),
            selection.getObjective(rps.getPrioritizedObjectiveForSelection()));

      if(rps.hasPredictivePlanningStrategy()) {
         final PredictiveReplanningStrategy pps = rps.getPredictiveReplanningStrategy();

         final PredictiveRunResult prr = this.runPredictivePlanningSimulation(conf.getStartingState(), pps,
               remainingPlan, conf.getSolutionLength(), conf.getPopulationSize(), selection.getResultGraph(),
               selection.getObjectives());

         runResult.addPredictiveRunResult(prr);
      }

      // Return vars of best solution
      return new ArrayList<>(selection.getVariablesAsList());
   }

   private PredictiveRunResult runPredictivePlanningSimulation(final EGraph startingState,
         final PredictiveReplanningStrategy pps, final List<ITransformationVariable> remainingPlan,
         final int idlePlanningSolutionLength, final int populationSize, final EGraph idlePlannedResultGraph,
         final double[] idlePlannedObjectives) {

      final Executor simExecutor = SearchConfiguration.getExecutor().copy();
      final ModelRuntimeEnvironment simMre = new ModelRuntimeEnvironment(startingState);
      simExecutor.setModelRuntimeEnvironment(simMre);

      // final TerminationCondition tc = createTerminationConditionForPredictiveReplanningType(
      // pps.getPredictiveReplanningType(), idlePlannedResultGraph);

      final PredictiveRunResult prr = new PredictiveRunResult(idlePlannedObjectives);

      final Iterator<Integer> simulateStepsIterator = pps.getPredictivePlanningAfterXSteps().iterator();
      final ListIterator<ITransformationVariable> planIterator = new ArrayList<>(remainingPlan).listIterator();

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

            final SearchConfiguration searchConf = new SearchConfiguration(simMre.getGraph(), pps,
                  idlePlanningSolutionLength, populationSize);

            // todo ADD SEED IF SET
            if(pps.isReseedingPopulationEnabled()) {
               searchConf.addSeedToPopulation(new ArrayList<>(remainingPlan), pps.getReseedingPortion());
            }

            searchConf.setTerminationCondition(
                  this.adaptTerminationCondition(afterSteps, searchConf.getTerminationCondition(), pps));

            // final SearchResult res = replanningBySearch(runtimeGraph, expName, run, curPlan,
            // planIterator.nextIndex(),
            // srStrategy.getPredictivePlanningStrategy(), curBestObj);
            //
            // final Solution s = res.getOptimalSolutionForObjective(
            // Map.of(utils.getFitnessFunction().getObjectiveIndex(evalObjectiveName), objOfIdleComputedPlan),
            // utils.getFitnessFunction().getObjectiveIndex("SolutionLength"));
            final SearchResult res = searchInstance.performSearch(searchConf);

            final TransformationSolution selection = SolutionAnalyzer.getOptimalSolution(res.getPopulation(),
                  pps.getPrioritizedObjectiveForSelection());

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
            this.addPredictiveRunResult(pps, searchConf.getTerminationCondition(), selection, afterSteps, res, prr);
            // runResult.addPlanningStats(res.getExecutionTime(), res.getExecutionEvaluations());
            // p.subheader("Predictive Replanning", verbose);
            p.str(String.format(
                  "Pred. Replanning (%d steps): Thresholds: %s -> Found %s after %d iterations (%f seconds)",
                  afterSteps, searchConf.getTerminationCondition().toString(),
                  Arrays.toString(selection.getObjectives()), res.getExecutionEvaluations(), res.getExecutionTime()));

         }
      }
      return prr;
   }

   public void setupEventListeners(final String listenerDir, final String experimentName, final int runId) {
      for(final AbstractProgressListener apl : eventListeners) {
         apl.setup(listenerDir, runId, experimentName);
      }
   }

}
