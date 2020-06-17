package anomalydetection.util;

import java.io.IOException;

public class Matrix {
	private double[][] 	matrix;
	private int 		rowsNum;
	private int			colsNum;	
	
	public  Matrix(double[][] data){
		matrix 	= data;
		rowsNum = data.length;
		colsNum = data[0].length;
	}
	//vector of length n will be represented as [1,n] matrix  
	public  Matrix(double[] data){
		this(1, data.length);
		setValue(0, data);
	}

	public  Matrix(int r, int c){
		matrix 	= new double[r][c];
		rowsNum = r;
		colsNum = c;
	}

	public static Matrix transpose(Matrix matrix) {
	    Matrix transposedMatrix = new Matrix(matrix.getNumberOfCols(), matrix.getNumberOfRows());
	    for (int i=0;i<matrix.getNumberOfRows();i++) {
	        for (int j=0;j<matrix.getNumberOfCols();j++) {
	            transposedMatrix.setValue(j, i, matrix.value(i, j));
	        }
	    }
	    return transposedMatrix;
	}

	public static double det(Matrix matrix) throws IOException {
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
	
	public static Matrix multiply(Matrix m1, Matrix m2) throws Exception {
		if (m1.getNumberOfCols() != m2.getNumberOfRows())
			throw new Exception("Matrix muliolication: number of columns of first matrix must equal number of rows of the second one");
		Matrix m3 = new Matrix(m1.getNumberOfRows(), m2.getNumberOfCols());
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
	
	public static Matrix multiply(Matrix m, double v) throws Exception {
		Matrix m1 = new Matrix(m.getNumberOfRows(), m.getNumberOfCols());
		for (int i=0; i<m.getNumberOfRows(); i++) {
			for (int j=0; j<m.getNumberOfCols(); j++) {
				m1.setValue(i, j, m.value( i, j) * v);
			}
		}
		return m1;
	}

	
	public static Matrix cofactor(Matrix matrix) throws IOException {
	    Matrix mat = new Matrix(matrix.getNumberOfRows(), matrix.getNumberOfCols());
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
	public static Matrix inverse(Matrix matrix) throws Exception {
		double detM = det(matrix);
		if (detM==0)
			throw new Exception("Because determinant is zero, inverse matrix of a given matrix does not exist");
		Matrix cofTransposed =  transpose(cofactor(matrix)); 
	    return (multiply(cofTransposed, 1.0/det(matrix)));
	}
	
	public static Matrix subMatrix(Matrix matrix, int skipRow, int skipCol) {
		int numOfExtractedRows = matrix.isValidRowIDX(skipRow)?1:0;
		int numOfExtractedCols = matrix.isValidColIDX(skipCol)?1:0;	
		Matrix mat = new Matrix(matrix.getNumberOfRows() - numOfExtractedRows, matrix.getNumberOfCols() - numOfExtractedCols);
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
	
	public static double cov(double[] x, double[] y, double meanX, double meanY){
		double sum = 0;
		for (int i = 0 ; i < x.length; ++i){
			sum += (x[i] -meanX)*(y[i]-meanY);
		}
		return sum / (x.length-1);
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

	public static Matrix covMatrix(Matrix data, double[] means){
		int len = data.getNumberOfRows();
		Matrix covM = new Matrix(len,len);		
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
	public static Matrix getIdentity(int size){
		Matrix m = new Matrix(size,size);
		for (int i = 0 ; i < size; ++i){ 
			m.setValue(i, i, 1);
		}
		return m;
	}
	
	public static void print(Matrix m) {
		for (int i=0; i < m.getNumberOfRows();++i){
			for (int j=0; j < m.getNumberOfCols();++j){
				System.out.print(m.value(i,  j)+"  ");
			}
			System.out.println();
		}
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
	public static Matrix subtract(Matrix m1, Matrix m2) throws Exception {
		if (m1.getNumberOfCols() != m2.getNumberOfCols() || m1.getNumberOfRows() != m2.getNumberOfRows())
			throw new Exception("Matrix subtraction: both matrices must be of the same size");

	    Matrix mat = new Matrix(m1.getNumberOfRows(), m1.getNumberOfCols());
	    for (int i=0;i<m1.getNumberOfRows();i++) {
	        for (int j=0; j<m1.getNumberOfCols();j++) {
	            mat.setValue(i, j, m1.value(i, j) - m2.value(i, j));
	        }
	    }    
	    return mat;
	}

	public static void main(String[] a) throws Exception{
		double[] x={1, 2, 0};
		double[] y={-1 , 1, 1};
		double[] z={1,2 , 3	};
		Matrix c = new Matrix (new double[][]{x,y, z});
		Matrix c1 = new Matrix (new double[][]{{2.0,3.0},{1,0}});
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
	System.out.println("+++++++++++++++++++++++");
		Matrix.print(c);
		//double[][] c = covMatrix(new double[][]{x,y, z});
				
	}

}
