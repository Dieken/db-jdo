package org.apache.jdo.exectck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.jdo.exectck.Utilities.InvocationResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal that runs the JDO TCK against the Reference Implementation (RI)
 * or an implementation under test (IUT).
 *
 * @goal runtck
 *
 * @phase integration-test
 *
 */
public class RunTCK extends AbstractMojo {

    /**
     * To skip running of TCK, set to false.
     * @parameter expression="${jdo.tck.doRunTCK}"
     *      default-value=true
     * @required
     */
    private boolean doRunTCK;
    /**
     * To run the RunTCK plugin goal in verbose mode.
     * @parameter expression="${jdo.tck.doRunTCK}"
     *      default-value=false
     * @required
     */
    private boolean runtckVerbose;
    /**
     * Run the TCK in a debugger.
     * @parameter expression="${jdo.tck.debugTCK}"
     *      default-value=false
     * @required
     */
    private boolean debugTCK;
    /**
     * Location of TCK generated output.
     * @parameter expression="${project.build.directory}"
     *      default-value="${basedir}/target"
     * @required
     */
    private String buildDirectory;
    /**
     * Location of the logs directory.
     * @parameter expression="${project.log.directory}"
     *      default-value="${project.build.directory}/logs"
     * @required
     */
    private File logsDirectory;
    /**
     * Location of the configuration directory.
     * @parameter expression="${project.conf.directory}"
     *      default-value="${basedir}/src/conf"
     * @required
     */
    private String confDirectory;
    /**
     * Location of the configuration directory.
     * @parameter expression="${project.sql.directory}"
     *      default-value="${basedir}/src/sql"
     * @required
     */
    private String sqlDirectory;
    /**
     * Implementation to be tested (jdori or iut).
     * Any value other than "jdori" will test an appropriately configured IUT
     * @parameter expression="${jdo.tck.impl}"
     *      default-value="jdori"
     * @required
     */
    private String impl;
    /**
     * List of configuration files, each describing a test configuration.
     * Note: Collection can only be configured in pom.xml. Using multi-valued
     *       type because long String cannot be broken across lines in pom.xml.
     * @parameter
     * @required
     */
    private ArrayList<String> cfgs;
    /**
     * List of configuration files, each describing a test configuration.
     * Allows command line override of configured cfgs value.
     * @parameter expression="${jdo.tck.cfglist}"
     *      default-value="detach.conf"
     * @optional
     */
    private String cfgList;
    /**
     * Name of file in src/conf containing pmf properties.
     * @parameter expression="${jdo.tck.pmfproperties}"
     * @optional
     */
    private String pmfProperties;
    /**
     * Name of file in src/conf containing property jdo.tck.exclude,
     *   whose value is a list of files to be excluded from testing.
     * @parameter expression="${jdo.tck.excludefile}"
     *      default-value="exclude.list"
     * @optional
     */
    private String exclude;
    /**
     * List of databases to run tests under.
     * Currently only derby is supported.
     * @parameter expression="${jdo.tck.dblist}" default-value="derby"
     * @required
     */
    private String dblist;
    private HashSet<String> dbs;
    /**
     * List of identity types to be tested.
     * @parameter expression="${jdo.tck.identitytypes}"
     *      default-value="applicationidentity datastoreidentity"
     * @required
     */
    private String identitytypes;
    private HashSet<String> idtypes;
    /**
     * List of mappings required by the current configurationd
     */
    private HashSet<String> mappings;
    /**
     * Run the TCK tests in verbose mode.
     * @parameter expression="${jdo.tck.verbose} default-value="false"
     * @optional
     */
    private String verbose;
    /**
     * To retain test output for debugging, set to false.
     * @parameter expression="${jdo.tck.cleanupaftertest} default-value="true"
     * @optional
     */
    private String cleanupaftertest;
    /**
     * Properties to use in accessing database.
     * @parameter expression="${database.runtck.sysproperties}"
     * @optional
     */
    private String dbproperties;    // NOTE: only allows for one db
    /**
     * Properties to use in accessing database.
     * @parameter expression="${jdo.tck.signaturefile}"
     * @optional
     */
    private String signaturefile;
    /**
     * JVM properties.
     * @parameter expression="${jdo.tck.jvmproperties}"
     *      default-value="-Xmx512m"
     * @optional
     */
    private String jvmproperties;
    /**
     * The port number the JVM should listen for a debugger on.
     * @parameter expression="${jdo.tck.debug.port}"
     *      default-value="8787"
     * @optional
     */
    private String debugPort;
    /**
     * User-supplied arguments for debug directives.
     * @parameter expression="${jdo.tck.debug.jvmargs}"
     *      default-value="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=${jdo.tck.debug.port}"
     * @optional
     */
    private String debugDirectives;
    /**
     * Class used to run a batch of tests.
     * @parameter expression="${jdo.tck.testrunnerclass}"
     *      default-value="org.apache.jdo.tck.util.BatchTestRunner"
     * @required
     */
    private String testRunnerClass;
    /**
     * Class used to output test result and configuration information.
     * @parameter expression="${jdo.tck.resultprinterclass}"
     *      default-value="org.apache.jdo.tck.util.BatchResultPrinter"
     * @required
     */
    private String resultPrinterClass;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!doRunTCK) {
            System.out.println("Skipping RunTCK!");
            return;
        }

        dbs = new HashSet();
        idtypes = new HashSet();
        mappings = new HashSet();
        Properties props = null;
        boolean alreadyran = false;
        String runonce = "false";
        List<String> propsString = new ArrayList<String>();
        List<String> command;
        String cpString = null;
        InvocationResult result;

        if (cfgs != null) {
//            System.out.println("Configurations specified in cfgs are " + cfgs.toString());
        } else if (cfgList != null) {
            cfgs = new ArrayList();
            PropertyUtils.string2List(cfgList, cfgs);
//            System.out.println("Configurations are " + cfgs.toString());
        } else {
            throw new MojoExecutionException(
                    "Could not find configurations to run TCK. "
                    + "Set cfgList parameter on command line "
                    + "or cfgs in pom.xml.");
        }

        PropertyUtils.string2Set(dblist, dbs);
        PropertyUtils.string2Set(identitytypes, idtypes);
        System.out.println("*>TCK to be run for " + impl
                + " on \n configurations: "
                + cfgs.toString() + "\n  databases: " + dbs.toString()
                + "\n  identity types: " + identitytypes.toString());

        // Properties required for test execution
        propsString.add("-DResultPrinterClass=" + resultPrinterClass);
        propsString.add("-Dverbose=" + verbose);
        propsString.add("-Djdo.tck.cleanupaftertest=" + cleanupaftertest);
        propsString.add("-DPMFProperties=" + buildDirectory + File.separator
                + "classes" + File.separator + pmfProperties);
        propsString.add("-DPMF2Properties=" + buildDirectory + File.separator
                + "classes" + File.separator + pmfProperties);
        String excludeFile = confDirectory + File.separator + exclude;
        propsString.add("-Djdo.tck.exclude="
                + PropertyUtils.getProperties(excludeFile).getProperty("jdo.tck.exclude"));

        // Create configuration log directory
        String timestamp = Utilities.now();
        String thisLogDir = logsDirectory + File.separator + timestamp
                + File.separator;
        String cfgDirName = thisLogDir + "configuration";
        File cfgDir = new File(cfgDirName);
        if (!(cfgDir.exists()) && !(cfgDir.mkdirs())) {
            throw new MojoExecutionException("Failed to create directory "
                    + cfgDirName);
        }
        propsString.add("-Djdo.tck.log.directory=" + thisLogDir);

        // Get ClassLoader URLs to build classpath below
        URL[] cpURLs = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
        ArrayList<URL> urlList = new ArrayList<URL>(Arrays.asList(cpURLs));

        // Get contents of pmf properties file to build new file below
        String pmfPropsReadFileName = confDirectory + File.separator + pmfProperties;
        String defaultPropsContents = "";
        try {
            defaultPropsContents = Utilities.readFile(pmfPropsReadFileName);
        } catch (IOException ex) {
            Logger.getLogger(RunTCK.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (String db : dbs) {
            System.setProperty("jdo.tck.database", db);
            alreadyran = false;

            for (String idtype : idtypes) {
                propsString.add("-Djdo.tck.identitytype=" + idtype);
                String enhancedDirName = buildDirectory + File.separator + "enhanced"
                        + File.separator + impl + File.separator + idtype + File.separator;
                File enhancedDir = new File(enhancedDirName);
                if (!(enhancedDir.exists())) {
                    throw new MojoExecutionException("Could not find enhanced directory "
                            + enhancedDirName + ". Execute Enhance goal before RunTCK.");
                }

                // Set classpath string: add new entries to URLS from loader
                ArrayList<URL> cpList = new ArrayList<URL>();
                cpList.addAll(urlList);
                try {
                    URL url1 = enhancedDir.toURI().toURL();
                    URL url2 = new File(buildDirectory + File.separator
                            + "classes" + File.separator).toURI().toURL();
                    cpList.add(url1);
                    cpList.add(url2);
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(RunTCK.class.getName()).log(Level.SEVERE, null, ex);
                }
                cpString = Utilities.urls2ClasspathString(cpList);
                if (runtckVerbose) {
                    System.out.println("\nClasspath is " + cpString);
                }

                for (String cfg : cfgs) {

                    // Parse conf file and set properties String
                    props = PropertyUtils.getProperties(confDirectory
                            + File.separator + cfg);
                    propsString.add("-Djdo.tck.testdata="
                            + props.getProperty("jdo.tck.testdata"));
                    propsString.add("-Djdo.tck.standarddata="
                            + props.getProperty("jdo.tck.standarddata"));
                    propsString.add("-Djdo.tck.mapping.companyfactory="
                            + props.getProperty("jdo.tck.mapping.companyfactory"));
//                    propsString.append("-Djdo.tck.description=\"" +
//                            props.getProperty("jdo.tck.description") + "\"");
                    propsString.add("-Djdo.tck.requiredOptions="
                            + props.getProperty("jdo.tck.requiredOptions"));
                    propsString.add("-Djdo.tck.signaturefile="
                            + signaturefile);
                    String mapping = props.getProperty("jdo.tck.mapping");
                    if (mapping == null) {
                        throw new MojoExecutionException(
                                "Could not find mapping value in conf file: " + cfg);
                    }
                    String classes = props.getProperty("jdo.tck.classes");
                    if (classes == null) {
                        throw new MojoExecutionException(
                                "Could not find classes value in conf file: " + cfg);
                    }
                    List<String> classesList = Arrays.asList(classes.split(" "));


                    propsString.add("-Djdo.tck.schemaname=" + idtype + mapping);
                    propsString.add("-Djdo.tck.cfg=" + cfg);

                    runonce = props.getProperty("runonce");
                    runonce = (runonce == null) ? "false" : runonce;

                    // Add Mapping and schemaname to properties file
                    StringBuffer propsFileData = new StringBuffer();
                    propsFileData.append("\n### Properties below added by maven 2 goal RunTCK.jdori");
                    propsFileData.append("\njavax.jdo.mapping.Schema=" + idtype + mapping);
                    mapping = (mapping.equals("0")) ? "" : mapping;
                    propsFileData.append("\njavax.jdo.option.Mapping=standard" + mapping);
                    propsFileData.append("\n");
                    String pmfPropsWriteFileName = buildDirectory + File.separator
                            + "classes" + File.separator + pmfProperties;
                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(pmfPropsWriteFileName, false));
                        out.write(defaultPropsContents + propsFileData.toString());
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(RunTCK.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // build command line string
                    command = new ArrayList<String>();
                    command.add("java");
                    command.add("-cp");
                    command.add(cpString);
                    command.addAll(propsString);
                    command.add(dbproperties);
                    // TODO!!! split jvmproperties into a List!!
                    command.add(jvmproperties);
                    if (debugTCK) {
                        command.add(debugDirectives);
                    }
                    command.add(testRunnerClass);
                    command.addAll(classesList);

                    // invoke class runner
                    System.out.println("*> Starting configuration=" + cfg
                            + " with database=" + db + " identitytype=" + idtype
                            + " mapping=" + mapping + " on the " + impl + ".");
                    if (debugTCK) {
                        System.out.println("Using debug arguments: \n"
                                + debugDirectives);
                    }
                    if (runonce.equals("true") && alreadyran) {
                        continue;
                    }
//                    System.out.println("\nCommand line is: \n" + command.toString());
                    result = (new Utilities()).invokeTest(command);

                    if (runtckVerbose) {
                        System.out.println("Test exit value is " + result.getExitValue());
                        System.out.println("Test result output:\n" + result.getOutputString());
                        System.out.println("Test result error:\n" + result.getErrorString());
                    }

                    if (runonce.equals("true")) {
                        alreadyran = true;
                    }

                }
            }
        }

        // Output results
        command = new ArrayList<String>();
        command.add("java");
        command.add("-cp");
        command.add(cpString);
        command.add("org.apache.jdo.tck.util.ResultSummary");
        command.add(thisLogDir);
        result = (new Utilities()).invokeTest(command, new File(buildDirectory));

        // Create system configuration description file
        command.set(3, "org.apache.jdo.tck.util.SystemCfgSummary");
        command.set(4, cfgDirName);
        command.add("system_config.txt");
        result = (new Utilities()).invokeTest(command, new File(buildDirectory));

        // Copy metadata from enhanced to configuration logs directory
        for (String idtype : idtypes) {
            String fromDirName = buildDirectory + File.separator + "enhanced"
                    + File.separator + impl + File.separator + idtype + File.separator;
            String[] metadataExtensions = {"jdo", "jdoquery", "orm", "xml", "properties"};
            File fromFile = null;
            File toFile = null;
            String fromFileName = null;
            String pkgName = null;
            int startIdx = -1;
            // iterator over list of abs name of metadata files in src
            Iterator<File> fi = FileUtils.iterateFiles(
                    new File(fromDirName), metadataExtensions, true);
            while (fi.hasNext()) {
                try {
                    fromFile = fi.next();
                    fromFileName = fromFile.toString();
//                    System.out.println("Copying " + fromFileName);
                    if ((startIdx = fromFileName.indexOf(idtype + File.separator)) > -1) {
                        // fully specified name of file (idtype + package + filename)
                        pkgName = fromFileName.substring(startIdx);
                        toFile = new File(cfgDirName + File.separator
                                + pkgName);
//                        System.out.println("Copy from source dir to " + toFile.toString());
                        FileUtils.copyFile(fromFile, toFile);
                    }
                } catch (IOException ex) {
                    throw new MojoExecutionException("Failed to copy files from "
                            + fromFileName + " to " + toFile.toString()
                            + ": " + ex.getLocalizedMessage());
                }
            }
        }
    }
}