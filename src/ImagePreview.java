import	javax.swing.*;
import	javax.swing.event.*;
import	java.awt.*;
import	java.io.*;
import	java.beans.*;




public class ImagePreview extends JComponent implements ListSelectionListener, PropertyChangeListener
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("rawtypes")
	private	Class		parent;			// the class using ImagePreview
	private	ImageIcon	thumbnail = null;	// the thumb nailimage
	private	String		imageName = null;	// the name of the file containing the image
	private	File		imageFile = null;




	/**
	 * Constructs and ImagePreview object.
	 * @param list the initialized JList containing the list of image file names.
	 * @param parent the object instantiating and using the ImagePreview (actually,
	 * the Class object describing the user).
	 */

	public ImagePreview(@SuppressWarnings("rawtypes") JList list, @SuppressWarnings("rawtypes") Class parent)
	{
		this.parent = parent;
		setPreferredSize(new Dimension(100, 50));
		list.addListSelectionListener(this);		// make the ImavePreview object listen to the JList
	}



	/**
	 * Constructs and ImagePreview object.
	 * @param fc the file choose upon which the image is previewed.
	 */

	public ImagePreview(JFileChooser fc)
	{
		setPreferredSize(new Dimension(100, 50));
		fc.addPropertyChangeListener(this);
	}



	/**
	 * Loads the image as an icon and makes it into a thumbnail.
	 */

	public void loadImage(ImageIcon tmpIcon)
	{
		if (tmpIcon != null)
		{
			if (tmpIcon.getIconWidth() > 90)
				thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(90, -1, Image.SCALE_DEFAULT));
			else
				thumbnail = tmpIcon;
		}
	}



	/**
	 * Paints the thumbnail image on the ImagePrevew.
	 */

	public void paintComponent(Graphics g)
	{
		if (thumbnail == null)
			return;
			//loadImage();

		if (thumbnail != null)
		{
			int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
			int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

			if (y < 0)
				y = 0;

			if (x < 5)
				x = 5;

			thumbnail.paintIcon(this, g, x, y);
		}
	}



	/**
	 * Listens for items be selected in a JList.
	 * Determines which image name was selected from the list and load the image.
	 */

	@SuppressWarnings("rawtypes")
	public void valueChanged(ListSelectionEvent event)
	{
		imageName = (String)((JList)event.getSource()).getSelectedValue();

		ImageIcon tmpIcon = new ImageIcon(parent.getResource("images/" + imageName));
		loadImage(tmpIcon);

		if (thumbnail != null)
			repaint();
	}


	/**
	 * Listens for changes in a JFileChooser.
	 */

	public void propertyChange(PropertyChangeEvent e)
	{
		boolean	update = false;
		String	prop = e.getPropertyName();

		//If the directory changed, don't show an image.
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop))
		{
			imageFile = null;
			update = true;
		}

		//If a file became selected, find out which one.
		else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop))
		{
			imageFile = (File) e.getNewValue();
			update = true;
		}

		//Update the preview accordingly.
		if (update && imageFile != null)
		{
			thumbnail = null;
			if (isShowing())
			{
				ImageIcon tmpIcon = new ImageIcon(imageFile.getPath());
				loadImage(tmpIcon);
				repaint();
			}
		}
	}
}

