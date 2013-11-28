package logist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import logist.agent.AgentStatistics;

import logist.ScoreBoard.Result;
import logist.config.ParserException;
import logist.config.Parsers;

class Scores {
    private static final Scanner scanner = new Scanner(System.in);

    static final Logger LOG = Logger.getLogger(Scores.class.getName());
    private final File tournamentDir;
    private final Map<String, ScoreBoard<Long>> boards;

    Scores(File tournamentDir) throws ParserException, IOException {
        this.tournamentDir = tournamentDir;

        this.boards = new HashMap<String, ScoreBoard<Long>>();
    }

    void parse() {

        for (File file : tournamentDir.listFiles()) {
            String[] names = file.getName().split("" + Tournament.SEP);

            if (names.length != 3)
                continue;

            if (!names[2].endsWith(".xml")) {
                LOG.warning("Skipping " + file.getName());
                continue;
            }

            LOG.info("Found file " + file.getName());

            ScoreBoard<Long> board = boards.get(names[0]);
            if (board == null)
                boards.put(names[0], board = new ScoreBoard<Long>());

            String agent1 = names[1];
            String agent2 = names[2].substring(0, names[2].length() - 4);

            try {
                read(board, file, agent1, agent2);
            } catch (LogistException lEx) {
                LOG.warning(lEx.getMessage());
            } catch (ParserException pEx) {
                LOG.warning(pEx.getMessage());

                Result result;
                String reason;
                if (agent1.equals("Javier-Tugdual")) {
                    result = Result.LOSE;
                    reason = "crash";
                } else if (agent2.equals("Javier-Tugdual")) {
                    result = Result.WIN;
                    reason = "crash";
                } else if (agent1.equals("Laurent-Alexandre")) {
                    result = Result.LOSE;
                    reason = "timeout";
                } else if (agent2.equals("Laurent-Alexandre")) {
                    result = Result.WIN;
                    reason = "timeout";
                } else {
                    // *
                    while (true) {
                        try {
                            System.out.println("Game " + agent1 + " vs "
                                    + agent2);
                            System.out.print("  result: ");
                            result = Result.valueOf(scanner.next());
                            System.out.print("  result: ");
                            reason = scanner.nextLine();

                            if (!reason.isEmpty()) {
                                break;
                            }
                        } catch (IllegalArgumentException iaEx) {
                            System.out.println("<bad result>");
                        }
                    }
                    // */
                }
                board.addFailedGame(agent1, agent2, result, reason);
            }
        }
    }

    private void read(ScoreBoard<Long> board, File file, String agent1,
            String agent2) throws ParserException {

        List<AgentStatistics> list = Parsers.parseHistory(file);

        if (list.size() != 2)
            throw new LogistException("Expected 2 stat entries but found "
                    + list.size());

        String winner = list.get(0).name();
        String loser = list.get(1).name();
        long winnerScore = list.get(0).getTotalProfit();
        long loserScore = list.get(1).getTotalProfit();

        if (agent1.equals(winner) && agent2.equals(loser)) {
            board.addGame(agent1, agent2, winnerScore, loserScore);

        } else if (agent1.equals(loser) && agent2.equals(winner)) {
            board.addGame(agent1, agent2, loserScore, winnerScore);

        } else {
            throw new LogistException("Agents names don't match");
        }
    }

    void write(String filename) throws IOException {
        File outFile = new File(tournamentDir, filename);
        PrintStream out = new PrintStream(new BufferedOutputStream(
                new FileOutputStream(outFile)));

        boolean first = true;
        List<String> configs = new ArrayList<String>(boards.keySet());
        Collections.sort(configs);

        for (String config : configs) {
            if (first)
                first = false;
            else
                out.println();

            ScoreBoard<Long> board = boards.get(config);
            out.println("### " + config + ".xml ###");
            out.println(board.toLongString());
        }

        out.flush();
        out.close();

        LOG.info("Written " + outFile);
    }
}
