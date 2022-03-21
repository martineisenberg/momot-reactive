package at.ac.tuwien.big.momot.reactive.result;

import java.util.ArrayList;
import java.util.List;

public class ReactiveResult {
   private final List<Float> finalObjectives;

   public ReactiveResult() {
      this.finalObjectives = new ArrayList<>();
   }

   public void addFinalObjective(final float obj) {
      this.finalObjectives.add(obj);
   }

   public List<Float> getFinalObjectives() {
      return this.finalObjectives;
   }
}
