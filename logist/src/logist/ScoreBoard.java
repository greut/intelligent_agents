package logist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static logist.ScoreBoard.Result.*;

/**
 * Utility class to compute the score board of a tournament.
 * <p>
 * The score board is a matrix of games sorted by the rank of the agents. NOTE:
 * Ranks are 1-based !!
 * 
 * 
 * @param <T>
 *            the type of the score values. typically a Double (old platform) or
 *            a Long (new platform).
 */
class ScoreBoard<T extends Comparable<T>> {

	public enum Result {
		WIN, DRAW, LOSE
	}

	private int nextID = 0;
	private Map<String, Integer> nameToID = new HashMap<String, Integer>();
	private List<Agent> idToAgent = new ArrayList<Agent>();
	private List<List<Game<T>>> games = new ArrayList<List<Game<T>>>();

	private boolean validRanks = false;
	private List<Agent> rankToAgent;

	/**
	 * Add the result of a game to the score board.
	 */
	public void addGame(String agent1, String agent2, T score1, T score2) {

		if (agent1.equals(agent2))
			throw new IllegalArgumentException(
					"An agent cannot play against itself !");

		int savedID = nextID;

		Integer id1 = nameToID.get(agent1);
		if (id1 == null) {
			nameToID.put(agent1, id1 = nextID);
			idToAgent.add(new Agent(agent1, nextID++));
		}

		Integer id2 = nameToID.get(agent2);
		if (id2 == null) {
			nameToID.put(agent2, id2 = nextID);
			idToAgent.add(new Agent(agent2, nextID++));
		}

		if (savedID != nextID)
			resize();

		Game<T> result = games.get(id1).get(id2);
		if (result != null) {
			if (!result.score1.equals(score1) || !result.score2.equals(score2)) {
				throw new IllegalStateException(
						"Duplicate game with different scores !");
			} else {
				System.err.println("Warning: received duplicate game");
			}
		} else {
			validRanks = false;
			Game<T> game = new Game<T>(score1, score2);
			games.get(id1).set(id2, game);

			switch (game.result) {
			case WIN:
				idToAgent.get(id1).wins++;
				idToAgent.get(id2).losses++;
				break;
			case DRAW:
				idToAgent.get(id1).draws++;
				idToAgent.get(id2).draws++;
				break;
			case LOSE:
				idToAgent.get(id1).losses++;
				idToAgent.get(id2).wins++;
				break;
			default:
				throw new AssertionError();
			}
		}
	}

	/**
	 * Add the result of a game to the score board.
	 */
	public void addFailedGame(String agent1, String agent2, Result result, String reason) {

		if (agent1.equals(agent2))
			throw new IllegalArgumentException(
					"An agent cannot play against itself !");

		int savedID = nextID;

		Integer id1 = nameToID.get(agent1);
		if (id1 == null) {
			nameToID.put(agent1, id1 = nextID);
			idToAgent.add(new Agent(agent1, nextID++));
		}

		Integer id2 = nameToID.get(agent2);
		if (id2 == null) {
			nameToID.put(agent2, id2 = nextID);
			idToAgent.add(new Agent(agent2, nextID++));
		}

		if (savedID != nextID)
			resize();

		Game<T> game = games.get(id1).get(id2);
		if (game != null) {
			if (!game.result.equals(result)) {
				throw new IllegalStateException(
						"Duplicate game with different result !");
			} else {
				System.err.println("Warning: received duplicate game");
			}
		} else {
			validRanks = false;
			game = new FailedGame<T>(result, reason);
			games.get(id1).set(id2, game);

			switch (game.result) {
			case WIN:
				idToAgent.get(id1).wins++;
				idToAgent.get(id2).losses++;
				break;
			case DRAW:
				idToAgent.get(id1).draws++;
				idToAgent.get(id2).draws++;
				break;
			case LOSE:
				idToAgent.get(id1).losses++;
				idToAgent.get(id2).wins++;
				break;
			default:
				throw new AssertionError();
			}
		}
	}
	
	/** the number of agent on the board */
	public int size() {
		return nextID;
	}

	/** get the agent at a given rank */
	public String getAgent(int rank) {
		sort();
		return rankToAgent.get(rank - 1).name;
	}
	
	/** get the agent at a given rank */
	public String getAgentStats(int rank) {
		sort();
		Agent agent = rankToAgent.get(rank - 1);
		return agent.wins + " - " + agent.draws + " - " + agent.losses;
	}

	/**
	 * get the game that was played by two agents at given ranks. returns null
	 * if the game is missing.
	 */
	public Game<T> getGame(int rank1, int rank2) {
		sort();
		int id1 = rankToAgent.get(rank1 - 1).id;
		int id2 = rankToAgent.get(rank2 - 1).id;
		return games.get(id1).get(id2);
	}

