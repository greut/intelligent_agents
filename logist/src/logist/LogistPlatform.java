package logist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import logist.LogistSettings.FileKey;
import logist.agent.AgentStatistics;
import logist.config.ParserException;
import logist.config.Parsers;
import logist.simulation.Manager;
import logist.topology.Topology;

/**
 * The entry point of the LogistPlatform.
 * <p>
 * The first argument to the <tt>main</tt> method is the configuration file. The
 * remaining arguments are the names of the participating agents. Each agent
 * must have an entry in the <tt>agents.xml</tt> configuration file specifying
 * the class and the class-path of its behavior class.
 * <p>
 * An output file for the history can be specified by the <tt>-o</tt> option.
 *
 * <pre>
 * Usage:
 *   create tournament
 *     java -jar logist.jar -new 'tournament_name' ['agent_directory']
 *   run tournament
 *     java -jar logist.jar -run 'tournament_name' ['template (directory or file)']
 *   simulate game
 *     java -jar logist.jar [-o 'history_file.xml] [-a 'agents_file.xml] 'template_file' agent1 [agent2] [...]
 * </pre>
 *
 * @author Robin Steiger
 */
public class LogistPlatform {

    private static LogistSettings settings;
    private static Topology topology;
    private static Logger LOG = Logger.getLogger(LogistPlatform.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            usage();
            System.exit(-1);
        }

        String command = args[0];

        // create tournament
        if (command.equals("-new")) {
            String agentDir = (2 < args.length) ? args[2] : "agents";
            createTournament(new File("tournament", args[1]),
                    new File(agentDir));
            return;
        }
        // run tournament
        else if (command.equals("-run")) {
            String templatesDir = (2 < args.length) ? args[2] : "templates";
            runTournament(new File("tournament", args[1]), new File(
                    templatesDir));
            return;
        }
        // tournament scores
        else if (command.equals("-score")) {
            String resultFilename = (2 < args.length) ? args[2] : "results.txt";
            scoreTournament(new File("tournament", args[1]), resultFilename);
            return;
        }
        // bad command
//		else if (command.startsWith("-")) {
//			System.err.println("Unrecognized command: " + command);
//			usage();
//			System.exit(-1);
//		}

        // parse options
        int ptr = 0;
        String historyFile = null;
        String agentsFile = null;
        while (ptr < args.length && args[ptr].startsWith("-")) {
            String arg = args[ptr++];

            if (arg.equals("-o")) {
                if (ptr < args.length)
                    historyFile = args[ptr++];
                else
                    System.err.println("-o requires a filename");
            } else if (arg.equals("-a")) {
                if (ptr < args.length)
                    agentsFile = args[ptr++];
                else
                    System.err.println("-a requires a filename");
            } else {
                // bad command
                System.err.println("Unrecognized command: " + command);
                usage();
                System.exit(-1);
            }
        }

        if (ptr + 1 >= args.length) {
            usage();
            System.exit(-1);
        }

        try {
//			System.out.println("Reading configuration " + args[ptr]);
//			System.out.println("Local path : " + new File(".").getAbsolutePath());

            String[] names = Arrays.copyOfRange(args, ptr + 1, args.length);
            Manager sim = Parsers.parseConfiguration(args[ptr], agentsFile,
                    names);

            topology = sim.getTopology();
            settings = sim.getSettings();
            settings.set(FileKey.CONFIGURATION, new File(args[ptr]));

            if (historyFile != null)
                settings.set(FileKey.HISTORY, new File(historyFile));
            else
                historyFile = settings.get(FileKey.HISTORY).getName();

            sim.run();
            // TimeoutGuard.terminate();
            List<AgentStatistics> stats = Parsers.parseHistory(new File(historyFile));
            for (AgentStatistics stat : stats) {
                LOG.info(stat.name() + " + " + stat.getTotalTasks() + ": € " + stat.getTotalProfit());
            }
            System.exit(0);
        } catch (ParserException pEx) {
            pEx.printStackTrace();
            System.exit(-2);
        } catch (LogistException lEx) {

            File hist = settings.get(FileKey.HISTORY);
            try {
                PrintWriter writer = new PrintWriter(hist);
                lEx.printStackTrace(writer);
            } catch (FileNotFoundException e) {
                System.err.println("Could not write error to " + hist);
            } finally {
                lEx.printStackTrace();
                System.exit(-3);
            }
        }
    }

    private static void createTournament(File tournamentDir, File jarsDir) {

        if (!jarsDir.exists()) {
            System.err.println("Agents directory " + jarsDir
                    + " does not exist.");
            return;
        }

        if (!tournamentDir.exists() && !tournamentDir.mkdirs()) {
            System.err.println("Could not create tournament directory "
                    + tournamentDir);
            return;
        }

        File tournamentFile = new File(tournamentDir, "agents.xml");
        JarFinder.createTournamentFile(tournamentFile, jarsDir);
    }

    private static void runTournament(File tournamentDir, File template) {

        File agentsFile = new File(tournamentDir, "agents.xml");
        if (!agentsFile.exists()) {
            System.err.println("Tournament file " + agentsFile
                    + " does not exist.");
            return;
        }

        if (!tournamentDir.exists() && !tournamentDir.mkdirs()) {
            System.err.println("Could not create tournament directory "
                    + tournamentDir);
            return;
        }

        try {
            System.out.println("Tournament directory : " + tournamentDir);
            System.out.println("Configuration file   : " + template);

            Tournament tournament = new Tournament(tournamentDir);
            tournament.play(template);

        } catch (ParserException pEx) {
            pEx.printStackTrace();
            // System.exit(-2);
        } catch (LogistException lEx) {
            lEx.printStackTrace();
            // System.exit(-3);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            // System.exit(-1);
        }
    }

    private static void scoreTournament(File tournamentDir,
            String resultFilename) {

        try {
            Scores scores = new Scores(tournamentDir);
            scores.parse();
            scores.write(resultFilename);
        } catch (ParserException e) {
            e.printStackTrace();
            // System.exit(-2);
        } catch (IOException e) {
            e.printStackTrace();
            // System.exit(-1);
        } catch (LogistException lEx) {
            lEx.printStackTrace();
            // System.exit(-3);
        }
    }

    private static final String RUN_LOGIST = "    java -jar logist.jar";

    private static void usage() {
        System.out.println("Usage:");

        System.out.println("  create tournament");
        System.out.print(RUN_LOGIST);
        System.out.println(" -new 'tournament_name' ['agent_directory']");

        System.out.println("  run tournament");
        System.out.print(RUN_LOGIST);
        System.out.println(" -run 'tournament_name' "
                + "['template (directory or file)']");

        System.out.println("  collect tournament score");
        System.out.print(RUN_LOGIST);
        System.out.println(" -score 'tournament_name' "
                + "['results (output) file']");

        System.out.println("  simulate game");
        System.out.print(RUN_LOGIST);
        System.out.println(" [-o 'history_file.xml] [-a 'agents_file.xml] 'template_file' "
                + "agent1 [agent2] [...]");
    }

    public static Topology getTopology() {
        return topology;
    }

    public static LogistSettings getSettings() {
        return settings;
    }
}
