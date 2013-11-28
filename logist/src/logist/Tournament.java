package logist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import logist.config.ParserException;
import logist.config.Parsers;

class Tournament {

    public static final char SEP = '-';
    private static Runtime runtime = Runtime.getRuntime();

    static final Logger LOG = Logger.getLogger(Tournament.class.getName());
    private final File tournamentDir;
    private final File agentsFile;
    private final List<String> agentNames;
    private final List<int[]> permutations;
    private final String classpath;

    Tournament(File tournamentDir) throws ParserException, IOException {
        this.tournamentDir = tournamentDir;
        this.agentsFile = new File(tournamentDir, "agents.xml");

        String cp = System.getProperty("java.class.path");
        cp = cp.replace(':', ';');
        cp = cp.replace(',', ';');
        String[] paths = cp.split(";");
        StringBuffer classpath = new StringBuffer();
        for (String path : paths) {
            if (path.endsWith("logist2.jar") ||
                    path.endsWith("jdom.jar") ||
                    path.endsWith("colt.jar") ||
                    path.endsWith("plot.jar") ||
                    path.endsWith("repast.jar")
            ) {
                classpath.append(path);
                classpath.append(":");
            }
        }
        classpath.append(".");
        this.classpath = classpath.toString();
        if (classpath == null) {
            LOG.severe("Could not find path to LogistPlatform.\njava.class.path = " + System.getProperty("java.class.path"));
        }
        System.err.println(this.classpath);
        //classpath = "logist/logist.jar";

        this.agentNames = Parsers.parseAgents(agentsFile);
        this.permutations = new Permutation(agentNames.size(), 2).permutations;

        FileHandler handler = new FileHandler(tournamentDir + "/runner%u.log");
        handler.setFormatter(new MyFormatter());
        handler.setLevel(Level.ALL);
        LOG.addHandler(handler);
        LOG.setLevel(Level.ALL);
    }

    void play(File templateFile) {
        if (agentNames.size() == 0) {
            System.err.println("No agents found in " + agentsFile);
            return;
        } else if (agentNames.size() == 1) {
            System.err.println("Cannot play a tournament with only one agent.");
            System.err.println("To play against yourself, duplicate the entry in\n"+ agentsFile+ " and rename the agent.");
            return;
        } else {
            System.err.println(templateFile);
        }

        // file is missing
        if (!templateFile.exists()) {
            LOG.warning("template file " + templateFile + " does not exist");
        }
        // file is directory
        else if (templateFile.isDirectory()) {
            for (File file : templateFile.listFiles())
                if (file.isFile())
                    play(file);
        }
        // file is an XML file
        else if (templateFile.getName().endsWith(".xml")) {
            // drop '.xml'
            String template = templateFile.getName();
            template = template.substring(0, template.length() - 4);

            for (int[] permutation : permutations) {
                String historyFilename = historyFile(template, permutation);
                File historyFile = new File(tournamentDir, historyFilename);

                // create a history file if it does exists.
                // we use empty place holder files to synchronize
                // concurrent tournament runners.
                try {
                    if (!historyFile.createNewFile())
                        continue;
                } catch (IOException ioEx) {
                    LOG.warning("Skipping file " + historyFile.getAbsolutePath());
                    logException(ioEx);
                    continue;
                }

                StringBuilder command = new StringBuilder();
//				command.append("java -Xmx1024m -classpath \"" + System.getProperty("java.class.path") + "\" logist.LogistPlatform");
                command.append("java -Xmx1024m -classpath ");
                command.append(classpath);
                command.append(" logist.LogistPlatform");

                // history file name
                command.append(" -o ");
//				command.append('"');
                command.append(historyFile);
//				command.append('"');

                // agents file name
                command.append(" -a ");
//				command.append('"');
                command.append(agentsFile);
//				command.append('"');

                // template file name
                command.append(' ');
//				command.append('"');
                command.append(templateFile);
//				command.append('"');

                // agents names
                for (int agent : permutation) {
                    command.append(' ');
//					command.append('"');
                    command.append(agentNames.get(agent));
//					command.append('"');
                }

                System.err.println(command.toString());
                run(command.toString(), historyFile);
            }

        }
    }

    String historyFile(String template, int[] permutation) {
        StringBuilder builder = new StringBuilder();
        builder.append(template);
        for (int agent : permutation) {
            builder.append(SEP);
            builder.append(agentNames.get(agent));
        }
        builder.append(".xml");
        return builder.toString();
    }

    void run(String command, File historyFile) {
        LOG.fine("Run command: " + command);
        System.out.println("Run command: " + command);

        try {
            Process process = runtime.exec(command);

            // any output or error message?
            new StreamRedirection(process.getErrorStream(), Level.WARNING).start();
            new StreamRedirection(process.getInputStream(), Level.CONFIG).start();

            // wait for termination and check exit status
            int status = process.waitFor();
            if (status == 0) {
                LOG.info("LogistPlatform finished successfully!");
            } else {
//				historyFile.delete();
                LOG.severe("LogistPlatform returned error code " + status);
            }

        } catch (Exception ex) {
            logException(ex);
        }
    }

    private void logException(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        LOG.severe(writer.toString());
    }

    private static class Permutation {
        int n, max;
        int[] current;
        boolean[] used;
        List<int[]> permutations;

        Permutation(int n, int max) {
            this.n = n;
            this.max = max;
            this.current = new int[n];
            this.used = new boolean[n];
            this.permutations = new ArrayList<int[]>();

            recurse(0);
        }

        private void recurse(int k) {
            if (k == max) {
                permutations.add(Arrays.copyOf(current, max));
                return;
            }

            for (int j = 0; j < n; j++)
                if (!used[j]) {
                    used[j] = true;
                    current[k] = j;
                    recurse(k + 1);
                    used[j] = false;
                }
        }
    }

    private class StreamRedirection extends Thread {
        InputStream is;
        Level level;

        StreamRedirection(InputStream is, Level level) {
            this.is = is;
            this.level = level;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    LOG.log(level, line);
//					LOG.info(type + "> " + line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static class MyFormatter extends Formatter {

        DateFormat fmt = new SimpleDateFormat("hh:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {

            StringBuilder builder = new StringBuilder();

            builder.append(fmt.format(new Date(record.getMillis())));
            builder.append(" ");
            builder.append(record.getLevel());
            builder.append(" | ");
            builder.append(record.getMessage());
            builder.append("\n");

            return builder.toString();
        }

    }
}
