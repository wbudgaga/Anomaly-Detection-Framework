package anomalydetection.experiments.anomalyDetection;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import anomalydetection.util.UtilClass;

public class FileReader {
	private 	DataInputStream in;
	protected 	int 			chunckSize = 64 * 1024 * 1024;
	private 	String[]		lines;
	int 		curLineIdx		= -1;
	public FileReader(String fName) throws FileNotFoundException{
		openFile(new File(fName)); 
	}
		
	private void openFile(File file) throws FileNotFoundException{
		FileInputStream f = new FileInputStream(file);
		BufferedInputStream buf = new BufferedInputStream(f);
		in = new DataInputStream(buf);
	}
	
	public int nextData(byte[] byteBuffer) throws IOException{
		int totalBytesRcvd 		= 0;  // Total bytes received so far
		int bytesRcvd;           // Bytes received in last read
		while (totalBytesRcvd < chunckSize) {
		      if ((bytesRcvd = in.read(byteBuffer, totalBytesRcvd, chunckSize - totalBytesRcvd)) == -1){
		    	  return totalBytesRcvd;
		      }
		      totalBytesRcvd += bytesRcvd;
		}
	    return totalBytesRcvd;
	}

	private String nextBlock() throws IOException{
		byte[] byteBuffer = new byte[chunckSize];
		int readData = nextData(byteBuffer);
		if (readData==0){
			in.close();
			return null;
		}
		byte[] dataArray =  Arrays.copyOfRange(byteBuffer, 0, readData);
		return new String(dataArray);
	}
	public boolean readLines() throws IOException{
		String data = nextBlock();
		if (data == null){
			return false;
		}
		lines = data.split("\n"); 
		for (int i=0; i<lines.length; ++i){
			lines[i]=lines[i].replace("\n", "").replace("\r", "");
		}
		curLineIdx = -1;
		return true;
	}
	
	public String nextLine() throws IOException{
		if (lines == null || curLineIdx == lines.length-1)
			if (!readLines())
				return null;
		return lines[++curLineIdx];
	}
	
	public static void main(String[] a) throws IOException{
		FileReader fr = new FileReader("C:\\Users\\Public\\Documents\\study\\mThesis\\anomalousDetection\\ACM_TAAS\\exp\\P_9pkv_09.txt");
		String line = fr.nextLine();
		int i=0;
/*		while(line!=null){
			++i;
			line = fr.nextLine();
		}
*/		
		String[] data = line.split(":");
		String[] features = data[1].trim().split("   ");
		System.out.println(features[0]+"##############"+features.length);
	}
}
