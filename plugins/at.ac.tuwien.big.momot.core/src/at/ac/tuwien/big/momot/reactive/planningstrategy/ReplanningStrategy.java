package at.ac.tuwien.big.momot.reactive.planningstrategy;

public abstract class ReplanningStrategy {
   public enum RepairStrategy {
      NAIVE, REPLAN_FOR_EVALUATIONS, REPLAN_FOR_CONDITION, MIXED, REPLAN_PREDICTIVE
   }

   protected RepairStrategy repairStrategy;

   protected ReplanningStrategy(final RepairStrategy repairStrategy) {
      this.repairStrategy = repairStrategy;
   }

   public RepairStrategy getRepairStrategy() {
      return this.repairStrategy;
   }

   @Override
   public abstract String toString();
}
