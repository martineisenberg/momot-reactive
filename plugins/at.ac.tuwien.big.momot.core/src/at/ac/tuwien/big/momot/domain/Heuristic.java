package at.ac.tuwien.big.momot.domain;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.Executor;

import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;

public interface Heuristic {

   List<ITransformationVariable> getInitialPopulationTs(EGraph g, Executor executor, final int populationSize);
}
