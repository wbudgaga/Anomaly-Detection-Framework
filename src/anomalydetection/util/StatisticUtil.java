package anomalydetection.util;

public class StatisticUtil {
	/** The small deviation allowed in double comparisons. */
	public static double SMALL = 1e-6;
	  
	/** The natural logarithm of 2. */
	public static double log2 = Math.log(2);

	/** Constant for normal distribution. */
	private static double m_normConst = Math.log(Math.sqrt(2 * Math.PI));

	public static boolean eq(double a, double b){
		return Math.abs(a - b) < SMALL;
	}

	public static  double log2(double a) {
	    return Math.log(a) / log2;
	}
	
	public static int maxIndex(double[] doubles) {
	    double maximum = Double.MIN_VALUE;
	    int maxIndex = 0;
	    for (int i = 0; i < doubles.length; i++) {
	    	if (doubles[i] > maximum) {
	    		maxIndex = i;
	    		maximum = doubles[i];
	    	}
	    }
	    return maxIndex;
	}
	
	public static double sum(double[] input){
		double sum = 0;
		int i;
		for (i = 0 ; i < input.length; ++ i){
			sum += input[i];
		}
		return sum;
	}
	public static double mean(double[] input){
		double sum = 0;
		int i;
		for (i = 0 ; i < input.length; ++ i){
			sum += input[i];
		}
		return sum / i;
	}


	public static double var(double[] input, double mean){
		double sum = 0;
		for (int i = 0 ; i < input.length; ++i){
			sum += (input[i] - mean ) * (input[i] - mean );
		}
		return sum/(input.length - 1);
	}

	public static double std(double[] input, double mean){
		double sum = 0;
		for (int i = 0 ; i < input.length; ++i){
			sum += (input[i] - mean ) * (input[i] - mean );
		}
		return Math.sqrt(sum/input.length);
	}

	public static void normalizeSTD(double[] input){
		double mean = mean (input);
		double std  = std (input, mean);
		for (int i = 0 ; i < input.length; ++ i){
			input[i] = (input[i] - mean)/std;
		}
	}	
	
/*	 public static void normalize(double[] doubles) {
		 double sum = 0;
		 for (int i = 0; i < doubles.length; i++) {
			 sum += doubles[i];
		 }
		 normalize(doubles, sum);
	 }
*/
	 public static void normalize(double[] doubles, double sum) {
		 if (Double.isNaN(sum)) {
			 throw new IllegalArgumentException("Can't normalize array. Sum is NaN.");
		 }
		 if (sum == 0) {
			 throw new IllegalArgumentException("Can't normalize array. Sum is zero.");
		 }
		 for (int i = 0; i < doubles.length; i++) {
			 doubles[i] /= sum;
		 }
	 }
	 
	 // calculates aa[i] = e^(a[i]-max)  and sum =sum(aa[i])
	public static double expOfDiff2Max(double[] a,  double max) {
	    double sum = 0;
	    for(int i = 0; i < a.length; i++) {
    		a[i] = Math.exp(a[i] - max);
   	    	sum += a[i];
	    }
	    return sum;
	} 
		


	 /**
	  * Density function of normal distribution for particular class(cluster)
	  *  
	  * log (P(X|C = k))= log(pdf)
	  * pdf(x,μ,σ) = 1/(σ(2pi)^0.5)  * e ^ ( - (x - μ )^2/(2σ^2) )
	  * 
	  * x: attribute in a sample, mean: distribution mean for attribute x,  stdDev: distribution stdDev for attribute x
	  */
	 public static double logPdfx(double x, double mean, double stdDev) {
		 double diff = x - mean;
		 if (Double.isNaN(-(diff * diff / (2 * stdDev * stdDev)) - m_normConst - Math.log(stdDev))) //#####
			 System.out.println("Can't normalize array. Sum is NaN.");


		 return -(diff * diff / (2 * stdDev * stdDev)) - m_normConst - Math.log(stdDev);
	 }
	 
	  /**
	   * Computes the log of the conditional density for particular cluster for a given sample x
	   * 
	   * P(X|C=k)=P(x1,x2,..,xn|C=k)=P(x1|C=k)P(x2|C=k)...P(xn|C=k)
	   * Log(P(X|C=k)) = log(P(x1|C=k))+log(P(x2|C=k))+..+log(P(xn|C=k))
	   * 
	   * x: sample(x1,x2,..,xn), 
	   * clusterMeans[]: distribution means for each attribute,  
	   * clusterStdDevs[]: distribution stdDevs for each attribute
	   */
	  public static double logPXC_k(double [] x, double[][] clusterMeansStdDevs){
		  double logPXCk = 0;
		  for (int i = 0; i < x.length; i++) {
			  logPXCk += logPdfx(x[i], clusterMeansStdDevs[i][0],clusterMeansStdDevs[i][1]);
		  }
		  return logPXCk;
	  }

	  /**
	   * The prior probability for each class p(C=K) can be computed as
	   *  P(C=K)= nk/N
	   * 
	   * x: sample(x1,x2,..,xn), 
	   * clusterMeans[][]: distribution means for each cluster for each attribute,
	   * clusterStdDevs[][]: for each cluster for each attribute
	   */	  
	  public static double[] PC_K(int[] clustersSizes){	  
		  double[] pc = new double[clustersSizes.length];
		  for (int j = 0; j < clustersSizes.length; j++) 
			  pc[j] = clustersSizes[j];

		  UtilClass.normalize(pc);
		  return pc;
	  }


	  public static void main (String[] args){
		  double[] d ={1000.0 ,36000.0 ,217.2021179199219, 18.0 ,8.379341125488281};
		  normalizeSTD(d);
		  System.out.println("{"+d[0]+",  "+d[1]+" , "+d[2]+",  "+d[3]+" , "+d[4]+"}  "+(d[0]+d[2]+d[3]+d[4]));
	  }  
}
