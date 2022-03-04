package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.momot.examples.stack.stack.Stack;
import at.ac.tuwien.big.momot.examples.stack.stack.StackModel;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import at.ac.tuwien.big.momot.util.MomotUtil;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.interpreter.EGraph;

public class Disturber {

   public static StackModel addStackToGraph(final EGraph g, final boolean decouple) {
      final StackModel sm = MomotUtil.getRoot(decouple ? MomotUtil.copy(g) : g, StackModel.class);

      final EClassifier sClassifier = StackPackage.eINSTANCE.getEClassifier("Stack");

      final Stack stack = (Stack) EcoreUtil.create((EClass) sClassifier);
      stack.setId(String.format("Stack_%d", sm.getStacks().size() + 1));
      stack.setLoad(0);
      stack.setRight(sm.getStacks().get(0));
      stack.setLeft(sm.getStacks().get(sm.getStacks().size() - 1));
      sm.getStacks().add(stack);

      return sm;

   }

}
