import java.awt.*;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
	private int name;
	private Color color;
	private ArrayList<Segment> segments;

	public Polyline(int x1, int y1, Color color) {
		this.color = color;
		Segment s = new Segment(x1, y1, color);
		segments = new ArrayList<Segment>();
		segments.add(s);
	}

	public Polyline(int x1, int y1, Color color, int name) {
		this.color = color;
		this.name = name;
		Segment s = new Segment(x1, y1, color);
		segments = new ArrayList<Segment>();
		segments.add(s);
	}

	public Polyline(int x1, int y1, int x2, int y2, Color color) {
		this.color = color;
		Segment s = new Segment(x1, y1, x2, y2, color);
		segments = new ArrayList<Segment>();
		segments.add(s);
	}

	public Polyline(int x1, int y1, int x2, int y2, Color color, int name) {
		this.color = color;
		this.name = name;
		Segment s = new Segment(x1, y1, x2, y2, color);
		segments = new ArrayList<Segment>();
		segments.add(s);
	}


	@Override
	public void moveBy(int dx, int dy) {
		for(Segment s : segments) {
			s.moveBy(dx, dy);
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		for(Segment s : segments) {
			s.setColor(color);
		}
	}
	
	@Override
	public boolean contains(int x, int y) {
		for(Segment s : segments) {
			if(s.contains(x, y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		for(Segment s : segments) {
			s.draw(g);
		}
	}

	public void addSegment(Segment s) {
		segments.add(s);
	}

	public void setCorners(int x1, int y1, int x2, int y2) {
		segments.add(new Segment(x1, y1, x2, y2, color));
	}

	@Override
	public String toString() {
		String str = "";
		for(Segment s : segments) {
			str += s.toString();
		}
		str += " " + color.getRGB() + " " + name;
		return str;
	}
}
