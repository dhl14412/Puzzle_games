import	java.awt.*;
import	java.awt.event.*;
import	java.util.*;
import	java.net.*;
import	javax.swing.*;
import	javax.swing.border.*;
import	java.io.*;
import	javax.swing.filechooser.*;


/**
 * Creates a jig saw puzzle.  Divides an image into small pieces and rearranges them at random.  Players reassemble the image
 * by clicking individual pieces, which are swapped.
 */

@SuppressWarnings("unused")
public class Puzzle extends JFrame implements MouseListener
{
	private static final long serialVersionUID = 1L;
	private	Image			image;						// the displayed image
	private GridBagLayout		board = new GridBagLayout();			// the layout manager
	private GridBagConstraints	constraints = new GridBagConstraints();		// used with the layout manager
	private	JPanel			picturePanel = new JPanel(board);		// the image goes here
	private	Picture			first = null;					// first image in swapping pair
	private	Picture			second = null;					// second image in swapping pair
	private	int			dWidth;						// width of puzzle piece
	private	int			dHeight;					// height of puzzle piece
	private	Border			normal = BorderFactory.createLineBorder(Color.BLACK);	// border of unselected puzzle piece
	private	Border			selected = BorderFactory.createLineBorder(Color.GREEN);	// border of selected puzzle piece
	private	ArrayList<Picture>	hat = new ArrayList<Picture>(100);		// contains random puzzle pieces
	private	Random			mixer = new Random();				// pseudo random number generator
	private	JComboBox<String>	choices;
	private	int			size = 9;
	private	int			pieces;




	public Puzzle()
	{
		setTitle("Puzzle");

		image = Toolkit.getDefaultToolkit().getImage(getFileName());

		MediaTracker	tracker = new MediaTracker(this);			// wait until the image is loaded
		tracker.addImage(image, 0);

		try
		{
			tracker.waitForAll();
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}

		add(picturePanel);							// and the panel to the center of the frame

		addWindowListener(new WindowAdapter()					// make the window closable
			{	public void windowClosing(WindowEvent event)
				{ System.exit(0); }
			} );

		JPanel panel = new JPanel();						// make a home for the play button
		add(panel, BorderLayout.SOUTH);						// set up the button
		JButton play = new JButton("Play");
		play.addActionListener(new ActionListener()
			{	public void actionPerformed(ActionEvent event)
				{ mixUp(event); }
			} );
		play.setToolTipText("Press play to scramble the image");
		panel.add(play);

		String[] pieces = { "4", "9", "16", "25", "36", "49", "64", "81", "100" };
		choices = new JComboBox<String>(pieces);	// Java 7 and later
		choices.setSelectedIndex(4);
		choices.addActionListener(new ActionListener()
			{	public void actionPerformed(ActionEvent e)
				{ getSize(e); }
			} );
		panel.add(choices);

		makePicture();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	// center the frame
		setLocation((screenSize.width - getWidth()) / 2,
				(screenSize.height - getHeight()) / 2);

		setVisible(true);							// make the frame visible
	}



	private void makePicture()
	{
		hat.clear();
		picturePanel.removeAll();

		dWidth = image.getWidth(this) / size;					// get the size of the individual pieces
		dHeight = image.getHeight(this) / size;

		for (int row = 0; row < size; row++)					// make the pieces by:
			for (int col = 0; col < size; col++)
			{
				Picture temp = new Picture(row, col);			// instantiating Picture objects
				hat.add(temp);						// putting the pieces into a hat
				addComponent(temp, row, col);				// adding the pieces to the picture panel
			}

		pack();
		validate();
	}



	// This version allows searching through a file system - cannot be used in a Jar

	private String getFileName()
	{
		File		current = new File(".");
		JFileChooser	fc = new JFileChooser(current);

		fc.setAccessory(new ImagePreview(fc));
		fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
				{
					public boolean accept(File f)
					{
						if (f.isDirectory()) return true;

						String name = f.getName().toLowerCase();
						if (name.endsWith("gif") || name.endsWith(".jpg") ||
							name.endsWith("png") || name.endsWith("tif")){
							return true;
						}
						else{
							return false;
						}
					}
					public String getDescription() {
						return "Image files";
					}
				});


		while (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			;

		return fc.getSelectedFile().getPath();
	}



	// This version assumes that the images are in an "images" sub-folder - can be used in a Jar

