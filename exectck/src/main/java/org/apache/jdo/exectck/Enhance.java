
package org.apache.jdo.exectck;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.jdo.JDOEnhancer;
import javax.jdo.JDOHelper;

/**
 * Goal that enhances test classes for testing a JDO implementation.
 *
 * @goal enhance
 *
 * @phase integration-test
 *
 */
public class Enhance extends AbstractMojo {

    /**
     * Location of TCK generated output.
     * @parameter expression="${jdo.tck.doEnhance}"
     *      default-value=true
     * @required
     */
    private boolean doEnhance;
    /**
     * Root of the TCK source installation.
     * @parameter expression="${project.src.directory}"
     *      default-value="${basedir}/src"
     * @required
     */
    private String srcDirectory;
    /**
     * Location of the logs directory.
     * @parameter expression="${project.log.directory}"
     *      default-value="${project.build.directory}/logs"
     * @required
     */
    private File logsDirectory;
    /**
     * Location of TCK generated output.
     * @parameter expression="${project.build.directory}"
     *      default-value="${basedir}/target"
     * @required
     */
    private String buildDirectory;
    /**
     * Implementation to be tested (jdori or iut).
     * Any value other than "jdori" will test an appropriately configured IUT
     * @parameter expression="${jdo.tck.impl}"
     *      default-value="jdori"
     * @required
     */
    private String impl;
    /**
     * List of identity types to be tested.
     * @parameter expression="${jdo.tck.identitytypes}"
     *      default-value="applicationidentity datastoreidentity"
     * @required
     */
    private String identitytypes;
    private HashSet<String> idtypes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!doEnhance) {
            System.out.println("Skipping Enhance!");
            return;
        }

        idtypes = new HashSet();
        PropertyUtils.string2Set(identitytypes, idtypes);

        // Create directory for enhancer logs
        String enhanceLogsDirName = logsDirectory + File.separator + "enhancer";
        File enhancerLogsDir = new File(enhanceLogsDirName);
        if (!(enhancerLogsDir.exists()) && !(enhancerLogsDir.mkdirs())) {
            throw new MojoExecutionException("Failed to create directory "
                    + enhancerLogsDir);
        }

        // Create directory for enhanced classes
        String enhancedDirName = buildDirectory + File.separator + "enhanced"
                + File.separator + impl + File.separator;
        File enhancedDir = new File(enhancedDirName);
        if (!(enhancedDir.exists()) && !(enhancedDir.mkdirs())) {
            throw new MojoExecutionException("Failed to create directory "
                    + enhancedDir);
        }



        String[] pcPkgNames = {"org/apache/jdo/tck/api/",
            "org/apache/jdo/tck/pc/",
            "org/apache/jdo/tck/models/inheritance/"};
        String[] metadataExtensions = {"jdo", "jdoquery", "orm", "xml", "properties"};  // we really want "jdo.properties", but this is easier
        String[] srcDirs = {"jdo", "orm", "testdata"};
        String genericPkgName = "org";
        File toFile = null;
        File fromFile = null;
        String fromFileName = null;
        String fromDirName = null;
        String pkgName = null;
        int startIdx = -1;
        Iterator<File> fi = null;
        String[] classArray = new String[10];

        // Copy metadata from src to enhanced
        for (String idtype : idtypes) {
            for (String srcDir : srcDirs) {
                fromDirName = srcDirectory + File.separator + srcDir;
                // iterator over list of abs name of metadata files in src
                fi = FileUtils.iterateFiles(
                        new File(fromDirName), metadataExtensions, true);

                while (fi.hasNext()) {
                    try {
                        fromFile = fi.next();
                        fromFileName = fromFile.toString();
//                    System.out.println("Copying " + fromFileName);
                        if ((startIdx = fromFileName.indexOf(idtype + File.separator)) > -1) {
                            // fully specified name of file (idtype + package + filename)
                            pkgName = fromFileName.substring(startIdx);
                            toFile = new File(enhancedDirName + File.separator
                                    + pkgName);
//                        System.out.println("Copy from source dir to " + toFile.toString());
                            FileUtils.copyFile(fromFile, toFile);
                        } else if (srcDir.equals("testdata")) {
                            startIdx = fromFileName.indexOf("org" + File.separator);
                            pkgName = fromFileName.substring(startIdx);
                            toFile = new File(enhancedDirName + File.separator
                                    + idtype + File.separator + pkgName);
                            System.out.println("Copy from " + fromFile.toString() + " to " + toFile.toString());
                            FileUtils.copyFile(fromFile, toFile);
                        } else {
                            continue;  // idtype not in pathname, do not copy
                        }
                    } catch (IOException ex) {
                        throw new MojoExecutionException("Failed to copy files from "
                                + fromFileName + " to " + toFile.toString()
                                + ": " + ex.getLocalizedMessage());
                    }
                }

                // Copy pc and pa classes from target/classes to enhanced
                String[] extensions = {"class"};
                fromDirName = buildDirectory + File.separator
                        + "classes" + File.separator;
                String enhancedIdDirName = enhancedDirName + idtype + File.separator;
                ArrayList<String> classes = new ArrayList<String>();
                for (String pcPkgName : pcPkgNames) {
                    // iterator over list of abs name of class files in target/classes
                    fi = FileUtils.iterateFiles(
                            new File(fromDirName + pcPkgName), extensions, true);
                    while (fi.hasNext()) {
                        try {
                            fromFile = fi.next();
                            fromFileName = fromFile.toString();
                            // fully specified name of file (package + filename)
                            toFile = new File(enhancedIdDirName + fromFileName.substring(
                                    fromFileName.indexOf(pcPkgName)));
                            FileUtils.copyFile(fromFile, toFile);
                            classes.add(toFile.toString());
                        } catch (IOException ex) {
                            throw new MojoExecutionException("Failed to copy files from "
                                    + fromFileName + " to " + toFile.toString()
                                    + ": " + ex.getLocalizedMessage());
                        }
                    }
                }

                // Enhance classes

                URL[] classPathURLs = new URL[2];
                ClassLoader loader = null;
                try {
                    classPathURLs[0] = (new File(enhancedIdDirName)).toURI().toURL();
                    classPathURLs[1] = (new File(fromDirName)).toURI().toURL();
                    loader = new URLClassLoader(classPathURLs, getClass().getClassLoader());
//                    Utilities.printClasspath(loader);
                    // debugging
//                Class cls = null;
//                try {
//                    cls = loader.loadClass("org.apache.jdo.tck.pc.companyListWithoutJoin.CompanyModelReader");
//                } catch (ClassNotFoundException ex) {
//                    Logger.getLogger(Enhance.class.getName()).log(Level.SEVERE, null, ex);
//                }
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Enhance.class.getName()).log(Level.SEVERE, null, ex);
                }
                JDOEnhancer enhancer = JDOHelper.getEnhancer();
                enhancer.setVerbose(true);
                String[] classArr = classes.toArray(classArray);
                enhancer.addClasses(classArr);
                enhancer.setClassLoader(loader);
                System.out.println("Enhancing classes");
                enhancer.enhance();
            }
        }
    }
}