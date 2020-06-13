package anomalydetection.clustering.distance;

import anomalydetection.util.MatrixL;
import anomalydetection.util.UtilClass;
import Jama.Matrix;


public class Mahalanobis implements Distance{
	private Matrix cov;
	private Matrix covInverse;
	
	public Mahalanobis(Matrix covInv) throws Exception{
		this.cov = covInv;
		updateCovInv();
	}
	
	public Mahalanobis(MatrixL covInv) throws Exception {
		this.cov = new Matrix(covInv.getNumberOfRows(),covInv.getNumberOfCols());
		for (int i=0;i<covInv.getNumberOfRows();++i)
			for (int j=0;j<covInv.getNumberOfCols();++j)
				this.cov.set(i, j, covInv.value(i,j));
		updateCovInv();
	}

	public void updateCovInv() {
			covInverse = MatrixL.pinv(cov);//cov.inverse();
	}

	@Override
	public double distance(double[] obs1, double[] obs2, double[][] ranges) throws Exception {
		return distance(obs1, obs2);
	}

	@Override
	public double distance(double[] obs1, double[] obs2, double[][] ranges,
			double cutOffValue) throws Exception {
		return distance(obs1, obs2);
	}

	@Override
	public double distance(double[] obs1, double[] obs2, double cutOffValue)
			throws Exception {
		return distance(obs1, obs2);
	}

	@Override
	public double distance(double[] obs1, double[] obs2) throws Exception {
		Matrix obsM1 = new Matrix(obs1,1);
		Matrix obsM2 = new Matrix(obs2,1);
		Matrix diff = obsM1.minus(obsM2);
		Matrix r1 = diff.times(covInverse);
		return r1.times(diff.transpose()).get(0,0); 
	}

	public static void main(String[] a) throws Exception{
		double[] x1={1, 	2, 		0,		3,		8,		11,		-4,		8,		2,	3};
		double[] x2={-1, 	31, 	1,		3.2,	-5,		6,		11,		20,		-1,	13};
		double[] x3={-1, 	1,		12,		1.2,	-5,		5,		11,		20,		-1,	9};
		double[] x4={1, 	-1, 	1,		6,		02,		-5,		6,		11,		20,	-1};
		double[] x5={7, 0.31, 1,0.2,-5,	6,	1,	2,	-1,7};
		double[] x6={-8, 2, 1,	4,-5,	2,	4,	110,	-1,4};
		double[] x7={-3, -5, 1,	7,-5,	6,	8,	-10,	-1,3};
		double[] x8={1, 1, 5,	11,-5,	6,	3,	7,	-1,13};
		double[] x9={-7, 1, 8,	0,-5,	6,	11,	1,	-1,13};
		double[] x10={-11, 41, 61,	1.2,-5,	6,	11,	20,	-1,13};
		double[] means= {UtilClass.mean(x1),UtilClass.mean(x2),UtilClass.mean(x3),UtilClass.mean(x4),UtilClass.mean(x5),UtilClass.mean(x6),UtilClass.mean(x7),UtilClass.mean(x8),UtilClass.mean(x9),UtilClass.mean(x10)};
		double[][] mat = {x1,x2,x3,x4,x5,x6,x7,x8,x9,x10};
		Matrix m = new Matrix(mat);
		
		Matrix covM = MatrixL.covMatrix(m, means);
		//Matrix covM = m.getMatrix(new int[]{1}, 0, 9);
		System.out.println(MatrixL.toString(covM));
		
/*		double t = System.currentTimeMillis();
		Matrix m = new Matrix(mat);
		System.out.println(m.det()+"    t = "+(System.currentTimeMillis()-t));
		t = System.currentTimeMillis();
		MatrixL c = new MatrixL (mat);
		System.out.println(MatrixL.det(c)+"    t = "+(System.currentTimeMillis()-t));

		t = System.currentTimeMillis();
		System.out.println(MatrixL.detLeibniz(c)+"    t = "+(System.currentTimeMillis()-t));
*/		
/*		double[] 		mu= {0, 0};
		double[][] 		cov= {{25,0},{0,1}};
		Matrix covM = new Matrix (cov);
	//	Matrix covMInv = Matrix.inverse(covM);
		Mahalanobis m = new Mahalanobis(covM);

		System.out.println(m.distance(new double[]{0,10}, mu));
*/		//Matrix.print(c);
		//double[][] c = covMatrix(new double[][]{x,y, z});
				
	}
	
	
}
