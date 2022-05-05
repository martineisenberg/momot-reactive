package at.ac.tuwien.big.momot.examples.reactive;

import at.ac.tuwien.big.moea.experiment.executor.listener.CurrentBestObjectiveListener;
import at.ac.tuwien.big.moea.experiment.executor.listener.GenerationExecutionTimeListener;
import at.ac.tuwien.big.moea.experiment.executor.listener.SeedreuseProportionListener;
import at.ac.tuwien.big.moea.util.CSVUtil;
import at.ac.tuwien.big.momot.domain.Heuristic;
import at.ac.tuwien.big.momot.examples.stack.StackHeuristic;
import at.ac.tuwien.big.momot.examples.stack.StackSearch;
import at.ac.tuwien.big.momot.examples.stack.StackSolutionWriter;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.reactive.AbstractDisturber;
import at.ac.tuwien.big.momot.reactive.Executor;
import at.ac.tuwien.big.momot.reactive.IReactiveUtilities;
import at.ac.tuwien.big.momot.reactive.Planner;
import at.ac.tuwien.big.momot.reactive.Printer;
import at.ac.tuwien.big.momot.reactive.ReactiveExperiment;
import at.ac.tuwien.big.momot.reactive.error.ErrorOccurence;
import at.ac.tuwien.big.momot.reactive.error.ErrorType;
import at.ac.tuwien.big.momot.reactive.planningstrategy.Planning;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PlanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.ReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.result.PredictiveRunResult;
import at.ac.tuwien.big.momot.reactive.result.PredictiveRunResult.PredictiveRunPlanningStats;
import at.ac.tuwien.big.momot.reactive.result.ReactiveExperimentResult;
import at.ac.tuwien.big.momot.util.MomotUtil;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/*
 * This class allows for testing combinations of parameters, e.g., for several disturber settings
 * and planning strategies, at once.
 */

public class ReactivePlanningSuite {
   // final static double OBJ_THRESHOLD = 20;

   // final static double OBJ_THRESHOLD = .0;
   final static double OBJ_THRESHOLD = 13.6795;
   // final static double OBJ_THRESHOLD = 25;
   // final static double OBJ_THRESHOLD = 1.5905;
   // final static double OBJ_THRESHOLD = 2.0;

   final static int OBJ_INDEX = 0; // standard deviation for stack problem

   final static Map<Integer, Double> objectiveThresholds = Map.of(OBJ_INDEX, OBJ_THRESHOLD);

   /* EXPERIMENT CONFIGURATION */

   final static Heuristic h = StackHeuristic.getInstance();
   // ----------- Planning Cases ------------ //
   final static List<Planning> PLANNING_STRATEGIES = Arrays.asList(
         //
         // Planning.create(PlanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds),
         // ReplanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds)
         // .castAsReplanningStrategy()
         // .withPredictivePlanning(PredictiveReplanningStrategy
         // .create("NSGAII", 1, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
         // PredictiveReplanningType.TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED, 10)
         // .withObjectiveThresholds(objectiveThresholds).castAsPredictiveReplanningStrategy())));

         // Planning.create(PlanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds),
         // ReplanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds)
         // .castAsReplanningStrategy()),
         // Planning.create(PlanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds),
         // ReplanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds)
         // .castAsReplanningStrategy().withReseedingInitialization(.1)));

         Planning.create(PlanningStrategy.create("NSGAII", 0).withObjectiveThresholds(objectiveThresholds),
               ReplanningStrategy.create("NSGAII", 0).withMaxEvaluations(20000).castAsReplanningStrategy()),
         Planning.create(PlanningStrategy.create("NSGAII", 0).withObjectiveThresholds(objectiveThresholds),
               ReplanningStrategy.create("NSGAII", 0).withMaxEvaluations(20000).castAsReplanningStrategy()
                     .withReseedingInitialization(.1)),
         Planning.create(PlanningStrategy.create("NSGAII", 0).withObjectiveThresholds(objectiveThresholds),
               ReplanningStrategy.naive()));

