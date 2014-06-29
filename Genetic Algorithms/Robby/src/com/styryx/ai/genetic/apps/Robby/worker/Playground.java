package com.styryx.ai.genetic.apps.Robby.worker;

import java.util.*;
import java.util.concurrent.Future;

import com.styryx.ai.genetic.Genom;
import com.styryx.ai.genetic.apps.Robby.exception.RobbyException;
import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;

public class Playground {
	
	public static class Position {
		public int row;
		public int col;
		public Position(int row, int col) {
			this.row = row;
			this.col = col;
		}
	}
	
	public static class Action {
		
		static final private HashMap<Integer, Action> sm_actionsIdent = new HashMap<Integer, Action>();
		static final private HashMap<Character, Action> sm_actionsChar = new HashMap<Character, Action>();
		
		private static final int MOVE_NORTH_ID = 0;
		private static final int MOVE_SOUTH_ID = 1;
		private static final int MOVE_EAST_ID = 2;
		private static final int MOVE_WEST_ID = 3;
		private static final int MOVE_RANDOM_ID = 4;
		private static final int STAY_PUT_ID = 5;
		private static final int PICK_UP_ID = 6;
		private static final int ACTIONS_COUNT = 7;
		
		static public final Action MOVE_NORTH = new Action(MOVE_NORTH_ID, "MoveNorth", "Move north");
		static public final Action MOVE_SOUTH = new Action(MOVE_SOUTH_ID, "MoveSouth", "Move south");
		static public final Action MOVE_EAST = new Action(MOVE_EAST_ID, "MoveEast", "Move east");
		static public final Action MOVE_WEST = new Action(MOVE_WEST_ID, "MoveWest", "Move west");
		static public final Action MOVE_RANDOM = new Action(MOVE_RANDOM_ID, "MoveRandom", "Move random");
		static public final Action STAY_PUT = new Action(STAY_PUT_ID, "StayPut", "Stay put");
		static public final Action PICK_UP = new Action(PICK_UP_ID, "PickUp", "Pick up can");
		
		final private int m_ident;
		final private String m_key;
		final private String m_defaultDesc;
		final private char m_char;
		
		private Action(int ident, String key, String defaultDesc) {
			m_ident = ident;
			m_key = key;
			m_defaultDesc = defaultDesc;
			m_char = Character.forDigit(ident, 10);
			sm_actionsIdent.put(m_ident, this);
			sm_actionsChar.put(m_char, this);
		}
		
		public String getKey() {
			return m_key;
		}
		
		public String getDefaultDescription() {
			return m_defaultDesc;
		}
		
		public static Action lookup(char c, Locale locale) throws RobbyException {
			Action action = sm_actionsChar.get(c);
			if (null == action) {
				throw new RobbyException(
						RobbyPanel.getMessages(locale).getString("InvalidActionChar", "Invalid action character")
						+ ": " + c);
			}
			return action;
		}

		public static Action lookup(int ident, Locale locale) throws RobbyException {
			Action action = sm_actionsIdent.get(ident);
			if (null == action) {
				throw new RobbyException(
						RobbyPanel.getMessages(locale).getString("InvalidActionId", "Invalid action identifier")
						+ ": " + ident);
			}
			return action;
		}
		
		public int value() {
			return m_ident;
		}
		
		public char charId() {
			return m_char;
		}

		public Position getTransition(Position pos, Random randomizer) {
			int ident = (m_ident != MOVE_RANDOM_ID) ? m_ident : randomizer.nextInt(MOVE_RANDOM_ID);
			switch (ident) {
			case MOVE_NORTH_ID:
				return new Position(pos.row - 1, pos.col);
			case MOVE_SOUTH_ID:
				return new Position(pos.row + 1, pos.col);
			case MOVE_WEST_ID:
				return new Position(pos.row, pos.col - 1);
			case MOVE_EAST_ID:
				return new Position(pos.row, pos.col + 1);
			}
			return pos;
		}

	}
	
	public static class Strategy extends Genom {

		final static private int STRATEGY_SIZE = 243;  // 3 x 3 x 3 x 3 x 3

