package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.moea.util.MathUtil;
import at.ac.tuwien.big.momot.domain.Heuristic;
import at.ac.tuwien.big.momot.examples.stack.stack.Stack;
import at.ac.tuwien.big.momot.examples.stack.stack.StackModel;
import at.ac.tuwien.big.momot.examples.stack.stack.impl.StackModelImpl;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.reactive.Executor;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.emf.henshin.interpreter.EGraph;

public class StackHeuristic implements Heuristic {

   private static StackHeuristic instance;

   public static Heuristic getInstance() {
      if(instance == null) {
         instance = new StackHeuristic();
      }
      return instance;
   }

   private StackHeuristic() {
   }

   @Override
   public List<ITransformationVariable> getInitialPopulationTs(EGraph g, final Executor executor,
         final int maxSolutionLength) {
      g = MomotUtil.copy(g);
      final StackModel sm = MomotUtil.getRoot(g, StackModelImpl.class);
      final double initialStd = MathUtil
            .getStandardDeviation(sm.getStacks().stream().map(s -> s.getLoad()).collect(Collectors.toList()));

      final List<ITransformationVariable> concatTrafoSeq = new ArrayList<>();

      for(int i = 0; i < 30; i++) {
         // while(concatTrafoSeq.size() < maxSolutionLength) {
         final int noStacks = sm.getStacks().size();

         final List<Integer> loads = sm.getStacks().stream().map(s -> s.getLoad()).collect(Collectors.toList());
         final double std = MathUtil.getStandardDeviation(loads);
         final double mean = MathUtil.getMean(loads);
         final List<Stack> lows = sm.getStacks().stream().filter(s -> s.getLoad() < mean).collect(Collectors.toList());
         final Map<Stack, Map<Stack, Double>> stackRankings = new HashMap<>();
         final List<Stack> highs = sm.getStacks().stream().filter(s1 -> s1.getLoad() > Math.ceil(mean))
               .collect(Collectors.toList());
         System.out.println(
               "Loading state: " + sm.getStacks().stream().map(s1 -> s1.getLoad()).collect(Collectors.toList()));

         // final List<ITransformationVariable> transformationSeqs = new ArrayList<>();

         for(final Stack s : lows) {

            final Map<Stack, Double> potentials = new HashMap<>();
            for(final Stack h : highs) {
               final double curPotential = rankStackPotential(sm.getStacks(), mean, h, s);
               potentials.put(h, curPotential);
            }
            stackRankings.put(s, potentials);
         }

         while(lows.size() > 0) {
            Stack selectedTargetStack = null;
            double maxPotential = Double.NEGATIVE_INFINITY;
            Stack maxPotentialStack = null;

            final List<ITransformationVariable> curTransformationSeq = new ArrayList<>();

            // final int curLowestLoadInLayout = stackRankings.keySet().stream().map(s -> s.getLoad()).mapToInt(l ->
            // l).min()
            // .getAsInt();
            // final Map<Stack, Map<Stack, Double>> lowestLoadStackRankings = stackRankings.entrySet().stream()
            // .filter(entry -> entry.getKey().getLoad() == curLowestLoadInLayout)
            // .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            final Iterator<Entry<Stack, Map<Stack, Double>>> stackRankingIter = stackRankings.entrySet().iterator();
            while(stackRankingIter.hasNext()) {
               final Entry<Stack, Map<Stack, Double>> targetsToSources = stackRankingIter.next();
               final Stack rankingOfStack = targetsToSources.getKey();
               if(targetsToSources.getValue().isEmpty()) {
                  lows.remove(rankingOfStack);
                  stackRankingIter.remove();
                  // stackRankings.remove(rankingOfStack);
                  continue;
               }

               final Entry<Stack, Double> maxPotentialEntry = targetsToSources.getValue().entrySet().stream()
                     .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();

               if(maxPotentialEntry.getValue() >= maxPotential) {
                  maxPotential = maxPotentialEntry.getValue();
                  selectedTargetStack = rankingOfStack;
                  maxPotentialStack = maxPotentialEntry.getKey();
               }
            }

            if(maxPotentialStack == null) {
               continue;
            }

            // System.out.println("Now shifting from load " + selectedTargetStack.getLoad());

            final int sendLoad = (int) Math.min(maxPotentialStack.getLoad() - mean,
                  mean - selectedTargetStack.getLoad());

            final boolean doShiftRight = this.isRightShortestPath(sm.getStacks().indexOf(maxPotentialStack),
                  sm.getStacks().indexOf(selectedTargetStack), noStacks);
            final String shiftRule = doShiftRight ? "shiftRight" : "shiftLeft";

            Stack toStack = getNN(maxPotentialStack, doShiftRight);
            Stack fromStack = maxPotentialStack;
            do {

               final ITransformationVariable var = executor.execute(shiftRule, g,
                     Map.of("fromId", fromStack.getId(), "toId", toStack.getId(), "amount", sendLoad));

               curTransformationSeq.add(var);

               toStack = getNN(toStack, doShiftRight);
               fromStack = getNN(fromStack, doShiftRight);

            } while(fromStack.getId().compareTo(selectedTargetStack.getId()) != 0
                  && concatTrafoSeq.size() + curTransformationSeq.size() < maxSolutionLength);

            lows.remove(selectedTargetStack);
            stackRankings.remove(selectedTargetStack);

            updateMapPostSelection(sm.getStacks(), maxPotentialStack, stackRankings, mean, std);

            // transformationSeqs.add(curTransformationSeq);
            concatTrafoSeq.addAll(curTransformationSeq);

            if(concatTrafoSeq.size() == maxSolutionLength) {
               break;
            }
         }

         System.out
               .println("After changes: " + sm.getStacks().stream().map(s -> s.getLoad()).collect(Collectors.toList()));
         System.out.println("New std: " + MathUtil
               .getStandardDeviation(sm.getStacks().stream().map(s -> s.getLoad()).collect(Collectors.toList())));
         System.out.println(String.format("Rel. Improvement to starting model in %%: %.3f",
               100 - 100 * MathUtil
                     .getStandardDeviation(sm.getStacks().stream().map(s -> s.getLoad()).collect(Collectors.toList()))
                     / initialStd));
         ;
         System.out.println("Plansize: " + concatTrafoSeq.size() + "\n");
         // }
      }
      // transformationSeqs.addAll(Stream.generate(() -> new ArrayList<>(concatTrafoSeq))
      // .limit((long) (populationSize * 0.2)).collect(Collectors.toList()));

      return concatTrafoSeq.subList(0, Math.min(maxSolutionLength, concatTrafoSeq.size()));
   }

