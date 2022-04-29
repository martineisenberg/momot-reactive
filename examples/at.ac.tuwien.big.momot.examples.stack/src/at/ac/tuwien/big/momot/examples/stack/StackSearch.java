package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.SearchResultManager;
import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.AbstractProgressListener;
import at.ac.tuwien.big.moea.experiment.executor.listener.CurrentBestObjectiveListener;
import at.ac.tuwien.big.moea.experiment.executor.listener.SeedreuseProportionListener;
import at.ac.tuwien.big.moea.experiment.executor.listener.SingleSeedPrintListener;
import at.ac.tuwien.big.moea.print.ISolutionWriter;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.RLAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.ApplicationState;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.IEnvironment;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.problem.solution.variable.TransformationPlaceholderVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveSearchInstance;
import at.ac.tuwien.big.momot.reactive.planningstrategy.SearchConfiguration;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationParameterMutation;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationPlaceholderMutation;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationVariableMutation;
import at.ac.tuwien.big.momot.search.algorithm.reinforcement.datastructures.SOQTable;
import at.ac.tuwien.big.momot.search.algorithm.reinforcement.environment.EnvironmentBuilder;
import at.ac.tuwien.big.momot.search.criterion.ThresholdCondition;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.ElapsedTimeCollector;
import org.moeaframework.analysis.collector.PopulationCollector;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.TerminationCondition;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TournamentSelection;

public class StackSearch implements IReactiveSearchInstance {

   private static final int SOLUTION_LENGTH = 8;

   private static final String INPUT_MODEL = "model/model_five_stacks.xmi";
   protected static final boolean PRINT_POPULATIONS = false;

   protected static final String PRINT_DIRECTORY = "output/populations/five_stacks";
   private static final int NR_RUNS = 1;
   private static final int POPULATION_SIZE = 100;

   private static final int MAX_EVALUATIONS = 20000;

