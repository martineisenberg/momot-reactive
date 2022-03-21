package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.SearchResultManager;
import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.SingleSeedPrintListener;
import at.ac.tuwien.big.moea.print.ISolutionWriter;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.RLAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.ApplicationState;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.IEnvironment;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.utils.IRLUtils;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.problem.solution.variable.TransformationPlaceholderVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationParameterMutation;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationPlaceholderMutation;
import at.ac.tuwien.big.momot.search.algorithm.reinforcement.algorithm.RLUtils;
import at.ac.tuwien.big.momot.search.algorithm.reinforcement.datastructures.SOQTable;
import at.ac.tuwien.big.momot.search.algorithm.reinforcement.environment.EnvironmentBuilder;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.TerminationCondition;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TournamentSelection;

public class StackSearch implements IReactiveSearch {

   private static final int SOLUTION_LENGTH = 8;

   private static final String INPUT_MODEL = "model/model_five_stacks.xmi";
   protected static final boolean PRINT_POPULATIONS = false;

   protected static final String PRINT_DIRECTORY = "output/populations/five_stacks";
   private static final int NR_RUNS = 1;
   private static final int POPULATION_SIZE = 100;

   private static final int MAX_EVALUATIONS = 10000;

   private static Map<String, Integer> OBJECTIVE_INDICES;
   private static String OUT_PATH;

   private static ISolutionWriter<TransformationSolution> SOLUTION_WRITER = null;

   protected static String baseName;

   protected static double significanceLevel = 0.01;

   private static IEGraphMultiDimensionalFitnessFunction FITNESS_FUNCTION;

   // public static TransformationSolution newSolutionFromVariables(final EGraph g,
   // final List<ITransformationVariable> vars) {
   // final TransformationSolution ts = new TransformationSolution(g, vars, 2);
   // StackSearch.evaluateSolution(ts);
   // return ts;
   // }

   // public static TransformationSolution newSolutionFromVariables(final Resource r,
   // final List<ITransformationVariable> vars) {
   // final TransformationSolution ts = new TransformationSolution(MomotUtil.eGraphOf(r, true), vars, 2);
   // ts.execute();
   // StackSearch.evaluateSolution(ts);
   // return ts;
   // }

   // public static void main(final String[] args) throws IOException {
   // StackPackage.eINSTANCE.eClass();
   //
   // final StackOrchestration search = new StackOrchestration(INPUT_MODEL, SOLUTION_LENGTH);
   //
   // // Init builder
   // final EnvironmentBuilder<TransformationSolution> envBuilder = new EnvironmentBuilder<>(
   // search.getFitnessFunction());
   //
   // // Define utilities and build enviroments
   // final Map<IEnvironment.Type, IEnvironment<TransformationSolution>> env = envBuilder
   // .singleObjective("Standard Deviation").build();
   //
   // final RLAlgorithmFactory<TransformationSolution> rl = search.createRLAlgorithmFactory(env);
   //
   // final EvolutionaryAlgorithmFactory<TransformationSolution> moea = search
   // .createEvolutionaryAlgorithmFactory(POPULATION_SIZE);
   //
   // // search.addAlgorithm("P", rl.createPolicyGradient(0.9, 3e-5, false, 0, 0, null, false, null, "temp_Stack5/PG",
   // // 100,
   // // false, 10000, true));
   //
   // search.addAlgorithm("NSGAII",
   // moea.createNSGAII(new TournamentSelection(2), new OnePointCrossover(1.0),
   // new TransformationParameterMutation(0.1, search.getModuleManager()),
   // new TransformationPlaceholderMutation(0.2)));
   //
   // // search.addAlgorithm("QLearningExplore", rl.createSingleObjectiveExploreQLearner(10, 0.9, 0.1, true, 1e-3, 0.1,
   // // null, 0, 0, "kb_5stacks", "kb_5stacks", true));
   // //
   // // search.addAlgorithm("QLearning", rl.createSingleObjectiveQLearner(0.9, 0.1, true, 1e-3, 0.1, null, 0, 0,
   // // "kb2_5stacks", "kb2_5stacks", false));
   //
   // final SearchExperiment<TransformationSolution> experiment = new SearchExperiment<>(search, MAX_EVALUATIONS);
   //
   // experiment.setNumberOfRuns(NR_RUNS);
   // // experiment.addProgressListener(new SeedRuntimePrintListener());
   //
   // final int printInterval = 1000;
   //
   // if(PRINT_POPULATIONS) {
   // final List<String> algoNames = new ArrayList<>();
   // for(final IRegisteredAlgorithm<? extends Algorithm> a : search.getAlgorithms()) {
   // algoNames.add(search.getAlgorithmName(a));
   // }
   //
   // if(!Files.exists(Paths.get(PRINT_DIRECTORY))) {
   // new File(PRINT_DIRECTORY).mkdirs();
   // }
   //
   // // experiment.addProgressListener(
   // // new CurrentNondominatedPopulationPrintListener(PRINT_DIRECTORY, algoNames, NR_RUNS, printInterval));
   // }
   //
   // printSearchInfo(search);
   //
   // experiment.run();
   //
   // System.out.println("-------------------------------------------------------");
   // System.out.println("Analysis");
   // System.out.println("-------------------------------------------------------");
   // performAnalysis(experiment);
   // System.out.println("-------------------------------------------------------");
   // System.out.println("Results");
   // System.out.println("-------------------------------------------------------");
   // handleResults(experiment);
   // }

