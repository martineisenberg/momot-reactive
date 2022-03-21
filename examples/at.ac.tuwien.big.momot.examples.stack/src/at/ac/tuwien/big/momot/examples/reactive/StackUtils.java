package at.ac.tuwien.big.momot.examples.reactive;

import at.ac.tuwien.big.moea.util.MathUtil;
import at.ac.tuwien.big.momot.examples.stack.stack.Stack;
import at.ac.tuwien.big.momot.examples.stack.stack.StackModel;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveUtilities;
import at.ac.tuwien.big.momot.search.fitness.EGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.dimension.AbstractEGraphFitnessDimension;
import at.ac.tuwien.big.momot.search.fitness.dimension.TransformationLengthDimension;
import at.ac.tuwien.big.momot.search.solution.repair.TransformationPlaceholderRepairer;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Parameter;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class StackUtils implements IReactiveUtilities {

   private static IEGraphMultiDimensionalFitnessFunction FITNESS_FUNCTION;

   protected static void executeSingleUnit(final EGraph modelGraph, final Engine engine, final Module module,
         final ITransformationVariable var) {
      final Unit unit = module.getUnit(var.getUnit().getName());

      final UnitApplication application = new UnitApplicationImpl(engine, modelGraph, unit, null);

      for(final Parameter param : var.getUnit().getParameters()) {
         application.setParameterValue(param.getName(), var.getParameterValue(param));
      }
      application.execute(null);
   }

   protected static String getStackReprFromModel(final StackModel sm) {
      final StringBuilder sb = new StringBuilder();

      final List<String> sIds = sm.getStacks().stream().map(x -> x.getId()).collect(Collectors.toList());
      final List<String> sLoads = sm.getStacks().stream().map(x -> String.valueOf(x.getLoad()))
            .collect(Collectors.toList());

      sb.append(String.join("\t", sIds) + "\n");
      sb.append(String.join("\t", sLoads));
      return sb.toString();
   }

   protected static Resource saveModelGraphToResource(final EObject o, final String path) {
      final HenshinResourceSet rSet = new HenshinResourceSet();
      final Resource oR = rSet.createResource(URI.createFileURI(path));
      oR.getContents().add(o);

      try {
         oR.save(null);
      } catch(final IOException e) {
         e.printStackTrace();
      }
      return oR;
   }

   @Override
   public IEGraphMultiDimensionalFitnessFunction getFitnessFunction() {
      if(FITNESS_FUNCTION == null) {
         FITNESS_FUNCTION = new EGraphMultiDimensionalFitnessFunction();
         FITNESS_FUNCTION.setSolutionRepairer(new TransformationPlaceholderRepairer()); // replace not executed rules
         // with
         // empty rules

         FITNESS_FUNCTION.addObjective(new AbstractEGraphFitnessDimension("Standard Deviation") {
            @Override
            protected double internalEvaluate(final TransformationSolution solution) {
               final StackModel stacks = MomotUtil.getRoot(solution.execute(), StackModel.class);
               final List<Integer> loads = new ArrayList<>();
               for(final Stack stack : stacks.getStacks()) {
                  final int load = stack.getLoad();
                  loads.add(load);
               }

               return MathUtil.getStandardDeviation(loads);
            }
         });

         FITNESS_FUNCTION.addObjective(new TransformationLengthDimension());
      }
      return FITNESS_FUNCTION;
   }

   @Override
   public String getReprFromEGraph(final EGraph g) {
      return StackUtils.getStackReprFromModel(MomotUtil.getRoot(g, StackModel.class));

   }

   protected String getStackReprFromResource(final Resource r) {
      return getReprFromEGraph(MomotUtil.eGraphOf(r, false));
   }

}
