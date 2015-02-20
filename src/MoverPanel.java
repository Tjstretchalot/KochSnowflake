import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * <p>Generates the Koch Snowflake in the "logical" way that a human would, by replacing
 * directions from the original snowflake with the appropriate sequence of directions
 * in the nest snowflake. For example, the original triangle consists of 3 straights and
 * 3 lefts, or 1 straight and 1 left 3 times.</p>
 * <p>If you replace a left with a right left right left, you create a sub triangle going to the right.
 * Similarly, if you replace a right with a right right right left, you create a subtriangle out of
 * that triangle</p>
 * <p>By repeating this pattern you get the koch snowflake, which is drawn here one step of the
 * sequence per 34 milliseconds, drawing 7 iterations of the koch snowflake before restarting</p>
 * @author Timothy
 * @author Donald
 * @author Matt
 * @author Nic
 */
public class MoverPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 191200614435209993L;
	
	/**
	 * We store directions in a boolean, where true is left
	 * and right is false
	 */
	private static final Boolean LEFT = Boolean.TRUE;
	private static final Boolean RIGHT = Boolean.FALSE;
	
	/**
	 * How much we need to turn to go left; thinking about
	 * the original triangle 
	 *   /\
	 *  /  \
	 *  ----
	 * This is clearly 120 degrees
	 */
	private static final double TURN_LEFT_THETA = -120;
	/**
	 * Turning right involves a less intense turn, which
	 * happens to be 60 degrees
	 */
	private static final double TURN_RIGHT_THETA = 60;
	
	/**
	 * How often a new step is drawn
	 */
	private static final int DELTA = 34;

	/**
	 * The length of the lines in the original triangle. The
	 * length of each step in n iterations is lineLength * 3^(-n)
	 */
	private double lineLength = 850;
	
	/**
	 * Since we are drawing lines, we remember a sequence of points
	 * and render by simply connecting point 0 to point 1, point 1
	 * to point 2, etc.
	 */
	private List<Point2D> points;
	
	/**
	 * Calls actionListener every DELTA milliseconds
	 */
	private Timer timer;
	
	/**
	 * How many iterations of the koch snowflake we are
	 * currently on
	 */
	private int nestLevel;
	
	/**
	 * This isn't necessary for a robot to draw the fractal, but we need
	 * to remember which direction we were going previously
	 */
	private double lastTheta;
	
	/**
	 * What straight+turn combos we have left to draw the current
	 * iteration of the koch triangle
	 */
	private List<Boolean> dirQueue;
	
	/**
	 * How many milliseconds left to wait before starting the next iteration
	 * of the koch triangle, counted after finishing the previous triangle
	 */
	private int restartCounter;
	
	/**
	 * If we have cleared the screen and drawn authors
	 */
	private volatile boolean cleared;
	
	/**
	 * Constructs the mover panel, creates an empty array of
	 * points, starts the time, sets nest level to 0 (so we
	 * start at 1 after resetting), creates an empty 
	 * direction queue, and sets restart counter to 1000 (so
	 * we don't start the first triangle immediately)
	 */
	public MoverPanel() {
		points = new ArrayList<>();
		
		timer = new Timer(DELTA, this);
		nestLevel = 0;
		dirQueue = new ArrayList<>();
		timer.start();
		restartCounter = 1000;
		cleared = false;
	}
	
	/**
	 * Returns the next iteration of the koch snowflake
	 * @param queue the last snowflake
	 * @return the new snowflake
	 */
	private List<Boolean> fractalize(List<Boolean> queue) {
		List<Boolean> newQueue = new ArrayList<>();
		
		for(Boolean b : queue) {
			if(b.equals(LEFT)) {
				newQueue.add(RIGHT);
				newQueue.add(LEFT);
				newQueue.add(RIGHT);
				newQueue.add(LEFT);
			}else {
				newQueue.add(RIGHT);
				newQueue.add(LEFT);
				newQueue.add(RIGHT);
				newQueue.add(RIGHT);
			}
		}
		
		return newQueue;
	}
	
	/**
	 * Converts from radius and degrees to a delta
	 * vector
	 * 
	 * @param r radius
	 * @param degrees degrees
	 * @return delta vector
	 */
	private Point2D fromPolar(double r, double degrees) {
		double x = r * Math.cos(degrees * Math.PI / 180.);
		double y = r * Math.sin(degrees * Math.PI / 180.);
		
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Gets the delta vector of the next point and
	 * updates lastTheta
	 * 
	 * @return the delta vector
	 */
	private Point2D getDelta() {
		/*
		 * Notice that lastTheta is the theta we use this time, that is
		 * so we always start by going 0 degrees from the positive x
		 * (horizontal). We update lastTheta to newTheta which is the
		 * "turning"
		 */
		
		double dist = lineLength * Math.pow(3, -(nestLevel - 1));
		boolean direction = dirQueue.get(0);
		dirQueue.remove(0);
		double newTheta = lastTheta;

		if(LEFT.equals(direction)) { // LEFT
			newTheta += TURN_LEFT_THETA;
		}else {
			newTheta += TURN_RIGHT_THETA;
		}
		while(newTheta < 0) {
			newTheta += 360;
		}
		while(newTheta >= 360) {
			newTheta -= 360;
		}
		Point2D res = fromPolar(dist, lastTheta);
		lastTheta = newTheta;
		return res;
	}
	
	/**
	 * Called every DELTA ms. If the points are empty, set up the
	 * first point so the snowflake will be centered, if the 
	 * direction queue is empty work on going on to the next iteration,
	 * and otherwise add the new delta to the list of points so it 
	 * will be rendered
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(!cleared)
			return;
		
		if(points.size() == 0) {
			lineLength = getHeight() * 0.85;
			double multiplier = (nestLevel == 1 ? 0.86602540 : 1.15470054);
			double width = lineLength;
			double height = lineLength * multiplier;
			double x = (getWidth() - width)/2;
			double y = getHeight() - ((getHeight() - height)/2);
			if(nestLevel > 1)
				y -= (129.903811) * (lineLength / 450.); // We don't actually start at the bottom left
			points.add(new Point2D.Double(x, y));
		} 
		if(dirQueue.size() == 0) {
			restartCounter -= DELTA;
			if(restartCounter <= 0) {
				nestLevel = (nestLevel + 1) % 8;
				if(nestLevel == 0)
					nestLevel = 1;
				restartCounter = 2000;
				points.clear();
				dirQueue.add(LEFT);
				
				for(int i = 1; i < nestLevel; i++) {
					dirQueue = fractalize(dirQueue);
				}
				
				int size = dirQueue.size();
				for(int i = 0; i < 2; i++) {
					for(int j = 0; j < size; j++) {
						dirQueue.add(dirQueue.get(j));
					}
				}
				
				cleared = false;
				repaint();
			}
			return;
		}
		Point2D curPoint = points.get(points.size() - 1);
		Point2D delta = getDelta();
		Point2D newPoint = new Point2D.Double(curPoint.getX() + delta.getX(), curPoint.getY() + delta.getY());
		points.add(newPoint);
		repaint();
	}
	
	/**
	 * Don't actually render the whole screen every time, just the new point. This
	 * means you can't minimize the triangle and be able to see it again until the
	 * next iteration. Hence the whole fullscreen thing
	 */
	@Override
	public void paintComponent(Graphics g) {
		if(points.size() < 2) {
			if(!cleared) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(getForeground());
				g.drawString("Koch Snowflake by Timothy Moore, Donald Moore, Nicolas Brown, and Matt Todd", 100, 100);
				cleared = true;
			}
			return;
		}
		g.setColor(getForeground());
		Point2D last = points.get(points.size() - 2);
		Point2D cur = points.get(points.size() - 1);
		g.drawLine((int) last.getX(), (int) last.getY(), (int) cur.getX(), (int) cur.getY());
	}
}
