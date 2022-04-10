package at.ac.tuwien.big.momot.search.algorithm.operator.mutation;

import at.ac.tuwien.big.moea.util.MathUtil;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.search.solution.executor.SearchHelper;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.henshin.interpreter.EGraph;

public class TransformationVariableMutation extends AbstractTransformationMutation {
   private final SearchHelper searchHelper;

   public TransformationVariableMutation(final SearchHelper searchHelper) {
      this.searchHelper = searchHelper;
   }

   public TransformationVariableMutation(final SearchHelper searchHelper, final double probability) {
      super(probability);
      this.searchHelper = searchHelper;
   }

   protected SearchHelper getSearchHelper() {
      return searchHelper;
   }

   @Override
   protected TransformationSolution mutate(final TransformationSolution mutant) {
      final int randomPosition = MathUtil.randomInteger(mutant.getNumberOfVariables());
      final int randomReplace = randomPosition + 1;
      final List<ITransformationVariable> subMatches = Arrays.asList(mutant.getVariables()).subList(0, randomReplace);
      final TransformationSolution subMutant = getSearchHelper().createTransformationSolution(mutant.getSourceGraph(),
            subMatches, mutant.getNumberOfObjectives(), mutant.getNumberOfConstraints());
      final EGraph result = subMutant.execute();
      final ITransformationVariable variable = getSearchHelper().findUnitApplication(result);
      if(variable != null) {
         mutant.setVariable(randomReplace, variable);
      }
      return mutant;
   }

   // @Override
   // protected TransformationSolution mutate(final TransformationSolution mutant) {
   // final List<ITransformationVariable> vars = new ArrayList<>(mutant.getVariablesAsList());
   // Collections.reverse(vars);
   // int pIdx = mutant.getVariables().length - 1;
   // for(final ITransformationVariable v : vars) {
   // if(v instanceof IPlaceholderVariable) {
   // break;
   // }
   // pIdx--;
   // }
   //
   // // no placeholder
   // if(pIdx < 0) {
   // return mutant;
   // }
   //
   // // final List<ITransformationVariable> shiftedVars = new ArrayList<>(mutant.getVariablesAsList());
   // while(pIdx < mutant.getVariables().length - 1) {
   // mutant.setVariable(pIdx, mutant.getVariables()[pIdx + 1]);
   // pIdx++;
   // }
   // // mutant.setVariable(pIdx, new TransformationPlaceholderVariable());
   //
   // // final int randomPosition = MathUtil.randomInteger(mutant.getNumberOfVariables());
   // // final int randomReplace = randomPosition + 1;
   // final List<ITransformationVariable> subMatches = Arrays.asList(mutant.getVariables()).subList(0, pIdx);
   // final TransformationSolution subMutant = getSearchHelper().createTransformationSolution(mutant.getSourceGraph(),
   // subMatches, mutant.getNumberOfObjectives(), mutant.getNumberOfConstraints());
   // final EGraph result = subMutant.execute();
   // final ITransformationVariable variable = getSearchHelper().findUnitApplication(result);
   // if(variable != null) {
   // mutant.setVariable(pIdx, variable);
   // }
   // return mutant;
   // }

   // @Override
   // protected TransformationSolution mutate(final TransformationSolution mutant) {
   // for(int i = 0; i < 3; i++) {
   // final int randomPosition = MathUtil.randomInteger(mutant.getNumberOfVariables());
   // final int randomReplace = randomPosition + 1;
   // // final List<ITransformationVariable> subMatches = Arrays.asList(mutant.getVariables()).subList(0,
   // // randomReplace);
   // // final TransformationSolution subMutant =
   // // getSearchHelper().createTransformationSolution(mutant.getSourceGraph(),
   // // subMatches, mutant.getNumberOfObjectives(), mutant.getNumberOfConstraints());
   // final EGraph result = mutant.execute();
   // final ITransformationVariable variable = getSearchHelper().findUnitApplication(result);
   // if(variable != null) {
   // mutant.setVariable(randomReplace, variable);
   // }
   // }
   // return mutant;
   // }
}
