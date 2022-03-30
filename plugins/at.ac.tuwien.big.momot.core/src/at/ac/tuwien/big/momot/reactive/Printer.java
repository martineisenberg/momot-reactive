package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.moea.SearchResultManager;
import at.ac.tuwien.big.moea.print.ISolutionWriter;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class Printer {
   protected PrintStream ps;
   protected ISolutionWriter sw;

   public Printer(final PrintStream ps, final ISolutionWriter sw) {
      this.ps = ps;
      this.sw = sw;

   }

   public Printer(final String outPath, final ISolutionWriter sw) {
      try {
         this.ps = new PrintStream(outPath);
      } catch(final FileNotFoundException e) {
         e.printStackTrace();
      }
      this.sw = sw;
   }

   public void close() {
      this.ps.close();
   }

   public PrintStream getPrintStream() {
      return ps;
   }

   public Printer header1(final String header) {
      ps.println("##########################################################################################");
      ps.println(header);
      ps.println("##########################################################################################\n");
      ps.flush();
      return this;
   }

   public Printer header1(final String header, final boolean verbose) {
      if(verbose) {
         return this.header1(header);
      }
      return this;
   }

   public Printer header2(final String header) {
      ps.println("******************************************************************************************");
      ps.println(header);
      ps.println("******************************************************************************************\n");

      return this;
   }

   public Printer header2(final String header, final boolean verbose) {
      if(verbose) {
         return this.header2(header);
      }
      return this;
   }

   public Printer newline() {
      ps.print("\n");
      return this;
   }

   public Printer newline(final boolean verbose) {
      if(verbose) {
         return this.newline();
      }
      return this;
   }

   public Printer plan(final TransformationSolution ts) {
      SearchResultManager.saveSolution(ps, ts, sw, false);
      ps.print("\n");
      return this;

   }

   public Printer plan(final TransformationSolution ts, final boolean verbose) {
      if(verbose) {
         return this.plan(ts);
      }
      return this;
   }

   // public Printer printModel(final EGraph graph, final boolean verbose) {
   // if(verbose) {
   // return this.str(StackUtils.getReprFromEGraph(graph));
   // }
   // return this;
   // }

   public Printer printChangeDetails(final int executedRulesSoFar, final int overallExecutionRules,
         final List<ITransformationVariable> plan, final boolean verbose) {
      if(verbose) {
         return header2("🛑 CHANGE EVENT")
               .subheader(
                     String.format("PLAN EXECUTED SO FAR (%d/%d Rules)", executedRulesSoFar, overallExecutionRules),
                     verbose)
               .str(plan.toString(), verbose).newline().subheader("RESULT MODEL (after planned execution so far)");

      }

      return this;
   }

   public Printer property(final String property, final String value) {
      ps.println(String.format("%s: %s", property, value));
      return this;
   }

   public Printer property(final String property, final String value, final boolean verbose) {
      if(verbose) {
         return this.property(property, value);
      }
      return this;
   }

   public Printer str(final String s) {
      ps.println(s + "\n");
      return this;
   }

   public Printer str(final String header, final boolean verbose) {
      if(verbose) {
         return this.str(header);
      }
      return this;
   }

   public Printer subheader(final String subheader) {
      ps.println("----- " + subheader + " -----\n");
      return this;
   }

   public Printer subheader(final String header, final boolean verbose) {
      if(verbose) {
         return this.subheader(header);
      }
      return this;
   }

}
