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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.moeaframework.core.PRNG;

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

   public void addStack(final EGraph g, final int num) {
      final StackModel sm = MomotUtil.getRoot(g, StackModel.class);

      final List<Stack> stacks = sm.getStacks();
      final List<Integer> addIndices = new ArrayList<>();
      for(int i = 0; i < stacks.size(); i += stacks.size() / num) {
         addIndices.add(PRNG.nextInt(i, i + stacks.size() / num - 1));
      }

      Collections.sort(addIndices, Collections.reverseOrder());
      int addedStacks = 1;
      for(final int addIndex : addIndices) {

         final EClassifier sClassifier = StackPackage.eINSTANCE.getEClassifier("Stack");

         final Stack stack = (Stack) EcoreUtil.create((EClass) sClassifier);
         stack.setId(String.format("Stack_I%d", addedStacks++));
         stack.setLoad(0);

         final Stack leftN = stacks.get(addIndex);
         final Stack rightN = leftN.getRight();

         leftN.setRight(stack);
         rightN.setLeft(stack);

         stack.setRight(rightN);
         stack.setLeft(leftN);

         // sm.getStacks().add(stack);
         stacks.add(addIndex + 1, stack);
         g.add(stack);
      }

   }

   public void disturb() {

      switch(eType) {
         case WEAK_ERROR:

            break;
         case REMOVE_STACKS:
            removeStackToShiftFrom(mre.getGraph(), this.errorsPerDisturbance);
            break;
         case ADD_STACKS:
            addStack(mre.getGraph(), this.errorsPerDisturbance);
            break;
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

   protected void removeStackToShiftFrom(final EGraph graph, final int num) {
      final StackModel sm = MomotUtil.getRoot(graph, StackModel.class);
      final List<Stack> stacks = sm.getStacks();
      final List<Integer> removeIndexes = new ArrayList<>();
      for(int i = 0; i < stacks.size(); i += stacks.size() / num) {
         removeIndexes.add(PRNG.nextInt(i, i + stacks.size() / num - 1));
      }

      Collections.sort(removeIndexes, Collections.reverseOrder());
      for(final int removeIndex : removeIndexes) {
         final Stack removeStack = sm.getStacks().get(removeIndex);
         final Stack r = removeStack.getRight();
         final Stack l = removeStack.getLeft();
         r.setLeft(l);
         l.setRight(r);

         final EObject graphRemoveObj = graph.stream()
               .filter(g -> g instanceof StackImpl && ((StackImpl) g).getId().compareTo(removeStack.getId()) == 0)
               .findFirst().get();

         stacks.remove(removeStack);
         graph.remove(graphRemoveObj);
      }
      // System.out.println(stacks.stream().map(s -> s.getLoad()).collect(Collectors.toList()));
      // System.out.println(stacks.stream().map(s -> s.getId()).collect(Collectors.toList()));
   }

   protected void reset(final ModelRuntimeEnvironment mre) {
      this.mre = mre;
      this.nrOfObservedDisturbances = 0;
   }

   public void setModelRuntimeEnvironment(final ModelRuntimeEnvironment mre) {
      this.mre = mre;
   }

}
