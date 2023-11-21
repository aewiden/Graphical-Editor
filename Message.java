import java.awt.*;

public class Message {
    private Sketch sketch;
    private Shape curr;
    private String[] info;
    private String m;

    // constrcutors for the message object
    public Message() {}
    public Message(String m, Sketch sketch, Editor e) {
        this.m = m;
        this.sketch = sketch;
    }

    // edits the sketch based on an input command and a sketch to edit
    public int editSketch(String m, Sketch sketch) {
        // instantiates a temporary sketch and current, as well as the info array
        curr = null;
        Sketch tempSketch = new Sketch();
        info = m.split(" ");

        // checks to see what the first command is
        if(info[0].equals("draw")) {
            // based on the shape, it sets the current shape to be edited to a new object of that shape
            if(info[1].equals("ellipse")) {
                curr = new Ellipse(Integer.parseInt(info[2]), Integer.parseInt(info[3]), Integer.parseInt(info[4]), Integer.parseInt(info[5]), new Color(Integer.parseInt(info[6])));
            }
            else if(info[1].equals("freehand")) {
                String[] pts = info[2].split(" ");
                curr = new Polyline(Integer.parseInt(pts[0]), Integer.parseInt(pts[1]), new Color(Integer.parseInt(info[3])));
            }
            else if(info[1].equals("rectangle")) {
                curr = new Rectangle(Integer.parseInt(info[2]), Integer.parseInt(info[3]), Integer.parseInt(info[4]), Integer.parseInt(info[5]), new Color(Integer.parseInt(info[6])));
            }
            else if(info[1].equals("segment")) {
                curr = new Segment(Integer.parseInt(info[2]), Integer.parseInt(info[3]), Integer.parseInt(info[4]), Integer.parseInt(info[5]), new Color(Integer.parseInt(info[6])));
            }
        }
        // if the command is to move, it moves the shape by the desired amount
        else if(info[0].equals("move")) {
            curr = sketch.getShape(Integer.parseInt(info[1]));
            if(curr != null) {
                curr.moveBy(Integer.parseInt(info[2]), Integer.parseInt(info[3]));
            }
        }
        // recolors the shape
        else if(info[0].equals("recolor")) {
            curr = sketch.getShape(Integer.parseInt(info[1]));
            curr.setColor(new Color(Integer.parseInt(info[2])));
        }
        // deletes the shape
        else if(info[0].equals("delete")) {
            curr = sketch.getShape(Integer.parseInt(info[1]));
            sketch.deleteSketch(Integer.parseInt(info[1]));
        }
        // if the command is to edit the sketch, it adds the shapes based on the new command
        else if(info[0].equals("editSketch")) {
            if(info[2].equals("ellipse")) {
                curr = new Ellipse(Integer.parseInt(info[3]), Integer.parseInt(info[4]), Integer.parseInt(info[5]), Integer.parseInt(info[6]), new Color(Integer.parseInt(info[7])));
                sketch.addSketch(Integer.parseInt(info[8]), curr);
            }
            else if(info[2].equals("freehand")) {
                String[] pts = info[3].split(" ");
                curr = new Polyline(Integer.parseInt(pts[0]), Integer.parseInt(pts[1]), new Color(Integer.parseInt(info[4])));
                sketch.addSketch(Integer.parseInt(info[5]), curr);
            }
            else if(info[2].equals("rectangle")) {
                curr = new Rectangle(Integer.parseInt(info[3]), Integer.parseInt(info[4]), Integer.parseInt(info[5]), Integer.parseInt(info[6]), new Color(Integer.parseInt(info[7])));
                sketch.addSketch(Integer.parseInt(info[8]), curr);
            }
            else if(info[2].equals("segment")) {
                curr = new Segment(Integer.parseInt(info[3]), Integer.parseInt(info[4]), Integer.parseInt(info[5]), Integer.parseInt(info[6]), new Color(Integer.parseInt(info[7])));
                sketch.addSketch(Integer.parseInt(info[8]), curr);
            }
        }
        return sketch.getID(curr);
    }
}
