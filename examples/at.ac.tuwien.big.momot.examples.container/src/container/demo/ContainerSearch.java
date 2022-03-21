package container.demo;

import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.SearchResultManager;
import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.SeedRuntimePrintListener;
import at.ac.tuwien.big.moea.experiment.executor.listener.SingleSeedPrintListener;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.LocalSearchAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.RLAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.provider.IRegisteredAlgorithm;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.environment.IEnvironment;
import at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension;
import at.ac.tuwien.big.momot.ModuleManager;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.reactive.IReactiveSearch;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationParameterMutation;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationPlaceholderMutation;
import at.ac.tuwien.big.momot.search.algorithm.reinforcement.environment.EnvironmentBuilder;
import at.ac.tuwien.big.momot.search.fitness.EGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.dimension.AbstractEGraphFitnessDimension;
import at.ac.tuwien.big.momot.search.fitness.dimension.TransformationLengthDimension;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.progress.ProgressListener;

import container.ContainerModel;
import container.ContainerPackage;

@SuppressWarnings("all")
public class ContainerSearch implements IReactiveSearch {
   protected static final String INITIAL_MODEL = "model/3S_8C.xmi";

   protected static final boolean PRINT_POPULATIONS = false;
   protected static final String PRINT_DIRECTORY = "output/populations/10S40C";

   protected static final int SOLUTION_LENGTH = 14;

   public static void finalization() {
      System.out.println("Search finished.");
   }

   public static void initialization() {
      ContainerPackage.eINSTANCE.eClass();
      System.out.println("Search started.");
   }

   protected final String[] modules = new String[] { "transformations/container.henshin" };

   protected final String[] unitsToRemove = new String[] { "container::containerModule::retrieveNonLastFromStack",
         "container::containerModule::canRetrieveContainer", "container::containerModule::checkNextToRetrieveIsLast",
         "container::containerModule::relocateNonLastOnStackToEmptyStack",
         "container::containerModule::relocateNonLastOnStackToNonEmptyStack",
         "container::containerModule::relocateLastOnStackToNonEmptyStack", "container::containerModule::checkIsLast",
         "container::containerModule::checkIsNotLast", "container::containerModule::checkTargetStackEmpty",
         "container::containerModule::RelocateLastOnStack", "container::containerModule::checkTargetStackNotEmpty",
         "container::containerModule::Relocate", "container::containerModule::Retrieve",
         "container::containerModule::RelocateNonLastOnStack", "container::containerModule::retrieveLastFromStack",
         "container::containerModule::checkContainerToRetrieveOnTopOfSuccessor",
         "container::containerModule::checkContainerToRetrieveLastOverall",
         "container::containerModule::retrieveOnTopOfSuccessorFromStack",
         "container::containerModule::retrieveLastOverallFromStack",
         "container::containerModule::RetrieveNormalLastOrNonLastOnStack",
         "container::containerModule::RetrieveNonLastOverall",
         "container::containerModule::RelocateLastOnStackToNonEmptyTargetStack",
         "container::containerModule::RelocateNonLastOnStackToEmptyTargetStack",
         "container::containerModule::RelocateNonLastOnStackToNonEmptyTargetStack" };

   protected final int nrRuns = 1;

   protected String baseName;

   protected double significanceLevel = 0.01;

   protected ProgressListener _createListener_0() {
      final SeedRuntimePrintListener _seedRuntimePrintListener = new SeedRuntimePrintListener();
      return _seedRuntimePrintListener;
   }

   protected IFitnessDimension<TransformationSolution> _createObjective_0(
         final TransformationSearchOrchestration orchestration) {
      final IFitnessDimension<TransformationSolution> dimension = _createObjectiveHelper_0();
      dimension.setName("SolutionLength");
      dimension.setFunctionType(at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum);
      return dimension;
   }

   protected IFitnessDimension<TransformationSolution> _createObjective_1(
         final TransformationSearchOrchestration orchestration) {
      return new AbstractEGraphFitnessDimension("RetrievedContainers",
            at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Maximum) {
         @Override
         protected double internalEvaluate(final TransformationSolution solution) {
            final EGraph graph = solution.execute();
            final EObject root = MomotUtil.getRoot(graph);
            return _createObjectiveHelper_1(solution, graph, root);
         }
      };
   }

   protected IFitnessDimension<TransformationSolution> _createObjectiveHelper_0() {
      final TransformationLengthDimension _transformationLengthDimension = new TransformationLengthDimension();
      return _transformationLengthDimension;
   }

   protected double _createObjectiveHelper_1(final TransformationSolution solution, final EGraph graph,
         final EObject root) {
      return ContainerUtils.calculateRetrievedContainers((ContainerModel) root);
   }

   protected IRegisteredAlgorithm<NSGAII> _createRegisteredAlgorithm_0(
         final TransformationSearchOrchestration orchestration,
         final EvolutionaryAlgorithmFactory<TransformationSolution> moea,
         final LocalSearchAlgorithmFactory<TransformationSolution> local) {
      final IRegisteredAlgorithm<NSGAII> _createNSGAII = moea.createNSGAII();
      return _createNSGAII;
   }