   //
   // PlanningStrategy.create("NSGAII", MinimumObjectiveCondition.create(objectiveThresholds),
   // StackHeuristic.getInstance(), .1,
   // ConditionReplanningStrategy.create("NSGAII", MinimumObjectiveCondition.create(objectiveThresholds))
   // .withHeuristic(StackHeuristic.getInstance(), .1).withPlanReuse(.1)
   // .withPredictivePlanning(PredictiveReplanningType.TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED,
   // List.of(2, 5, 10), "NSGAII", StackHeuristic.getInstance(), .1))

   // PlanningStrategy.create("NSGAII", MinimumObjectiveCondition.create(objectiveThresholds),
   // ConditionReplanningStrategy.create("NSGAII", MinimumObjectiveCondition.create(objectiveThresholds))
   // .withPlanReuse(0.2)));
   // PlanningStrategy.create("NSGAII_1", MinimumObjectiveCondition.create(objectiveThresholds),
   // ConditionReplanningStrategy.create("NSGAII_1", MinimumObjectiveCondition.create(objectiveThresholds))));
   // PlanningStrategy.create("NSGAII", 10000,
   // EvaluationReplanningStrategy.create("NSGAII", 10000).withPlanReuse(0.2)),
   // PlanningStrategy.create("NSGAII", 10000, NaivePlanningStrategy.get()));

   // ----------- Model, Algorithm ------------ //
   final static String INITIAL_MODEL = Paths.get("model", "model_fifty_stacks_std50_2500_27.359.xmi").toString();
   // final static String INITIAL_MODEL = Paths.get("model", "gen100_1to100.xmi").toString();

   // final static String INITIAL_MODEL = Paths.get("model", "model_ten_stacks.xmi").toString();

   final static String HENSHIN_MODULE = Paths.get("model", "stack.henshin").toString();
   final static int MAX_SOLUTION_LENGTH = 200;
   final static int POPULATION_SIZE = 100;
   private static final int EXPERIMENT_RUNS = 30;
   final static String EVAL_OBJECTIVE = "Standard Deviation";
   final static String OBJECTIVE_SELECTION_NAME = "SolutionLength";

   /* DISTURBER CONFIGURATION */
   private static final List<ErrorType> ERROR_TYPE_LIST = ImmutableList.of(ErrorType.ADD_STACKS);
   private static final List<ErrorOccurence> ERROR_OCCURENCE_LIST = ImmutableList.of(ErrorOccurence.FIRST_10_PERCENT,
         ErrorOccurence.MIDDLE_10_PERCENT, ErrorOccurence.LAST_10_PERCENT);
   final static int ERRORS_PER_DISTURBANCE = 5;

   /* OUTPUTS */
   final static boolean VERBOSE = false;
   final static boolean RECORD_OBJECTIVE_DEVELOPMENT = true;
   final static boolean RECORD_GENERATIONAL_EXECUTION_TIME = true;
   final static boolean RECORD_SEEDREUSE_INFORMATION = true;

   final static String PRINT_DIR = Paths.get("output", "simulation").toString();
   // final static String PRINT_FILENAME = "50stacks_1to100_threshold_10.9436_predictive";
   // final static String PRINT_FILENAME = "50stacks_1to10_steps2_5_10";
   final static String PRINT_FILENAME = "1to100_initial_dec50perc_replan_20k";

   // final static String PRINT_FILENAME = "test";

   final static Printer PS_OUT = new Printer(Paths.get(PRINT_DIR, PRINT_FILENAME + ".txt").toString(),
         new StackSolutionWriter(new StackUtils().getFitnessFunction()));
   final static boolean PRINT_RESULTS_TO_CSV = true;
   // final static Printer PS_OUT = new Printer(System.out,
   // new StackSolutionWriter(new StackUtils().getFitnessFunction()));

   private static <T> double getAverageOverLastElementsOfSubLists(final List<List<T>> allRunsList) {
      double avgRuntime = 0;
      for(final List<T> element : allRunsList) {
         avgRuntime += (double) element.get(element.size() - 1);
      }

      return avgRuntime / allRunsList.size();
   }

