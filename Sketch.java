import java.awt.*;
import java.util.TreeMap;

public class Sketch {
    public TreeMap<Integer, Shape> sketches;
    public int id = 0;

    public Sketch() {
        sketches = new TreeMap<Integer, Shape>();
    }

    public synchronized int getID(Shape s) {
        for(Integer id : sketches.keySet()) {
            if(sketches.get(id).equals(s)) {
                return id;
            }
        }
        return -1;
    }

    public Shape getShape(Integer id) {
        return sketches.get(id);
    }

    public TreeMap<Integer, Shape> getShapes() {
        return sketches;
    }

    public synchronized void addSketch(Integer id, Shape s) {
        sketches.put(id, s);
    }

    public synchronized void addSketch(Shape s) {
        sketches.put(id, s);
        id++;
    }

    public synchronized void deleteSketch(Integer id) {
        sketches.remove(id);
    }

    public void draw(Graphics g) {
        for(Shape s : sketches.values()) {
            s.draw(g);
        }
    }

    public Shape contains(Point p) {
        for(Integer id : sketches.descendingKeySet()) {
            if(sketches.get(id).contains(p.x, p.y)) {
                return sketches.get(id);
            }
        }
        return null;
    }

}
