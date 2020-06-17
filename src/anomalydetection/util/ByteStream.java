package anomalydetection.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


// this class contains static methods (helps methods) that are used by packing and unpacking between primitive types and byte stream
public class ByteStream {
	
		public static final byte[] getBytes(byte[]  byteStream,int start, int length){
			byte[] bytes = new byte[length];
			int end = start+length;
			for(int i = start; i< end; ++i){
				bytes[i-start] = byteStream[i];
			}
			return bytes;
		}
		 public static byte[] toByteArray(long value, int n){ 
	            byte[] ret = new byte[n]; 
	            for(int i=0;i<n;++i){
	                int idx = n-(i+1);
	                ret[i] = (byte) ((value >>> (idx*8)));
	            }
	            return ret; 
	        }
	               
	        public static final long byteArrayToLong(byte [] b) {
	            long ret = 0;           
	            for(int i=0;i<b.length;++i){
	                 ret <<= 8;
	                 ret ^= b[i] & 0xFF;
	            }
	            return ret;
	        }
	       
	        public static final byte[] longToByteArray(long value) {
	            return toByteArray(value,8);
	        }
	        
	        public static final byte[] doubleToByteArray(double value) {
	        	long l = Double.doubleToRawLongBits(value);
	            return longToByteArray(l);
	        }
	        
	        public static final double byteArrayToDouble(byte [] b) {
	        	return Double.longBitsToDouble(byteArrayToLong(b));
	        }
	        
	        
	        
		public static final byte[] intToByteArray(int value) {
	        return new byte[] {
	                (byte)(value >>> 24),// right shift 24 bits. the remaining, the most significant 8 bits are stored in position 0 in the array
	                (byte)(value >>> 16),// right shift 16 bits. the remaining, the most significant 16 bits are casting to byte. the least significant 8 bits of them are stored in position 1 in the array
	                (byte)(value >>> 8),// right shift 8 bits. the remaining, the most significant 16 bits are casting to byte. the least significant 8 bits of them are stored in position 2 in the array
	                (byte)value};	// cast value to byte,  the least significant 8 bits are stored in position 3 in the array
		}


		public static final int byteArrayToInt(byte [] b) {
	        return (b[0] << 24) // left shift 24 bits to put the contents of b[0] in int's positions from 24 to 31 
	                + ((b[1] & 0xFF) << 16) // left shift 16 bits to put the contents of b[1] in int's positions from 16 to 23
	                + ((b[2] & 0xFF) << 8) // left shift 8 bits to put the contents of b[2] in int's positions from 8 to 15
	                + (b[3] & 0xFF);	// put the contents of b[3] in int's positions from 0 to 7
		}								// casting to int will convert from binary representation to decimal
		public static final byte[] StringToByteArray(String value) {
			return value.getBytes();
		}
		
		public static final String  byteArrayToString(byte[] value) {
			return new String(value);
		}

		private static final byte[] joinTwoArrays(byte[] array1, byte[] array2) {
			byte [] resultArray = new byte[array1.length + array2.length];
			System.arraycopy(array1,0,resultArray,0         ,array1.length);
			System.arraycopy(array2, 0, resultArray, array1.length, array2.length);
			return resultArray;
		}
		public static final byte[] join(byte[] array1, byte[] array2) {
			if (array1==null && array2==null)
				return null;
			if (array1==null && array2!=null)
				return array2;
			if (array1!=null && array2==null)
				return array1;

			return joinTwoArrays(array1,array2);
		}
		
		public static byte[] readFileBytes(File file){
			try {
				InputStream is = new FileInputStream(file);
				long length = file.length();
				if (length > Integer.MAX_VALUE) {
			        System.out.println("File is too large. It can not be transfered!");
			        return null;
			    }
				
				byte[] bytes = new byte[(int)length];
			    // Read in the bytes
			    int offset = 0;
			    int numRead = 0;
			    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			        offset += numRead;
			    }
			    if (offset < bytes.length) {
			        throw new IOException("Could not completely read file "+file.getName());
			    }

			    // Close the input stream and send the msg
			    is.close();
			    return bytes;
			    
			 
			} catch (FileNotFoundException e) {
				System.out.println("The file: "+file.getName()+ " could not be found in: "+file.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		public static byte[] readFileBytes(String sourceDir, String fileName){
			File file = new File(sourceDir,fileName); 
			return readFileBytes(file);
		}
		
		/**
		* This method converts a set of bytes into a Hexadecimal representation.
		*
		*/
		public static String convertBytesToHex(byte[] buf) {
			StringBuffer strBuf = new StringBuffer();
			for (int i = 0; i < buf.length; i++) {
				int byteValue = (int) buf[i] & 0xff;
				if (byteValue <= 15) {
					strBuf.append("0");
				}
				strBuf.append(Integer.toString(byteValue, 16));
			}
			return strBuf.toString();
		}
		/**
		* This method converts a specified hexadecimal String into a set of bytes.
		*
		* @param hexString
		* @return
		*/
		public static byte[] convertHexToBytes(String hexString) {
			int size = hexString.length();
			byte[] buf = new byte[size / 2];
			int j = 0;
			for (int i = 0; i < size; i++) {
				String a = hexString.substring(i, i + 2);
				int valA = Integer.parseInt(a, 16);
				i++;
				buf[j] = (byte) valA;
				j++;
			}
			return buf;
		}
		
		public static final byte[] packString(String value) {
			byte[] bytes = StringToByteArray(value);
			byte[] bytesLength = intToByteArray(bytes.length);
			return join(bytesLength, bytes);
		}


		public static byte[] addPacketHeader(byte [] packetBody){
			int packetLegth = packetBody.length;
			byte[] packetLegthInBytes = intToByteArray(packetLegth);
			return join(packetLegthInBytes, packetBody);
		}
	
		// remove the header which is 4 bytes at the beginning of the packet
		public static byte[] removePacketHeader(byte[] byteStream){
			return Arrays.copyOfRange(byteStream, 4, byteStream.length); 
		}
		public static void main (String[]s){
			double d =16290.95001;
			byte[] b = doubleToByteArray(d);
			System.out.println(byteArrayToDouble(b));
			
		}
}