		public static final int EMPTY_GO_EAST = 0;
		public static final int EMPTY_GO_SOUTH = 1;
		public static final int EMPTY_GO_RANDOM = 2;

		private static class StateParser {
			private int m_status;
			private int m_shift;
			public StateParser(int status) {
				m_status = status;
				m_shift = STRATEGY_SIZE / 3;
			}
			public int nextState() {
				int state = m_status / m_shift;
				m_status -= state * m_shift;
				m_shift /= 3;
				return state;
			}
		}
		
		protected static Action optimalStrategyNormal(int north, int south, int west, int east) {
			// Prefer going to south/east, unless it is a wall.
			if (SITE_WALL != north) {
				return Action.MOVE_NORTH;
			} else if (SITE_WALL != west) {
				return Action.MOVE_WEST;
			} else if (SITE_WALL != south) {
				return Action.MOVE_SOUTH;
			} else if (SITE_WALL != east) {
				return Action.MOVE_EAST;
			} else {
				// Walls everywhere! (only possible for playground 1x1)
				return Action.STAY_PUT;
			}
		}
		
//		protected static Action optimalStrategyReturn(int north, int south, int west, int east) {
//			// Prefer going to south/east, unless it is a wall.
//			if ((SITE_WALL != north) && (SITE_WALL != south) && (SITE_WALL != west) && (SITE_WALL != east)) {
//				// All empty - prefer north (or do a random move?).
//				return Action.MOVE_RANDOM;
//			} else if (SITE_WALL == south) {
//				return Action.MOVE_NORTH;
//			} else if (SITE_WALL == east) {
//				return Action.MOVE_WEST;
//			} else if (SITE_WALL == north) {
//				return Action.MOVE_SOUTH;
//			} else if (SITE_WALL == west) {
//				return Action.MOVE_EAST;
//			} else {
//				// Walls everywhere! (only possible for playground 1x1)
//				return Action.STAY_PUT;
//			}
//		}
//		
		protected static Action optimalStrategyEmptyGoRandom(int north, int south, int west, int east) {
			// Prefer going to south/east, unless it is a wall.
			if ((SITE_WALL != north) && (SITE_WALL != south) && (SITE_WALL != west) && (SITE_WALL != east)) {
				// All empty - do a random move.
				return Action.MOVE_RANDOM;
			} else if (SITE_WALL == east) {
				if ((SITE_WALL == south) && (SITE_WALL != north)) {
					return Action.MOVE_NORTH;
				} else if (SITE_WALL != west) {
					return Action.MOVE_WEST;
				} else if (SITE_WALL != north) {
					return Action.MOVE_NORTH;
				} else if (SITE_WALL != south) {
					return Action.MOVE_SOUTH;
				} else {
					// Walls everywhere! (only possible for playground 1x1)
					return Action.STAY_PUT;
				}
			} else if (SITE_WALL == south) {
				if ((SITE_WALL == west) || (SITE_WALL == north)) {
					return Action.MOVE_EAST;
				} else {
					return Action.MOVE_NORTH;
				}
			} else if (SITE_WALL == west) {
				return Action.MOVE_EAST;
			} else {
				return Action.MOVE_SOUTH;
			}
		}
		
		protected static Action optimalStrategyEmptyGoSouth(int north, int south, int west, int east) {
			// Prefer going to south/east, unless it is a wall.
			if ((SITE_WALL != north) && (SITE_WALL != south) && (SITE_WALL != west) && (SITE_WALL != east)) {
				// All empty - prefer south.
				return Action.MOVE_SOUTH;
			} else if (SITE_WALL == east) {
				if (SITE_WALL != north) {
					return Action.MOVE_NORTH;
				} else if (SITE_WALL != west) {
					return Action.MOVE_WEST;
				} else if (SITE_WALL != south) {
					return Action.MOVE_SOUTH;
				} else {
					// Walls everywhere! (only possible for playground 1x1)
					return Action.STAY_PUT;
				}
			} else if (SITE_WALL == west) {
				if (SITE_WALL != south) {
					return Action.MOVE_SOUTH;
				} else {
					return Action.MOVE_EAST;
				}
			} else if (SITE_WALL == south) {
//				if (SITE_WALL != north) {
//					return Action.MOVE_NORTH;
//				} else {
					return Action.MOVE_EAST;
//				}
			} else {
				return Action.MOVE_SOUTH;
			}
		}
		