	/**
	 * a game is represented by the scores of both player and (redundantly) by
	 * the result of the game.
	 */
	public static class Game<T extends Comparable<T>> {
		public final T score1, score2;
		public final Result result;

		private Game(T score1, T score2) {
			this.score1 = score1;
			this.score2 = score2;
			int diff = score1.compareTo(score2);
			this.result = (diff > 0) ? WIN : (diff < 0) ? LOSE : DRAW;
		}
		
		private Game(Result result) {
			this.result = result;
			this.score1 = this.score2 = null;
		}
		

		/**
		 * whether this game was a win for the first player (and hence a loss
		 * for the second player)
		 */
		public boolean isWin() {
			return result == WIN;
		}

		/**
		 * whether this game was a loss for the first player (and hence a win
		 * for the second player)
		 */
		public boolean isLoss() {
			return result == LOSE;
		}

		/** whether this game was a draw for both players */
		public boolean isDraw() {
			return result == DRAW;
		}
		
		@Override
		public String toString() {
			return result + " (" + score1 + " : " + score2 + ")";
		}
	}
	
	private static class FailedGame<T extends Comparable<T>> extends Game<T> {
		final String reason;
		
		private FailedGame(Result result, String reason) {
			super(result);
			this.reason = reason;
		}

		@Override
		public String toString() {
			return result + " (" + reason + ")";
		}
	}

	private void sort() {
		if (!validRanks) {
			rankToAgent = new ArrayList<Agent>(idToAgent);
			Collections.sort(rankToAgent);
			validRanks = true;
		}
	}

	private void resize() {
		while (games.size() < nextID)
			games.add(new ArrayList<Game<T>>(nextID));

		for (List<Game<T>> row : games)
			while (row.size() < nextID)
				row.add(null);
	}

	private class Agent implements Comparable<Agent> {
		final String name;
		final int id;
		int wins, draws, losses;

		Agent(String name, int id) {
			this.name = name;
			this.id = id;
		}

		@Override
		public int compareTo(Agent that) {
			int diff = -(this.wins - that.wins);
			if (diff == 0) {
				diff = this.losses - that.losses;
				if (diff == 0) {
					diff = this.draws - that.draws;
				}
			}
			return diff;
		}
	}
	
	private int numGames() {
		int count = 0;
		for (List<Game<T>> row : games)
			for (Game<T> game : row)
				if (game != null)
					count++;
		return count;
	}
	
	public String toString() {
		return "[ScoreBoard, " + idToAgent.size() + " agents, " + numGames() + "games]";
	}
	
	public String toLongString() {
		int N = size();
		String[][] table = new String[N+1][N+2];

		table[0][0] = "Agents";
		table[0][1] = "Win - Draw - Lose";
		
		int maxWidth = table[0][1].length();
		for (int rank = 1; rank <= N; rank++) {
			
			table[0][1+rank] = table[rank][0] = getAgent(rank);
			table[rank][1] = getAgentStats(rank);
			
			if (maxWidth < table[rank][0].length())
				maxWidth = table[rank][0].length();
			if (maxWidth < table[rank][1].length())
				maxWidth = table[rank][1].length();
			
			for (int opponentRank = 1; opponentRank <= N; opponentRank++) {

				Game<T> game = getGame(rank, opponentRank);
				String cell = (game == null) ? "-" : game.toString();
				table[rank][1+opponentRank] = cell;
				
				if (maxWidth < cell.length())
					maxWidth = cell.length();
			}
			
		}
		
		StringBuilder builder = new StringBuilder();
		for (int r = 0; r <= N; r++) {
			for (int c = 0; c <= N+1; c++) {

				if (c > 0)
					builder.append(" | ");
				
				int half2 = maxWidth - table[r][c].length();
				int half1 = half2 / 2;
				half2 -= half1;
				
				while (half1-- > 0)
					builder.append(' ');
				builder.append(table[r][c]);
				while (half2-- > 0)
					builder.append(' ');
			}
			
			builder.append('\n');
		}
		builder.append("\nFirst company results");
		builder.append("\n  # wins   : ");
		builder.append(firstPlayerStats(WIN));
		builder.append("\n  # draws  : ");
		builder.append(firstPlayerStats(DRAW));
		builder.append("\n  # losses : ");
		builder.append(firstPlayerStats(LOSE));
		builder.append('\n');		
		
		return builder.toString();
	}
	
	private int firstPlayerStats(Result result) {
		int count = 0;
		for (List<Game<T>> row : games)
			for (Game<T> game : row)
				if (game != null && game.result == result)
					count++;
		return count;
	}
}