   protected static final String PRINT_OBECJTIVE_DEV_DIRECTORY = "output/simulation/listeners";
   protected static final int RECORD_OBJECTIVE_INDEX = 0;

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
         final TerminationCondition terminationCondition, final List<AbstractProgressListener> listeners,
         final List<List<ITransformationVariable>> initialPopulation, final double initialModelObjValue) {
      final SearchExperiment<TransformationSolution> experiment = new SearchExperiment<>(orchestration, evaluations,
            terminationCondition);
      experiment.setNumberOfRuns(NR_RUNS);
      experiment.addProgressListener(new SingleSeedPrintListener());
      experiment.addCustomCollector(new ElapsedTimeCollector());
      experiment.addCustomCollector(new PopulationCollector());

      if(!Files.exists(Paths.get(PRINT_OBECJTIVE_DEV_DIRECTORY))) {
         new File(PRINT_OBECJTIVE_DEV_DIRECTORY).mkdirs();
      }

      for(final AbstractProgressListener l : listeners) {
         if(l instanceof SeedreuseProportionListener) {
            if(initialPopulation != null && !initialPopulation.isEmpty()) {
               ((SeedreuseProportionListener) l).setSeedSolution(initialPopulation.get(0));
               experiment.addProgressListener(l);
            }
         } else if(l instanceof CurrentBestObjectiveListener) {
            ((CurrentBestObjectiveListener) l).setPriorBestObjValue(initialModelObjValue);
            experiment.addProgressListener(l);

         } else {
            experiment.addProgressListener(l);
         }
      }

      // if(recordBestObjective) {
      // if(!Files.exists(Paths.get(PRINT_OBECJTIVE_DEV_DIRECTORY))) {
      // new File(PRINT_OBECJTIVE_DEV_DIRECTORY).mkdirs();
      // }
      //
      // experiment.addProgressListener(new CurrentBestObjectiveListener(PRINT_OBECJTIVE_DEV_DIRECTORY,
      // RECORD_OBJECTIVE_INDEX, experimentName, runNr, reseedBestObj, 100));
      // }
      return experiment;

   }

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

   protected TransformationSearchOrchestration createOrchestration(final EGraph graph, final String algorithmName,
         final int solutionLength, final int populationSize, final List<List<ITransformationVariable>> reinitSeed) {
      StackPackage.eINSTANCE.eClass();

      final StackOrchestration orchestration = new StackOrchestration(graph, solutionLength);

      final EvolutionaryAlgorithmFactory<TransformationSolution> moea = orchestration
            .createEvolutionaryAlgorithmFactory(populationSize);

      /** Reinit seed for evolutionary algorithms **/
      if(reinitSeed != null && algorithmName.compareTo("NSGAII") == 0) {
         // final int insertNrOfSolutions = (int) (populationSize * reinitPortion) > 0
         // ? (int) (populationSize * reinitPortion)
         // : 1;

         final TransformationSolution[] solutionArr = new TransformationSolution[reinitSeed.size()];

         for(int i = 0; i < reinitSeed.size(); i++) {
            final List<ITransformationVariable> seq = reinitSeed.get(i);

            List<ITransformationVariable> initSequence = new ArrayList<>(seq);
            if(seq.size() < solutionLength) {
               initSequence = Stream
                     .of(initSequence,
                           Stream.generate(TransformationPlaceholderVariable::new)
                                 .limit(solutionLength - initSequence.size()).collect(Collectors.toList()))
                     .flatMap(Collection::stream).collect(Collectors.toList());
            } else {
               initSequence = initSequence.subList(0, solutionLength);
            }

            // seq.addAll(Stream.generate(TransformationPlaceholderVariable::new).limit(solutionLength - seq.size())
            // .collect(Collectors.toList()));

            // orchestration.getSearchHelper().appendRandomVariables(null, i);

            solutionArr[i] = new TransformationSolution(MomotUtil.copy(graph), initSequence,
                  orchestration.getFitnessFunction().evaluatesNrObjectives());

         }

         // Arrays.fill(solutionArr, new TransformationSolution(MomotUtil.copy(graph), reinitSeed,
         // orchestration.getFitnessFunction().evaluatesNrObjectives()));

         moea.setInitialSolutions(List.of(solutionArr));
      }

      final EnvironmentBuilder<TransformationSolution> envBuilder = new EnvironmentBuilder<>(
            orchestration.getFitnessFunction());
      final Map<IEnvironment.Type, IEnvironment<TransformationSolution>> env = envBuilder
            .singleObjective("Standard Deviation").build();

      /** Reinit seed for reinforcement algorithms (table) **/

      final SOQTable<List<ApplicationState>, List<ApplicationState>> qTableInitialized = null;

      if(reinitSeed != null
            && (algorithmName.compareTo("QLearning") == 0 || algorithmName.compareTo("QLearningExplore") == 0)) {

         throw new RuntimeException("Reimplement seed reuse for RL algorithms!");
         // final TransformationSolution ts = TransformationSolution
         // .removePlaceholdersKeepUnitApplicationAssignment(new TransformationSolution(MomotUtil.copy(graph),
         // reinitSeed.get(0), orchestration.getFitnessFunction().evaluatesNrObjectives()));
         //
         // final IRLUtils<TransformationSolution> rlUtils = new RLUtils<>();
         // qTableInitialized = new SOQTable<>();
         //
         // final List<ApplicationState> as = rlUtils.getApplicationStates(ts);
         // for(int i = 0; i < as.size(); i++) {
         //
         // final List<ApplicationState> stateRepr = as.subList(0, i);
         // qTableInitialized.addStateIfNotExists(stateRepr);
         //
         // final TransformationSolution curStateTS = new TransformationSolution(MomotUtil.copy(graph),
         // ts.getVariablesAsList().subList(0, i + 1), orchestration.getNumberOfObjectives());
         // final double fitness = orchestration.getFitnessFunction().evaluate(curStateTS);
         // qTableInitialized.update(stateRepr, i < as.size() - 1 ? as.subList(i, i + 1) : List.of(as.get(i)), fitness);
         //
         // }
      }

      final RLAlgorithmFactory<TransformationSolution> rl = orchestration.createRLAlgorithmFactory(env,
            qTableInitialized);
      // if(algorithmName.compareTo("NSGAII_1") == 0) {
      // orchestration.addAlgorithm(algorithmName, moea.createCustomNSGAII(new TournamentSelection(2),
      // // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
      // List.of(new CustomOnePointCrossover(1.0, orchestration.getSearchHelper(), false),
      // new TransformationParameterMutation(0.2, orchestration.getModuleManager())),
      //
      // .9, .1, 1.0, 0.01, 10));
      //
      // }

      if(algorithmName.compareTo("NSGAII") == 0) {
         orchestration.addAlgorithm(algorithmName, moea.createNSGAII(new TournamentSelection(2),
               // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
               new OnePointCrossover(.8), new TransformationParameterMutation(0.2, orchestration.getModuleManager()),
               new TransformationVariableMutation(orchestration.getSearchHelper(), 0.2),
               new TransformationPlaceholderMutation(0.1)));

      }

      if(algorithmName.compareTo("QLearning") == 0) {
         orchestration.addAlgorithm(algorithmName,
               rl.createSingleObjectiveQLearner(0.9, 0.2, false, 1e-3, 0.1, null, 0, 0, null, null, false));
      }

      if(algorithmName.compareTo("QLearningExplore") == 0) {
         orchestration.addAlgorithm(algorithmName,
               rl.createSingleObjectiveExploreQLearner(10, 0.9, 0.1, false, 1e-3, 0.1, null, 0, 0, null, null, false));
      }

      return orchestration;
   }

   private SearchResult createSearchResult(final SearchExperiment<TransformationSolution> experiment,
         final TransformationSearchOrchestration orchestration, final String algorithmName) {

      for(final Entry<SearchExecutor, List<NondominatedPopulation>> entry : experiment.getResults().entrySet()) {
         if(entry.getKey().getName().compareTo(algorithmName) != 0) {
            continue;
         }

         final Accumulator acc = entry.getKey().getInstrumenter().getLastAccumulator();
         double executionTime = 0;
         int performedEvaluations = 0;
         if(acc.keySet().contains("Elapsed Time")) {
            executionTime = (double) acc.get("Elapsed Time", acc.size("Elapsed Time") - 1);
            performedEvaluations = (int) acc.get("NFE", acc.size("NFE") - 1);
         }

         final TerminationCondition c = experiment.getTerminationCondition();

         final Population population = SearchResultManager.createApproximationSet(experiment, algorithmName);
         final Population resultPopulation = new NondominatedPopulation();
         for(final TransformationSolution ts : MomotUtil.asIterables(population, TransformationSolution.class)) {
            // final double curStd = ts
            // .getObjective(orchestration.getFitnessFunction().getObjectiveIndex("Standard Deviation"));
            // if(curStd < minStd) {
            // minStd = curStd;
            // optimalTS = TransformationSolution.removePlaceholders(ts);
            // ts.execute();
            // }
            final TransformationSolution addSol = TransformationSolution.removePlaceholders(ts);

            orchestration.getFitnessFunction().evaluate(addSol);

            if(c instanceof ThresholdCondition && !((ThresholdCondition) c).satisfiesCriteria(addSol)) {
               continue;

            }

            resultPopulation.add(addSol);
         }

         // found solution not part of nondominated set, add manually
         if(resultPopulation.isEmpty() && c instanceof ThresholdCondition) {
            final ThresholdCondition tc = (ThresholdCondition) c;
            final TransformationSolution terminationSol = TransformationSolution
                  .removePlaceholders((TransformationSolution) tc.getTerminationSolution());
            orchestration.getFitnessFunction().evaluate(terminationSol);
            resultPopulation.add(terminationSol);

         }
         return new SearchResult(resultPopulation, executionTime, performedEvaluations);

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

   @Override
   public SearchResult performSearch(final SearchConfiguration conf) {
      return performSearch(conf, new ArrayList<>());

   }

   @Override
   public SearchResult performSearch(final SearchConfiguration conf, final List<AbstractProgressListener> listeners) {
      final TransformationSearchOrchestration orchestration = createOrchestration(conf.getStartingState(),
            conf.getAlgoritmName(), conf.getSolutionLength(), conf.getPopulationSize(), conf.getInitialPopulation());

      deriveBaseName(orchestration);

      // double reinitBestObj = 0;
      // if(reinitSeed != null) {
      // final TransformationSolution ts = new TransformationSolution(MomotUtil.copy(graph), reinitSeed,
      // orchestration.getFitnessFunction().evaluatesNrObjectives());
      // orchestration.getFitnessFunction().evaluate(ts);
      // reinitBestObj = ts.getObjective(0);
      // }
      // printSearchInfo(orchestration);

      final SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration,
            conf.getMaxEvaluations(), conf.getTerminationCondition(), listeners, conf.getInitialPopulation(),
            MomotUtil.calculateObjectiveOnModel("Standard Deviation", orchestration.getFitnessFunction(),
                  conf.getStartingState(), new ArrayList<>()));
      experiment.run();

      SOLUTION_WRITER = experiment.getSearchOrchestration().createSolutionWriter();

      return createSearchResult(experiment, orchestration, conf.getAlgoritmName());

   }

}