   protected SearchExperiment<TransformationSolution> createExperiment(
         final TransformationSearchOrchestration orchestration, final int evaluations) {
      final SearchExperiment<TransformationSolution> experiment = new SearchExperiment<>(orchestration, evaluations);
      experiment.setNumberOfRuns(nrRuns);
      experiment.addProgressListener(new SingleSeedPrintListener());

      return experiment;
   }

   protected IEGraphMultiDimensionalFitnessFunction createFitnessFunction(
         final TransformationSearchOrchestration orchestration) {
      final IEGraphMultiDimensionalFitnessFunction function = new EGraphMultiDimensionalFitnessFunction();
      function.addObjective(_createObjective_0(orchestration));
      function.addObjective(_createObjective_1(orchestration));
      return function;
   }

   protected EGraph createInputGraph(final String initialGraph, final ModuleManager moduleManager) {
      final EGraph graph = moduleManager.loadGraph(initialGraph);
      return graph;
   }

   protected ModuleManager createModuleManager() {
      final ModuleManager manager = new ModuleManager();
      for(final String module : modules) {
         manager.addModule(URI.createFileURI(new File(module).getPath().toString()).toString());
      }
      manager.removeUnits(unitsToRemove);
      return manager;
   }

   protected TransformationSearchOrchestration createOrchestration(final EGraph graph, final String algorithmName,
         final int solutionLength, final int populationSize, final List<TransformationSolution> reinitSolutions) {
      final TransformationSearchOrchestration orchestration = new TransformationSearchOrchestration();
      final ModuleManager moduleManager = createModuleManager();
      orchestration.setModuleManager(moduleManager);
      orchestration.setProblemGraph(graph);
      orchestration.setSolutionLength(solutionLength);
      final IEGraphMultiDimensionalFitnessFunction fitnessFunction = createFitnessFunction(orchestration);
      orchestration.setFitnessFunction(fitnessFunction);

      final EvolutionaryAlgorithmFactory<TransformationSolution> moea = orchestration
            .createEvolutionaryAlgorithmFactory(populationSize);

      if(reinitSolutions != null) {
         moea.setInitialSolutions(reinitSolutions);
      }

      // Init builder
      final EnvironmentBuilder<TransformationSolution> envBuilder = new EnvironmentBuilder<>(fitnessFunction);

      // Define utilities and build enviroments
      final Map<IEnvironment.Type, IEnvironment<TransformationSolution>> env = envBuilder
            .singleObjective("RetrievedContainers").build();

      final RLAlgorithmFactory<TransformationSolution> rl = orchestration.createRLAlgorithmFactory(env);

      if(algorithmName.compareTo("NSGAII") == 0) {
         orchestration.addAlgorithm(algorithmName,
               moea.createNSGAII(new TournamentSelection(2), new OnePointCrossover(1.0),
                     new TransformationParameterMutation(0.1, orchestration.getModuleManager()),
                     new TransformationPlaceholderMutation(0.2)));
      }

      if(algorithmName.compareTo("QLearning") == 0) {
         orchestration.addAlgorithm(algorithmName,
               rl.createSingleObjectiveQLearner(0.9, 0.3, false, 1e-3, 0.1, null, 0, 0, null, null, false));
      }

      if(algorithmName.compareTo("QLearningExplore") == 0) {
         orchestration.addAlgorithm(algorithmName,
               rl.createSingleObjectiveExploreQLearner(10, 0.9, 0.3, false, 1e-3, 0.1, null, 0, 0, null, null, false));
      }

      return orchestration;
   }

   private SearchResult createSearchResult(final SearchExperiment<TransformationSolution> experiment,
         final TransformationSearchOrchestration orchestration, final String algorithmName) {

      for(final Entry<SearchExecutor, List<NondominatedPopulation>> entry : experiment.getResults().entrySet()) {
         if(entry.getKey().getName().compareTo(algorithmName) != 0) {
            continue;
         }

         double minC = Double.POSITIVE_INFINITY;
         TransformationSolution optimalTS = null;
         final Population population = SearchResultManager.createApproximationSet(experiment, algorithmName);
         for(final TransformationSolution ts : MomotUtil.asIterables(population, TransformationSolution.class)) {
            final double curC = ts
                  .getObjective(orchestration.getFitnessFunction().getObjectiveIndex("RetrievedContainers"));
            if(curC < minC) {
               minC = curC;
               optimalTS = TransformationSolution.removePlaceholdersKeepUnitApplicationAssignment(ts);
               ts.execute();
            }
         }
         orchestration.getFitnessFunction().evaluate(optimalTS);

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

   @Override
   public SearchResult performSearch(final EGraph graph, final String algorithmName, final int evaluations,
         final int solutionLength, final int populationSize, final List<TransformationSolution> reinitSolutions) {
      final TransformationSearchOrchestration orchestration = createOrchestration(graph, algorithmName, solutionLength,
            populationSize, reinitSolutions);

      deriveBaseName(orchestration);
      // printSearchInfo(orchestration);
      final SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration, evaluations);
      experiment.run();

      return createSearchResult(experiment, orchestration, algorithmName);
   }

}
