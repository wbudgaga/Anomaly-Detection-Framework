package anomalydetection.clustering;

public class Stats{ 
	/** The number of values seen */
	public double count = 0;

	/** The sum of values seen */
	public double sum = 0;

	/** The sum of values squared seen */
	public double sumSq = 0;

	/** The std deviation of values at the last calculateDerived() call */    
	public double stdDev = Double.NaN;

	/** The mean of values at the last calculateDerived() call */    
	public double mean = Double.NaN;

	/** The minimum value seen, or Double.NaN if no values seen */
	public double min = Double.NaN;

	/** The maximum value seen, or Double.NaN if no values seen */
	public double max = Double.NaN;
    
  /**
   * Adds a value to the observed values
   *
   * @param value the observed value
   */
	public void add(double value) {
		add(value, 1);
	}

  /**
   * Adds a value that has been seen n times to the observed values
   *
   * @param value the observed value
   * @param n the number of times to add value
   */
	public void add(double value, double n) {
	    sum += value * n;
	    sumSq += value * value * n;
	    count += n;
	    if (Double.isNaN(min)) {
	    	min = max = value;
	    } else if (value < min) {
	    	min = value;
	    } else if (value > max) {
	    	max = value;
	    }
	}

  /**
   * Removes a value to the observed values (no checking is done
   * that the value being removed was actually added). 
   *
   * @param value the observed value
   */
	public void subtract(double value) {
	    subtract(value, 1);
	}

  /**
   * Subtracts a value that has been seen n times from the observed values
   *
   * @param value the observed value
   * @param n the number of times to subtract value
   */
	public void subtract(double value, double n) {
	    sum 	-= value * n;
	    sumSq 	-= value * value * n;
	    count 	-= n;
	}

  /**
   * Tells the object to calculate any statistics that don't have their
   * values automatically updated during add. Currently updates the mean
   * and standard deviation.
   */
	public void calculateDerived() {
		mean = Double.NaN;
		stdDev = Double.NaN;
		if (count > 0) {
			mean = sum / count;
			stdDev = Double.POSITIVE_INFINITY;
			if (count > 1) {
				stdDev = sumSq - (sum * sum) / count;
				stdDev /= (count - 1);
				if (stdDev < 0) {
					stdDev = 0;
				}
				stdDev = Math.sqrt(stdDev);
			}
		}
	}
    
  /**
   * Returns a string summarising the stats so far.
   *
   * @return the summary string
   */
  public String toString() {

    calculateDerived();
    return
    	"Count   " + (Math.round(count * 1.0E8) / 1.0E8)	+ '\n'  
      + "Min     " + (Math.round(min * 1.0E8) 	/ 1.0E8) 	+ '\n'
      + "Max     " + (Math.round(max * 1.0E8) 	/ 1.0E8) 	+ '\n'
      + "Sum     " + (Math.round(sum * 1.0E8) 	/ 1.0E8)	+ '\n'
      + "SumSq   " + (Math.round(sumSq * 1.0E8) / 1.0E8)	+ '\n'
      + "Mean    " + (Math.round(mean * 1.0E8) 	/ 1.0E8) 	+ '\n'
      + "StdDev  " + (Math.round(stdDev * 1.0E8)/ 1.0E8) 	+ '\n';
  }
  
  /**
   * Tests the paired stats object from the command line.
   * reads line from stdin, expecting two values per line.
   *
   * @param args ignored.
   */
  public static void main(String [] args) {
      Stats ps = new Stats();
 	  ps.add(5);
 	  ps.add(4);
 	  ps.add(5);
 	  ps.add(3);      
      ps.calculateDerived();
      System.err.println(ps);
   }

} // Stats

