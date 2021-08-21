import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class DrawPanel extends JPanel implements Serializable {
	ImageIcon pencilImg = new ImageIcon("img/pencil.png");
	ImageIcon eraserImg = new ImageIcon("img/eraser.png");
	Toolkit toolKit = Toolkit.getDefaultToolkit();
	Cursor pencil = toolKit.createCustomCursor(pencilImg.getImage(), new Point(0, 0), "pencil");
	Cursor eraser = toolKit.createCustomCursor(eraserImg.getImage(), new Point(0, 0), "eraser");

	int startX, startY, endX, endY;
	int thickness;

	Graphics graphics;
	Graphics2D g;
	Color drawColor;

	public DrawPanel() {
		setPanel();

		this.thickness = 5;
	}

	public void setPanel() {
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(300, 600));
		this.setCursor(pencil);
		this.addMouseListener(new MyMouseListener());
		this.addMouseMotionListener(new Paint());
	}

	class MyMouseListener implements MouseListener {
		@Override
		public void mousePressed(MouseEvent e) {
			startX = e.getX();
			startY = e.getY();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
	// End MouseListener

	// Start MouseMotionListener
	public class Paint implements MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent e) {
			endX = e.getX();
			endY = e.getY();

			graphics = getGraphics();
			g = (Graphics2D) graphics;
			g.setColor(drawColor);
			g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, 0));
			g.drawLine(startX, startY, endX, endY);

			startX = endX;
			startY = endY;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}
	// End MouseMotionListener

	public void cleanAll() {
		graphics = getGraphics();
		g = (Graphics2D) graphics;

		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

}
