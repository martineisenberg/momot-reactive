package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.examples.stack.stack.Stack;
import at.ac.tuwien.big.momot.examples.stack.stack.StackModel;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.examples.stack.stack.impl.StackImpl;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.error.Disturbance;
import at.ac.tuwien.big.momot.reactive.error.ErrorOccurence;
import at.ac.tuwien.big.momot.reactive.error.ErrorType;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.Random;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.interpreter.EGraph;

public abstract class AbstractDisturber {

   public static abstract class AbstractDisturberBuilder {
      protected ErrorType eType;

      protected ErrorOccurence eOccurence;
      protected ModelRuntimeEnvironment mre;
      protected int maxNrOfDisturbances;
      protected int errorsPerDisturbance;
      public float eProbability;

      public abstract AbstractDisturber build();

      public AbstractDisturberBuilder errorsPerDisturbance(final int errorsPerDisturbance) {
         this.errorsPerDisturbance = errorsPerDisturbance;
         return this;
      }

      // public AbstractDisturberBuilder maxPlanLength(final int maxLength) {
      // this.maxSteps = maxLength;
      // return this;
      // }

      public AbstractDisturberBuilder maxNrOfDisturbances(final int maxNrOfDisturbances) {
         this.maxNrOfDisturbances = maxNrOfDisturbances;
         return this;
      }

      public AbstractDisturberBuilder occurence(final ErrorOccurence eo) {
         this.eOccurence = eo;
         return this;
      }

      public AbstractDisturberBuilder probability(final float p) {
         this.eProbability = p;
         return this;
      }

      public AbstractDisturberBuilder runtimeModelEnvironment(final ModelRuntimeEnvironment mre) {
         this.mre = mre;
         return this;
      }

      public AbstractDisturberBuilder type(final ErrorType et) {
         this.eType = et;
         return this;
      }
   }

   protected final ErrorType eType;

   protected final ErrorOccurence eOccurence;
   protected ModelRuntimeEnvironment mre;
   protected int nrOfObservedDisturbances;
   protected final int maxNrOfDisturbances;
   protected final Random rand;
   protected final int errorsPerDisturbance;

   protected AbstractDisturber(final AbstractDisturberBuilder builder) {
      this.eType = builder.eType;
      this.eOccurence = builder.eOccurence;
      this.mre = builder.mre;
      this.maxNrOfDisturbances = builder.maxNrOfDisturbances;
      this.rand = new Random();
      this.nrOfObservedDisturbances = 0;
      this.errorsPerDisturbance = builder.errorsPerDisturbance;
   }

   public StackModel addStack(final EGraph g) {
      final StackModel sm = MomotUtil.getRoot(g, StackModel.class);

      final EClassifier sClassifier = StackPackage.eINSTANCE.getEClassifier("Stack");

      final Stack stack = (Stack) EcoreUtil.create((EClass) sClassifier);

      stack.setId(String.format("Stack_%d", sm.getStacks().size() + 1));
      stack.setLoad(0);
      stack.setRight(sm.getStacks().get(0));
      stack.setLeft(sm.getStacks().get(sm.getStacks().size() - 1));
      sm.getStacks().get(sm.getStacks().size() - 1).setRight(stack);
      sm.getStacks().get(0).setLeft(stack);

      sm.getStacks().add(stack);
      g.add(stack);

      return sm;

   }

   protected void disturb(final ITransformationVariable nextStep) {
      for(int i = 0; i < this.errorsPerDisturbance; i++) {

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
   }

   /**
    * Check for any occuring disturbance; Will occur based
    * on disturber settings (error type, probability, first/second half of planned execution).
    *
    * @param curExecutionNr
    *           .. Nr. of current rule excution
    * @param plannedExecutions
    *           .. Nr. of executions of current plan
    * @param nextExecution
    *           .. Next variable to execute, relevant for introducing specific error
    * @return .. null if no disturbance occured, otherwise disturbance object for information
    */
   public abstract Disturbance pollForDisturbance(final int curExecutionNr, final int plannedExecutions,
         final ITransformationVariable nextExecution);

   protected void removeStackToShiftFrom(final EGraph graph, final ITransformationVariable nextStep) {
      final StackModel sm = MomotUtil.getRoot(graph, StackModel.class);

      // final String removeStackIdx = (String) nextStep.getResultParameterValue("fromId");

      final Stack removeStack = sm.getStacks().stream().findAny().get();

      final Stack r = removeStack.getRight();
      final Stack l = removeStack.getLeft();
      r.setLeft(l);
      l.setRight(r);
      // r.setRight(null);
      // r.setLeft(null);

      final EObject graphRemoveObj = graph.stream()
            .filter(g -> g instanceof StackImpl && ((StackImpl) g).getId().compareTo(removeStack.getId()) == 0)
            .findFirst().get();

      sm.getStacks().remove(removeStack);
      graph.remove(graphRemoveObj);
   }

   protected void reset() {
      this.mre = null;
      this.nrOfObservedDisturbances = 0;
   }

   public void setModelRuntimeEnvironment(final ModelRuntimeEnvironment mre) {
      this.mre = mre;
   }

}