	/*private String getFileName()
	{
		String[] imageList = {"mini-v27.jpg"};
		JList<String> chooser = new JList<String>(imageList);	// Java 7 and later
		chooser.setVisibleRowCount(4);

		Object[] message = new Object[3];
		message[0] = new ImagePreview(chooser, Puzzle.class);
		message[1] = "Select Puzzle Image";
		message[2] = new JScrollPane(chooser);

		JOptionPane.showMessageDialog(this, message, "Image Selector", JOptionPane.QUESTION_MESSAGE);

		return "images/" + (String)chooser.getSelectedValue();
	}*/



	// called by "Play" button event handler.  Scrambles the puzzle pieces

	private void mixUp(ActionEvent e)
	{
		picturePanel.removeAll();
		pieces = size * size;

		Collections.shuffle(hat);

		int	index = 0;
		for (int row = 0; row < size; row++)					// randomly places the pieces
			for (int col = 0; col < size; col++)
			{
				Picture pic = hat.get(index++);
				pic.addMouseListener(this);				// make each piece a mouse listener
				addComponent(pic, row, col);
				pic.setPosition(row, col);				// set the new location in the puzzle
			}

		choices.setEnabled(false);
		((JButton)e.getSource()).setEnabled(false);				// deactivates the play button

		validate();
	}



	public void getSize(ActionEvent event)
	{
		@SuppressWarnings("rawtypes")
		JComboBox hat = (JComboBox)event.getSource();
		size = hat.getSelectedIndex() + 2;
		makePicture();
	}



	public boolean checkDone()
	{
		for (Picture p : hat)
			if (p.getRow() != p.getImageRow() || p.getCol() != p.getImageCol())
				return false;
		return true;
	}



	// adds component (i.e., puzzle pieces) to the GridGagLayout

	private void addComponent(Component c, int row, int col)
	{
		constraints.gridx = col;
		constraints.gridy = row;
		board.setConstraints(c, constraints);
		picturePanel.add(c);
	}


	// ----------------------------------------------------------------------------------------
	// MouseListener Event Handlers -----------------------------------------------------------
	// ----------------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e)
	{
		Picture	temp = (Picture)e.getSource();					// get the clicked puzzle piece

		if (first == null)							// select the clicked puzzle piece
		{
			first = temp;
			first.setBorder(selected);
			return;
		}
		else if (temp == first)							// deselect the clicked puzzple piece
		{
			first.setBorder(normal);
			first = null;
			return;
		}

		second = temp;								// get the second puzzle piece

		int row1 = first.getRow();						// get the positions of the clicked pieces
		int col1 = first.getCol();
		int row2 = second.getRow();
		int col2 = second.getCol();

		picturePanel.remove(first);						// remove the clicked pieces
		picturePanel.remove(second);

		first.setPosition(row2, col2);						// swap the clicked pieces
		first.setBorder(normal);
		second.setPosition(row1, col1);

		addComponent(first, row2, col2);
		addComponent(second, row1, col1);

		first = second = null;							// get ready for the next swap

		validate();								// update the display

		if (checkDone())
			JOptionPane.showMessageDialog(this, "Puzzle Solved", "Game Over", JOptionPane.INFORMATION_MESSAGE);
	}


	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}


	// ----------------------------------------------------------------------------------------
	// Inner class ----------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------

	/** provides a panel on which an image may be drawn */

	class Picture extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private	int	imageRow;						// image on this piece
		private	int	imageCol;
		private	int	col;							// current position of this piece
		private	int	row;


		public Picture(int imageRow, int imageCol)
		{
			row = this.imageRow = imageRow;
			col = this.imageCol = imageCol;

			setBorder(normal);
			setPreferredSize(new Dimension(image.getWidth(this)/size, image.getHeight(this)/size));
		}


		public void setPosition(int row, int col)				// set position of piece in puzzle
		{
			this.row = row;
			this.col = col;
			if (row == imageRow && col == imageCol)
			{
				MouseListener[] listeners = getMouseListeners();
				removeMouseListener(listeners[0]);
				pieces--;
			}

		}

		public int getCol()
		{
			return col;
		}

		public int getRow()
		{
			return row;
		}

		public int getImageRow()
		{
			return imageRow;
		}

		public int getImageCol()
		{
			return imageCol;
		}


		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(image, 0, 0, dWidth, dHeight, imageCol * dWidth, imageRow * dHeight,
					(imageCol + 1) * dWidth, (imageRow + 1) * dHeight, this);
		}
	}



	// ----------------------------------------------------------------------------------------
	// Program Start - main -------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------

	public static void main(String[] args)
	{
		new Puzzle();
	}
}

