package at.ac.tuwien.big.momot.reactive.error;

public class Disturbance {
   public static Disturbance of(final ErrorType type, final int atIteration, final int plannedIterations) {
      return new Disturbance(type, atIteration, plannedIterations);
   }

   private final ErrorType type;
   private final int atIteration;

   private final int plannedIterations;

   private Disturbance(final ErrorType type, final int atIteration, final int plannedIterations) {
      this.type = type;
      this.atIteration = atIteration;
      this.plannedIterations = plannedIterations;
   }

   public int getAtIteration() {
      return atIteration;
   }

   public int getPlannedIterations() {
      return plannedIterations;
   }

   public ErrorType getType() {
      return type;
   }

   @Override
   public String toString() {
      return String.format("(%d, %s, %d", atIteration, type, plannedIterations);
   }

}
