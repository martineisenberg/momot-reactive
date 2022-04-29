package at.ac.tuwien.big.moea.experiment.executor.listener;

import at.ac.tuwien.big.moea.problem.solution.variable.PlaceholderVariable;
import at.ac.tuwien.big.moea.util.CSVUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Solution;
import org.moeaframework.util.progress.ProgressEvent;

public class GenerationExecutionTimeListener extends AbstractProgressListener {

   private final int printInterval;
   private List<String[]> dataLines;
   private int nfeCount = 0;

   public GenerationExecutionTimeListener(final int printInterval) {
      dataLines = new ArrayList<>();

      // dataLines.add(
      // new String[] { String.valueOf(runNr), String.valueOf(0), String.valueOf(0), String.valueOf(reseedObj) });
      this.printInterval = printInterval;
   }

   private List<Integer> getPlanSizeWithoutPlaceholders(final List<Solution> sList) {
      final List<Integer> lengths = new ArrayList<>();
      for(final Solution s : sList) {
         int nrOfUnitApps = 0;
         for(int i = 0; i < s.getNumberOfVariables(); i++) {
            if(!(s.getVariable(i) instanceof PlaceholderVariable)) {
               nrOfUnitApps++;
            }
         }
         lengths.add(nrOfUnitApps);
      }
      return lengths;
   }

   @Override
   public void update(final ProgressEvent event) {
      if(isStarted(event) || isSeedStarted(event)) {
         dataLines = new ArrayList<>();

      }

      final int currentNFE = event.getCurrentNFE();
      if(event.getCurrentAlgorithm() != null && currentNFE / printInterval >= nfeCount) {
         nfeCount++;

         // final Algorithm a = event.getCurrentAlgorithm();

         final Accumulator a = event.getExecutor().getInstrumenter().getLastAccumulator();
         final ArrayList<Solution> population = (ArrayList<Solution>) a.get("Population", a.size("Population") - 1);

         // Population curP = null;
         // if(a instanceof EvolutionaryAlgorithm) {
         // curP = ((EvolutionaryAlgorithm) a).getPopulation();
         // }

         final List<Integer> planLengths = getPlanSizeWithoutPlaceholders(population);
         // curP.forEach(s -> {
         // planLength.add((double) s.getNumberOfVariables());
         // });

         final DoubleSummaryStatistics stat = planLengths.stream().mapToDouble(Double::valueOf).summaryStatistics();

         dataLines.add(new String[] { String.valueOf(experimentName), String.valueOf(runNr),
               String.valueOf(event.getElapsedTime()), String.valueOf(currentNFE), String.valueOf(stat.getAverage()),
               String.valueOf(stat.getMax()), String.valueOf(stat.getMin()), String.valueOf(stat.getSum()),
               String.valueOf(stat.getCount()) });

      }
      if(event.isSeedFinished()) {
         nfeCount = 0;

         if(!Files.exists(Paths.get(listenerDir + "_generationExecutionTime.csv"))) {
            dataLines.add(0, new String[] { "variant", "run", "seconds", "evaluations", "length_avg", "length_max",
                  "length_min", "length_sum", "plan_count" });
         }

         try(PrintWriter pw = new PrintWriter(
               new FileOutputStream(new File(Paths.get(listenerDir + "_generationExecutionTime.csv").toString()),
                     true /* append = true */))) {
            dataLines.stream().map(CSVUtil::convertToCSV).forEach(pw::println);
         } catch(final FileNotFoundException e1) {
            e1.printStackTrace();
         }

         dataLines = new ArrayList<>();

      }

   }
}