		protected static Action optimalStrategyEmptyGoEast(int north, int south, int west, int east) {
			// Prefer going to south/east, unless it is a wall.
			if ((SITE_WALL != north) && (SITE_WALL != south) && (SITE_WALL != west) && (SITE_WALL != east)) {
				// All empty - prefer east.
				return Action.MOVE_EAST;
			} else if (SITE_WALL == south) {
				if (SITE_WALL != west) {
					return Action.MOVE_WEST;
				} else if (SITE_WALL != north) {
					return Action.MOVE_NORTH;
				} else if (SITE_WALL != east) {
					return Action.MOVE_EAST;
				} else {
					// Walls everywhere! (only possible for playground 1x1)
					return Action.STAY_PUT;
				}
			} else if (SITE_WALL == north) {
				if (SITE_WALL != east) {
					return Action.MOVE_EAST;
				} else {
					return Action.MOVE_SOUTH;
				}
			} else if (SITE_WALL == east) {
//				if (SITE_WALL != west) {
//					return Action.MOVE_WEST;
//				} else {
					return Action.MOVE_SOUTH;
//				}
			} else {
				return Action.MOVE_EAST;
			}
		}
		
		public static Strategy getOptimal(int id, Locale locale) throws RobbyException {
			Action[] actions = new Action[STRATEGY_SIZE];
			// Set actions for current site.
			for (int i = 0; i < STRATEGY_SIZE; ++i) {
				StateParser parser = new StateParser(i);
				int north = parser.nextState();
				int south = parser.nextState();
				int east = parser.nextState();
				int west = parser.nextState();
				int current = parser.nextState();
				// First, check if there is a can on current site.
				switch (current) {
				case SITE_CAN:
					actions[i] = Action.PICK_UP;
					break;
				case SITE_EMPTY:
					// Prefer going to north, then west - if there is a can, go there.
					// It's because we are beginning in the upper left corner, so for best
					// Robby performance it is best to favor returns.
					if (SITE_CAN == west) {
						// Go there.
						actions[i] = Action.MOVE_WEST;
					} else {
						if (SITE_CAN == north) {
							// Go there.
							actions[i] = Action.MOVE_NORTH;
						} else {
							if (SITE_CAN == south) {
								// Go there.
								actions[i] = Action.MOVE_SOUTH;
							} else {
								if (SITE_CAN == east) {
									// Go there.
									actions[i] = Action.MOVE_EAST;
								} else {
									// No can in neighborhood.
									Action action;
									switch (id) {
									case EMPTY_GO_RANDOM:
										action = optimalStrategyEmptyGoRandom(north, south, west, east);
										break;
									case EMPTY_GO_SOUTH:
										action = optimalStrategyEmptyGoSouth(north, south, west, east);
										break;
									case EMPTY_GO_EAST:
									default:
										action = optimalStrategyEmptyGoEast(north, south, west, east);
										break;
									}
									if (null == action) {
										throw new RobbyException(
												RobbyPanel.getMessages(locale).getString("StateNotProcessed",
														"State not processed")
												+ ": " + i);
									}
									actions[i] = action;
								}
							}
						}
					}
					break;
				default: // current
					// Anyway, should not ever happen.
					actions[i] = Action.STAY_PUT;
					break;
				}
			}
			return new Strategy(actions, null, locale);
		}

		public static Strategy newRandomStrategy(Random randomizer, Locale locale) throws RobbyException {
			Action[] actions = new Action[STRATEGY_SIZE];
			StringBuffer str = new StringBuffer(STRATEGY_SIZE);
			for (int i = 0; i < STRATEGY_SIZE; ++i) {
				Action action = Action.lookup(randomizer.nextInt(Action.ACTIONS_COUNT), locale);
				actions[i] = action;
				str.append(action);
			}
			return new Strategy(actions, str.toString(), locale);
		}
		
