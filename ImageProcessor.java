/*
 * ImageProcessor.java
 * Gary Read 662193
 * Swansea University
 * CS255 Computer Graphics Coursework 1
 * Created 13th Feb 14 (Updated 19th Feb 14)
 *
 * I Gary Read declare every byte of code in this class has been typed with my hands, where otherwise, a source to auther is indicated
 *
 * Main method takes in args[4];
 *	 filename width height depth - default params {CThead,256,256,113} where args != 4
 *
 * This class is a massive mess. Too many responsablities and isn't robust.
 *	Methods contaned in this class are to Create a GUI, event handlers, and perform Image manipulations
 *
 * Note: Contains several annoyomous classes and a private class, GUIhandler.
 */

//Imports ----------------------------------------
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import java.io.IOException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
//-------------------------------------------------

public class ImageProcessor extends JFrame {

	//Constructor, gets main data pointers for later
	public ImageProcessor(VolumeData volumeData) {
		this.volumeData = volumeData;
		mapping = volumeData.getMapping();
		i_max = volumeData.getImax();
		j_max = volumeData.getJmax();
		k_max = volumeData.getKmax();
		w_img = i_max;
		h_img = j_max;
		ImageProcessor();
	}

	//Constructs GUI...
	public void ImageProcessor() {
		setResizable(false);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension((w_max + tab_WIDTH + r_SPACER + l_SPACER), 
											(h_max + t_SPACER + b_SPACER + 35)));
		setTitle("Image Processor" + ((volumeData.getFileName() == null) ? "" : " - " + volumeData.getFileName()));
		
		xyzImage = new BufferedImage(w_img, h_img, BufferedImage.TYPE_3BYTE_BGR);
		xyzImageIcon = new JLabel(new ImageIcon(xyzImage));
		
		//Left panel, containing the image to be worked on
		leftPanel = new JPanel();
			leftPanel.setBorder(new EmptyBorder(t_SPACER,l_SPACER,b_SPACER,r_SPACER));
			leftPanel.add(xyzImageIcon);
		leftScroll = new JScrollPane(xyzImageIcon);
			leftScroll.setPreferredSize(new Dimension(w_max,h_max));
			leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			leftScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			add(leftScroll, BorderLayout.WEST);
		
		/*
		 * Content in Tabs... All GUI, don't read it... Poorly Built, should have used methods but...
		 */
		tabbedPane = new JTabbedPane(BoxLayout.Y_AXIS, JTabbedPane.TOP);
			tabbedPane.setBorder(new EmptyBorder(t_SPACER*7,l_SPACER,b_SPACER*7,r_SPACER*7));
				//Image view tab = 0
				JPanel imageViewOuter = new JPanel(new FlowLayout());
					//imageViewOuter.setPreferredSize(new Dimension());
						imageViewTab = getImageViewTab();
						imageViewOuter.add(imageViewTab);
					tabbedPane.addTab("Image View",imageViewOuter);
				
				//Thumbnails Tab = 1
				JPanel thumbTab = new JPanel(new BorderLayout());
					JPanel ThumbOptionPanel = new JPanel(new FlowLayout());
						ThumbOptionPanel.setPreferredSize(new Dimension(tab_WIDTH-20,50));
						xthumb_Spinner = new JSpinner();
							xthumb_Spinner.setValue(50);
							xthumb_Spinner.setPreferredSize(new Dimension(w_SPINNER*1,h_SPINNER));
							ThumbOptionPanel.add(new JLabel("Resize "));
							ThumbOptionPanel.add(xthumb_Spinner);

					thumbButtonGroup = new ButtonGroup();
						xthumb_axis_radio = new JRadioButton("x",false);
							xthumb_axis_radio.setAlignmentX(Component.CENTER_ALIGNMENT);
							thumbButtonGroup.add(xthumb_axis_radio);
						ythumb_axis_radio = new JRadioButton("y",false);
							ythumb_axis_radio.setAlignmentX(Component.CENTER_ALIGNMENT);
							thumbButtonGroup.add(ythumb_axis_radio);
						zthumb_axis_radio = new JRadioButton("z",false);
							zthumb_axis_radio.setAlignmentX(Component.CENTER_ALIGNMENT);
							thumbButtonGroup.add(zthumb_axis_radio);
						ThumbOptionPanel.add(xthumb_axis_radio);
						ThumbOptionPanel.add(ythumb_axis_radio);
						ThumbOptionPanel.add(zthumb_axis_radio);
					thumbTab.add(ThumbOptionPanel, BorderLayout.NORTH);
					
