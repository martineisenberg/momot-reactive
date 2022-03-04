package at.ac.tuwien.big.momot.examples.stack;

import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Printer {
   protected PrintStream ps;

   protected Printer(final PrintStream ps) {
      this.ps = ps;
   }

   public Printer(final String outPath) {
      try {
         this.ps = new PrintStream(outPath);
      } catch(final FileNotFoundException e) {
         e.printStackTrace();
      }
   }

   public void close() {
      this.ps.close();
   }

   public Printer header1(final String header) {
      ps.println("##########################################################################################");
      ps.println(header);
      ps.println("##########################################################################################\n");
      return this;
   }

   public Printer header2(final String header) {
      ps.println("##################################");
      ps.println(header);
      ps.println("##################################\n   ");
      return this;
   }

   public Printer newline() {
      ps.print("\n");
      return this;
   }

   public Printer plan(final TransformationSolution ts) {
      StackSearch.saveSolution(ps, ts);
      ps.print("\n");
      return this;

   }

   public Printer property(final String property, final String value) {
      ps.println(String.format("%s: %s", property, value));
      return this;
   }

   public Printer str(final String s) {
      ps.println(s + "\n");
      return this;
   }

   public Printer subheader(final String subheader) {
      ps.println("-----" + subheader + "-----\n");
      return this;
   }

}
