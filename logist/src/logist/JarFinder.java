package logist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import logist.history.XMLWriter;

import logist.behavior.AuctionBehavior;


class JarFinder {

    private File jarsDir;
    private XMLWriter writer;
    private Set<String> names;

    static void createTournamentFile(File tournamentFile, File jarsDir) {
        new JarFinder(jarsDir).writeFile(tournamentFile);
    }

    private JarFinder(File jarsDir) {
        this.jarsDir = jarsDir;
    }

    private void writeFile(File tournamentFile) {
        try {
            names = new HashSet<String>();
            writer = new XMLWriter(new FileWriter(tournamentFile));

            writer.writeTag("agents");
            writer.writeComment("An auto-generated list of agents"
                    + " and their behavior classes");
            writer.writeComment(" - jar directory : "
                    + addSlash(jarsDir.toString()));
            writer.writeComment(" - generated on  : " + new Date());

//			findClassesInPath("bin");
            findJars();

            writer.endTag();
            writer.close();

            System.out.println("Successfully generated " + tournamentFile);
        } catch (IOException ioEx) {
            throw new LogistException("I/O error", ioEx);
        } catch (ClassNotFoundException cnfEx) {
            throw new LogistException("class error", cnfEx);
        }
    }

    private String addSlash(String s) {
        return (s.endsWith("/") ? s : s + "/");
    }

    private interface ClassCallback {
        void handleClass(String className);
    }

    private void findClassesInPath(final String classPath) {
        File cp = new File(classPath);
        URLClassLoader loader_ = null;
        try {
            loader_ = new URLClassLoader(new URL[] { cp.toURI().toURL() });
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("Skipping classpath : " + classPath);
            return;
        }

        final URLClassLoader loader = loader_;
        findClasses(cp, "", new ClassCallback() {
            @Override
            public void handleClass(String className) {
                // TODO Auto-generated method stub

                try {
                    Class<?> clazz = loader.loadClass(className);
                    if (AuctionBehavior.class.isAssignableFrom(clazz)) {
                        // interface with legacy API
                        // || Behavior.class.isAssignableFrom(clazz)) {

                        System.out.println("- behavior: " + className);

                        String name = "YourName";

                        writer.writeTag("agent");
                        writer.writeAttribute("name", uniqueName(name));
                        writer.writeTag("set");
                        writer.writeAttribute("class-path", classPath);
                        writer.endTag();
                        writer.writeTag("set");
                        writer.writeAttribute("class-name", className);
                        writer.endTag();
                        writer.endTag();
                        writer.flush();

                    }
                } catch (NoClassDefFoundError ncdfEr) {
                    System.err.println("- bad class: " + ncdfEr.getMessage());
                } catch (ClassNotFoundException cnfEx) {
                    System.err.println("- class not found: "
                            + cnfEx.getMessage());
                }

            }
        });
    }

    private void findClasses(File file, String className, ClassCallback cb) {
//		System.out.println("file="+file+", cname="+className);

        if (file.isDirectory()) {
            if (!className.isEmpty()) className = className + ".";
            for (String name : file.list())
                findClasses(new File(file, name), className + name, cb);
        } else if (className.endsWith(".class")) {

            // Replace directories with package names
            //className = className.replaceAll("/", ".");
            // Remove the trailing .class
            className = className.substring(0, className.length() - 6);

            cb.handleClass(className);
            // if (className.startsWith("bin"))
            // className = className.substring(4);



        }

    }

    /**
     * Finds all jar files which might contain behaviors
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws Exception
     *             if something bad happens...
     */
    private void findJars() throws FileNotFoundException, IOException,
            ClassNotFoundException {
        String[] competitorFiles = jarsDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        // Create a class loader that is able to load all classes
        // List<URL> competitorURLs = new ArrayList<URL>();
        // for (String filename : competitorFiles) {
        // competitorURLs.add(new File(jarsDir, filename).toURI().toURL());
        // }
        // URLClassLoader loader = new URLClassLoader(competitorURLs
        // .toArray(new URL[0]));

        // Try to find all classes in the specified jar files that are instances
        // of epfl.lia.logist.agent.behavior.Behavior
        for (String jarFileName : competitorFiles) {
            System.out.println("Found JAR: " + jarFileName);
            File jarFile = new File(jarsDir, jarFileName);
            URLClassLoader loader = new URLClassLoader(new URL[] { jarFile
                    .toURI().toURL() });
            JarInputStream jarInput = new JarInputStream(new FileInputStream(
                    jarFile));

            JarEntry jarEntry;
            while ((jarEntry = jarInput.getNextJarEntry()) != null) {

                String className = jarEntry.getName();
                if (!className.endsWith(".class"))
                    continue;

                // Replace directories with package names
                className = className.replaceAll("/", ".");
                // Remove the trailing .class
                className = className.substring(0, className.length() - 6);

                // if (className.startsWith("bin"))
                // className = className.substring(4);

                try {
                    Class<?> clazz = loader.loadClass(className);
                    int modifiers = clazz.getModifiers();
                    if (AuctionBehavior.class.isAssignableFrom(clazz) && !Modifier.isAbstract(modifiers)) {
                        // interface with legacy API
                        // || Behavior.class.isAssignableFrom(clazz)) {

                        System.out.println("- behavior: " + className);

                        String name = jarFileName.substring(0, jarFileName
                                .length() - 4);

                        writer.writeTag("agent");
                        writer.writeAttribute("name", uniqueName(name));
                        writer.writeTag("set");
                        writer.writeAttribute("class-path", jarFile);
                        writer.endTag();
                        writer.writeTag("set");
                        writer.writeAttribute("class-name", className);
                        writer.endTag();
                        writer.endTag();
                        writer.flush();

                    }
                } catch (NoClassDefFoundError ncdfEr) {
                    System.err.println("- bad class: " + ncdfEr.getMessage());
                } catch (ClassNotFoundException cnfEx) {
                    System.err.println("- class not found: "
                            + cnfEx.getMessage());
                }
            }
        }
    }

    private String uniqueName(String name) {
        int num = 2;
        String current = name;
        while (names.contains(current)) {
            current = name + num++;
        }
        names.add(current);
        return current;
    }
}