					imageThumbnailsTab = new JPanel();	
					scrollPane = new JScrollPane(imageThumbnailsTab);
						scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					thumbTab.add(scrollPane);
				tabbedPane.addTab("Image Thumnails",thumbTab);
				
		add(tabbedPane, BorderLayout.CENTER);
		
		// Now all the handlers class
		GUIEventHandler handler = new GUIEventHandler();

		//associate appropriate handlers
		tabbedPane.addChangeListener(handler);
		
		histogram_box.addActionListener(handler);
		
		i_slider.addMouseListener(handler);
		j_slider.addMouseListener(handler);
		k_slider.addMouseListener(handler);
		slice_slider.addChangeListener(handler);
		 
		minSlice_Spinner.addChangeListener(handler);
		maxSlice_Spinner.addChangeListener(handler);
		 
		x_Spinner.addChangeListener(handler);
		mip_Button.addActionListener(handler);
		
		xthumb_Spinner.addChangeListener(handler);
		x_axis_radio.addActionListener(handler);
		y_axis_radio.addActionListener(handler);
		z_axis_radio.addActionListener(handler);
		custom_axis_radio.addActionListener(handler);
		xthumb_axis_radio.addActionListener(handler);
		ythumb_axis_radio.addActionListener(handler);
		zthumb_axis_radio.addActionListener(handler);
        