   // public static void saveModel(final String outPath, final TransformationSolution s) {
   // TransformationResultManager.saveModel(Paths.get(OUT_PATH, outPath).toString(), s);
   // }
   //
   // public static void saveModels(final List<File> modelFiles) {
   // for(final File file : modelFiles) {
   // final EGraph graph = MomotUtil.loadGraph(file.getPath());
   // MomotUtil.saveGraph(graph, file.getPath());
   // }
   // }
   //
   // public static void saveSolution(final PrintStream ps, final TransformationSolution solution) {
   // SearchResultManager.saveSolution(ps, solution, SOLUTION_WRITER, false);
   //
   // }
   //
   // public static void saveSolution(final String filePath, final TransformationSolution s,
   // ISolutionWriter<TransformationSolution> solutionWriter) {
   // if(solutionWriter == null) {
   // solutionWriter = SOLUTION_WRITER;
   // }
   // SearchResultManager.saveSolution(Paths.get(OUT_PATH, filePath).toString(), s, solutionWriter);
   // }

   protected SearchExperiment<TransformationSolution> createExperiment(
         final TransformationSearchOrchestration orchestration, final int evaluations,
         final TerminationCondition terminationCondition) {
      final SearchExperiment<TransformationSolution> experiment = new SearchExperiment<>(orchestration, evaluations,
            terminationCondition);
      experiment.setNumberOfRuns(NR_RUNS);
      experiment.addProgressListener(new SingleSeedPrintListener());

      // if(PRINT_POPULATIONS) {
      // final List<String> algoNames = new ArrayList<>();
      // for(final IRegisteredAlgorithm<? extends Algorithm> a : orchestration.getAlgorithms()) {
      // algoNames.add(orchestration.getAlgorithmName(a));
      // }
      //
      // if(!Files.exists(Paths.get(PRINT_DIRECTORY))) {
      // new File(PRINT_DIRECTORY).mkdirs();
      // }
      //
      // experiment.addProgressListener(
      // new CurrentNondominatedPopulationPrintListener(PRINT_DIRECTORY, algoNames, NR_RUNS, 100));
      // }

      return experiment;
   }

