package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.moea.SearchAnalysis;
import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.SearchResultManager;
import at.ac.tuwien.big.moea.experiment.analyzer.SearchAnalyzer;
import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.SeedRuntimePrintListener;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.momot.TransformationResultManager;
import at.ac.tuwien.big.momot.domain.Heuristic;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.problem.solution.variable.TransformationPlaceholderVariable;
import at.ac.tuwien.big.momot.reactive.Executor;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationParameterMutation;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationPlaceholderMutation;
import at.ac.tuwien.big.momot.search.algorithm.operator.mutation.TransformationVariableMutation;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.operator.OnePointCrossover;
import org.moeaframework.core.operator.TournamentSelection;

public class OriginalStackSearch {
   private static final int SOLUTION_LENGTH = 150;
   private static final int POPULATION_SIZE = 100;
   private static final int MAX_EVALUATIONS = 20000;

   private static final String INPUT_MODEL = "model/model_fifty_stacks_std50_2500_27.359.xmi";
   // private static final String INPUT_MODEL = "comparison_11-4-2-5-0-19-12-14-7-2.xmi";

   // private static final String REFERENCE_SET = "model/input/referenceSet/model_five_stacks_reference_set.pf";
   private static final int NR_RUNS = 7;

   final static String HENSHIN_MODULE = Paths.get("model", "stack.henshin").toString();

   protected static TransformationResultManager handleResults(
         final SearchExperiment<TransformationSolution> experiment) {

      final TransformationResultManager resultManager = new TransformationResultManager(experiment);

      System.out.println("REFERENCE SET:");
      System.out.println(SearchResultManager.printObjectives(SearchResultManager.getReferenceSet(experiment, null)));
      System.out.println(SearchResultManager.printObjectives(resultManager.createApproximationSet()));

      Population population;
      population = SearchResultManager.createApproximationSet(experiment, (String[]) null);
      System.out.println("- Save objectives of all algorithms to 'output/objectives/objective_values.txt'");
      SearchResultManager.saveObjectives("output/objectives/objective_values.txt", population);

      population = SearchResultManager.createApproximationSet(experiment, (String[]) null);
      System.out.println("- Save models of all algorithms to 'output/models/'");

      System.out.println("- Save objectives of algorithms seperately to 'output/objectives/<algorithm>.txt'");
      System.out.println("- Save models of algorithms seperately to 'output/solutions/<algorithm>.txt'´\n");

      for(final Entry<SearchExecutor, List<NondominatedPopulation>> entry : resultManager.getResults().entrySet()) {

         System.out.println(entry.getKey().getName());

         population = SearchResultManager.createApproximationSet(experiment, entry.getKey().getName());
         System.out.println(SearchResultManager.printObjectives(population) + "\n");

         population = SearchResultManager.createApproximationSet(experiment, entry.getKey().getName());
         SearchResultManager.saveObjectives("output/objectives/" + entry.getKey().getName() + ".txt", population);
      }

      return resultManager;
   }

