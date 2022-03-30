package at.ac.tuwien.big.momot.reactive.error;

import java.util.Random;

public class ErrorUtils {

   private static Random rand = new Random();

   public static int getIndexForErrorRange(final ErrorOccurence eo, final int planLength) {
      switch(eo) {
         case FIRST_10_PERCENT:
            return rand.ints(0, (int) (planLength * 0.1)).findFirst().getAsInt();

         case LAST_10_PERCENT:
            return rand.ints((int) (planLength * 0.9), planLength - 1).findFirst().getAsInt();
         case MIDDLE_10_PERCENT:
            return rand.ints((int) (planLength * 0.45), (int) (planLength * 0.55)).findFirst().getAsInt();
         default:
            throw new RuntimeException("Wrong error occurence type passed; CAnnot generate index for error range!");

      }
   }
}
