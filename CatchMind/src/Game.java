import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Game extends JPanel {
	ImageIcon pencilImg = new ImageIcon("img/pencil.png");
	ImageIcon eraserImg = new ImageIcon("img/eraser.png");
	Toolkit toolKit = Toolkit.getDefaultToolkit();
	Cursor pencil = toolKit.createCustomCursor(pencilImg.getImage(), new Point(0, 0), "pencil");
	Cursor eraser = toolKit.createCustomCursor(eraserImg.getImage(), new Point(0, 0), "eraser");

	ArrayList<Point> board = new ArrayList<>();

	JButton[] colorButtons;
	JButton cleanAllBtn, eraserBtn, thickBtn, thinBtn;
	JLabel thickness;
	JPanel drawPanel, toolPanel, colorPanel;

	ObjectInputStream reader;
	ObjectOutputStream writer;

	// Listener Classes
	Draw draw = new Draw();
	ButtonListener buttonListener = new ButtonListener();
	MyMouseListener myMouseListener = new MyMouseListener();

	// To Draw
	Graphics graphics;
	Graphics2D g;
	Color drawColor;
	
	private int startX, startY, endX, endY;
	int thicknessInt = 5;
	
	boolean isActiveDraw, isActiveButton;

	// Constructor
	public Game() {
		// 버튼 세팅 메소드
		setButton();

		// GUI 배치
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		drawPanel = new JPanel();
		drawPanel.setBackground(Color.white);
		drawPanel.setPreferredSize(new Dimension(300, 600));
		drawPanel.setCursor(pencil);
		drawPanel.addMouseListener(myMouseListener);
		drawPanel.addMouseMotionListener(draw);

		toolPanel = new JPanel();
		toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.X_AXIS));

		toolPanel.add(thickBtn);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		toolPanel.add(thinBtn);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		thickness = new JLabel("" + thicknessInt);
		toolPanel.add(thickness);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		for (int i = 0; i < colorButtons.length; i++) {
			toolPanel.add(colorButtons[i]);
			toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		}
		toolPanel.add(cleanAllBtn);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		toolPanel.add(eraserBtn);
		toolPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		this.add(drawPanel);
		this.add(toolPanel);

		setPreferredSize(new Dimension(770, 900));
	}

	// 버튼 세팅 메소드
	private void setButton() {
		// 색상
		colorButtons = new JButton[6];
		for (int i = 0; i < colorButtons.length; i++) {
			colorButtons[i] = new JButton();
			colorButtons[i].setPreferredSize(new Dimension(10, 50));
			colorButtons[i].addActionListener(buttonListener);
		}
		colorButtons[0].setBackground(Color.red);
		colorButtons[1].setBackground(Color.orange);
		colorButtons[2].setBackground(Color.yellow);
		colorButtons[3].setBackground(Color.green);
		colorButtons[4].setBackground(Color.blue);
		colorButtons[5].setBackground(Color.black);

		// 모두 지우기
		cleanAllBtn = new JButton("모두 지우기");
		cleanAllBtn.addActionListener(buttonListener);

		// 지우기
		eraserBtn = new JButton("지우기");
		eraserBtn.addActionListener(buttonListener);

		// 굵기 조절
		thickBtn = new JButton("굵게");
		thickBtn.addActionListener(buttonListener);
		thinBtn = new JButton("얇게");
		thinBtn.addActionListener(buttonListener);
	}

	// Start of Listeners
	class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (isActiveButton) {
				// 빨간색
				if (e.getSource() == colorButtons[0]) {
					drawPanel.setCursor(pencil);
					drawColor = Color.red;
				}
				// 주황색
				else if (e.getSource() == colorButtons[1]) {
					drawPanel.setCursor(pencil);
					drawColor = Color.orange;
				}
				// 노란색
				else if (e.getSource() == colorButtons[2]) {
					drawPanel.setCursor(pencil);
					drawColor = Color.yellow;
				}
				// 초록색
				else if (e.getSource() == colorButtons[3]) {
					drawPanel.setCursor(pencil);
					drawColor = Color.green;
				}
				// 파란색
				else if (e.getSource() == colorButtons[4]) {
					drawPanel.setCursor(pencil);
					drawColor = Color.blue;
				}
				// 검정색
				else if (e.getSource() == colorButtons[5]) {
					drawPanel.setCursor(pencil);
					drawColor = Color.black;
				}
				// 모두 지우기
				else if (e.getSource() == cleanAllBtn) {
					try {
						writer.writeObject(new Message(Message.MsgType.CLEAR));
						writer.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					cleanAll();
				}
				// 지우기
				else if (e.getSource() == eraserBtn) {
					drawPanel.setCursor(eraser);
					drawColor = Color.white;
				}
				// 굵게
				else if (e.getSource() == thickBtn) {
					thicknessInt += 1;
					thickness.setText("" + thicknessInt);
				}
				// 얅게
				else if (e.getSource() == thinBtn) {
					if (thicknessInt == 5)
						return;

					thicknessInt -= 1;
					thickness.setText("" + thicknessInt);
				}
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
	} // End MouseListener

	// To Draw
	public class Draw implements MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent e) {
			if (isActiveDraw) {
				endX = e.getX();
				endY = e.getY();

				draw(g);

				try {
					writer.writeObject(new Message(Message.MsgType.DRAW, new Point(startX, startY),
							new Point(endX, endY), drawColor, thicknessInt));
					writer.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				startX = endX;
				startY = endY;
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	} // End MouseMotionListener

	// Method to draw
	public void draw(Graphics2D g) {
		graphics = getGraphics();
		g = (Graphics2D) graphics;
		g.setColor(drawColor);
		g.setStroke(new BasicStroke(thicknessInt, BasicStroke.CAP_ROUND, 0));
		g.drawLine(startX, startY, endX, endY);
	}

	public void cleanAll() {
		graphics = getGraphics();
		g = (Graphics2D) graphics;
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
		thicknessInt = thickness;
	}

	public void setGraphics(Graphics2D g) {
		this.g = g;
	}
}