package at.ac.tuwien.big.moea.experiment.executor.listener;

import at.ac.tuwien.big.moea.problem.solution.variable.IPlaceholderVariable;
import at.ac.tuwien.big.moea.search.algorithm.reinforcement.datastructures.ApplicationState;
import at.ac.tuwien.big.moea.util.CSVUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.henshin.interpreter.Assignment;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.model.Parameter;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.util.progress.ProgressEvent;

public class SeedreuseProportionListener extends AbstractProgressListener {

   private List<String[]> dataLines;
   private List<ApplicationState> seedApps;

   public SeedreuseProportionListener() {

   }

   private Map<String, Object> extractParameters(final Assignment assignment) {
      final Map<String, Object> paramValues = new HashMap<>();

      for(final Parameter p : assignment.getUnit().getParameters()) {
         if(assignment.getParameterValue(p) != null) {
            paramValues.put(p.getName(), assignment.getParameterValue(p));
         }
      }

      return paramValues;
   }

   public void setSeedSolution(final List<? extends UnitApplication> s) {
      for(final UnitApplication ua : s) {
         seedApps.add(new ApplicationState(ua.getUnit(), this.extractParameters(ua.getAssignment())));
      }
   }

   @Override
   public void update(final ProgressEvent event) {
      if(isStarted(event) || isSeedStarted(event)) {
         dataLines = new ArrayList<>();

      }

      if(event.isSeedFinished()) {

         if(!Files.exists(Paths.get(listenerDir + "_seedreuseProportion.csv"))) {
            dataLines.add(0, new String[] { "variant", "run", "seconds", "evaluations", "overlap_seeded_rules",
                  "solution_rules", "seeded_rules" });
         }

         final NondominatedPopulation ndp = event.getCurrentAlgorithm().getResult();

         for(final Solution s : ndp) {
            final List<ApplicationState> curSolStates = new ArrayList<>();
            for(int i = 0; i < s.getNumberOfVariables(); i++) {
               final UnitApplication ua = (UnitApplication) s.getVariable(i);
               if(!(ua instanceof IPlaceholderVariable)) {
                  curSolStates.add(new ApplicationState(ua.getUnit(), this.extractParameters(ua.getAssignment())));
               }
            }

            final long noOverlaps = curSolStates.stream().filter(seedApps::contains).count();
            dataLines.add(new String[] { String.valueOf(experimentName), String.valueOf(runNr),
                  String.valueOf(event.getElapsedTime()), String.valueOf(event.getCurrentNFE()),
                  String.valueOf(noOverlaps), String.valueOf(curSolStates.size()), String.valueOf(seedApps.size()) });

         }

         try(PrintWriter pw = new PrintWriter(new FileOutputStream(
               new File(Paths.get(listenerDir + "_seedreuseProportion.csv").toString()), true /* append = true */))) {
            dataLines.stream().map(CSVUtil::convertToCSV).forEach(pw::println);
         } catch(final FileNotFoundException e1) {
            e1.printStackTrace();
         }

         dataLines = new ArrayList<>();

      }

   }
}