		//Display all
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		rotate(ai,aj,ak);
    }
	
	//Return overrided JLabel with icon gained from rotate(d,d,d) - Uses Nearest Neighbour
	private JLabelSlice getThumbnail(BufferedImage oldImage, int tag, int w, int h) {
		BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		JLabelSlice thumbImageIcon = new JLabelSlice(new ImageIcon(newImage), tag);

		//Setting vars
		int old_h = oldImage.getHeight();
		int old_w = oldImage.getWidth();
		byte[] old_data = volumeData.getImageData(oldImage); //ref to image data

		int new_h = newImage.getHeight();
		int new_w = newImage.getWidth();
		byte[] new_data = volumeData.getImageData(newImage); //ref to image data

		int c, i, j, k; //loop constraints
		byte col; //colour

		thumbImageIcon.setIcon(new ImageIcon(rotate(new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR),ai,aj,ak)));
		
		return thumbImageIcon;
	}
	
	//Method just makes specific GUI tab
	private JPanel getImageViewTab() {
		imageViewTab = new JPanel();
				imageViewTab.setBorder(new EmptyBorder(t_SPACER*5,l_SPACER*5,b_SPACER*5,r_SPACER*5));
				imageViewTab.setLayout(new BoxLayout(imageViewTab, BoxLayout.Y_AXIS));
					
				JPanel radioPanel = new JPanel(new FlowLayout());
					radioPanel.setPreferredSize(new Dimension(tab_WIDTH-20,50));
					xyzButtonGroup = new ButtonGroup();
						x_axis_radio = new JRadioButton("x",true);
							x_axis_radio.setAlignmentX(Component.CENTER_ALIGNMENT);
							xyzButtonGroup.add(x_axis_radio);
						y_axis_radio = new JRadioButton("y",false);
							y_axis_radio.setAlignmentX(Component.CENTER_ALIGNMENT);
							xyzButtonGroup.add(y_axis_radio);
						z_axis_radio = new JRadioButton("z",false);
							z_axis_radio.setAlignmentX(Component.CENTER_ALIGNMENT);
							xyzButtonGroup.add(z_axis_radio);
						custom_axis_radio = new JRadioButton("xyz",false);
							custom_axis_radio.setAlignmentX(Component.CENTER_ALIGNMENT);
							xyzButtonGroup.add(custom_axis_radio);
						radioPanel.add(x_axis_radio);
						radioPanel.add(y_axis_radio);
						radioPanel.add(z_axis_radio);
						radioPanel.add(custom_axis_radio);
					imageViewTab.add(radioPanel);
				
				JPanel histopanel = new JPanel(new FlowLayout());
					 histogram_box = new JCheckBox();
						histopanel.add(new JLabel("Equalize Histogram"));
						histopanel.add(histogram_box);
					imageViewTab.add(histopanel);
				
				JPanel ijk_sliders = new JPanel();
					JPanel ijk_inner = new JPanel();
						ijk_inner.setLayout(new BoxLayout(ijk_inner, BoxLayout.Y_AXIS));
						JPanel i_panel = new JPanel(new FlowLayout());
							i_slider = new JSlider(SwingConstants.HORIZONTAL, min_ANGLE, max_ANGLE, ini_ANGLE);
								i_slider.setMajorTickSpacing(max_ANGLE / 8);
								i_slider.setPaintTrack(true);
								i_slider.setPaintTicks(true);
								i_panel.add(new JLabel("X       "));
								i_panel.add(i_slider);
							ijk_inner.add(i_panel);
						
						JPanel j_panel = new JPanel(new FlowLayout());
							j_slider = new JSlider(SwingConstants.HORIZONTAL, min_ANGLE, max_ANGLE, ini_ANGLE);
								j_slider.setMajorTickSpacing(max_ANGLE / 8);
								j_slider.setPaintTrack(true);
								j_slider.setPaintTicks(true);
								j_panel.add(new JLabel("Y       "));
								j_panel.add(j_slider);
							ijk_inner.add(j_panel);
						
						JPanel k_panel = new JPanel(new FlowLayout());
							k_slider = new JSlider(SwingConstants.HORIZONTAL, min_ANGLE, max_ANGLE, ini_ANGLE);
								k_slider.setMajorTickSpacing(max_ANGLE / 8);
								k_slider.setPaintTrack(true);
								k_slider.setPaintTicks(true);
								k_panel.add(new JLabel("Z       "));
								k_panel.add(k_slider);
							ijk_inner.add(k_panel);
							
						JPanel slice_panel = new JPanel(new FlowLayout());
							slice_slider = new JSlider(SwingConstants.HORIZONTAL, 1, i_max-1, slice);
								slice_slider.setMajorTickSpacing(k_max/2);
								slice_slider.setPaintTrack(true);
								slice_slider.setPaintTicks(true);
								slice_panel.add(new JLabel("Slice "));
								slice_panel.add(slice_slider);
							ijk_inner.add(slice_panel);
						ijk_sliders.add(ijk_inner);
					imageViewTab.add(ijk_sliders);

				JPanel mipRangePanel = new JPanel(new FlowLayout());
					minSlice_Spinner = new JSpinner();
						minSlice_Spinner.setValue(min_SLICE);
						minSlice_Spinner.setPreferredSize(new Dimension(w_SPINNER,h_SPINNER));
						mipRangePanel.add(new JLabel("MIP from "));
						mipRangePanel.add(minSlice_Spinner);
					maxSlice_Spinner = new JSpinner();
						maxSlice_Spinner.setValue(max_SLICE);
						maxSlice_Spinner.setPreferredSize(new Dimension(w_SPINNER,h_SPINNER));
						mipRangePanel.add(new JLabel(" to "));
						mipRangePanel.add(maxSlice_Spinner);
					mip_Button = new JButton("MIP OFF");
						mip_Button.setAlignmentX(Component.CENTER_ALIGNMENT);
						mipRangePanel.add(mip_Button);
					imageViewTab.add(mipRangePanel);
					
				JPanel resizePanel = new JPanel(new FlowLayout());
					x_Spinner = new JSpinner();
						x_Spinner.setValue(256);
						x_Spinner.setPreferredSize(new Dimension(w_SPINNER*2,h_SPINNER));
						resizePanel.add(new JLabel("Resize "));
						resizePanel.add(x_Spinner);
						final JComboBox resizebox = new JComboBox(new String[]{"Nearest Neighbour", "Bilinear"});
							resizebox.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									bilinear = (resizebox.getSelectedIndex() == 1) ? true : false;
									rotate(ai,aj,ak);
								}
							});
							resizePanel.add(resizebox);
					imageViewTab.add(resizePanel);

		//Init some stuff..
		mip_on = false;
		i_slider.setEnabled(false);
		j_slider.setEnabled(false);
		k_slider.setEnabled(false);
		i_slider.setValue(0);
		j_slider.setValue(0);
		k_slider.setValue(180);
		
		return imageViewTab;
	}
	
	//Method just makes specific GUI tab
	private JPanel getThumbnailPane(Axis ax, int w, int h) {
		imageThumbnailsTab = new JPanel();
		
		mip_on = false; //Dont want to capture mip... would take FOREVER and be pointless
		int slicetemp = slice; //Set to temp so we can change back later
		
		//Scaling...
		int maxSlice = (int)(ax.max() * (i_max*1.0 / ax.max()));
		
		//Create a JLabel with an image for every slice in data set
		for (slice = 0; slice < maxSlice; slice++) {			
			BufferedImage thumbImage = rotate(new BufferedImage(i_max, j_max, BufferedImage.TYPE_3BYTE_BGR), ax.ri(), ax.rj(), ax.rk());
			
			final JLabelSlice thumbIcon = getThumbnail(thumbImage, slice, w, h);
				thumbIcon.setAxis(ax);
				//Add listener for mouse clicks to open a larger image in a popup JFrame
				thumbIcon.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						if (e.getClickCount()%2 == 0) {
							popupImage(thumbIcon.getAxis(), thumbIcon.getTag());
						}
					}
				});
			thumbIcon.setBorder(new EmptyBorder(5,5,5,5));
			imageThumbnailsTab.add(thumbIcon);
		}
		
		slice = slicetemp; //reset slice
		
		int gridWidth = (int)(tab_WIDTH / (w+5)) - 1; //calc number of colums
		imageThumbnailsTab.setLayout(new GridLayout(0,gridWidth));
		return imageThumbnailsTab;
	}

	//Method for xyz sliders - updates image when released
	private void sliderReleased() {
		ai = Math.toRadians(i_slider.getValue());
		aj = Math.toRadians(j_slider.getValue());
		ak = Math.toRadians(k_slider.getValue());

		rotate(ai,aj,ak);
	}
	
	//Handles GUI events
    private class GUIEventHandler implements ActionListener, ChangeListener, MouseListener {
        public void mouseClicked(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseExited(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) {}
		 
		public void mouseReleased(MouseEvent e) {
			//Rotate sliders
			if (e.getSource() == i_slider || e.getSource() == j_slider || e.getSource() == k_slider) {
				sliderReleased();
			}
		}
		 
		public void stateChanged(ChangeEvent e) {			
			//Slice slider
			if (e.getSource() == slice_slider) {
				slice = slice_slider.getValue();
				rotate(ai,aj,ak);
			}
			
			//Resize spinners
			if (e.getSource() == x_Spinner) {
				w_img = (int)x_Spinner.getValue();
				h_img = (int)x_Spinner.getValue();
				if ((w_img > 0) && (h_img > 0)) {
					rotate(ai,aj,ak);
				}
			}
			
			//Resize thumbnail spinner
			if (e.getSource() == xthumb_Spinner) {
				thumbX = (int)xthumb_Spinner.getValue();
				thumbY = (int)xthumb_Spinner.getValue();
				if ((thumbX < i_max) && (thumbY < j_max)) {
					imageThumbnailsTab.removeAll();
					imageThumbnailsTab.add(getThumbnailPane(curr_AXIS, thumbX, thumbY));
					scrollPane.setViewportView(imageThumbnailsTab);
					tabbedPane.revalidate();
				}
			}
			
			//Mip range spinners
			if (e.getSource() == minSlice_Spinner || e.getSource() == maxSlice_Spinner) {
				int min_ = (int)minSlice_Spinner.getValue();
				int max_ = (int)maxSlice_Spinner.getValue();
				if ((min_ < max_) && (min_ > -1) && (max_ < j_max + 1)) {
					min_SLICE = min_;
					max_SLICE = max_;
					rotate(ai,aj,ak);
				}
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			//Button to set MIP on and off
			if (e.getSource() == mip_Button) {
				if (mip_on) {
					mip_on = false;
					slice_slider.setEnabled(true);
					mip_Button.setText("MIP OFF");
				} else {
					mip_on = true;
					slice_slider.setEnabled(false);
					mip_Button.setText("MIP ON");
				}
				rotate(ai,aj,ak);
			}
			
			//Histogram equalisation check
			if (e.getSource() == histogram_box) {
				rotate(ai,aj,ak);
			}
		
			//Radio buttons for xyzImage direction
			if (e.getSource() == x_axis_radio) {
				mip_on = false;
				slice_slider.setEnabled(true);
				i_slider.setEnabled(false);
				j_slider.setEnabled(false);
				k_slider.setEnabled(false);
				i_slider.setValue(0);
				j_slider.setValue(0);
				k_slider.setValue(180);
				sliderReleased();
			}
			if (e.getSource() == y_axis_radio) {
				slice_slider.setEnabled(true);
				mip_on = false;
				i_slider.setEnabled(false);
				j_slider.setEnabled(false);
				k_slider.setEnabled(false);
				i_slider.setValue(180);
				j_slider.setValue(180);
				k_slider.setValue(270);
				sliderReleased();
			}
			if (e.getSource() == z_axis_radio) {
				slice_slider.setEnabled(true);
				mip_on = false;
				i_slider.setEnabled(false);
				j_slider.setEnabled(false);
				k_slider.setEnabled(false);
				i_slider.setValue(270);
				j_slider.setValue(0);
				k_slider.setValue(0);
				sliderReleased();
			}
			if (e.getSource() == custom_axis_radio) {
				slice_slider.setEnabled(true);
				mip_on = false;
				i_slider.setEnabled(true);
				j_slider.setEnabled(true);
				k_slider.setEnabled(true);
				sliderReleased();
			}
			
			//Radio buttons for thumbnail direction
			if (e.getSource() == xthumb_axis_radio) {
				curr_AXIS = Axis.X;
				imageThumbnailsTab.removeAll();
				imageThumbnailsTab.add(getThumbnailPane(curr_AXIS, thumbX, thumbY));
				scrollPane.setViewportView(imageThumbnailsTab);
				tabbedPane.revalidate();
			}
			if (e.getSource() == ythumb_axis_radio) {
				curr_AXIS = Axis.Y;
				imageThumbnailsTab.removeAll();
				imageThumbnailsTab.add(getThumbnailPane(curr_AXIS, thumbX, thumbY));
				scrollPane.setViewportView(imageThumbnailsTab);
				tabbedPane.revalidate();
			}
			if (e.getSource() == zthumb_axis_radio) {
				curr_AXIS = Axis.Z;
				imageThumbnailsTab.removeAll();
				imageThumbnailsTab.add(getThumbnailPane(curr_AXIS, thumbX, thumbY));
				scrollPane.setViewportView(imageThumbnailsTab);
				tabbedPane.revalidate();
			}
		}
	}
	
	//JFrame to be displayed when a thumbnail is clicked
	private void popupImage(Axis axis, int tag) {
		//create temp slice and set slice to the tag
		int sliceS = slice;
		slice = tag;

		//Create JFrame with image and show it
		final JFrame popup = new JFrame("Image: " + tag + ", Axis: " + axis);
			final JLabel imageIcon = new JLabel(new ImageIcon(rotate(new BufferedImage(i_max, j_max, BufferedImage.TYPE_3BYTE_BGR), axis.ri(), axis.rj(), axis.rk())));
				imageIcon.setRequestFocusEnabled(false);
			popup.add(imageIcon);
			popup.setResizable(false);
			
			//Close JFrame when focus is lost
			popup.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent fe){ popup.dispose(); }
				public void focusGained(FocusEvent fe){}
			});

			//Get Frame ready and display it
			popup.pack();
			popup.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			popup.setLocationRelativeTo(null);
			popup.setVisible(true);
		
		slice = sliceS; //Reset slice back to origional value
	}

	//Method to refresh xyzImage
	private void rotate(double ai, double aj, double ak) {
		xyzImage = rotate(new BufferedImage(w_img, h_img, BufferedImage.TYPE_3BYTE_BGR),ai,aj,ak);
		xyzImageIcon.setIcon(new ImageIcon(xyzImage));
	}
	
	//Method for roating data set in 3d's ... Should be threaded...
	private BufferedImage rotate(BufferedImage image, double ai, double aj, double ak) {
		int c=0, i=slice, j=0, k=0; //loop constraints
		int w = image.getWidth();
		int h = image.getHeight();
		int d = (w < h) ? h : w;
		byte col; //colour
		short datum;
		float min_f = (float)volumeData.getMin();
		float max_f = (float)volumeData.getMax(); //Perform casting out of loop for effiency
		byte[] data = volumeData.getImageData(image); //ref to image data
		short[][][] volData = volumeData.getVolumeData(); //Obtaining volume data reference

		double Ri = 0;
		double Rj = 0; 
		double Rk = 0; //Rotated axels
		int sliceS = slice;
		int minS = min_SLICE;
		int maxS = max_SLICE;
		double iCenter = w/2;
		double jCenter = h/2; 
		double kCenter = (iCenter <= jCenter) ? iCenter : jCenter;
		double roll_i = Math.cos(aj) * Math.cos(ak);
		double roll_j = Math.sin(ai) * Math.sin(aj) * Math.cos(ak) + Math.cos(ai) * Math.sin(ak);
		double roll_k = -Math.cos(ai) * Math.sin(aj) * Math.cos(ak) + Math.sin(ai) * Math.sin(ak);
		double tilt_i = -Math.cos(aj) * Math.sin(ak);
		double tilt_j = -Math.sin(ai) * Math.sin(aj) * Math.sin(ak) + Math.cos(ai) * Math.cos(ak);
		double tilt_k = Math.cos(ai) * Math.sin(aj) * Math.sin(ak) + Math.sin(ai) * Math.cos(ak);		
		double jaw_i = Math.sin(aj);
		double jaw_j = -Math.sin(ai) * Math.cos(aj);
		double jaw_k = Math.cos(ai) * Math.cos(aj);
		
		for (i=0; i<w; i++) {
			for (j=0; j<h; j++) {
				datum = Short.MIN_VALUE;
				
				maxS = (mip_on) ? maxS : (sliceS+1);
				minS = (mip_on) ? minS : (sliceS); //Check to active MIP or not - only project though 1 slice if so.
				
				for (k=minS; k<maxS; k++) {
					int k_ = k*w/i_max; //refactoring k, gives full range for JSliders
					
					//Compute new coordinates for x,y,z given angles for each and translate image to center of img
					Ri = ((j-iCenter)*roll_i + (k_-jCenter)*tilt_i + (i-kCenter)*jaw_i)+(iCenter);
					Rj = ((j-iCenter)*roll_j + (k_-jCenter)*tilt_j + (i-kCenter)*jaw_j)+(jCenter);
					Rk = ((j-iCenter)*roll_k + (k_-jCenter)*tilt_k + (i-kCenter)*jaw_k)+(kCenter);
				
					try {
						if (bilinear) {
							datum = (short)Math.max(datum, bilinearResize(w, h, d, 256, 256, 113, Ri, Rj, Rk));
						} else {
							datum = (short)Math.max(datum, nearestResize(w, h, d, 256, 256, 113, Ri, Rj, Rk));
						}

						if (histogram_box.isSelected()) {
							col = (byte)mapping[datum-(int)min_f];
						} else {
							col = (byte)(255.0f*(datum-min_f)/(max_f-min_f));
						}

						for (c=0; c<3; c++) {
							data[c+3*j+3*i*h] = col;
						}//colour
					} catch (Exception e) {  }
				}//slice
			}//row
		}//colum
		return image;
	}
	
	private short nearestResize(int newX, int newY, int newZ, int oldX, int oldY, int oldZ, double i, double j, double k) throws Exception {
		float resizeX = 0;
		float resizeY = 0;
		float resizeZ = 0;
		short datum   = 0;
		
		short[][][] volData = volumeData.getVolumeData();
		
		resizeX = (float)i * ((float)oldX/(float)newX);
		resizeY = (float)j * ((float)oldY/(float)newY);
		resizeZ = (float)k * ((float)oldZ/(float)newZ);
		
		datum = volData[(int)resizeZ][(int)resizeY][(int)resizeX];
		
		return datum;
	}
	
	private short bilinearResize(int newX, int newY, int newZ, int oldX, int oldY, int oldZ, double i, double j, double k) throws Exception {
		int n = 1;
		float 	xz_c11 = 0, yz_c11 = 0, xy_c11 = 0,
				xz_c12 = 0, yz_c12 = 0, xy_c12 = 0,
				xz_c21 = 0, yz_c21 = 0, xy_c21 = 0,
				xz_c22 = 0, yz_c22 = 0, xy_c22 = 0;
		float xin = (float)i * ((float)oldX/(float)newX);
		float yin = (float)j * ((float)oldY/(float)newY);
		float zin = (float)k * ((float)oldZ/(float)newZ);
		short[][][] volData = volumeData.getVolumeData();

		//XZ
		if (xin < 256 && zin < 113) { 
			xz_c11 = volData[(int)zin] [(int)yin] [(int)xin];
		}
		if (xin + n < 256 && zin < 113) { 
			xz_c21 = volData[(int)zin] [(int)yin] [(int)xin + n];
		}
		if (xin < 256 && zin + n < 113) { 
			xz_c12 = volData[(int)zin + n] [(int)yin] [(int)xin];
		}
		if (xin + n < 256 && zin + n < 113) { 
			xz_c22 = volData[(int)zin + n] [(int)yin] [(int)xin + n];
		}
		
		//YZ
		if (yin < 256 && zin < 113) { 
			yz_c11 = volData[(int)zin] [(int)yin] [(int)xin];
		}
		if (yin + n < 256 && zin < 113) { 
			yz_c21 = volData[(int)zin] [(int)yin + n] [(int)xin];
		}
		if (yin < 256 && zin + n < 113) { 
			yz_c12 = volData[(int)zin + n] [(int)yin] [(int)xin];
		}
		if (yin + n < 256 && zin + n < 113) { 
			yz_c22 = volData[(int)zin + n] [(int)yin + n] [(int)xin];
		}
		
		//YX
		if (yin < 256 && xin < 256) { 
			xy_c11 = volData[(int)zin] [(int)yin] [(int)xin];
		}
		if (yin < 256 && xin + n < 256) { 
			xy_c21 = volData[(int)zin] [(int)yin] [(int)xin + n];
		}
		if (yin + n < 256 && xin < 256) { 
			xy_c12 = volData[(int)zin] [(int)yin + n] [(int)xin];
		}
		if (yin + n < 256 && xin + n < 256) { 
			xy_c22 = volData[(int)zin] [(int)yin + n] [(int)xin + n];
		}
		
		int x1 = (int) xin;
		int x2 = (int) xin+n;
		
		int y1 = (int) yin;
		int y2 = (int) yin+n;
		
		int z1 = (int) zin;
		int z2 = (int) zin+n;

		//XZ
		short vxz1 = (short)(xz_c11 + (xz_c21 - xz_c11) * ((xin - x2) / (x2 - x1)));
		short vxz2 = (short)(xz_c12 + (xz_c22 - xz_c12) * ((xin - x2) / (x2 - x1)));
		short vxz  = (short)(vxz1 + (vxz2 - vxz1) * ((zin - z1) / (z2 - z1)));
		vxz = (short)Math.max(vxz,volumeData.getMin());
		vxz = (short)Math.min(vxz,volumeData.getMax());
		
		//YZ
		short vyz1 = (short)(yz_c11 + (yz_c21 - yz_c11) * ((yin - y2) / (y2 - y1)));
		short vyz2 = (short)(yz_c12 + (yz_c22 - yz_c12) * ((zin - z2) / (z2 - z1)));
		short vyz  = (short)(vyz1 + (vyz2 - vyz1) * ((yin - y1) / (y2 - y1)));
		vyz = (short)Math.max(vyz,volumeData.getMin());
		vyz = (short)Math.min(vyz,volumeData.getMax());
		
		//XY
		short vxy1 = (short)(xy_c11 + (xy_c21 - xy_c11) * ((xin - x2) / (x2 - x1)));
		short vxy2 = (short)(xy_c12 + (xy_c22 - xy_c12) * ((xin - x2) / (x2 - x1)));
		short vxy  = (short)(vxy1 + (vxy2 - vxy1) * ((yin - y1) / (y2 - y1)));
		vxy = (short)Math.max(vxy,volumeData.getMin());
		vxy = (short)Math.min(vxy,volumeData.getMax());

		short vxyz = (short)((vxz + vyz + vxy) / 3);
		
		vxyz = (short)Math.max(vxyz,volumeData.getMin());
		vxyz = (short)Math.min(vxyz,volumeData.getMax());
		
		return vxyz;
	}
	
    public static void main(String[] args) throws IOException {
		String fn = "";
		int w, h, d;
		if (args.length == 4) {
			fn = args[0];
			w = Integer.parseInt(args[1]);
			h = Integer.parseInt(args[2]);
			d = Integer.parseInt(args[3]);
		} else {
			fn = "CThead";
			w = 256;
			h = 256;
			d = 113;
		}
		
	   ImageProcessor imgProcessor = new ImageProcessor(new VolumeData(fn,w,h,d));
    }
	
	//LOTS OF MEMBER VARIABLES
	private VolumeData volumeData; // Volume Data Object
	private int i_max;
	private int j_max;
	private int k_max; // Volume data dimensions
	private double ai;
	private double aj;
	private double ak; // Axis angles
	private boolean mip_on; //Boolean indicating mip's state
	private boolean bilinear; //Booleans that indicate resize algorithm
	private int w_img;
	private int h_img; //width and height of current xyzimage
	private Axis curr_AXIS = Axis.Y; //maintains current selected axis
	private int thumbX = 50;
	private int thumbY = 50; //Maintains current thumbnail size
	private int[] mapping; //pointer to mapping from volumeData class
	/*
	 * GUI Related variables
	 */
	private BufferedImage xyzImage; // Working Image
	private JLabel xyzImageIcon; // JLabel icon
	private JCheckBox histogram_box; // toggle histogram box
	private JSlider i_slider;
	private JSlider j_slider;
	private JSlider k_slider; //Init JSliders for axis Rotation
	private JSlider slice_slider; //Init JSliders slice selection
	private JSpinner xthumb_Spinner; //Init spinners for resizing thumbnails
	private JSpinner x_Spinner; //Init spinners for zyxImage
	private JSpinner minSlice_Spinner; 
	private JSpinner maxSlice_Spinner; //Init spinners for the MIP slice range
	private JButton mip_Button; //Init button to toggle mip
	private JPanel leftPanel; //init left panel for image
	private JTabbedPane tabbedPane; //init TabbedPane
	private JPanel imageViewTab; //init image view tab
	private JPanel imageThumbnailsTab; //init image thumbnails tab
	private JPanel imageOptionsTab; //init image options tab
	private JScrollPane leftScroll; //init scroll pane left pane image
	private JScrollPane scrollPane; //init scroll pane for thumbs tab
	private ButtonGroup xyzButtonGroup;
	private JRadioButton custom_axis_radio;
	private JRadioButton x_axis_radio;
	private JRadioButton y_axis_radio;
	private JRadioButton z_axis_radio;//init buttons for rotational axis
	private ButtonGroup thumbButtonGroup;
	private JRadioButton xthumb_axis_radio;
	private JRadioButton ythumb_axis_radio;
	private JRadioButton zthumb_axis_radio;//init buttons for thumbnail view
	private int w_max = 450;
	private int h_max = 450;
	private int t_SPACER = 1; // top
	private int b_SPACER = 1; // bottom
	private int l_SPACER = 1; // left
	private int r_SPACER = 1; // right border spacers
	private int pack_SPACER = 10; // Width of packing, used for correcting borders
	private int tab_WIDTH = 350; // JTabbedPane width
	private int w_SPINNER = 42;
	private int h_SPINNER = 22; // Width and height of spinners
	private int ini_ANGLE = 0; // Initial slider values
	private int min_ANGLE = 0;
	private int max_ANGLE = 360; // Limit for axis sliders
	private int min_SLICE = 0;
	private int max_SLICE = 256; // min,max slice Range for MIP
	private int slice = 135; //used when mip isnt being used

}
//endfile