   private Stack getNN(final Stack s, final boolean shiftRight) {
      return shiftRight ? s.getRight() : s.getLeft();
   }

   private double getNormIndexDistance(final int s1, final int s2, final int size) {
      final double indexDiff = Math.abs(s1 - s2);
      final double otherSide = size - indexDiff;

      return Math.min(indexDiff, otherSide) / (size / 2.0);
   }

   private boolean isRightShortestPath(final int from, final int to, final int size) {
      final double indexDiff = Math.abs(from - to);
      return from > to && indexDiff > size - indexDiff || from < to && indexDiff < size - indexDiff;
   }

   private double rankStackPotential(final List<Stack> stacks, final double mean, final Stack sourceStack,
         final Stack targetStack) {
      final double distanceFactor = 1
            - getNormIndexDistance(stacks.indexOf(targetStack), stacks.indexOf(sourceStack), stacks.size());

      final List<Stack> nns = new ArrayList<>();
      Stack ln = sourceStack.getLeft();
      Stack rn = sourceStack.getRight();

      for(int i = 0; i < (int) Math.sqrt(stacks.size()); i++) {
         nns.addAll(List.of(ln, rn));
         ln = ln.getLeft();
         rn = rn.getRight();
      }

      if(nns.contains(targetStack)) {
         nns.remove(targetStack);
      }
      final List<Double> reqLoads = new ArrayList<>();
      nns.forEach(st -> {
         reqLoads.add(mean - st.getLoad());
      });
      final int reqSum = (int) MathUtil.getSum(reqLoads);
      final double offerFactor = reqSum >= 0 ? Math.max(0, 1 - reqSum / (sourceStack.getLoad() - mean)) : 1;
      return distanceFactor / 2.0 + offerFactor / 2.0;
   }

   private void updateMapPostSelection(final List<Stack> stacks, final Stack shiftedFrom,
         final Map<Stack, Map<Stack, Double>> stackRankings, final double mean, final double std) {
      if(shiftedFrom.getLoad() > mean + std) {
         // update
         for(final Entry<Stack, Map<Stack, Double>> targetsToSources : stackRankings.entrySet()) {
            final Map<Stack, Double> curTargetPotentialMap = targetsToSources.getValue();
            curTargetPotentialMap.put(shiftedFrom,
                  rankStackPotential(stacks, mean, shiftedFrom, targetsToSources.getKey()));
         }

      } else { // remove as potential source for all target stacks
         for(final Entry<Stack, Map<Stack, Double>> targetsToSources : stackRankings.entrySet()) {
            targetsToSources.getValue().remove(shiftedFrom);
         }
      }
   }

}