   private static <T> Object[] getSumOfSubListsOfNestedLists(final List<List<T>> allRunsRuntimesList) {
      final List<Double> overallRuntimes = new ArrayList<>();
      for(final List<T> runList : allRunsRuntimesList) {
         overallRuntimes.add(runList.stream().mapToDouble(f -> (double) f).sum());
      }
      return overallRuntimes.toArray();
   }

   public static void main(final String[] args) {
      StackPackage.eINSTANCE.eClass();
      final HenshinResourceSet hrs = new HenshinResourceSet();
      hrs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

      final Resource initialModelRes = hrs.getResource(INITIAL_MODEL);

      final IReactiveUtilities utils = new StackUtils();
      final Printer p = PS_OUT;
      System.setOut(p.getPrintStream());

      final List<List<Object>> combinations = com.google.common.collect.Lists.cartesianProduct(ERROR_TYPE_LIST,
            ERROR_OCCURENCE_LIST);
      for(final List<Object> o : combinations) {

         final ErrorType eType = (ErrorType) o.get(0);
         final ErrorOccurence eOccurence = (ErrorOccurence) o.get(1);
         final AbstractDisturber disturber = new RangeDisturber.RangeDisturberBuilder().type(eType)
               .occurence(eOccurence).maxNrOfDisturbances(1).errorsPerDisturbance(ERRORS_PER_DISTURBANCE).build();

         final Executor executor = new Executor(HENSHIN_MODULE);
         final Planner planner = new Planner(new StackSearch(), EVAL_OBJECTIVE, PS_OUT, utils);

         if(RECORD_OBJECTIVE_DEVELOPMENT) {
            planner.addEventListener(new CurrentBestObjectiveListener(OBJ_INDEX, POPULATION_SIZE));
         }

         if(RECORD_GENERATIONAL_EXECUTION_TIME) {
            planner.addEventListener(new GenerationExecutionTimeListener(POPULATION_SIZE));
         }

         if(RECORD_SEEDREUSE_INFORMATION) {
            planner.addEventListener(new SeedreuseProportionListener());
         }
         final ReactiveExperiment experiment = new ReactiveExperiment(MomotUtil.eGraphOf(initialModelRes, true),
               PLANNING_STRATEGIES, utils, planner, disturber, executor, EVAL_OBJECTIVE, OBJECTIVE_SELECTION_NAME,
               EXPERIMENT_RUNS, MAX_SOLUTION_LENGTH, POPULATION_SIZE, p, VERBOSE);

         p.header1(String.format("Experiment %s, %s", eType, eOccurence));

         final String listenerOutPath = Paths.get(PRINT_DIR, "listeners", PRINT_FILENAME + "_" + disturber.toString())
               .toString();
         final Map<String, ReactiveExperimentResult> results = experiment.runExperiment(listenerOutPath);

         p.header2(String.format("RESULTS (%s, %s)", eType, eOccurence));

         printExperimentResults(p, results);

         if(PRINT_RESULTS_TO_CSV) {
            printExperimentResultsToCSV(Paths.get(PRINT_DIR, PRINT_FILENAME + "_" + disturber.toString()).toString(),
                  results);
         }

      }

      p.close();
   }

   private static void printExperimentResults(final Printer p, final Map<String, ReactiveExperimentResult> r) {

      for(final Entry<String, ReactiveExperimentResult> e : r.entrySet()) {
         p.subheader(e.getKey());
         p.property(String.format("Final model objectives (%s)", EVAL_OBJECTIVE),
               Arrays.toString(e.getValue().getFinalObjectives().toArray()));

         p.property("Final failed executions", Arrays.toString(e.getValue().getFinalFailedExecutions().toArray()));

         p.property("Final runtimes (for all plannings, per run)",
               Arrays.toString(getSumOfSubListsOfNestedLists(e.getValue().getRuntimes())));

         p.property("Final evaluations needed (for all plannings, per run)",
               Arrays.toString(getSumOfSubListsOfNestedLists(e.getValue().getEvaluations()))).newline();

         p.property("Disturbances (atIteration, type, plannedIterations): ", e.getValue().getDisturbances().toString());

         p.property(String.format("Avg. final objective (%s)", EVAL_OBJECTIVE),
               Double.toString(Arrays.stream(
                     e.getValue().getFinalObjectives().stream().mapToDouble(f -> f != null ? f : Double.NaN).toArray())
                     .average().getAsDouble()));

         p.property("Avg. runtime for replanning",
               String.valueOf(getAverageOverLastElementsOfSubLists(e.getValue().getRuntimes())));

         p.property("Avg. evaluations for replanning",
               String.valueOf(getAverageOverLastElementsOfSubLists(e.getValue().getEvaluations()))).newline();
      }

   }

