package at.ac.tuwien.big.moea.experiment.executor.listener;

import at.ac.tuwien.big.moea.util.CSVUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.util.progress.ProgressEvent;

public class CurrentBestObjectiveListener extends AbstractProgressListener {

   private final int printInterval;
   private List<String[]> dataLines;
   private int nfeCount = 0;
   private final int objectiveIndex;
   private double priorBestObjValue;

   public CurrentBestObjectiveListener(final int objectiveIndex, final int printInterval) {
      dataLines = new ArrayList<>();

      // dataLines.add(
      // new String[] { String.valueOf(runNr), String.valueOf(0), String.valueOf(0), String.valueOf(reseedObj) });
      this.printInterval = printInterval;
      this.objectiveIndex = objectiveIndex;
   }

   public void setPriorBestObjValue(final double val) {
      this.priorBestObjValue = val;
   }

   @Override
   public void update(final ProgressEvent event) {
      if(isStarted(event) || isSeedStarted(event)) {
         dataLines = new ArrayList<>();

      }
      final int currentNFE = event.getCurrentNFE();
      if(event.getCurrentAlgorithm() != null && currentNFE / printInterval >= nfeCount) {
         nfeCount++;

         final NondominatedPopulation ndp = event.getCurrentAlgorithm().getResult();
         double minObj = Double.POSITIVE_INFINITY;
         for(final Solution s : ndp) {
            final double curSolObj = s.getObjective(objectiveIndex);
            if(curSolObj < minObj) {
               minObj = curSolObj;
            }
         }

         dataLines.add(new String[] { String.valueOf(experimentName), String.valueOf(runNr),
               String.valueOf(event.getElapsedTime()), String.valueOf(currentNFE), String.valueOf(minObj) });

      }
      if(event.isSeedFinished()) {
         nfeCount = 0;

         dataLines.add(0, new String[] { String.valueOf(experimentName), String.valueOf(runNr), "0", "0",
               String.valueOf(this.priorBestObjValue) });

         if(!Files.exists(Paths.get(listenerDir + "_bestObj.csv"))) {

            dataLines.add(0, new String[] { "variant", "run", "seconds", "evaluations", "objective_value" });
         }

         try(PrintWriter pw = new PrintWriter(new FileOutputStream(
               new File(Paths.get(listenerDir + "_bestObj.csv").toString()), true /* append = true */))) {
            dataLines.stream().map(CSVUtil::convertToCSV).forEach(pw::println);
         } catch(final FileNotFoundException e1) {
            e1.printStackTrace();
         }

         dataLines = new ArrayList<>();

      }

   }
}
