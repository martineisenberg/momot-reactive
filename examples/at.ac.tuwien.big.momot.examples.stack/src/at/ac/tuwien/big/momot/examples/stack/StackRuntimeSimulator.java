package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.momot.examples.stack.stack.StackModel;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class StackRuntimeSimulator {

   final static String INITIAL_MODEL = "model/model_five_stacks.xmi";
   final static String OUT_BASE_PATH = Paths.get("output", "simulation").toString();
   final static Printer PS_OUT = new Printer(System.out);
   final static Printer PS_DIFF = new Printer(Paths.get(OUT_BASE_PATH, "summary.txt").toString());
   final static Random RAND = new Random();

   public static void main(final String[] args) {
      // Inits
      StackPackage.eINSTANCE.eClass();
      final HenshinResourceSet hrs = new HenshinResourceSet();
      hrs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
      final Resource initialModelRes = hrs.getResource(Paths.get("model", "model_five_stacks.xmi").toString());
      final Module module = hrs.getModule(Paths.get("model", "stack.henshin").toString());
      final Engine engine = new EngineImpl();

      final StackSearch search = new StackSearch(OUT_BASE_PATH);
      final List<String> includeAlgorithms = Arrays.asList("NSGAII", "QLearning");

      PS_OUT.header1("INITIAL INPUT MODEL").str(StackUtils.getStackReprFromResource(initialModelRes));

      // Perform initial search and replanning for each algorithm in algorithm list (needs to be declared in
      // StackSearch.java!)
      for(int i = 0; i < includeAlgorithms.size(); i++) {
         final String algoName = includeAlgorithms.get(i);
         PS_OUT.header1(String.format("%s  (%d/%d)", algoName, i + 1, includeAlgorithms.size()))
               .header2("INITIAL PLAN");
         PS_DIFF.header1(algoName);
         // Search for initial plan for algorithm 'algoNname', max. rules per solution = 8
         List<ITransformationVariable> plan = search.performInitialSearch(INITIAL_MODEL, 8, algoName);

         final TransformationSolution initialTS = StackSearch.newSolutionFromVariables(initialModelRes, plan);
         PS_DIFF.header2("DIFFERENCE STATS").property("Initial Plan", Arrays.toString(initialTS.getObjectives()));
         PS_OUT.plan(initialTS);

         final List<ITransformationVariable> executedVars = new ArrayList<>();
         final EGraph curModelGraph = MomotUtil.eGraphOf(initialModelRes, true);

         // Execute rule by rule of solution until change occurs
         for(final ITransformationVariable var : plan) {
            StackUtils.executeSingleUnit(curModelGraph, engine, module, var);
            executedVars.add(var);

            // Change occurs
            if(RAND.nextDouble() < 1) {

               // Evaluate solution from so far executed rules and break; continue below with perturbation
               final TransformationSolution ts = StackSearch.newSolutionFromVariables(initialModelRes, executedVars);

               saveSolutionAndModel(ts, "change_1", algoName + "_plan_partial_execution_1.txt",
                     algoName + "_model_partial_execution_1.xmi");

               PS_OUT.header2("🛑 CHANGE EVENT")
                     .subheader(String.format("PLAN EXECUTED SO FAR (%d/%d Rules)", executedVars.size(), plan.size()))
                     .plan(ts).newline().subheader("RESULT MODEL (after planned execution so far)")
                     .str(StackUtils.getStackReprFromEGraph(ts.getResultGraph()));

               break;
            }

         }

         // Perturb model; Add additional stack and save as resource
         final StackModel newModel = Disturber.addStackToGraph(curModelGraph, true);
         final Resource perturbedModelRes = StackUtils.saveModelGraphToResource(newModel,
               Paths.get(OUT_BASE_PATH, "change_1", algoName + "_model_perturbed_1.xmi").toString());

         // Execute initial plan on perturbed model (=no adaption of original plan)
         final TransformationSolution perturbedWithInitialTS = StackSearch.newSolutionFromVariables(perturbedModelRes,
               plan);

         PS_DIFF.property("Without replanning", Arrays.toString(perturbedWithInitialTS.getObjectives()));
         PS_OUT.subheader("MODEL CHANGE (added stack)").str(StackUtils.getStackReprFromModel(newModel));

         // Replanning for perturbed model
         PS_OUT.header2("REPLANNING (1)");

         plan = search.performReplanningSearch(
               Paths.get("output", "simulation", "change_1", algoName + "_model_perturbed_1.xmi").toString(), 8,
               algoName, "1");

         final TransformationSolution perturbedWithReplanningTS = StackSearch
               .newSolutionFromVariables(perturbedModelRes, plan);

         PS_OUT.plan(perturbedWithReplanningTS);
         PS_DIFF.property("With replanning", Arrays.toString(perturbedWithReplanningTS.getObjectives()));

      }

      PS_DIFF.close();
   }

   private static TransformationSolution saveSolutionAndModel(final TransformationSolution ts, final String dir,
         final String solutionFilename, final String modelFilename) {

      StackSearch.saveSolution(Paths.get(dir, solutionFilename).toString(), ts, null);
      StackSearch.saveModel(Paths.get(dir, modelFilename).toString(), ts);
      return ts;
   }

}
