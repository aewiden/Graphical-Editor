import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	public Message m;


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();
		m = new Message();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// for each shape in the shape list, draw it on the sketch
		for(Integer i : sketch.sketches.keySet()) {
			sketch.sketches.get(i).draw(g);
		}

	}

	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// checks to see the mode
		if(mode == Mode.DRAW) {
			// sets the point to draw from
			drawFrom = p;
			// goes through each shape type and sets the current shape variable to the entered shape
			if(shapeType.equals("ellipse")) {
				// initializes current using the drawFrom point and adds it to the sketch
				curr = new Ellipse((int)drawFrom.getX(), (int)drawFrom.getY(), color);
				sketch.addSketch(curr);
			}
			// does the same for each shape type
			else if(shapeType.equals("freehand")) {
				curr = new Polyline((int)drawFrom.getX(), (int)drawFrom.getY(), color);
				sketch.addSketch(curr);
			}
			else if(shapeType.equals("rectangle")) {
				curr = new Rectangle((int)drawFrom.getX(), (int)drawFrom.getY(), color);
				sketch.addSketch(curr);
			}
			else if(shapeType.equals("segment")) {
				curr = new Segment((int)drawFrom.getX(), (int)drawFrom.getY(), color);
				sketch.addSketch(curr);
			}
			repaint();
		}
		// handles the move mode
		else if(mode == Mode.MOVE) {
			// checks if the sketch contains the point in any of its shapes
			if(sketch.contains(p) != null) {
				// sets the move from point to the clicked point and sets the current shape to that clicked on
				moveFrom = p;
				curr = sketch.contains(p);
				// sets the moving id to the current shape being moved
				movingId = sketch.getID(curr);
			}
		}
		// handles the recolor mode
		else if(mode == Mode.RECOLOR) {
			// checks if the sketch contains the point in any of its shapes
			if(sketch.contains(p) != null) {
				// sets the current shape to that which contains the point
				curr = sketch.contains(p);
				// sends the communicator a message to recolor the shape and which color to set it to
				comm.send("recolor " + sketch.getID(curr) + " " + color.getRGB());
			}
		}
		// handles the delete mode
		else if(mode == Mode.DELETE) {
			// checks if the sketch contains the point in any of its shapes
			if(sketch.contains(p) != null) {
				// sets the current shape to that which contains the point
				curr = sketch.contains(p);
				// sends the communicator a message to delete the shape
				comm.send("delete " + sketch.getID(curr));
			}
			repaint();
		}
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// handles the draw mode
		if(mode == Mode.DRAW) {
			// checks for the shape type
			if(shapeType.equals("ellipse")) {
				// casts the current shape to the input shape and sets the new corners
				((Ellipse)curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			}
			// does the same for each possible shape type
			else if(shapeType.equals("freehand")) {
				// casts the current shape to the input shape and adds the new segment
				((Polyline)curr).addSegment(new Segment(drawFrom.x, drawFrom.y, p.x, p.y, color));
			}
			else if(shapeType.equals("rectangle")) {
				// casts the current shape to the input shape and sets the new corners
				((Rectangle)curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			}
			else if(shapeType.equals("segment")) {
				// casts the current shape to the input shape and sets the new end point to the segment
				((Segment)curr).setEnd(p.x, p.y);
			}
			repaint();
		}
		// handles the move mode
		if(mode == Mode.MOVE) {
			// sets the current shape to that which contains the point
			if(sketch.contains(p) != null) {
				// sends the communicator a message to move the shape and which points to move it to
				comm.send("move " + movingId + " " + moveFrom.x + " " + moveFrom.y + " " + (p.x - moveFrom.x) + " " + (p.y - moveFrom.y));
				// resets moveFrom to the new point
				moveFrom = p;
			}
			repaint();
		}
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		// handles the draw mode
		if(mode == Mode.DRAW) {
			// sends the communicator a message to draw the shape
			comm.send("draw " + curr.toString());
			repaint();
		}
		// move mode handled in drag/press
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
