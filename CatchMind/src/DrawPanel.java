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

public class DrawPanel extends JPanel {
	ImageIcon pencilImg = new ImageIcon("img/pencil.png");
	ImageIcon eraserImg = new ImageIcon("img/eraser.png");
	Toolkit toolKit = Toolkit.getDefaultToolkit();
	Cursor pencil = toolKit.createCustomCursor(pencilImg.getImage(), new Point(0, 0), "pencil");
	Cursor eraser = toolKit.createCustomCursor(eraserImg.getImage(), new Point(0, 0), "eraser");

	int startX, startY, endX, endY;
	int thickness;

	public DrawPanel() {
		setPanel();

		this.thickness = 5;
	}

	public void setPanel() {
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(300, 600));
		this.setCursor(pencil);
	}
}
