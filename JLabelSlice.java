/*
 * JLabelSlice.java
 * Gary Read 662193
 * Swansea University
 * CS255 Computer Graphics Coursework 1
 * Created 13th Feb 14 (Updated 17th Feb 14)
 *
 * I Gary Read declare every byte of code in this class has been typed with my hands, where otherwise, a source to auther is indicated
 *
 * Overriding JLabel so that I could have my own member objects, to store values used with thumbnails.
 */

import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class JLabelSlice extends JLabel {
	private int tag;
	private Axis axis;
	
	public JLabelSlice(ImageIcon image, int tag, Axis axis) {
		super(image);
		setTag(tag);
		setAxis(axis);
	}
	
	public JLabelSlice(ImageIcon image, int tag) {
		super(image);
		setTag(tag);
	}
	
	public int getTag() {
		return tag;
	}
	
	public void setTag(int tag) {
		this.tag = tag;
	}

	public Axis getAxis() {
		return axis;
	}
	
	public void setAxis(Axis axis) {
		this.axis = axis;
	}
}