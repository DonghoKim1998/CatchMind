import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	int startX, startY, endX, endY;

	ObjectInputStream reader;
	ObjectOutputStream writer;

	public Game() {
		// ���� ��ư ����
		setButton();

		// GUI ��ġ
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		drawPanel = new DrawPanel();

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
		// ���� ����
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

		// ��� ����� ��ư
		cleanAll = new JButton("��� �����");
		cleanAll.addActionListener(new ButtonListener());

		// ����� ��ư
		eraser = new JButton("�����");
		eraser.addActionListener(new ButtonListener());

		// ���� ���� ��ư
		thick = new JButton("����");
		thick.addActionListener(new ButtonListener());
		thin = new JButton("���");
		thin.addActionListener(new ButtonListener());
	}

	// Start ActionListener
	class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// ������
			if (e.getSource() == colorButtons[0]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawPanel.drawColor = Color.red;
			}
			// ��Ȳ��
			else if (e.getSource() == colorButtons[1]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawPanel.drawColor = Color.orange;
			}
			// �����
			else if (e.getSource() == colorButtons[2]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawPanel.drawColor = Color.yellow;
			}
			// �ʷϻ�
			else if (e.getSource() == colorButtons[3]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawPanel.drawColor = Color.green;
			}
			// �Ķ���
			else if (e.getSource() == colorButtons[4]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawPanel.drawColor = Color.blue;
			}
			// ������
			else if (e.getSource() == colorButtons[5]) {
				drawPanel.setCursor(drawPanel.pencil);
				drawPanel.drawColor = Color.black;
			}
			// ��� �����
			else if (e.getSource() == cleanAll) {
				drawPanel.cleanAll();
			}
			// �����
			else if (e.getSource() == eraser) {
				drawPanel.setCursor(drawPanel.eraser);
				drawPanel.drawColor = Color.white;
			}
			// ����
			else if (e.getSource() == thick) {
				drawPanel.thickness += 1;
				thickness.setText("" + drawPanel.thickness);
			}
			// ����
			else if (e.getSource() == thin) {
				if (drawPanel.thickness == 5)
					return;

				drawPanel.thickness -= 1;
				thickness.setText("" + drawPanel.thickness);
			}
		}
	}
	// End ActionListener
}