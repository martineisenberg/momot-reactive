
package at.ac.tuwien.big.moea.search.algorithm;

import at.ac.tuwien.big.moea.search.algorithm.operator.CustomCompoundVariation;
import at.ac.tuwien.big.moea.search.algorithm.operator.crossover.AbstractParentalCrossoverVariation;
import at.ac.tuwien.big.moea.search.algorithm.operator.mutation.AbstractMutationVariation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.EpsilonBoxEvolutionaryAlgorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.TournamentSelection;

/**
 * Implementation of NSGA-II, with the ability to attach an optional
 * &epsilon;-dominance archive.
 * <p>
 * References:
 * <ol>
 * <li>Deb, K. et al. "A Fast Elitist Multi-Objective Genetic Algorithm:
 * NSGA-II." IEEE Transactions on Evolutionary Computation, 6:182-197,
 * 2000.
 * <li>Kollat, J. B., and Reed, P. M. "Comparison of Multi-Objective
 * Evolutionary Algorithms for Long-Term Monitoring Design." Advances in
 * Water Resources, 29(6):792-807, 2006.
 * </ol>
 */
public class CustomNSGAII extends AbstractEvolutionaryAlgorithm implements EpsilonBoxEvolutionaryAlgorithm {

   /**
    * The selection operator. If {@code null}, this algorithm uses binary
    * tournament selection without replacement, replicating the behavior of the
    * original NSGA-II implementation.
    */
   private final Selection selection;

   /**
    * The variation operator.
    */
   private final Variation variation;

   private final double delta;
   private final double increaseMTo;
   private final double decreaseRTo;
   private final int everyNGenerations;
   private int generations;
   private double prevBestEvalObj;
   private final double adaptBy;

   /**
    * Constructs the NSGA-II algorithm with the specified components.
    *
    * @param problem
    *           the problem being solved
    * @param population
    *           the population used to store solutions
    * @param archive
    *           the archive used to store the result; can be {@code null}
    * @param selection
    *           the selection operator
    * @param variation
    *           the variation operator
    * @param initialization
    *           the initialization method
    */
   public CustomNSGAII(final Problem problem, final NondominatedSortingPopulation population,
         final EpsilonBoxDominanceArchive archive, final Selection selection, final Variation variation,
         final Initialization initialization, final double increaseMutationTo, final double decreaseRecombinationTo,
         final double delta, final double increaseBy, final int everyNGens) {
      super(problem, population, archive, initialization);
      this.selection = selection;
      this.variation = variation;
      this.delta = delta;
      this.increaseMTo = increaseMutationTo;
      this.decreaseRTo = decreaseRecombinationTo;
      this.everyNGenerations = everyNGens;
      this.generations = 0;
      this.prevBestEvalObj = Double.POSITIVE_INFINITY;
      this.adaptBy = increaseBy;
   }

   @Override
   public EpsilonBoxDominanceArchive getArchive() {
      return (EpsilonBoxDominanceArchive) super.getArchive();
   }

   @Override
   public NondominatedSortingPopulation getPopulation() {
      return (NondominatedSortingPopulation) super.getPopulation();
   }

   @Override
   public void iterate() {
      final NondominatedSortingPopulation population = getPopulation();
      final EpsilonBoxDominanceArchive archive = getArchive();
      final Population offspring = new Population();
      final int populationSize = population.size();

      // AbstractProbabilityVariation
      // if(this.populationVariation != null) {
      // final Solution[] sArr = new Solution[population.size()];
      // int idx = 0;
      //
      // for(final Solution s : population) {
      // sArr[idx++] = s;
      // }
      //
      // populationVariation.evolve(sArr);
      // }

      if(generations > 0 && generations % this.everyNGenerations == 0) {
         double minObj = Double.POSITIVE_INFINITY;
         for(final Solution s : population) {
            final double curObj = s.getObjective(0);
            if(curObj < minObj) {
               minObj = curObj;
            }
         }
         if(this.prevBestEvalObj - minObj < this.delta) {
            // adapt
            if(this.variation instanceof CustomCompoundVariation) {
               final CustomCompoundVariation ccv = (CustomCompoundVariation) this.variation;
               for(final Variation v : ccv.getVariationOperators()) {
                  if(v instanceof AbstractMutationVariation) {
                     final AbstractMutationVariation apv = (AbstractMutationVariation) v;
                     if(apv.getProbability() <= this.increaseMTo - adaptBy) {
                        final double setTo = apv.getProbability() + adaptBy;
                        apv.setProbability(setTo);
                        System.out.println(String.format("Changed mp to %.2f", setTo));

                     }
                  } else if(v instanceof AbstractParentalCrossoverVariation) {
                     final AbstractParentalCrossoverVariation apc = (AbstractParentalCrossoverVariation) v;
                     if(apc.getProbability() >= this.decreaseRTo + adaptBy) {
                        final double setTo = apc.getProbability() - adaptBy;
                        apc.setProbability(setTo);
                        System.out.println(String.format("Changed cp to %.2f", setTo));
                     }
                  }
               }
            }

         }
         this.prevBestEvalObj = minObj;
      }

      if(selection == null) {
         // recreate the original NSGA-II implementation using binary
         // tournament selection without replacement; this version works by
         // maintaining a pool of candidate parents.
         final LinkedList<Solution> pool = new LinkedList<>();

         final DominanceComparator comparator = new ChainedComparator(new ParetoDominanceComparator(),
               new CrowdingComparator());

         while(offspring.size() < populationSize) {
            // ensure the pool has enough solutions
            while(pool.size() < 2 * variation.getArity()) {
               final List<Solution> poolAdditions = new ArrayList<>();

               for(final Solution solution : population) {
                  poolAdditions.add(solution);
               }

               PRNG.shuffle(poolAdditions);
               pool.addAll(poolAdditions);
            }

            // select the parents using a binary tournament
            final Solution[] parents = new Solution[variation.getArity()];

            for(int i = 0; i < parents.length; i++) {
               parents[i] = TournamentSelection.binaryTournament(pool.removeFirst(), pool.removeFirst(), comparator);
            }

            // evolve the children
            offspring.addAll(variation.evolve(parents));
         }
      } else {
         // run NSGA-II using selection with replacement; this version allows
         // using custom selection operators
         while(offspring.size() < populationSize) {
            final Solution[] parents = selection.select(variation.getArity(), population);

            offspring.addAll(variation.evolve(parents));
         }
      }

      evaluateAll(offspring);

      if(archive != null) {
         archive.addAll(offspring);
      }

      population.addAll(offspring);
      population.truncate(populationSize);

      this.generations++;
   }

}
