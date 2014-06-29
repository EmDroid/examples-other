package com.styryx.ai.genetic.apps.Robby.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

import com.styryx.Main;
import com.styryx.ai.genetic.apps.Robby.exception.RobbyException;
import com.styryx.ai.genetic.apps.Robby.gui.actions.*;
import com.styryx.ai.genetic.apps.Robby.gui.components.*;
import com.styryx.ai.genetic.apps.Robby.worker.*;
import com.styryx.ai.genetic.selector.RouletteWheelSelector;
import com.styryx.gui.RootPanelHolder;
import com.styryx.gui.components.*;
import com.styryx.util.ResourceBundle;

public class RobbyPanel implements WindowListener {
	
	private static class PanelInitializer extends RootPanelHolder.PanelInitializer {

		protected void setupLookAndFeel() throws Exception {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}

		protected void createMainPanel(JRootPane rootPane, Locale locale) throws Exception {
			new RobbyPanel(rootPane, locale);
		}
		
	}
	
	public static PanelInitializer createPanelInitializer() {
		return new PanelInitializer();
	}

	private class ComboItem {
		private final String m_description;
		private final int m_id;
		public ComboItem(String description, int id) {
			m_description = description;
			m_id = id;
		}
		public String toString() {
			return m_description;
		}
		public int getId() {
			return m_id;
		}
	}

//	private static final ResourceBundle sm_messages = ResourceBundle.getBundle("Robby");

	public static ResourceBundle getMessages(Locale locale) {
		return ResourceBundle.getBundle("Robby", locale);
	}

	public static void logException(Throwable e, String title, Locale locale) {
		/*
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(out));
		try {
			out.flush();
		} catch (IOException e1) {
			//TODO.
		}
		*/
		JOptionPane.showMessageDialog(null,
				getMessages(locale).getString("ExceptionCaught", "Exception caught")
				+ ":\n\n" + e.getLocalizedMessage() + ""/*out.toString()*/,
				title + "!", JOptionPane.ERROR_MESSAGE);
	}

	final static private NumberFormat sm_floatFormatter = NumberFormat.getInstance();
	
	final static private NumberFormat sm_percentFormatter = NumberFormat.getPercentInstance();
	
	private static String createPercentPattern() {
		String pattern = sm_percentFormatter.format(0.01);
		pattern = pattern.replace("1", "#.#");
		return pattern;
	}
	
	final private static String sm_percentPattern = createPercentPattern();
	
	public class World {
		private int m_generation;
		private Playground.Strategy[] m_population;
		public World(int populationSize, Random randomizer, Locale locale) throws RobbyException {
			// Create new initial population.
			m_generation = 0;
			m_population = new Playground.Strategy[populationSize];
			for (int i = 0; i < populationSize; ++i) {
				m_population[i] = Playground.Strategy.newRandomStrategy(randomizer, locale);
			}
		}
		public int getGeneration() {
			return m_generation;
		}
		public void addGeneration(int generation) {
			m_generation += generation;
		}
		public Playground.Strategy[] getPopulation() {
			return m_population;
		}
		public void setPopulation(Playground.Strategy[] population) {
			m_population = population;
		}
	}
	
	World m_world = null;
	
	private static final int PLAYGROUND_SIZE = 10;
	
	private Playground m_playground = null;
	
	private final Random m_randomizer;
	
	private final JRootPane m_rootPane;
	
	private final ActionEvolve m_actionEvolve;
	private final ActionManual m_actionManual;

	private final ActionCreate m_actionCreate;
	private final ActionStart m_actionStart;
	private final ActionStop m_actionStop;

	private final JButton m_create;
	private final JButton m_start;
	private final JButton m_stop;
	
	private final JSpinner m_populationSize;
	private final JSpinner m_sessions;
	private final JSpinner m_actions;
	private final JButton m_newPopulation;
	
	private final JSpinner m_generations;
	private final JSpinner m_mutations;
	private final JComboBox m_selection;
	private final JCheckBox m_crossover;
	private final JSpinner m_threads;
	private final JButton m_evolve;
	private final JProgressBar m_progress;

