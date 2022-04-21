package at.ac.tuwien.big.momot.reactive.planningstrategy;

public class Planning {

   public static Planning create(final PlanningStrategy planningStrategy, final ReplanningStrategy replanningStrategy) {
      return new Planning(planningStrategy, replanningStrategy);
   }

   private final ReplanningStrategy replanningStrategy;
   private final PlanningStrategy planningStrategy;

   private Planning(final PlanningStrategy planningStrategy, final ReplanningStrategy replanningStrategy) {
      this.planningStrategy = planningStrategy;
      this.replanningStrategy = replanningStrategy;

   }

   public PlanningStrategy getPlanningStrategy() {
      return planningStrategy;
   }

   public ReplanningStrategy getReplanningStrategy() {
      return replanningStrategy;
   }

}