		private Action[] m_actions;
		private String m_chain;

		private Strategy(Action[] actions, String chain, Locale locale) throws RobbyException {
			if (null == actions) {
				throw new NullPointerException(
						RobbyPanel.getMessages(locale).getString("StrategyActionsEmpty", "Strategy actions cannot be empty!"));
			}
			if (actions.length != STRATEGY_SIZE) {
				throw new RobbyException(new Formatter().format(
						RobbyPanel.getMessages(locale).getString("InvalidStrategyLength",
								"Invalid length of strategy actions: %d, expected: %d"),
						actions.length, STRATEGY_SIZE).out().toString());
			}
			m_actions = actions;
			m_chain = chain;
		}

		public String getString() {
			if (null == m_chain) {
				StringBuffer chain = new StringBuffer(m_actions.length);
				for (int i = 0; i < m_actions.length; ++i) {
					chain.append(m_actions[i].charId());
				}
				m_chain = chain.toString();
			}
			return m_chain;
		}

		public Action[] getActions() {
			return m_actions;
		}

		public static Strategy[] crossover(Strategy parent1, Strategy parent2,
				double mutationProbability, Random randomizer, Locale locale) throws RobbyException {
			int cross = randomizer.nextInt(STRATEGY_SIZE);
			Action[] actions1 = parent1.getActions();
			Action[] actions2 = parent2.getActions();
			Action[] child1 = new Action[STRATEGY_SIZE];
			Action[] child2 = new Action[STRATEGY_SIZE];
			for (int i = 0; i < cross; ++i) {
				child1[i] = mutate(actions1[i], mutationProbability, randomizer, locale);
				child2[i] = mutate(actions2[i], mutationProbability, randomizer, locale);
			}
			for (int i = cross; i < STRATEGY_SIZE; ++i) {
				child2[i] = mutate(actions1[i], mutationProbability, randomizer, locale);
				child1[i] = mutate(actions2[i], mutationProbability, randomizer, locale);
			}
			return new Strategy[]{new Strategy(child1, null, locale), new Strategy(child2, null, locale)};
		}

		private static Action mutate(Action action, double mutationProbability, Random randomizer, Locale locale) throws RobbyException {
			if (randomizer.nextDouble() < mutationProbability) {
				return Action.lookup(randomizer.nextInt(Action.ACTIONS_COUNT), locale);
			} else {
				return action;
			}
		}

		public static Strategy mutate(Strategy strategy, double mutationProbability, Random randomizer, Locale locale) throws RobbyException {
			Action[] original = strategy.getActions();
			Action[] mutated = new Action[original.length];
			for (int i = 0; i < original.length; ++i) {
				mutated[i] = mutate(original[i], mutationProbability, randomizer, locale);
			}
			return new Strategy(mutated, null, locale);
		}

	}
	
	private static final int SITE_EMPTY = 0;
	private static final int SITE_CAN = 1;
	private static final int SITE_WALL = 2;

	private static final int REWARD_CAN = 10;
	private static final int REWARD_NO_CAN = -1;
	private static final int REWARD_WALL = -5;
	
	final private int m_size;
	private final Random m_randomizer;
	private final Locale m_locale;
	final private boolean[][] m_playground;
	final private Position m_position;
	
	private Strategy m_strategy;
	
	private int m_score;

	public Playground(int size, Random randomizer, Locale locale) {
		m_size = size;
		m_randomizer = randomizer;
		m_locale = locale;
		m_position = new Position(0, 0);
		m_playground = new boolean[size][size];
		int fullSize = size * size;
		// Get random count of cans between 50-75% of the size.
		int cans = fullSize >> 1;
//		cans +=
			randomizer.nextInt(fullSize >> 2);
		for (int i = 0; i < cans; ++i) {
			// Get random can position.
			int canRow, canCol;
			do {
				canRow = randomizer.nextInt(size);
				canCol = randomizer.nextInt(size);
			} while(m_playground[canRow][canCol]);
			m_playground[canRow][canCol] = true;
		}
		m_score = 0;
		m_strategy = null;
	}
	
