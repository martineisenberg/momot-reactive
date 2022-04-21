package at.ac.tuwien.big.moea.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVUtil {

   public static void addWithEnumeration(final List<String[]> data, final String[] exp, final List<String[]> addVals) {
      int run = 0;

      for(final String[] o : addVals) {
         data.add(concatArrays(exp, new String[] { String.valueOf(run++) }, o));
      }
   }

   public static void addWithEnumeration(final List<String[]> data, final String[] exp, final List<String[]> addVals,
         final int div) {
      int run = 0;

      for(final String[] o : addVals) {
         data.add(concatArrays(exp, new String[] { String.valueOf(run / div) }, o));
         run++;
      }
   }

   public static String[] concatArrays(final String[]... arrays) {
      return Arrays.stream(arrays).flatMap(Arrays::stream).toArray(String[]::new);
   }

   public static String convertToCSV(final String[] data) {
      return Stream.of(data).map(CSVUtil::escapeSpecialCharacters).collect(Collectors.joining(","));
   }

   public static String escapeSpecialCharacters(String data) {
      String escapedData = data.replaceAll("\\R", " ");
      if(data.contains(",") || data.contains("\"") || data.contains("'")) {
         data = data.replace("\"", "\"\"");
         escapedData = "\"" + data + "\"";
      }
      return escapedData;
   }

}