	private final JTextArea m_evoLog;
	
	private final ChartWindow m_chartWindow;

	private final JComboBox m_chartRange;
	private final Chart m_chart;
	private final JComboBox m_chartRangeBig;
	private final Chart m_chartBig;

	private final JTextField m_score;
	private final JTextArea m_chain;

	private final JComboBox m_manual;
	private final JButton m_manualRefresh;

	private final PlaygroundPanel m_playgroundPanel;
	
	private final JTextField m_testChain;
	private final JTextField m_testStep;
	private final JTextField m_testAction;
	private final JTextField m_testScore;

	private ArrayList<Chart.ValueLine.Value> m_chartFull =
		new ArrayList<Chart.ValueLine.Value>();
	private ArrayList<Chart.ValueLine.Value> m_chartIteration =
		new ArrayList<Chart.ValueLine.Value>();

	public ActionEvolve getActionEvolve() {
		return m_actionEvolve;
	}
	public ActionCreate getActionCreate() {
		return m_actionCreate;
	}
	public ActionStart getActionStart() {
		return m_actionStart;
	}
	public ActionStop getActionStop() {
		return m_actionStop;
	}
	
	public RobbyPanel(JRootPane rootPane, Locale locale) {
		m_randomizer = new Random();
		m_rootPane = rootPane;
		
		Component root = SwingUtilities.getRoot(rootPane);
		if (root instanceof Window) {
			Window mainWin = (Window) root;
			mainWin.addWindowListener(this);
		}
		
		int cpus;
		try {
			cpus = Math.max(1, Runtime.getRuntime().availableProcessors());
		} catch (Throwable e) {
			// Ignore any error.
			cpus = 1;
		}
		ResourceBundle messages = getMessages(locale);
		Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

		// Create the upper panel.
		JPanel mainPanel = new JPanel();
//			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
//			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rootPane.getContentPane().add(mainPanel, BorderLayout.CENTER);
		
			// The population size.
			m_populationSize = new JSpinner(new SpinnerNumberModel(200, 1, 10000, 1));
			m_populationSize.setToolTipText(messages.getString("PopulationSize", "Size of population"));
			
			// The number of sessions.
			m_sessions = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));
			m_sessions.setToolTipText(messages.getString("NumSessions", "Number of sessions"));
			
			// The number of actions per session.
			m_actions = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));
			m_actions.setToolTipText(messages.getString("NumSessionActions", "Number of actions per session"));
			
			// The "New world" button.
			m_newPopulation = new JButton(new ActionNewPopulation(this, locale));
			
			// The world settings panel.
			JPanel worldSettings = new WorldSettings(this, messages);
			
			// The generations count.
			m_generations = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
			m_generations.setToolTipText(messages.getString("NumGenerations", "Number of generations"));
			
			// The mutation probability.
			m_mutations = new JSpinner(new SpinnerNumberModel(0.005, 0.0, 1.0, 0.001));
			m_mutations.setEditor(new JSpinner.NumberEditor(m_mutations, sm_percentPattern));
			m_mutations.setToolTipText(messages.getString("NumMutations", "The probability of random mutation"));
			
			// The selection type.
			m_selection = new JComboBox(new ComboItem[]{
				new ComboItem(messages.getString("Rank"),
						RouletteWheelSelector.SELECTION_RANK_BASED),
				new ComboItem(messages.getString("Fitness"),
						RouletteWheelSelector.SELECTION_FITNESS_BASED),
			});
			m_selection.setToolTipText(
					messages.getString("PopulationSelectionAlgorithm", "The algorithm of population selection"));
			
			// The crossover checkbox.
			m_crossover = new JCheckBox(messages.getString("ApplyCrossover", "apply crossover"), true);
			m_crossover.setToolTipText(messages.getString("ApplyCrossoverHint",
					"Unselect - simple selection of best candidates"));
			
			// The threads count.
			m_threads = new JSpinner(new SpinnerNumberModel(cpus, 1, cpus * 2, 1));
			m_threads.setToolTipText(messages.getString("NumThreadsHint", "Number of threads for the computation"));
			
			// The "Evolve" button.
			m_evolve = new JButton(m_actionEvolve = new ActionEvolve(this, locale));
			
			// The evolution progress.
			m_progress = new JProgressBar();
			m_progress.setStringPainted(true);
			m_progress.setToolTipText(messages.getString("EvolutionProgress", "Evolution progress"));
			
			// The evolution settings panel.
			JPanel evoSettings = new EvolutionSettings(this, messages);
			
			// The evolution log window.
			m_evoLog = new JTextArea();
			m_evoLog.setEditable(false);
			m_evoLog.setLineWrap(false);
			m_evoLog.setFont(UIManager.getFont("Text.font"));
			m_evoLog.setCursor(textCursor);
			JScrollPane evoLogScroll = new JScrollPane(m_evoLog);
			evoLogScroll.setBorder(BorderFactory.createLoweredBevelBorder());
			evoLogScroll.setMinimumSize(new Dimension(250, 100));
			evoLogScroll.setPreferredSize(new Dimension(250, 100));
			
			// The evolution chart range.
			m_chartRange = new JComboBox();
			m_chartRangeBig = new JComboBox();
			
			// The bigger evolution chart.
			m_chartBig = new Chart();
			m_chartBig.setToolTipText(messages.getString("ChartClose", "Close the chart window"));
			
			m_chartWindow = new ChartWindow(this, locale, m_chartRangeBig, m_chartBig, messages);
			
			// The evolution chart.
			m_chart = new Chart();
			m_chart.setPreferredSize(new Dimension(250, 150));
			m_chart.setToolTipText(messages.getString("ChartNewWindow", "View chart in new window"));
			m_chart.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseClicked(MouseEvent e) {
					if (!m_chartWindow.isVisible()) m_chartWindow.setVisible(true);
				}
			});
			
			JPanel chartPanel = new ChartPanel(this, locale, m_chartRange, m_chart, messages);
			
			Font resultFont;
			
			// The best score.
			m_score = new JTextField(5);
			m_score.setEditable(false);
			m_score.setCursor(textCursor);
			m_score.setBorder(BorderFactory.createLoweredBevelBorder());
			resultFont = m_score.getFont();
			resultFont = resultFont.deriveFont(Font.BOLD, resultFont.getSize() + 2);
			m_score.setFont(resultFont);
			m_score.setHorizontalAlignment(JTextField.RIGHT);
			m_score.setToolTipText(messages.getString("ScoreArchieved", "The score archieved"));
			
			// The manual solution.
			m_manual = new JComboBox(new ComboItem[] {
				new ComboItem(messages.getString("EmptyGoEast", "Empty-Go-East"),
						Playground.Strategy.EMPTY_GO_EAST),
				new ComboItem(messages.getString("EmptyGoSouth", "Empty-Go-South"),
						Playground.Strategy.EMPTY_GO_SOUTH),
				new ComboItem(messages.getString("EmptyGoRandom", "Empty-Go-Random"),
						Playground.Strategy.EMPTY_GO_RANDOM),
			});
			m_manual.setToolTipText(messages.getString("SelectManualSolution", "Select manual solution"));
			m_manual.setAction(m_actionManual = new ActionManual(this, locale));
			
			// The manual refresh button.
			m_manualRefresh = new JButton(m_actionManual);
			
			// The best strategy chain.
			m_chain = new JTextArea(3, 60);
			m_chain.setBorder(BorderFactory.createLoweredBevelBorder());
			m_chain.setEditable(false);
			m_chain.setLineWrap(true);
			m_chain.setFont(resultFont);
			m_chain.setCursor(textCursor);
			m_chain.setBackground(m_score.getBackground());
			m_chain.setToolTipText(messages.getString("StrategyChain", "The strategy chain"));
			
			// The evolution results panel.
			JPanel results = new EvolutionResults(this, messages);
			
			// The playground panel.
			m_playgroundPanel = new PlaygroundPanel(this);
			
			// The playground buttons.
			m_create = new JButton(m_actionCreate = new ActionCreate(this, locale));
			m_start = new JButton(m_actionStart = new ActionStart(this, locale));
			m_stop = new JButton(m_actionStop = new ActionStop(this, locale));
			
			// The tested strategy chain.
			m_testChain = new JTextField(10);
			m_testChain.setToolTipText(messages.getString("TestedStrategyChain", "Tested strategy chain"));
			
			// The step counter.
			m_testStep = new JTextField("", 5);
			m_testStep.setEditable(false);
			m_testStep.setBorder(BorderFactory.createLoweredBevelBorder());
			m_testStep.setFont(resultFont);
			m_testStep.setToolTipText(messages.getString("StepCounter", "The step counter"));
			
			// The test action.
			m_testAction = new JTextField("", 20);
			m_testAction.setEditable(false);
			m_testAction.setBorder(BorderFactory.createLoweredBevelBorder());
			m_testAction.setFont(resultFont);
			m_testAction.setToolTipText(messages.getString("ActionTaken", "The action taken"));
			
			// The test score.
			m_testScore = new JTextField("", 5);
			m_testScore.setEditable(false);
			m_testScore.setBorder(BorderFactory.createLoweredBevelBorder());
			m_testScore.setFont(resultFont);
			m_testScore.setToolTipText(messages.getString("ScoreArchieved", "The score archieved"));
			
			// The testing panel.
			TestingPanel testingPanel = new TestingPanel(this, messages);
			
			// The author's link.
			Hyperlink author = Main.getAuthorLink(locale);
			
			// The main panel layout.
			GroupLayout layout = new GroupLayout(mainPanel);
			mainPanel.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
						.addComponent(worldSettings)
						.addComponent(evoSettings)
					)
					.addComponent(evoLogScroll)
					.addComponent(chartPanel)
					.addComponent(results)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(testingPanel)
					.addComponent(author, Alignment.TRAILING)
				)
			);
			layout.setVerticalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
						.addComponent(worldSettings,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE,
								Integer.MAX_VALUE)
						.addComponent(evoSettings,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE,
								Integer.MAX_VALUE)
					)
					.addComponent(evoLogScroll)
					.addComponent(chartPanel)
					.addComponent(results)
				)
				.addGroup(layout.createSequentialGroup()
					.addComponent(testingPanel)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
						GroupLayout.DEFAULT_SIZE,
						Integer.MAX_VALUE)
					.addComponent(author)
				)
			);
					
		//TODO Create the lower panel.
			
		// Set the default button to Evolve.
		rootPane.setDefaultButton(m_evolve);
	}
	
	public Playground getPlayground() {
		return m_playground;
	}

	public int getPlaygroundSize() {
		return PLAYGROUND_SIZE;
	}

	public void createNewWorld(Locale locale) throws RobbyException {
		m_world = new World((Integer)m_populationSize.getValue(), m_randomizer, locale);
		m_chartFull.clear();
		m_chart.clear();
		m_chartBig.clear();
	}

	public JButton getEvolveButton() {
		return m_evolve;
	}

	public void setDefaultButton(JButton evolveButton) {
		m_rootPane.setDefaultButton(evolveButton);
	}

	public JTextArea getEvolutionLog() {
		return m_evoLog;
	}

	public void ensureWorldCreated() {
		// If world not created yet, create it now.
		if (null == m_world) {
			m_newPopulation.doClick();
		}
	}

	public int getGenerations() {
		return (Integer)m_generations.getValue();
	}

	public World getWorld() {
		return m_world;
	}

	public int getSessions() {
		return (Integer)m_sessions.getValue();
	}

	public int getActionsPerSession() {
		return (Integer)m_actions.getValue();
	}

	public double getMutations() {
		return (Double)m_mutations.getValue();
	}

	public void resetProgress(int generations) {
		m_progress.setValue(0);
		m_progress.setMaximum(generations);
	}

	public void setProgress(int generation) {
		m_progress.setValue(generation);
	}

	public String formatScore(double score) {
		return sm_floatFormatter.format(score);
	}

	public JButton getCreateButton() {
		return m_create;
	}

	public int getSelectionType() {
		return ((ComboItem)m_selection.getSelectedItem()).getId();
	}

	public boolean getCrossover() {
		return m_crossover.isSelected();
	}

	public int getManualStrategyId() {
		return ((ComboItem)m_manual.getSelectedItem()).getId();
	}

	public void setBestStrategy(double score, String chain) {
		m_score.setText(formatScore(score));
		m_chain.setText(chain);
		m_testChain.setText(chain);
	}

	public void createNewPlayground(Locale locale) {
		m_playground = new Playground(PLAYGROUND_SIZE, m_randomizer, locale);
		m_playgroundPanel.repaint();
	}

	public JButton getStartButton() {
		return m_start;
	}

	public JButton getStopButton() {
		return m_stop;
	}

	public void playgroundStep(int step, Locale locale) throws RobbyException {
		Playground.Action action = m_playground.step();
		m_playgroundPanel.repaint();
		m_testStep.setText(" " + step);
		m_testAction.setText(" " + getMessages(locale).getString(action.getKey(), action.getDefaultDescription()));
		m_testScore.setText(" " + m_playground.getScore());
	}

	public void setPlaygroundStrategy() throws RobbyException {
		m_playground.setStrategy(m_testChain.getText());
	}

	public JTextField getTestChain() {
		return m_testChain;
	}

	public JTextField getTestStep() {
		return m_testStep;
	}

	public JTextField getTestAction() {
		return m_testAction;
	}

	public JTextField getTestScore() {
		return m_testScore;
	}

	public PlaygroundPanel getPlaygroundPanel() {
		return m_playgroundPanel;
	}

	public JSpinner getPopulationComponent() {
		return m_populationSize;
	}

	public JSpinner getSessionsComponent() {
		return m_sessions;
	}

	public JSpinner getActionsPerSessionComponent() {
		return m_actions;
	}

	public JButton getCreateWorldButton() {
		return m_newPopulation;
	}

	public JSpinner getGenerationsComponent() {
		return m_generations;
	}

	public JSpinner getMutationsComponent() {
		return m_mutations;
	}

	public JComboBox getSelectionComponent() {
		return m_selection;
	}

	public JCheckBox getCrossoverComponent() {
		return m_crossover;
	}

	public JProgressBar getProgressComponent() {
		return m_progress;
	}

	public JTextField getScoreComponent() {
		return m_score;
	}

	public JComboBox getManualStrategyComponent() {
		return m_manual;
	}

	public JButton getManualRefreshButton() {
		return m_manualRefresh;
	}

	public JTextArea getChainComponent() {
		return m_chain;
	}

	public void clearChart() {
		m_chartIteration .clear();
		if (0 == m_chartRange.getSelectedIndex()) {
			m_chart.clear();
		}
		if (0 == m_chartRangeBig.getSelectedIndex()) {
			m_chartBig.clear();
		}
	}

	public void chartAdd(int gen, double fitness) {
		Chart.ValueLine.Value value = new Chart.ValueLine.Value(gen, fitness);
		m_chartFull.add(value);
		m_chartIteration.add(value);
		m_chart.addPoint(value);
		m_chartBig.addPoint(value);
	}

	public void windowClosing(WindowEvent evt) {
		m_actionEvolve.cancel();
		m_chartWindow.dispose();
	}

	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	public ArrayList<Chart.ValueLine.Value> getChartIteration() {
		return m_chartIteration;
	}

	public ArrayList<Chart.ValueLine.Value> getChartFull() {
		return m_chartFull;
	}

	public Window getRootWindow() {
		return (Window) m_rootPane.getTopLevelAncestor();
	}

	public JComboBox getChartRange() {
		return m_chartRange;
	}

	public JComboBox getChartRangeBig() {
		return m_chartRangeBig;
	}

	public int getThreads() {
		return (Integer)m_threads.getValue();
	}

	public JSpinner getThreadsComponent() {
		return m_threads;
	}

}