   protected TransformationSearchOrchestration createOrchestration(final EGraph graph, final String algorithmName,
         final int solutionLength, final int populationSize, final List<TransformationSolution> reinitSolutions) {
      StackPackage.eINSTANCE.eClass();

      final StackOrchestration orchestration = new StackOrchestration(graph, solutionLength);

      final EvolutionaryAlgorithmFactory<TransformationSolution> moea = orchestration
            .createEvolutionaryAlgorithmFactory(populationSize);

      if(reinitSolutions != null) {
         moea.setInitialSolutions(reinitSolutions);
      }

      final EnvironmentBuilder<TransformationSolution> envBuilder = new EnvironmentBuilder<>(
            orchestration.getFitnessFunction());
      final Map<IEnvironment.Type, IEnvironment<TransformationSolution>> env = envBuilder
            .singleObjective("Standard Deviation").build();

      SOQTable<List<ApplicationState>, List<ApplicationState>> qTableInitialized = null;

      if(reinitSolutions != null) {
         final TransformationSolution ts = TransformationSolution
               .removePlaceholdersKeepUnitApplicationAssignment(reinitSolutions.get(0));
         final IRLUtils<TransformationSolution> rlUtils = new RLUtils<>();
         qTableInitialized = new SOQTable<>();

         final List<ApplicationState> as = rlUtils.getApplicationStates(ts);
         for(int i = 0; i < as.size(); i++) {

            final List<ApplicationState> stateRepr = as.subList(0, i);
            qTableInitialized.addStateIfNotExists(stateRepr);

            final TransformationSolution curStateTS = new TransformationSolution(MomotUtil.copy(graph),
                  ts.getVariablesAsList().subList(0, i + 1), orchestration.getNumberOfObjectives());
            final double fitness = orchestration.getFitnessFunction().evaluate(curStateTS);
            qTableInitialized.update(stateRepr, i < as.size() - 1 ? as.subList(i, i + 1) : List.of(as.get(i)), fitness);

         }
      }

      final RLAlgorithmFactory<TransformationSolution> rl = orchestration.createRLAlgorithmFactory(env,
            qTableInitialized);
      if(algorithmName.compareTo("NSGAII") == 0) {
         orchestration.addAlgorithm(algorithmName,
               moea.createNSGAII(new TournamentSelection(2), new OnePointCrossover(1.0),
                     new TransformationParameterMutation(0.1, orchestration.getModuleManager()),
                     new TransformationPlaceholderMutation(0.2)));
      }

      if(algorithmName.compareTo("QLearning") == 0) {
         orchestration.addAlgorithm(algorithmName,
               rl.createSingleObjectiveQLearner(0.9, 0.2, false, 1e-3, 0.1, null, 0, 0, null, null, false));
      }

      if(algorithmName.compareTo("QLearningExplore") == 0) {
         orchestration.addAlgorithm(algorithmName,
               rl.createSingleObjectiveExploreQLearner(10, 0.9, 0.2, false, 1e-3, 0.1, null, 0, 0, null, null, false));
      }

      return orchestration;
   }

   private SearchResult createSearchResult(final SearchExperiment<TransformationSolution> experiment,
         final TransformationSearchOrchestration orchestration, final String algorithmName) {

      for(final Entry<SearchExecutor, List<NondominatedPopulation>> entry : experiment.getResults().entrySet()) {
         if(entry.getKey().getName().compareTo(algorithmName) != 0) {
            continue;
         }

         double minStd = Double.POSITIVE_INFINITY;
         TransformationSolution optimalTS = null;
         final Population population = SearchResultManager.createApproximationSet(experiment, algorithmName);
         for(final TransformationSolution ts : MomotUtil.asIterables(population, TransformationSolution.class)) {
            final double curStd = ts
                  .getObjective(orchestration.getFitnessFunction().getObjectiveIndex("Standard Deviation"));
            if(curStd < minStd) {
               minStd = curStd;
               optimalTS = TransformationSolution.removePlaceholders(ts);
               ts.execute();
            }
         }
         // algorithmToBestRuleSequence.put(algName, optimalTS.getVariablesAsList());
         orchestration.getFitnessFunction().evaluate(optimalTS);
         // StackSearch.saveSolution(Paths.get(dir, algorithmName + "_plan_" + solutionFilename + ".txt").toString(),
         // optimalTS, SOLUTION_WRITER);
         // StackSearch.saveModel(Paths.get(dir, algorithmName + "_model_" + modelFilename + ".xmi").toString(),
         // optimalTS);
         return new SearchResult(population, optimalTS, optimalTS.getVariablesAsList());

      }
      return null;
   }

