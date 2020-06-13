package anomalydetection.clustering;

import anomalydetection.util.UtilClass;



/**
 * A Utility class that contains summary information on an
 * the values that appear in a dataset for a particular attribute.
 *
 * @author <a href="mailto:len@reeltwo.com">Len Trigg</a>
 * @version $Revision: 8034 $
 */
public class AttributeStats{
  
  /** The number of int-like values */
  public int intCount = 0;
  
  /** The number of real-like values (i.e. have a fractional part) */
  public int realCount = 0;
  
  /** The number of missing values */
  public int missingCount = 0;
  
  /** The number of distinct values */
  public int distinctCount = 0;
  
  /** The number of values that only appear once */
  public int uniqueCount = 0;
  
  /** The total number of values (i.e. number of instances) */
  public int totalCount = 0;
  
  /** Stats on numeric value distributions */
  // perhaps Stats should be moved from weka.experiment to weka.core
  public Stats numericStats;
  
  /** Counts of each nominal value */
  public int [] nominalCounts;
  
  /** Weight mass for each nominal value */
  public double[] nominalWeights;
    
  /**
   * Updates the counters for one more observed distinct value.
   *
   * @param value the value that has just been seen
   * @param count the number of times the value appeared
   * @param weight the weight mass of the value
   */
  protected void addDistinct(double value, int count, double weight) {   
	  if (count > 0) {
		  if (count == 1) {
			  uniqueCount++;
		  }
		  if (UtilClass.eq(value, (double)((int)value))) {
			  intCount += count;
		  } else {
			  realCount += count;
		  }
		  if (nominalCounts != null) {
			  nominalCounts[(int)value] = count;
			  nominalWeights[(int)value] = weight;
		  }
		  if (numericStats != null) {
			  numericStats.add(value, weight);
			  numericStats.calculateDerived();
		  }
	  }
	  distinctCount++;
  }

}
