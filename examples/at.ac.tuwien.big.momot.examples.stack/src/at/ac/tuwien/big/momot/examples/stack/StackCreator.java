package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.momot.ModuleManager;
import at.ac.tuwien.big.momot.examples.stack.stack.Stack;
import at.ac.tuwien.big.momot.examples.stack.stack.StackModel;
import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;

import java.io.IOException;
import java.util.Random;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class StackCreator {
   public static final String EMPTY_MODEL = "empty_model.xmi";

   public static final StackCreator INSTANCE = new StackCreator();

   private static final int INITIAL_VALUE = 0;

   private static final int STEP_SIZE = 1;

   private static int STACK_INDEX = 0;

   private static final String BASE_NAME = "Stack_";

   private static String currentName() {
      return previousName(0);
   }

   private static String firstName() {
      return BASE_NAME + (INITIAL_VALUE + STEP_SIZE);
   }

   public static void main(final String[] args) {
      StackPackage.eINSTANCE.eClass();
      final StackCreator c = new StackCreator();
      final Random r = new Random();

      c.createStacksGraph(100, r.ints(100, 1, 100).toArray(), "model/gen100_1to100.xmi");
   }

   private static String newName() {
      STACK_INDEX += STEP_SIZE;
      return currentName();
   }

   protected static String nextName() {
      return nextName(1);
   }

   private static String nextName(final int steps) {
      return BASE_NAME + (STACK_INDEX + steps * STEP_SIZE);
   }

   private static String previousName() {
      return previousName(1);
   }

   private static String previousName(final int steps) {
      return BASE_NAME + (STACK_INDEX - steps * STEP_SIZE);
   }

   private final ModuleManager henshin = new ModuleManager("model/");

   public StackCreator() {
      henshin.addModule("stack.henshin");
   }

   public void createStacksGraph(final int nrStacks, final int[] js, final String targetResource) {

      // Define resource set for this ecore model
      final HenshinResourceSet rSet = new HenshinResourceSet();
      rSet.getPackageRegistry().put(StackPackage.eNS_URI, StackPackage.eINSTANCE);

      // Define classifiers for needed model elements
      final EClassifier smClassifier = StackPackage.eINSTANCE.getEClassifier("StackModel");
      final EClassifier sClassifier = StackPackage.eINSTANCE.getEClassifier("Stack");

      final StackModel sm = (StackModel) EcoreUtil.create((EClass) smClassifier);

      int id = 1;
      for(final int load : js) {
         final Stack s = (Stack) EcoreUtil.create((EClass) sClassifier);
         s.setLoad(load);
         s.setId("Stack_" + id++);

         sm.getStacks().add(s);
      }

      for(int i = 0; i < sm.getStacks().size(); i++) {
         final Stack s = sm.getStacks().get(i);

         if(i == 0) {
            s.setLeft(sm.getStacks().get(sm.getStacks().size() - 1));
            s.setRight(sm.getStacks().get(1));
         } else if(i == js.length - 1) {
            s.setLeft(sm.getStacks().get(i - 1));
            s.setRight(sm.getStacks().get(0));
         } else {
            s.setLeft(sm.getStacks().get(i - 1));
            s.setRight(sm.getStacks().get(i + 1));

         }
      }

      final Resource oR = rSet.createResource(URI.createFileURI(targetResource));
      oR.getContents().add(sm);
      try {
         oR.save(null);
      } catch(final IOException e) {
         e.printStackTrace();
         // System.out.println(e.getMessage());
      }
   }

}
