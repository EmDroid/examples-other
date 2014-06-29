package com.styryx.ai.genetic.apps.Robby.gui.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.GroupLayout.*;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.ai.genetic.apps.Robby.worker.Playground;

public class PlaygroundPanel extends JPanel {
	
	private static final Dimension FIELD_SIZE = new Dimension(30, 30);
	
	private static final int ROBBY_SHIFT = 4;
	private static final int ROBBY_SHIFT_DBL = ROBBY_SHIFT << 1;
	
	private static final int CAN_SHIFT = ROBBY_SHIFT_DBL;
	private static final int CAN_SHIFT_DBL = CAN_SHIFT << 1;
	
	private class Field extends JPanel {
		
		private final int m_row;
		private final int m_col;

		public Field(int row, int col) {
			m_row = row;
			m_col = col;
			setPreferredSize(FIELD_SIZE);
			setBorder(BorderFactory.createRaisedBevelBorder());
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Playground playground = m_main.getPlayground();
			if ((null != playground) && (null != g)) {
				g = g.create();
				try {
					// Have a playground - check if we have comething to draw.
					Playground.Position pos = playground.getPosition();
	    			Dimension size = getSize();
	    			Rectangle bounds = new Rectangle(0, 0, size.width, size.height);
					//TODO Optimize drawing.
					if ((m_row == pos.row) && (m_col == pos.col)) {
						// Draw the robot.
						Rectangle robbyBounds = (Rectangle) bounds.clone();
						robbyBounds.x += ROBBY_SHIFT;
						robbyBounds.y += ROBBY_SHIFT;
						robbyBounds.width -= ROBBY_SHIFT_DBL;
						robbyBounds.height -= ROBBY_SHIFT_DBL;
						int robbyHeight = robbyBounds.height * 3 / 4;
						int eyeX = robbyBounds.width / 8;
						int eyeWidth = (eyeX * 3) + 1;
						// The "body".
						Rectangle body = new Rectangle(
								robbyBounds.x,
								robbyBounds.y + robbyBounds.height - robbyHeight - eyeX,
								robbyBounds.width,
								robbyHeight);
						g.setColor(Color.RED);
						g.fillOval(body.x, body.y, body.width, body.height);
						g.setColor(Color.BLACK);
						g.drawOval(body.x, body.y, body.width, body.height);
						// The "eyes".
						Rectangle eye1 = new Rectangle(
								robbyBounds.x + eyeX,
								robbyBounds.y,
								eyeWidth,
								robbyBounds.height >> 1);
						Rectangle eye2 = new Rectangle(
								robbyBounds.x + robbyBounds.width - eyeX - eyeWidth,
								robbyBounds.y,
								eyeWidth,
								robbyBounds.height >> 1);
						g.setColor(Color.WHITE);
						g.fillOval(eye1.x, eye1.y, eye1.width, eye1.height);
						g.fillOval(eye2.x, eye2.y, eye2.width, eye2.height);
						g.setColor(Color.BLACK);
						// The "irises".
						Rectangle iris1 = new Rectangle(
								robbyBounds.x + eyeX,
								robbyBounds.y + (robbyBounds.height >> 1) - eyeWidth,
								eyeWidth,
								eyeWidth);
						Rectangle iris2 = new Rectangle(
								robbyBounds.x + robbyBounds.width - eyeX - eyeWidth,
								robbyBounds.y + (robbyBounds.height >> 1) - eyeWidth,
								eyeWidth,
								eyeWidth);
						g.fillOval(iris1.x, iris1.y, iris1.width, iris1.height);
						g.fillOval(iris2.x, iris2.y, iris2.width, iris2.height);
						g.drawOval(eye1.x, eye1.y, eye1.width, eye1.height);
						g.drawOval(eye2.x, eye2.y, eye2.width, eye2.height);
					}
					if (playground.hasCan(m_row, m_col)) {
						// Draw the can.
						bounds.x += CAN_SHIFT;
						bounds.y += CAN_SHIFT_DBL - ROBBY_SHIFT;
						bounds.width -= CAN_SHIFT_DBL;
						bounds.height -= CAN_SHIFT_DBL;
						int upSize = bounds.height / 3;
						bounds.width >>= 1;
						bounds.x += (bounds.width >> 1);
						// The can "bottom".
						Rectangle bottom = new Rectangle(
								bounds.x,
								bounds.y + bounds.height - upSize,
								bounds.width,
								upSize);
						// The can "body".
						Rectangle body = new Rectangle(
								bounds.x,
								bounds.y + (upSize >> 1),
								bounds.width,
								bounds.height - upSize - 1);
						// The can "top".
						Rectangle top = new Rectangle(
								bounds.x,
								bounds.y,
								bounds.width,
								upSize);
						g.setColor(Color.BLACK);
						g.fillOval(bottom.x, bottom.y, bottom.width, bottom.height);
						g.drawOval(bottom.x, bottom.y, bottom.width, bottom.height);
						g.drawRect(body.x, body.y, body.width, body.height);
						g.fillRect(body.x, body.y, body.width, body.height);
						g.setColor(Color.GRAY);
						g.fillOval(top.x, top.y, top.width, top.height);
						g.setColor(Color.BLACK);
						g.drawOval(top.x, top.y, top.width, top.height);
					}
				} finally {
					g.dispose();
				}
			}
		}

	}
	
	private final RobbyPanel m_main;
	private final JPanel[][] m_playgroundPanels;

	public PlaygroundPanel(RobbyPanel main) {
		super();
		setBorder(BorderFactory.createEmptyBorder());
		m_main = main;
		
		// The playground panels.
		int playgroundSize = main.getPlaygroundSize();
		m_playgroundPanels = new JPanel[playgroundSize][playgroundSize];
		
		GroupLayout playgroundLayout = new GroupLayout(this);
		setLayout(playgroundLayout);
		ParallelGroup playgroundRow = playgroundLayout.createParallelGroup();
		SequentialGroup playgroundColumn = playgroundLayout.createSequentialGroup();
		for (int row = 0; row < playgroundSize; ++row) {
			SequentialGroup playgroundHorizontal = playgroundLayout.createSequentialGroup();
			ParallelGroup playgroundVertical = playgroundLayout.createParallelGroup();
			for (int col = 0; col < playgroundSize; ++col) {
				JPanel field = new Field(row, col);
				m_playgroundPanels[row][col] = field;
				playgroundHorizontal.addComponent(field,
						GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE);
				playgroundVertical.addComponent(field,
						GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE);
			}
			playgroundRow.addGroup(playgroundHorizontal);
			playgroundColumn.addGroup(playgroundVertical);
		}
		playgroundLayout.setHorizontalGroup(playgroundRow);
		playgroundLayout.setVerticalGroup(playgroundColumn);
	}
	
}