   private static void printExperimentResultsToCSV(final String path, final Map<String, ReactiveExperimentResult> r) {
      final List<String> header = new ArrayList<>(Arrays.asList("variant", "run", "initial_planning_time",
            "initial_planning_evaluations", "initial_planning_objective", "initial_pre_disturbance_std",
            "initial_post_disturbance_std", "last_replanning_time", "last_replanning_evaluations",
            "overall_replanning_time", "overall_replanning_evaluations", "final_objective_value", "failed_executions"));

      final List<String> headerDist = new ArrayList<>(
            Arrays.asList("variant", "run", "at_iteration", "planned_iterations", "disturbance_type"));

      final List<String> headerPred = new ArrayList<>(
            Arrays.asList("variant", "run", "idle_computed_objectives", "predicted_steps", "pred_plan_objectives",
                  "pred_plan_iter", "pred_plan_runtime", "runtime_obj_satisfied", "max_runtime_if_obj_satisfied"));

      final List<String[]> dataLines = new ArrayList<>();
      final List<String[]> dataLinesDist = new ArrayList<>();
      final List<String[]> dataLinesPred = new ArrayList<>();

      for(final Entry<String, ReactiveExperimentResult> e : r.entrySet()) {

         final List<Double> initialPlanningTimes = new ArrayList<>();
         final List<Double> initialPlanningEvaluations = new ArrayList<>();
         final List<Double> lastReplanningTimes = new ArrayList<>();
         final List<Double> lastReplanningEvaluations = new ArrayList<>();
         final List<Double> overallReplanningEvaluations = new ArrayList<>();
         final List<Double> overallReplanningTimes = new ArrayList<>();
         final List<Double> initialPlanningObjective = new ArrayList<>();
         final List<Double> initialPostDisturbanceObjective = new ArrayList<>();
         final List<Double> initialPreDisturbanceObjective = new ArrayList<>();

         e.getValue().getRuntimes().forEach(f -> initialPlanningTimes.add(f.get(0)));
         e.getValue().getEvaluations().forEach(f -> initialPlanningEvaluations.add((Double) f.get(0)));
         e.getValue().getRuntimes().forEach(f -> lastReplanningTimes.add(f.get(f.size() - 1)));
         e.getValue().getEvaluations().forEach(f -> lastReplanningEvaluations.add((Double) f.get(f.size() - 1)));
         e.getValue().getPostDisturbanceObjectives().forEach(f -> initialPostDisturbanceObjective.add(f.get(0)));
         e.getValue().getPreDisturbanceObjectives().forEach(f -> initialPreDisturbanceObjective.add(f.get(0)));
         e.getValue().getPlannedObjectives().forEach(f -> initialPlanningObjective.add(f.get(0)));

         e.getValue().getRuntimes()
               .forEach(f -> overallReplanningTimes.add(f.stream().mapToDouble(g -> (double) g).sum()));
         e.getValue().getEvaluations()
               .forEach(f -> overallReplanningEvaluations.add(f.stream().mapToDouble(g -> (double) g).sum()));

         final List<Double> atIterations = new ArrayList<>();
         final List<Double> plannedIterations = new ArrayList<>();
         final List<String> errorTypes = new ArrayList<>();

         e.getValue().getDisturbances().forEach(f -> {
            atIterations.add((double) f.get(0).getAtIteration());
            plannedIterations.add((double) f.get(0).getPlannedIterations());
            errorTypes.add(f.get(0).getType().toString());
         });

         // final List<Double> idleCompObjectives = new ArrayList<>();
         // final List<Double> predSteps = new ArrayList<>();
         // final List<Double> predPlanObjectives = new ArrayList<>();
         // final List<Double> predPlanIterations = new ArrayList<>();
         // final List<Double> predPlanRuntimes = new ArrayList<>();

         final List<String[]> appendValues = new ArrayList<>();
         final List<String[]> appendValuesDist = new ArrayList<>();
         final List<String[]> appendValuesPred = new ArrayList<>();

         if(!e.getValue().getPredictiveRunResults().isEmpty()) {
            for(final List<PredictiveRunResult> prr : e.getValue().getPredictiveRunResults()) {
               if(!prr.isEmpty()) {
                  final PredictiveRunResult curPrr = prr.get(0);
                  final String idlePlanObjStr = Arrays.toString(curPrr.getIdlePlanningObjectives());
                  for(final Entry<Integer, PredictiveRunPlanningStats> e2 : curPrr.getStepsToResult().entrySet()) {
                     final PredictiveRunPlanningStats prps = e2.getValue();
                     appendValuesPred.add(new String[] { idlePlanObjStr, e2.getKey().toString(),
                           Arrays.toString(prps.getObjectives()), String.valueOf(prps.getEvaluations()),
                           String.valueOf(prps.getRuntime()), String.valueOf(prps.getRuntimeTilObjectivesSatisfised()),
                           String.valueOf(prps.getMaxRuntimeIfObjectivesSatisfied()) });
                  }
               }
            }
         }

         IntStream.range(0, lastReplanningTimes.size()).forEach(i -> {
            appendValues.add(new String[] { initialPlanningTimes.get(i).toString(),
                  initialPlanningEvaluations.get(i).toString(), initialPlanningObjective.get(i).toString(),
                  initialPostDisturbanceObjective.get(i).toString(), initialPreDisturbanceObjective.get(i).toString(),
                  lastReplanningTimes.get(i).toString(), lastReplanningEvaluations.get(i).toString(),
                  overallReplanningTimes.get(i).toString(), overallReplanningEvaluations.get(i).toString(),
                  e.getValue().getFinalObjectives().get(i).toString(),
                  e.getValue().getFinalFailedExecutions().get(i).toString() });

            appendValuesDist.add(new String[] { atIterations.get(i).toString(), plannedIterations.get(i).toString(),
                  errorTypes.get(i).toString() });

         });

         CSVUtil.addWithEnumeration(dataLines, new String[] { e.getKey() }, appendValues);

         CSVUtil.addWithEnumeration(dataLinesDist, new String[] { e.getKey() }, appendValuesDist);

         CSVUtil.addWithEnumeration(dataLinesPred, new String[] { e.getKey() }, appendValuesPred);

      }

      dataLines.add(0, header.toArray(new String[0]));
      dataLinesDist.add(0, headerDist.toArray(new String[0]));

      final File csvOutputFile = new File(path + ".csv");
      try(PrintWriter pw = new PrintWriter(csvOutputFile)) {
         dataLines.stream().map(CSVUtil::convertToCSV).forEach(pw::println);
      } catch(final FileNotFoundException e1) {
         e1.printStackTrace();
      }

      final File csvOutputFileDist = new File(path + "_disturbances.csv");
      try(PrintWriter pw = new PrintWriter(csvOutputFileDist)) {
         dataLinesDist.stream().map(CSVUtil::convertToCSV).forEach(pw::println);
      } catch(final FileNotFoundException e1) {
         e1.printStackTrace();
      }

      if(dataLinesPred.size() > 0) {

         dataLinesPred.add(0, headerPred.toArray(new String[0]));

         final File csvOutputFilePred = new File(path + "_predictive.csv");
         try(PrintWriter pw = new PrintWriter(csvOutputFilePred)) {
            dataLinesPred.stream().map(CSVUtil::convertToCSV).forEach(pw::println);
         } catch(final FileNotFoundException e1) {
            e1.printStackTrace();
         }
      }
   }

}
