/*
 * VolumeData.java
 * Gary Read 662193
 * Swansea University
 * CS255 Computer Graphics Coursework 1
 * Created 13th Feb 14 (Updated 18th Feb 14)
 *
 * I Gary Read declare every byte of code in this class has been typed with my hands, where otherwise, a source to auther is indicated
 *
 * A class to process, hold, and manage volume data, can be used by other classes.
 */

//Imports ----------------------------------------
import java.awt.*;
import java.awt.image.*;

import java.io.*;
import javax.imageio.*;
import javax.swing.*;
//--------------------------------------------------

public class VolumeData {

	private String fileName;
	private short imageData[][][]; //store the 3D volume data set
	private short min, max; //min/max value in the 3D volume data set
	private int i_max, j_max, k_max; //xyz limits of data set
	private int[] mapping, histogram;

    public VolumeData(String fileName, int i_max, int j_max, int k_max) throws IOException {
		this.fileName = fileName;
		this.i_max = i_max;
		this.j_max = j_max;
		this.k_max = k_max;
		
		File file = new File(fileName);
		
		//Read the data 
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		short read;
		int i, j, k; //loop through the 3D data set
		int b1, b2; //data is wrong Endian
		min=Short.MAX_VALUE; 
		max=Short.MIN_VALUE;

		//Read data to memory, performing bit operations
		imageData = new short[k_max][j_max][i_max];
		for (k=0; k<k_max; k++) {
			for (j=0; j<j_max; j++) {
				for (i=0; i<i_max; i++) {
					//because the Endianess is wrong, it needs to be read byte at a time and swapped
					b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
					b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
					read=(short)((b2<<8) | b1); //and swizzle the bytes around
					if (read<min) min=read; //update the minimum
					if (read>max) max=read; //update the maximum
					imageData[k][j][i]=read; //put the short into memory
				}
			}
		}
		
		EqualizeHistogram();
    }
	
	//returns pointer to image array
	public byte[] getImageData(BufferedImage image) {
		WritableRaster WR = image.getRaster();
		DataBuffer DB = WR.getDataBuffer();
			
		if (DB.getDataType() != DataBuffer.TYPE_BYTE)
			throw new IllegalStateException("That's not of type byte");

		return ((DataBufferByte) DB).getData();
    }
	
	//Compute histogram & mapping
	private void EqualizeHistogram() {
		int i, j, k, index; //loop constraints
		int g_levels = max-min+1;
		histogram = new int[g_levels];
		mapping = new int[g_levels];
		int t_i = 0;
		float size = i_max*j_max*k_max;

		for (i=0; i<histogram.length; i++) {
			histogram[i] = 0;
		}

		for (i=0; i<i_max; i++) {
			for (j=0; j<j_max; j++) {
					for (k=0; k<k_max; k++) {
						index = imageData[k][j][i];
						histogram[index-min]++;
					}//k
			}//j
		}//i

		String st = "";
		for (i=0; i < histogram.length; i++) {
			t_i += histogram[i];
			mapping[i] = (int)((255.0f * t_i) / size);
		}
	}
	
	public int[] getMapping() {
		return mapping;
	}
	
	public int[] getHistogram() {
		return histogram;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public short[][][] getVolumeData() {
		return imageData;
	}
	
	public short getMin() {
		return min;
	}
	
	public short getMax() {
		return max;
	}
	
	public int getImax() {
		return i_max;
	}
	
	public int getJmax() {
		return j_max;
	}
	
	public int getKmax() {
		return k_max;
	}
}