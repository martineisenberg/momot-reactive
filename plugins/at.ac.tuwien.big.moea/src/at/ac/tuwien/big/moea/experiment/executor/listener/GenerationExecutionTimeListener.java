package at.ac.tuwien.big.moea.experiment.executor.listener;

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

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.EvolutionaryAlgorithm;
import org.moeaframework.core.Population;
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

   @Override
   public void update(final ProgressEvent event) {
      if(isStarted(event) || isSeedStarted(event)) {
         dataLines = new ArrayList<>();

      }

      final int currentNFE = event.getCurrentNFE();
      if(event.getCurrentAlgorithm() != null && currentNFE / printInterval >= nfeCount) {
         nfeCount++;

         final Algorithm a = event.getCurrentAlgorithm();
         Population curP = null;
         if(a instanceof EvolutionaryAlgorithm) {
            curP = ((EvolutionaryAlgorithm) a).getPopulation();
         }

         final List<Double> planLength = new ArrayList<>();

         curP.forEach(s -> {
            planLength.add((double) s.getNumberOfVariables());
         });

         final DoubleSummaryStatistics stat = planLength.stream().mapToDouble(Double::valueOf).summaryStatistics();

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
