package at.ac.tuwien.big.moea.experiment.executor.listener;

import at.ac.tuwien.big.moea.problem.solution.variable.PlaceholderVariable;
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
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.util.progress.ProgressEvent;

public class SeedreuseProportionListener extends AbstractProgressListener {

   private List<String[]> dataLines;
   private final List<ApplicationState> seedApps;

   public SeedreuseProportionListener() {
      this.seedApps = new ArrayList<>();

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
            dataLines.add(0, new String[] { "variant", "run", "solution_nr", "runtime", "evaluations", "solution_rules",
                  "overlap_seeded_rules", "seeded_rules" });
         }
         // event.getExecutor().getInstrumenter().getLastAccumulator().get("p, runNr)

         final Accumulator a = event.getExecutor().getInstrumenter().getLastAccumulator();
         final ArrayList<Solution> population = (ArrayList<Solution>) a.get("Population", a.size("Population") - 1);
         final NondominatedPopulation ndp = new NondominatedPopulation(population);

         int solutionNr = 0;
         for(final Solution s : ndp) {
            final List<ApplicationState> curSolStates = new ArrayList<>();
            for(int i = 0; i < s.getNumberOfVariables(); i++) {
               final Variable v = s.getVariable(i);
               if(!(v instanceof PlaceholderVariable)) {
                  final UnitApplication ua = (UnitApplication) v;
                  curSolStates.add(new ApplicationState(ua.getUnit(), this.extractParameters(ua.getAssignment())));
               }
            }

            final long noOverlaps = curSolStates.stream().filter(seedApps::contains).count();
            dataLines.add(new String[] { String.valueOf(experimentName), String.valueOf(runNr),
                  String.valueOf(solutionNr++), String.valueOf(event.getElapsedTime()),
                  String.valueOf(event.getMaxNFE()), String.valueOf(curSolStates.size()), String.valueOf(noOverlaps),
                  String.valueOf(seedApps.size()) });

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
