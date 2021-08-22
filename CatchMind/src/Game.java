import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Game extends JPanel {
	ArrayList<Point> board = new ArrayList<>();

	JButton[] colorButtons;
	JButton cleanAll, eraser, thick, thin;
	JLabel thickness;
	JPanel toolPanel, colorPanel;
	DrawPanel drawPanel;

	private int startX, startY, endX, endY;

	ObjectInputStream reader;
	ObjectOutputStream writer;
	
	Graphics graphics;
	Graphics2D g;
	Color drawColor;

	public Game() {
		// 색깔 버튼 설정
		setButton();

		// GUI 배치
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		drawPanel = new DrawPanel();
		drawPanel.addMouseListener(new MyMouseListener());
		drawPanel.addMouseMotionListener(new Paint());

		toolPanel = new JPanel();
		toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.X_AXIS));

		toolPanel.add(thick);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		toolPanel.add(thin);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		thickness = new JLabel("" + drawPanel.thickness);
		toolPanel.add(thickness);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		for (int i = 0; i < colorButtons.length; i++) {
			toolPanel.add(colorButtons[i]);
			toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		}
		toolPanel.add(cleanAll);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		toolPanel.add(eraser);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		this.add(drawPanel);
		this.add(toolPanel);

		setPreferredSize(new Dimension(770, 900));
	}

	private void setButton() {
		// 색상 변경
		colorButtons = new JButton[6];
		for (int i = 0; i < colorButtons.length; i++) {
			colorButtons[i] = new JButton();
			colorButtons[i].setPreferredSize(new Dimension(10, 50));
			colorButtons[i].addActionListener(new ButtonListener());
		}
		colorButtons[0].setBackground(Color.red);
		colorButtons[1].setBackground(Color.orange);
		colorButtons[2].setBackground(Color.yellow);
		colorButtons[3].setBackground(Color.green);
		colorButtons[4].setBackground(Color.blue);
		colorButtons[5].setBackground(Color.black);

		// 모두 지우기 버튼
		cleanAll = new JButton("모두 지우기");
		cleanAll.addActionListener(new ButtonListener());

		// 지우기 버튼
		eraser = new JButton("지우기");
		eraser.addActionListener(new ButtonListener());

		// 굵기 조절 버튼
		thick = new JButton("굵게");
		thick.addActionListener(new ButtonListener());
		thin = new JButton("얇게");
		thin.addActionListener(new ButtonListener());
	}

	// Start ActionListener
	class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 빨간색
			if (e.getSource() == colorButtons[0]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawColor = Color.red;
			}
			// 주황색
			else if (e.getSource() == colorButtons[1]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawColor = Color.orange;
			}
			// 노란색
			else if (e.getSource() == colorButtons[2]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawColor = Color.yellow;
			}
			// 초록색
			else if (e.getSource() == colorButtons[3]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawColor = Color.green;
			}
			// 파란색
			else if (e.getSource() == colorButtons[4]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawColor = Color.blue;
			}
			// 검정색
			else if (e.getSource() == colorButtons[5]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawColor = Color.black;
			}
			// 모두 지우기
			else if (e.getSource() == cleanAll) {
				cleanAll();
			}
			// 지우기
			else if (e.getSource() == eraser) {
				drawPanel.setCursor(drawPanel.eraser);
				drawColor = Color.white;
			}
			// 굵게
			else if (e.getSource() == thick) {
				drawPanel.thickness += 1;
				thickness.setText("" + drawPanel.thickness);
			}
			// 얅게
			else if (e.getSource() == thin) {
				if (drawPanel.thickness == 5)
					return;

				drawPanel.thickness -= 1;
				thickness.setText("" + drawPanel.thickness);
			}
		}
	} // End ActionListener

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

			draw(g);

			try {
				writer.writeObject(new Message(Message.MsgType.DRAW, new Point(startX, startY), new Point(endX, endY),
						drawColor, drawPanel.thickness));
				writer.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			startX = endX;
			startY = endY;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}
	// End MouseMotionListener

	public void draw(Graphics2D g) {
		graphics = getGraphics();
		g = (Graphics2D)graphics;
		g.setColor(drawColor);
		g.setStroke(new BasicStroke(drawPanel.thickness, BasicStroke.CAP_ROUND, 0));
		g.drawLine(startX, startY, endX, endY);
	}

	public void cleanAll() {
		g.setColor(Color.white);
		g.fillRect(0, 0, drawPanel.getWidth(), drawPanel.getHeight());
	}

	public void setStartPoint(Point point) {
		this.startX = point.x;
		this.startY = point.y;
	}

	public void setEndPoint(Point point) {
		this.endX = point.x;
		this.endY = point.y;
	}

	public Point getEndPoint() {
		return new Point(this.endX, this.endY);
	}

	public void setColor(Color color) {
		this.drawColor = color;
	}

	public void setThickness(int thickness) {
		drawPanel.thickness = thickness;
	}
	
	public void setGraphics(Graphics2D g) {
		this.g = g;
	}
}