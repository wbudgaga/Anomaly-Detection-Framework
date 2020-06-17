package anomalydetection.util;

import java.io.IOException;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class MatrixL {
	private double[][] 	matrix;
	private int 		rowsNum;
	private int			colsNum;	
	public static double MACHEPS = 2E-16;
	
	public  MatrixL(double[][] data){
		matrix 	= data;
		rowsNum = data.length;
		colsNum = data[0].length;
	}
	//vector of length n will be represented as [1,n] matrix  
	public  MatrixL(double[] data){
		this(1, data.length);
		setValue(0, data);
	}

	public  MatrixL(int r, int c){
		matrix 	= new double[r][c];
		rowsNum = r;
		colsNum = c;
	}

	public static MatrixL transpose(MatrixL matrix) {
	    MatrixL transposedMatrix = new MatrixL(matrix.getNumberOfCols(), matrix.getNumberOfRows());
	    for (int i=0;i<matrix.getNumberOfRows();i++) {
	        for (int j=0;j<matrix.getNumberOfCols();j++) {
	            transposedMatrix.setValue(j, i, matrix.value(i, j));
	        }
	    }
	    return transposedMatrix;
	}
	
    public static double detLeibniz(MatrixL mat)  {
        PermuteArray perm = new PermuteArray( mat.colsNum);

        double total = 0;

        int p[] = perm.next();

        while( p != null ) {

            double prod = 1;

            for( int i = 0; i < mat.rowsNum; i++ ) {
                prod *= mat.value(i,p[i]);
            }

            total += perm.sgn()*prod;
            p = perm.next();
        }

        return total;
    }

	public static double det(MatrixL matrix) throws IOException {
	    if (!matrix.isSquareMatrix())
	        throw new IOException("determinant operation is applicatbale for square, but the proivded is ("+matrix.getNumberOfRows()+","+matrix.getNumberOfCols()+")");
	    if (matrix.getNumberOfCols() == 1) 
	    	return matrix.value(0, 0);
	    
	    if (matrix.getNumberOfCols()==2) {
	        return (matrix.value(0, 0) * matrix.value(1, 1)) - ( matrix.value(0, 1) * matrix.value(1, 0));
	    }
	    double sum = 0.0;
	    for (int i=0; i<matrix.getNumberOfCols(); i++) {
	        sum +=  getSign(i)* matrix.value(0, i) * det(subMatrix(matrix, 0, i));
	    }
	    return sum;
	}
	
	public static MatrixL multiply(MatrixL m1, MatrixL m2) throws Exception {
		if (m1.getNumberOfCols() != m2.getNumberOfRows())
			throw new Exception("Matrix muliolication: number of columns of first matrix must equal number of rows of the second one");
		MatrixL m3 = new MatrixL(m1.getNumberOfRows(), m2.getNumberOfCols());
		for (int i=0; i<m1.getNumberOfRows(); i++) {
			for (int j=0; j<m2.getNumberOfCols(); j++) {
				double sum =0 ;
				for (int k=0; k<m2.getNumberOfRows(); k++) {
					sum += (m1.value(i, k) * m2.value(k, j));
				}
				m3.setValue(i, j, sum);
			}
		}
		return m3;
	}
	
	public static MatrixL multiply(MatrixL m, double v) throws Exception {
		MatrixL m1 = new MatrixL(m.getNumberOfRows(), m.getNumberOfCols());
		for (int i=0; i<m.getNumberOfRows(); i++) {
			for (int j=0; j<m.getNumberOfCols(); j++) {
				m1.setValue(i, j, m.value( i, j) * v);
			}
		}
		return m1;
	}

	
	public static MatrixL cofactor(MatrixL matrix) throws IOException {
	    MatrixL mat = new MatrixL(matrix.getNumberOfRows(), matrix.getNumberOfCols());
	    for (int i=0;i<matrix.getNumberOfRows();i++) {
	        for (int j=0; j<matrix.getNumberOfCols();j++) {
	            mat.setValue(i, j, getSign(i) * getSign(j) * det(subMatrix(matrix, i, j)));
	        }
	    }    
	    return mat;
	}
	// return -1 if v is odd, otherwise 1
	private static int getSign(int v){
		if(v%2==1)
			return -1;
		return 1;
	}
	public static MatrixL inverse(MatrixL matrix) throws Exception {
		double t1 = System.currentTimeMillis();
		double detM = detLeibniz(matrix);
		System.out.println("spend "+ (System.currentTimeMillis() - t1));
		if (detM==0)
			throw new Exception("Because determinant is zero, inverse matrix of a given matrix does not exist");
		MatrixL cofTransposed =  transpose(cofactor(matrix)); 
	    return (multiply(cofTransposed, 1.0/det(matrix)));
	}
	
	public static MatrixL subMatrix(MatrixL matrix, int skipRow, int skipCol) {
		int numOfExtractedRows = matrix.isValidRowIDX(skipRow)?1:0;
		int numOfExtractedCols = matrix.isValidColIDX(skipCol)?1:0;	
		MatrixL mat = new MatrixL(matrix.getNumberOfRows() - numOfExtractedRows, matrix.getNumberOfCols() - numOfExtractedCols);
	    int curRow = -1;
	    for (int i=0;i<matrix.getNumberOfRows();i++) {
	        if (i!=skipRow){
	            ++curRow;
	            int curCol = -1;
		        for (int j=0;j<matrix.getNumberOfCols();j++) {
		            if (j != skipCol)
		                mat.setValue(curRow, ++curCol, matrix.value(i, j));
		        }
	        }
	    }
	    return mat;
	} 

	public static double cov(double[] x, double[] y){
		double meanx = UtilClass.mean(x);
		double meany = UtilClass.mean(y);
		double sum = 0;
		for (int i = 0 ; i < x.length; ++i){
			sum += (x[i] -meanx)*(y[i]-meany);
		}
		return sum / (x.length-1);
	}

	public static double cov(double[] x, double[] y, double meanX, double meanY){
		double sum = 0;
		for (int i = 0 ; i < x.length; ++i){
			sum += (x[i] -meanX)*(y[i]-meanY);
		}
		return sum / (x.length-1);
	}

	public static MatrixL covMatrix(MatrixL data, double[] means){
		int len = data.getNumberOfRows();
		MatrixL covM = new MatrixL(len,len);		
		for (int i = 0 ; i < len; ++i){
			for (int j = i ; j < len; ++j){
				double covIJ = cov(data.value(i), data.value(j), means[i], means[j]);
				covM.setValue(j, i, covIJ);
				if (i!=j)
					covM.setValue(i, j, covIJ);
			}
		}
		return covM;
	}

	public static double cov(Matrix x, Matrix y, double meanX, double meanY){
		double sum = 0;
		int len = x.getColumnDimension();
		for (int i = 0 ; i < len ; ++i){
			sum += (x.get(0,i) -meanX)*(y.get(0,i)-meanY);
		}
		return sum / (len-1);
	}

	public static Matrix pinv(Matrix x) {
		if (x.rank() < 1)
			return null;
		if (x.getColumnDimension() > x.getRowDimension())
			return pinv(x.transpose()).transpose();
		
		SingularValueDecomposition svdX = new SingularValueDecomposition(x);
		double[] singularValues = svdX.getSingularValues();
	 	double tol = Math.max(x.getColumnDimension(), x.getRowDimension()) * singularValues[0] * MACHEPS;
	 	double[] singularValueReciprocals = new double[singularValues.length];
	 	for (int i = 0; i < singularValues.length; i++)
	 		singularValueReciprocals[i] = Math.abs(singularValues[i]) < tol ? 0 : (1.0 / singularValues[i]);
	 	
	 	double[][] u = svdX.getU().getArray();
	 	double[][] v = svdX.getV().getArray();
	 	int min = Math.min(x.getColumnDimension(), u[0].length);
	 	double[][] inverse = new double[x.getColumnDimension()][x.getRowDimension()];
	 	for (int i = 0; i < x.getColumnDimension(); i++)
	 		for (int j = 0; j < u.length; j++)
	 			for (int k = 0; k < min; k++)
	 				inverse[i][j] += v[i][k] * singularValueReciprocals[k] * u[j][k];
	 	return new Matrix(inverse);
	}
	
	public static Matrix covMatrix(Matrix data, double[] means){
		int len = data.getRowDimension();
		int cols = data.getColumnDimension();
		Matrix covM = new Matrix(len,len);		
		for (int i = 0 ; i < len; ++i){
			for (int j = i ; j < len; ++j){
				double covIJ = cov(data.getMatrix(new int[]{i},0,cols-1), data.getMatrix(new int[]{j},0,cols-1), means[i], means[j]);
				covM.set(j, i, covIJ);
				if (i!=j)
					covM.set(i, j, covIJ);
			}
		}
		return covM;
	}
	public static MatrixL getIdentity(int size){
		MatrixL m = new MatrixL(size,size);
		for (int i = 0 ; i < size; ++i){ 
			m.setValue(i, i, 1);
		}
		return m;
	}
	
	public static void print(MatrixL m) {
		for (int i=0; i < m.getNumberOfRows();++i){
			for (int j=0; j < m.getNumberOfCols();++j){
				System.out.print(m.value(i,  j)+"  ");
			}
			System.out.println();
		}
	}	
	
	public static String toString(Matrix mat) {
		String tmpString ="";
		for (int i=0; i < mat.getRowDimension();++i){
			for (int j=0; j < mat.getColumnDimension();++j){
				tmpString += (mat.get(i,  j)+",");
			}
			tmpString = tmpString.substring(0, tmpString.length()-1)+"\n";
		}

		return tmpString;
	}


	public String toString() {
		String tmpString ="";
		for (int i=0; i < getNumberOfRows();++i){
			for (int j=0; j < getNumberOfCols();++j){
				tmpString += (value(i,  j)+",");
			}
			tmpString = tmpString.substring(0, tmpString.length()-1)+"\n";
		}

		return tmpString;
	}

	public double[][] getMatrix() {
		return matrix;
	}

	public boolean isValidRowIDX(int rowIdx) {
		return (rowIdx >=0 && rowIdx < getNumberOfRows());
	}
	public boolean isValidColIDX(int colIdx) {
		return (colIdx >=0 && colIdx < getNumberOfCols());
	}
	public boolean isSquareMatrix() {
		return getNumberOfRows() == getNumberOfCols();
	}

	public int getNumberOfRows() {
		return rowsNum;
	}

	public int getNumberOfElements() {
		return rowsNum * colsNum;
	}

	public int getNumberOfCols() {
		return colsNum;
	} 
	public double value(int r, int c) {
		return matrix[r][c];
	}

	public double[] value(int r) {
		return matrix[r];
	}

	public void setValue(int r, int c, double value) {
		matrix[r][c] = value;
	}
	public void setValue(int r, double[] data) {
		matrix[r] = data;
	}	
	public static MatrixL subtract(MatrixL m1, MatrixL m2) throws Exception {
		if (m1.getNumberOfCols() != m2.getNumberOfCols() || m1.getNumberOfRows() != m2.getNumberOfRows())
			throw new Exception("Matrix subtraction: both matrices must be of the same size");

	    MatrixL mat = new MatrixL(m1.getNumberOfRows(), m1.getNumberOfCols());
	    for (int i=0;i<m1.getNumberOfRows();i++) {
	        for (int j=0; j<m1.getNumberOfCols();j++) {
	            mat.setValue(i, j, m1.value(i, j) - m2.value(i, j));
	        }
	    }    
	    return mat;
	}

	public static void main(String[] a) throws Exception{
		double[] x1={1, 2, 0,	3,	8,	11,	-4,	8,	2,	3};
		double[] x2={-1, 31, 1,	3.2,-5,	6,	11,	20,	-1,13};
		double[] x3={-1, 1, 12,	1.2,-5,	5,	11,	20,	-1,9};
		double[] x4={1, -1, 1,	6,02,-5,	6,	11,	20,	-1,1};
		double[] x5={7, 0.31, 1,0.2,-5,	6,	1,	2,	-1,7};
		double[] x6={-8, 2, 1,	4,-5,	2,	4,	110,	-1,4};
		double[] x7={-3, -5, 1,	7,-5,	6,	8,	-10,	-1,3};
		double[] x8={1, 1, 5,	11,-5,	6,	3,	7,	-1,13};
		double[] x9={-7, 1, 8,	0,-5,	6,	11,	1,	-1,13};
		double[] x10={-11, 41, 61,	1.2,-5,	6,	11,	20,	-1,13};
		
		MatrixL c = new MatrixL (new double[][]{x1,x2,x3,x4,x5,x6,x7,x8,x9,x10});
/*		double t = System.currentTimeMillis();
		System.out.println("det = "+MatrixL.det(c)+"\t"+(System.currentTimeMillis() -t));
*/		
		
		
/*		t = System.currentTimeMillis();
		System.out.println("detLei = "+MatrixL.detLeibniz(c)+"\t"+(System.currentTimeMillis() -t));
*/		//System.out.println("det = "+Matrix.det(c)+"\t"+(System.currentTimeMillis() -t));
/*		Matrix c1 = new Matrix (new double[][]{{2.0,3.0},{1,0}});
		Matrix t = new Matrix(x);
		System.out.println(Matrix.cov(x,y));
		System.out.println("+++++++++++++++++++++ttttt");
		System.out.println(Matrix.det(c)+"\n\n");
		Matrix invC = Matrix.inverse(c);
		System.out.println(Matrix.multiply(c, invC).toString());
		System.out.println("----------------------------------");
		Matrix.print(Matrix.multiply(c, invC));
		System.out.println("+++++++++++++++++++++++");

		Matrix.print(Matrix.cofactor(c));
		System.out.println("+++++++++++++++++++++++");
		Matrix.print(Matrix.multiply(c1, c1));
*/	System.out.println("+++++++++++++++++++++++");
		MatrixL.print(c);
		//double[][] c = covMatrix(new double[][]{x1,x2, x3});
				
	}

}
