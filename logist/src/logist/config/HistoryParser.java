package logist.config;

import java.util.ArrayList;
import java.util.List;

import logist.agent.AgentStatistics;

import logist.Measures;

class HistoryParser {

	private List<AgentStatistics> statistics;
	
	List<AgentStatistics> parse(XMLTag historyTag) throws ParserException {

		statistics = new ArrayList<AgentStatistics>();

		parseStatistics(historyTag.getUniqueChild("statistics"));

		return statistics;
	}

	private void parseStatistics(XMLTag statisticsTag) throws ParserException {

		int rank = 0;
		XMLTag tag;
		for (XMLTag statTag : statisticsTag.getAllChildren("stat")) {
			rank++;
			AgentRecord record = new AgentRecord();

			int actualRank = statTag.getAttribute("rank", Integer.class);
			if (rank != actualRank)
				throw new ParserException("Expected rank = " + rank
						+ " but found rank = " + actualRank);

			record.name = statTag.getAttribute("agent", String.class);

			tag = statTag.getUniqueChild("total-tasks");
			record.totalTasks = tag.getAttribute("value", Integer.class);
			tag = statTag.getUniqueChild("total-distance");
			double kilometers = tag.getAttribute("value", Double.class);
			record.totalDistance = Measures.kmToUnits(kilometers);
			tag = statTag.getUniqueChild("total-cost");
			record.totalCost = tag.getAttribute("value", Long.class);
			tag = statTag.getUniqueChild("total-reward");
			record.totalReward = tag.getAttribute("value", Long.class);
			tag = statTag.getUniqueChild("total-profit");
			record.totalProfit = tag.getAttribute("value", Long.class);

			statistics.add(record);
		}
	}

	private static class AgentRecord implements AgentStatistics {
		String name;
		int totalTasks;
		long totalDistance;
		long totalCost;
		long totalReward;
		long totalProfit;

		@Override
		public long getTotalCost() {
			return totalCost;
		}

		@Override
		public double getTotalDistance() {
			return Measures.unitsToKM(totalDistance);
		}

		@Override
		public long getTotalDistanceUnits() {
			return totalDistance;
		}

		@Override
		public long getTotalProfit() {
			return totalProfit;
		}

		@Override
		public long getTotalReward() {
			return totalReward;
		}

		@Override
		public int getTotalTasks() {
			return totalTasks;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public int compareTo(AgentStatistics that) {
			long myProfit = this.getTotalProfit();
			long hisProfit = that.getTotalProfit();

			if (myProfit > hisProfit)
				return -1;
			if (myProfit < hisProfit)
				return 1;
			return 0;
		}
	}
}