   protected void deriveBaseName(final TransformationSearchOrchestration orchestration) {
      final EObject root = MomotUtil.getRoot(orchestration.getProblemGraph());
      if(root == null || root.eResource() == null || root.eResource().getURI() == null) {
         baseName = getClass().getSimpleName();
      } else {
         baseName = root.eResource().getURI().trimFileExtension().lastSegment();
      }
   }

   // public List<ITransformationVariable> performInitialSearch(final String initialGraph, final int solutionLength,
   // final String algorithmName) {
   // final TransformationSearchOrchestration orchestration = createOrchestration(initialGraph, solutionLength,
   // algorithmName, null);
   // deriveBaseName(orchestration);
   // // printSearchInfo(orchestration);
   // final SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration);
   // experiment.run();
   //
   // SOLUTION_WRITER = experiment.getSearchOrchestration().createSolutionWriter();
   // FITNESS_FUNCTION = orchestration.getFitnessFunction();
   //
   // return getOptimalSolutionForAlgorithm(experiment, orchestration, algorithmName, "initial", "initial",
   // "after_initial_plan");
   //
   // }
   //
   // public List<ITransformationVariable> performReplanningSearch(final String initialGraph, final int solutionLength,
   // final String algorithmName, final String runName, final List<TransformationSolution> reinitPlan) {
   //
   // final TransformationSearchOrchestration orchestration = createOrchestration(initialGraph, solutionLength,
   // algorithmName, reinitPlan);
   // deriveBaseName(orchestration);
   // // printSearchInfo(orchestration);
   // final SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration);
   // experiment.run();
   //
   // return getOptimalSolutionForAlgorithm(experiment, orchestration, algorithmName, "replan_" + runName,
   // "replan_" + runName, "after_replan_" + runName);
   // }

   private List<TransformationSolution> getBestSolutionNTimes(final EGraph g,
         final IEGraphMultiDimensionalFitnessFunction fitnessFunction, final int n,
         final List<ITransformationVariable> trafoVars, final int fillWithPlaceholdersUntil) {
      final List<TransformationSolution> solutions = new ArrayList<>();
      final TransformationSolution[] solutionArr = new TransformationSolution[n];

      trafoVars.addAll(Stream.generate(TransformationPlaceholderVariable::new)
            .limit(fillWithPlaceholdersUntil - trafoVars.size()).collect(Collectors.toList()));

      // solutions.add(new TransformationSolution(MomotUtil.copy(g), curVars,
      // utils.getFitnessFunction().evaluatesNrObjectives()));

      Arrays.fill(solutionArr,
            new TransformationSolution(MomotUtil.copy(g), trafoVars, fitnessFunction.evaluatesNrObjectives()));

      return List.of(solutionArr);

   }

   @Override
   public SearchResult performSearch(final EGraph graph, final String algorithmName, final int evaluations,
         final TerminationCondition condition, final int solutionLength, final int populationSize,
         final List<TransformationSolution> reinitSolutions) {
      final TransformationSearchOrchestration orchestration = createOrchestration(graph, algorithmName, solutionLength,
            populationSize, reinitSolutions);

      deriveBaseName(orchestration);
      // printSearchInfo(orchestration);
      final SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration, evaluations,
            condition);
      experiment.run();

      SOLUTION_WRITER = experiment.getSearchOrchestration().createSolutionWriter();

      return createSearchResult(experiment, orchestration, algorithmName);

   }

}
