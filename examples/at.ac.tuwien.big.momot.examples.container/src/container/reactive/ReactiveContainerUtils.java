package container.reactive;

import at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.IReactiveUtilities;
import at.ac.tuwien.big.momot.search.fitness.EGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.dimension.AbstractEGraphFitnessDimension;
import at.ac.tuwien.big.momot.search.fitness.dimension.TransformationLengthDimension;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import container.Container;
import container.ContainerModel;
import container.Stack;
import container.demo.ContainerUtils;

public class ReactiveContainerUtils implements IReactiveUtilities {

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

   protected static String getContainerBayRepr(final ContainerModel cm) {
      final StringBuilder sb = new StringBuilder();

      for(final Stack s : cm.getStack()) {
         final List<String> stackContainerIds = new ArrayList<>();
         Container c = s.getTopContainer();
         System.out.print("\n" + s.getId() + ": ");

         while(c != null) {
            stackContainerIds.add(c.getId());
            c = c.getOnTopOf();
         }
         Collections.reverse(stackContainerIds);
         System.out.print(String.join(" ", stackContainerIds));

      }
      System.out.println("\n");
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

   protected IFitnessDimension<TransformationSolution> _createObjective_0() {
      final IFitnessDimension<TransformationSolution> dimension = _createObjectiveHelper_0();
      dimension.setName("SolutionLength");
      dimension.setFunctionType(at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum);
      return dimension;
   }

   protected IFitnessDimension<TransformationSolution> _createObjective_1() {
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

   @Override
   public IEGraphMultiDimensionalFitnessFunction getFitnessFunction() {
      if(FITNESS_FUNCTION == null) {
         FITNESS_FUNCTION = new EGraphMultiDimensionalFitnessFunction();
         // with
         // empty rules
         FITNESS_FUNCTION.addObjective(_createObjective_0());
         FITNESS_FUNCTION.addObjective(_createObjective_1());
      }
      return FITNESS_FUNCTION;
   }

   protected IEGraphMultiDimensionalFitnessFunction getFitnessFunction(
         final TransformationSearchOrchestration orchestration) {
      final IEGraphMultiDimensionalFitnessFunction function = new EGraphMultiDimensionalFitnessFunction();

      return function;
   }

   @Override
   public String getReprFromEGraph(final EGraph g) {
      return ReactiveContainerUtils.getContainerBayRepr(MomotUtil.getRoot(g, ContainerModel.class));

   }

}