	public void setStrategy(String strategy) throws RobbyException {
		int length = strategy.length();
		Action[] actions = new Action[length];
		for (int i = 0; i < length; ++i) {
			actions[i] = Action.lookup(strategy.charAt(i), m_locale);
		}
		setStrategy(new Strategy(actions, strategy, m_locale));
	}
	
	public void setStrategy(Strategy strategy) {
		m_strategy = strategy;
	}

	public Strategy getStrategy() throws RobbyException {
		if (null == m_strategy) {
			// Create new random strategy.
			m_strategy = Strategy.newRandomStrategy(m_randomizer, m_locale);
		}
		return m_strategy;
	}

	public String getStrategyChain() throws RobbyException {
		return getStrategy().getString();
	}

	public boolean hasCan(int row, int col) {
		return m_playground[row][col];
	}

	public Position getPosition() {
		return m_position;
	}
	
	public int getScore() {
		return m_score;
	}
	
	/**
	 * Do single step.
	 * 
	 * @return
	 * The action taken.
	 * 
	 * @throws RobbyException 
	 */
	public Action step() throws RobbyException {
		Action[] strategy = getStrategy().getActions();
		// Get the state on current position.
		int state = getState();
		// Get the action for that state.
		Action action = strategy[state];
		// Do the action.
		switch (action.value()) {
		case Action.MOVE_NORTH_ID:
		case Action.MOVE_SOUTH_ID:
		case Action.MOVE_EAST_ID:
		case Action.MOVE_WEST_ID:
		case Action.MOVE_RANDOM_ID:
			Position transition = action.getTransition(m_position, m_randomizer);
			switch (checkSite(transition)) {
			case SITE_WALL:
				m_score += REWARD_WALL;
				break;
			default:
				m_position.row = transition.row;
				m_position.col = transition.col;
				break;
			}
			break;
		case Action.STAY_PUT_ID:
			// Nothing to be done.
			break;
		case Action.PICK_UP_ID:
			if (m_playground[m_position.row][m_position.col]) {
				// No more can on that site.
				m_playground[m_position.row][m_position.col] = false;
				m_score += REWARD_CAN;
			} else {
				m_score += REWARD_NO_CAN;
			}
			break;
		default:
			throw new RobbyException(
					RobbyPanel.getMessages(m_locale).getString("InvalidAction", "Invalid action")
					+ ": "+ action.toString());
		}
		// Return the action taken.
		return action;
	}

	private int getState() {
		int shift = 1;
		int state = 0;
		// The positions are not random - they are according the assignment (from less significant).
		// Check current site.
		state += shift * checkSite(m_position);
		// Check size on west.
		state += (shift *= 3) * checkSite(Action.MOVE_WEST.getTransition(m_position, null));
		// Check size on east.
		state += (shift *= 3) * checkSite(Action.MOVE_EAST.getTransition(m_position, null));
		// Check size on south.
		state += (shift *= 3) * checkSite(Action.MOVE_SOUTH.getTransition(m_position, null));
		// Check size on north.
		state += (shift *= 3) * checkSite(Action.MOVE_NORTH.getTransition(m_position, null));
		return state;
	}

	private int checkSite(Position pos) {
		if ((pos.row < 0) || (pos.col < 0) || (pos.row >= m_size) || (pos.col >= m_size)) {
			return SITE_WALL;
		}
		if (m_playground[pos.row][pos.col]) {
			return SITE_CAN;
		}
		return SITE_EMPTY;
	}

	public static double processStrategy(Future<Boolean> runner, Random randomizer, Strategy strategy,
			int size, int sessions, int steps, Locale locale) throws RobbyException {
		// Test the strategy.
		int score = 0;
		for (int ses = 0; ses < sessions; ++ses) {
			// Check for cancel request.
			if ((null != runner) && runner.isCancelled()) {
				return Double.NaN;
			}
			// Create new session (playground).
			Playground playground = new Playground(size, randomizer, locale);
			playground.setStrategy(strategy);
			for (int step = 0; step < steps; ++step) {
				playground.step();
			}
			score += playground.getScore();
		}
		// Compute average score from the sessions.
		return score / (double)sessions;
	}

}