   public static void main(final String[] args) throws IOException {
      StackPackage.eINSTANCE.eClass();

      // search orchestration
      //
      // for(final String modelPath : List.of(INPUT_MODEL, "model/gen50.xmi", "model/gen50.xmi",
      // "model/model_fifty_stacks_std5_250_3.181.xmi", "model/model_fifty_stacks.xmi",
      // "model/model_five_stacks.xmi", "model/model_ten_stacks.xmi", "model/model_twentyfive_stacks.xmi",
      // "model/model_twentyfive_stacks_1_to_30.xmi")) {
      // ;

      final EGraph g = MomotUtil.copy(MomotUtil.loadGraph(INPUT_MODEL));
      final Heuristic h = new StackHeuristic();
      final Executor executor = new Executor(HENSHIN_MODULE);
      //
      // final AbstractDisturber disturber = new RangeDisturber.RangeDisturberBuilder().type(ErrorType.ADD_STACKS)
      // .occurence(ErrorOccurence.FIRST_10_PERCENT).maxNrOfDisturbances(1).errorsPerDisturbance(5).build();

      // final ModelRuntimeEnvironment mre = new ModelRuntimeEnvironment(g);
      // disturber.setModelRuntimeEnvironment(mre);
      // disturber.disturb();

      final List<ITransformationVariable> reinitSeed = h.getInitialPopulationTs(g, executor, solutionLength);
      // }

      // 1. 1 solution
      final StackOrchestration search = new StackOrchestration(INPUT_MODEL, SOLUTION_LENGTH);

      final EvolutionaryAlgorithmFactory<TransformationSolution> moea = search
            .createEvolutionaryAlgorithmFactory(POPULATION_SIZE);

      final List<ITransformationVariable> paddedSeq = new ArrayList<>(
            reinitSeed.subList(0, Math.min(reinitSeed.size(), SOLUTION_LENGTH)));
      if(reinitSeed.size() < SOLUTION_LENGTH) {
         paddedSeq.addAll(Stream.generate(TransformationPlaceholderVariable::new)
               .limit(SOLUTION_LENGTH - reinitSeed.size()).collect(Collectors.toList()));
      }

      final TransformationSolution singleSolution = new TransformationSolution(MomotUtil.copy(g),
            new ArrayList<>(paddedSeq), search.getFitnessFunction().evaluatesNrObjectives());
      // moea.setInitialSolutions(List.of(singleSolution));

      // search.addAlgorithm("NSGA-II_2_1_.25_.1_sol1",
      // moea.createNSGAII(List.of(singleSolution), new TournamentSelection(2), new OnePointCrossover(1.0),
      // new TransformationParameterMutation(0.25, search.getModuleManager()),
      // new TransformationPlaceholderMutation(0.1)));
      //
      // search.addAlgorithm("NSGA-II_2_1_.1_.25_.1_sol1",
      // moea.createNSGAII(List.of(singleSolution), new TournamentSelection(2),
      // // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
      // new OnePointCrossover(1.0), new TransformationParameterMutation(0.1, search.getModuleManager()),
      // new TransformationVariableMutation(search.getSearchHelper(), 0.25),
      // new TransformationPlaceholderMutation(0.1)));
      //
      // final List<TransformationSolution> multipleSolutions = Stream
      // .generate(() -> new TransformationSolution(MomotUtil.copy(g), new ArrayList<>(paddedSeq),
      // search.getFitnessFunction().evaluatesNrObjectives()))
      // .limit((long) (POPULATION_SIZE * 0.1)).collect(Collectors.toList());
      //
      // // moea.setInitialSolutions(multipleSolutions);
      //
      // search.addAlgorithm("NSGA-II_2_1_.25_.1_sol10%",
      // moea.createNSGAII(multipleSolutions, new TournamentSelection(2), new OnePointCrossover(1.0),
      // new TransformationParameterMutation(0.25, search.getModuleManager()),
      // new TransformationPlaceholderMutation(0.1)));

      // ## BEST ##!

      search.addAlgorithm("NSGA-II_2_1_.1_.25_.1_sol10_1",
            moea.createNSGAII(
                  Stream.generate(() -> new TransformationSolution(MomotUtil.copy(g), new ArrayList<>(paddedSeq),
                        search.getFitnessFunction().evaluatesNrObjectives())).limit(10).collect(Collectors.toList()),
                  new TournamentSelection(2),
                  // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
                  new OnePointCrossover(1.0), new TransformationParameterMutation(0.25, search.getModuleManager()),
                  new TransformationVariableMutation(search.getSearchHelper(), 0.1),
                  new TransformationPlaceholderMutation(0.1)));

      search.addAlgorithm("NSGA-II_2_1_.1_.25_.1_sol10_2",
            moea.createNSGAII(
                  Stream.generate(() -> new TransformationSolution(MomotUtil.copy(g), new ArrayList<>(paddedSeq),
                        search.getFitnessFunction().evaluatesNrObjectives())).limit(10).collect(Collectors.toList()),
                  new TournamentSelection(2),
                  // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
                  new OnePointCrossover(1.0), new TransformationParameterMutation(0.1, search.getModuleManager()),
                  new TransformationVariableMutation(search.getSearchHelper(), 0.25),
                  new TransformationPlaceholderMutation(0.1)));

      search.addAlgorithm("NSGA-II_2_1_.1_.25_.1_sol10_3",
            moea.createNSGAII(
                  Stream.generate(() -> new TransformationSolution(MomotUtil.copy(g), new ArrayList<>(paddedSeq),
                        search.getFitnessFunction().evaluatesNrObjectives())).limit(10).collect(Collectors.toList()),
                  new TournamentSelection(2),
                  // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
                  new OnePointCrossover(1.0), new TransformationParameterMutation(0.2, search.getModuleManager()),
                  new TransformationVariableMutation(search.getSearchHelper(), 0.25),
                  new TransformationPlaceholderMutation(0.2)));

      search.addAlgorithm("NSGA-II_2_1_.1_.25_.1_sol10_4",
            moea.createNSGAII(
                  Stream.generate(() -> new TransformationSolution(MomotUtil.copy(g), new ArrayList<>(paddedSeq),
                        search.getFitnessFunction().evaluatesNrObjectives())).limit(10).collect(Collectors.toList()),
                  new TournamentSelection(2),
                  // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
                  new OnePointCrossover(1.0), new TransformationParameterMutation(0.2, search.getModuleManager()),
                  new TransformationVariableMutation(search.getSearchHelper(), 0.1),
                  new TransformationPlaceholderMutation(0.1)));
      // TransformationSolution[] solutionArr = new TransformationSolution[POPULATION_SIZE];
      //
      // for(int i = 0; i < solutionArr.length; i++) {
      // final List<ITransformationVariable> seq = new ArrayList<>(
      // reinitSeed.subList(0, Math.min(reinitSeed.size(), SOLUTION_LENGTH)));
      //
      // List<ITransformationVariable> initSequence = new ArrayList<>(seq);
      // final int preFillVars = PRNG.nextInt(SOLUTION_LENGTH - initSequence.size() + 1);
      // initSequence = Stream.of(
      // Stream.generate(TransformationPlaceholderVariable::new).limit(preFillVars).collect(Collectors.toList()),
      // initSequence, Stream.generate(TransformationPlaceholderVariable::new)
      // .limit(SOLUTION_LENGTH - preFillVars - seq.size()).collect(Collectors.toList()))
      // .flatMap(Collection::stream).collect(Collectors.toList());

      // seq.addAll(Stream.generate(TransformationPlaceholderVariable::new).limit(solutionLength - seq.size())
      // .collect(Collectors.toList()));

      // orchestration.getSearchHelper().appendRandomVariables(null, i);

      // solutionArr[i] = new TransformationSolution(MomotUtil.copy(g), initSequence,
      // search.getFitnessFunction().evaluatesNrObjectives());
      //
      // }

      // search.addAlgorithm("NSGA-II_2_1_.25_.1_sol_shifted_full",
      // moea.createNSGAII(List.of(solutionArr), new TournamentSelection(2), new OnePointCrossover(1.0),
      // new TransformationParameterMutation(0.25, search.getModuleManager()),
      // new TransformationPlaceholderMutation(0.1)));
      //
      // search.addAlgorithm("NSGA-II_2_1_.1_.25_.1_shifted_full",
      // moea.createNSGAII(List.of(solutionArr), new TournamentSelection(2),
      // // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
      // new OnePointCrossover(1.0), new TransformationParameterMutation(0.1, search.getModuleManager()),
      // new TransformationVariableMutation(search.getSearchHelper(), 0.25),
      // new TransformationPlaceholderMutation(0.1)));
      //
      // solutionArr = new TransformationSolution[(int) (POPULATION_SIZE * 0.1)];
      //
      // for(int i = 0; i < solutionArr.length; i++) {
      // final List<ITransformationVariable> seq = new ArrayList<>(
      // reinitSeed.subList(0, Math.min(reinitSeed.size(), SOLUTION_LENGTH)));
      //
      // List<ITransformationVariable> initSequence = new ArrayList<>(seq);
      // final int preFillVars = PRNG.nextInt(SOLUTION_LENGTH - initSequence.size() + 1);
      // initSequence = Stream.of(
      // Stream.generate(TransformationPlaceholderVariable::new).limit(preFillVars).collect(Collectors.toList()),
      // initSequence, Stream.generate(TransformationPlaceholderVariable::new)
      // .limit(SOLUTION_LENGTH - preFillVars - seq.size()).collect(Collectors.toList()))
      // .flatMap(Collection::stream).collect(Collectors.toList());

      // seq.addAll(Stream.generate(TransformationPlaceholderVariable::new).limit(solutionLength - seq.size())
      // .collect(Collectors.toList()));

      // orchestration.getSearchHelper().appendRandomVariables(null, i);

      // solutionArr[i] = new TransformationSolution(MomotUtil.copy(g), initSequence,
      // search.getFitnessFunction().evaluatesNrObjectives());
      //
      // }
      //
      // // moea.setInitialSolutions(List.of(solutionArr));
      //
      // search.addAlgorithm("NSGA-II_2_1_.25_.1_sol_shifted_part",
      // moea.createNSGAII(List.of(solutionArr), new TournamentSelection(2), new OnePointCrossover(1.0),
      // new TransformationParameterMutation(0.25, search.getModuleManager()),
      // new TransformationPlaceholderMutation(0.1)));
      //
      // search.addAlgorithm("NSGA-II_2_1_.1_.25_.1_shifted_part",
      // moea.createNSGAII(List.of(solutionArr), new TournamentSelection(2),
      // // new RetiringSolutionVariation(orchestration.getSearchHelper(), 50, populationSize, 90),
      // new OnePointCrossover(1.0), new TransformationParameterMutation(0.1, search.getModuleManager()),
      // new TransformationVariableMutation(search.getSearchHelper(), 0.25),
      // new TransformationPlaceholderMutation(0.1)));

      //
      // // Arrays.fill(solutionArr, new TransformationSolution(MomotUtil.copy(graph), reinitSeed,
      // // orchestration.getFitnessFunction().evaluatesNrObjectives()));
      //
      // // algorithms
      //
      // experiment
      final SearchExperiment<TransformationSolution> experiment = new SearchExperiment<>(search, MAX_EVALUATIONS);
      // experiment.setReferenceSetFile(REFERENCE_SET);
      experiment.setNumberOfRuns(NR_RUNS);
      experiment.addProgressListener(new SeedRuntimePrintListener());
      //
      experiment.run();

      System.out.println("-------------------------------------------------------");
      System.out.println("Analysis");
      System.out.println("-------------------------------------------------------");

      performAnalysis(experiment);
      System.out.println("-------------------------------------------------------");
      System.out.println("Results");
      System.out.println("-------------------------------------------------------");
      handleResults(experiment);

   }

   protected static SearchAnalyzer performAnalysis(final SearchExperiment<TransformationSolution> experiment) {
      final SearchAnalysis analysis = new SearchAnalysis(experiment);
      analysis.setHypervolume(true);
      analysis.setShowAggregate(true);
      analysis.setShowIndividualValues(true);
      analysis.setShowStatisticalSignificance(true);
      analysis.setSignificanceLevel(0.05);
      final SearchAnalyzer searchAnalyzer = analysis.analyze();
      System.out.println("---------------------------");
      System.out.println("Analysis Results");
      System.out.println("---------------------------");
      searchAnalyzer.printAnalysis();
      System.out.println("---------------------------");
      try {
         System.out.println("- Save Analysis to 'output/analysis/analysis.txt'");
         searchAnalyzer.saveAnalysis(new File("output/analysis/analysis.txt"));
      } catch(final IOException e) {
         e.printStackTrace();
      }
      return searchAnalyzer;
   }
}
