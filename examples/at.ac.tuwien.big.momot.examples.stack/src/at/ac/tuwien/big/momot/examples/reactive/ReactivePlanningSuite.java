package at.ac.tuwien.big.momot.examples.reactive;

import at.ac.tuwien.big.momot.examples.stack.StackSearch;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.reactive.AbstractDisturber;
import at.ac.tuwien.big.momot.reactive.Executor;
import at.ac.tuwien.big.momot.reactive.IReactiveUtilities;
import at.ac.tuwien.big.momot.reactive.Printer;
import at.ac.tuwien.big.momot.reactive.ReactiveExperiment;
import at.ac.tuwien.big.momot.reactive.error.ErrorOccurence;
import at.ac.tuwien.big.momot.reactive.error.ErrorType;
import at.ac.tuwien.big.momot.reactive.planningstrategy.EvaluationReplanningStrategy;
import at.ac.tuwien.big.momot.reactive.planningstrategy.PlanningStrategy;
import at.ac.tuwien.big.momot.reactive.result.ReactiveResult;
import at.ac.tuwien.big.momot.util.MomotUtil;

import com.google.common.collect.ImmutableList;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/*
 * This class allows for testing combinations of parameters, e.g., for several disturber settings
 * and planning strategies, at once.
 */

public class ReactivePlanningSuite {
   final static boolean VERBOSE = false;

   /* EXPERIMENT CONFIGURATION */
   final static List<PlanningStrategy> PLANNING_STRATEGIES = Arrays.asList(new PlanningStrategy.PlanningStrategyBuilder(
         "NSGAII",
         new EvaluationReplanningStrategy.EvaluationReplanningStrategyBuilder("NSGAII").maxEvaluations(5000).build())
               .maxEvaluations(5000).build());
   // new PlanningStrategy.PlanningStrategyBuilder("NSGAII", new
   // EvaluationReplanningStrategy.EvaluationReplanningStrategyBuilder("NSGAII")
   // .terminationCondition(MinimumObjectiveCriterion.create(1, 0)).build())
   // .terminationCondition(MinimumObjectiveCriterion.create(1, 0)).build())
   // new PlanningStrategy.PlanningStrategyBuilder("NSGAII",
   // new EvaluationReplanningStrategy.EvaluationReplanningStrategyBuilder("NSGAII")
   // .terminationCondition(MinimumObjectiveCriterion.create(1, 0)).build())
   // .terminationCondition(MinimumObjectiveCriterion.create(1, 0)).build());
   // new PlanningStrategy.PlanningStrategyBuilder("NSGAII",
   // new EvaluationReplanningStrategy.EvaluationReplanningStrategyBuilder("NSGAII").maxEvaluations(5000)
   // .reusePreviousPlan(true, 0.1f).build()).maxEvaluations(5000).build());

   // .PlanningStrategy.create("QLearningExplore", 5000,
   // EvaluationReplanningStrategy.create("QLearningExplore", 1000, false, 0.0f)),
   // PlanningStrategy.create("QLearningExplore", 5000,
   // EvaluationReplanningStrategy.create("QLearningExplore", 1000, true, 0.5f)),
   // PlanningStrategy.create("QLearning", 5000,
   // EvaluationReplanningStrategy.create("QLearning", 1000, false, 0.0f)),
   // PlanningStrategy.create("QLearning", 5000,
   // EvaluationReplanningStrategy.create("QLearning", 1000, true, 0.5f)));
   // PlanningStrategy.create("NSGAII", 5000, EvaluationReplanningStrategy.create("NSGAII", 5000, true, 0.2f)),
   // PlanningStrategy.create("NSGAII", 5000, EvaluationReplanningStrategy.create("NSGAII", 5000, true, 0.5f)),
   // PlanningStrategy.create("NSGAII", 5000, EvaluationReplanningStrategy.create("NSGAII", 5000, true, 0.8f)));

   final static String INITIAL_MODEL = Paths.get("model", "model_five_stacks.xmi").toString();
   final static String HENSHIN_MODULE = Paths.get("model", "stack.henshin").toString();
   final static int MAX_SOLUTION_LENGTH = 8;
   final static int POPULATION_SIZE = 100;
   private static final int EXPERIMENT_RUNS = 5;
   final static String EVAL_OBJECTIVE = "Standard Deviation";

   /* DISTURBER CONFIGURATION */
   private static final List<ErrorType> ERROR_TYPE_LIST = ImmutableList.of(ErrorType.OPTIMALITY_ERROR);
   private static final List<ErrorOccurence> ERROR_OCCURENCE_LIST = ImmutableList.of(ErrorOccurence.FIRST_HALF);
   private static final List<Float> ERROR_PROBABILITY_LIST = ImmutableList.of(0.99f);
   private static final int MAX_DISTURBANCES_PER_RUN = 1;

   /* OUTPUTS */
   final static String OUT_BASE_PATH = Paths.get("output", "simulation").toString();
   final static Printer PS_OUT = new Printer(Paths.get(OUT_BASE_PATH, "summary7.txt").toString());
   // final static Printer PS_DIFF = new Printer(Paths.get(OUT_BASE_PATH, "summary.txt").toString());

   public static void main(final String[] args) {
      StackPackage.eINSTANCE.eClass();
      final HenshinResourceSet hrs = new HenshinResourceSet();
      hrs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

      final Resource initialModelRes = hrs.getResource(INITIAL_MODEL);

      final IReactiveUtilities utils = new StackUtils();
      final Printer p = PS_OUT;
      System.setOut(p.getPrintStream());

      final List<List<Object>> combinations = com.google.common.collect.Lists.cartesianProduct(ERROR_TYPE_LIST,
            ERROR_OCCURENCE_LIST, ERROR_PROBABILITY_LIST);
      for(final List<Object> o : combinations) {

         final ErrorType eType = (ErrorType) o.get(0);
         final ErrorOccurence eOccurence = (ErrorOccurence) o.get(1);
         final float eProbability = (float) o.get(2);
         final AbstractDisturber disturber = new Disturber.DisturberBuilder().type(eType).occurence(eOccurence)
               .probability(eProbability).maxPlanLength(MAX_SOLUTION_LENGTH)
               .maxNrOfDisturbances(MAX_DISTURBANCES_PER_RUN).build();

         final Executor executor = new Executor(HENSHIN_MODULE);

         final ReactiveExperiment experiment = new ReactiveExperiment(MomotUtil.eGraphOf(initialModelRes, true),
               PLANNING_STRATEGIES, new StackSearch(), utils, disturber, executor, EVAL_OBJECTIVE, EXPERIMENT_RUNS,
               MAX_SOLUTION_LENGTH, POPULATION_SIZE, p, VERBOSE);

         p.header1(String.format("Experiment %s, %s, %s", eType, eOccurence, eProbability));

         final Map<String, ReactiveResult> results = experiment.runExperiment();

         p.header2(String.format("RESULTS (%s, %s, %s", eType, eOccurence, eProbability));

         for(final Entry<String, ReactiveResult> e : results.entrySet()) {
            p.subheader(e.getKey());
            p.property(String.format("Final model objectives (%s)", EVAL_OBJECTIVE),
                  Arrays.toString(e.getValue().getFinalObjectives().toArray()));
            p.property(String.format("Avg. final objective (%s)", EVAL_OBJECTIVE), Double.toString(Arrays.stream(
                  e.getValue().getFinalObjectives().stream().mapToDouble(f -> f != null ? f : Float.NaN).toArray())
                  .average().getAsDouble())).newline();
         }

      }

      p.close();

   }
}
