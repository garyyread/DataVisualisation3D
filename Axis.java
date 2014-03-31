/*
 * Axis.java
 * Gary Read 662193
 * Swansea University
 * CS255 Computer Graphics Coursework 1
 * Created 13th Feb 14 (Updated 17th Feb 14)
 *
 * I Gary Read declare every byte of code in this class has been typed with my hands, where otherwise, a source to auther is indicated
 *
 * Enum class, used to fix constant angles depending on Axis, used with ImageProcessor.java
 *	Also contains a constant int 'max' used in ImageProcessor for finding the max slice the particular Axis 
 */


//Cool feature to fix angles when creating thumbnails
//	See year1 cs110 Coursework1 - 21 Card Game
public enum Axis {
	//enums, passing on there angles in degrees
	X(0,0,180,256),
	Y(180,180,270,256),
	Z(270,0,0,113);
	
	//angles in radians
	private final double ri;
	private final double rj;
	private final double rk;
	private final int max;
	
	//constructor setting the stuffs
	Axis(int ai, int aj, int ak, int max) {
		ri = Math.toRadians(ai);
		rj = Math.toRadians(aj);
		rk = Math.toRadians(ak);
		this.max = max;
	}
	
	public double ri() {
		return ri;
	}

	public double rj() {
		return rj;
	}

	public double rk() {
		return rk;
	}
	
	public int max() {
		return max;
	}
}