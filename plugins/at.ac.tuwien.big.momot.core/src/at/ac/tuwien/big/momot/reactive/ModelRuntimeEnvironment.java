package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;

public class ModelRuntimeEnvironment {

   private final EGraph initialGraph;
   private final EGraph graph;
   private final List<ITransformationVariable> executedUnits;

   public ModelRuntimeEnvironment(final EGraph initialGraph) {
      this.initialGraph = MomotUtil.copy(initialGraph);
      this.graph = initialGraph;
      this.executedUnits = new ArrayList<>();
   }

   public void addExecutedUnit(final ITransformationVariable var) {
      this.executedUnits.add(var);
   }

   public List<ITransformationVariable> getExecutedUnits() {
      return executedUnits;
   }

   public EGraph getGraph() {
      return graph;
   }

   public EGraph getInitialGraph() {
      return initialGraph;
   }

   public int getNrOfExecutedUnits() {
      return executedUnits.size();
   }

}
