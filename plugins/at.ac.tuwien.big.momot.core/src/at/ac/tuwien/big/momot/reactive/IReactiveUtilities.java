package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;

import org.eclipse.emf.henshin.interpreter.EGraph;

public interface IReactiveUtilities {
   IEGraphMultiDimensionalFitnessFunction getFitnessFunction();

   String getReprFromEGraph(EGraph graph);
}
