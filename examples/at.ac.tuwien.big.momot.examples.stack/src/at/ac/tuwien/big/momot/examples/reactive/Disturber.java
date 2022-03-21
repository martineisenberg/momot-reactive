package at.ac.tuwien.big.momot.examples.reactive;

import at.ac.tuwien.big.momot.examples.stack.stack.Stack;
import at.ac.tuwien.big.momot.examples.stack.stack.StackModel;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.examples.stack.stack.impl.StackImpl;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.AbstractDisturber;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.interpreter.EGraph;

public class Disturber extends AbstractDisturber {
   public static class DisturberBuilder extends AbstractDisturberBuilder {

      @Override
      public Disturber build() {
         final Disturber d = new Disturber(this);

         if(maxSteps <= 0 || eType == null || eOccurence == null || eProbability < 0 || eProbability > 1) {
            throw new RuntimeException(
                  "Failed building disturber component: Must define max number of steps (>0), error type, error occurence, and error probability (1 >= p >= 0");
         }
         return d;
      }

      // public DisturberBuilder maxNrOfDisturbances(final int maxNrOfDisturbances) {
      // this.maxNrOfDisturbances = maxNrOfDisturbances;
      // return this;
      // }
      //
      // public DisturberBuilder maxPlanLength(final int maxLength) {
      // this.maxSteps = maxLength;
      // return this;
      // }
      //
      // public DisturberBuilder occurence(final ErrorOccurence eo) {
      // this.eOccurence = eo;
      // return this;
      // }
      //
      // public DisturberBuilder probability(final float p) {
      // this.eProbability = p;
      // return this;
      // }
      //
      // public DisturberBuilder runtimeModelEnvironment(final ModelRuntimeEnvironment mre) {
      // this.mre = mre;
      // return this;
      // }
      //
      // public DisturberBuilder type(final ErrorType et) {
      // this.eType = et;
      // return this;
      // }
   }

   private Disturber(final DisturberBuilder builder) {
      super(builder);
   }

   public StackModel addStack(final EGraph g) {
      final StackModel sm = MomotUtil.getRoot(g, StackModel.class);

      final EClassifier sClassifier = StackPackage.eINSTANCE.getEClassifier("Stack");

      final Stack stack = (Stack) EcoreUtil.create((EClass) sClassifier);
      stack.setId(String.format("Stack_%d", sm.getStacks().size() + 1));
      stack.setLoad(0);
      stack.setRight(sm.getStacks().get(0));
      stack.setLeft(sm.getStacks().get(sm.getStacks().size() - 1));
      sm.getStacks().add(stack);
      g.add(stack);

      return sm;

   }

   public StackModel addStack(final EGraph g, final boolean decouple) {
      final StackModel sm = MomotUtil.getRoot(decouple ? MomotUtil.copy(g) : g, StackModel.class);

      final EClassifier sClassifier = StackPackage.eINSTANCE.getEClassifier("Stack");

      final Stack stack = (Stack) EcoreUtil.create((EClass) sClassifier);
      stack.setId(String.format("Stack_%d", sm.getStacks().size() + 1));
      stack.setLoad(0);
      stack.setRight(sm.getStacks().get(0));
      stack.setLeft(sm.getStacks().get(sm.getStacks().size() - 1));
      sm.getStacks().add(stack);
      g.add(stack);

      return sm;

   }

   @Override
   protected void disturb(final ITransformationVariable nextStep) {
      switch(eType) {
         case WEAK_ERROR:

            break;
         case STRONG_ERROR:
            removeStackToShiftFrom(mre.getGraph(), nextStep);
            break;
         case OPTIMALITY_ERROR:
            addStack(mre.getGraph());
            break;
      }
   }

   private void removeStackToShiftFrom(final EGraph graph, final ITransformationVariable nextStep) {
      final StackModel sm = MomotUtil.getRoot(graph, StackModel.class);

      final String removeStackIdx = (String) nextStep.getResultParameterValue("fromId");

      final Stack removeStack = sm.getStacks().stream().filter(s -> s.getId().compareTo(removeStackIdx) == 0)
            .findFirst().get();

      final List<EObject> eob = graph.getRoots();

      final EObject graphRemoveObj = graph.stream()
            .filter(g -> g instanceof StackImpl && ((StackImpl) g).getId().compareTo(removeStackIdx) == 0).findFirst()
            .get();

      sm.getStacks().remove(removeStack);
      graph.remove(graphRemoveObj);
   }

}
