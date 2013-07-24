/*
diffxml and patchxml - diff and patch for XML files

Copyright 2013 Adrian Mouat

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package org.diffxml.patchxml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.diffxml.diffxml.DOMOps;

/**
 * Applies a DUL patch to an XML document.
 */

public final class PatchXML {
    
    /** 
     * If true, extra debug data is output.
     */
    public static boolean debug = false;

    /**
     * If true, attempt to reverse the sense of the patch.
     */
    private static boolean reverse = false;

    /**
     * Determines whether original file overwritten.
     *
     * Currently breaks from unix patch, file not implicitly written to.
     * To change this set the boolean dryrun to false
     * You will then need to use -dry-run to avoid overwriting files
     */
    private static boolean dryrun = true;

    /** Holds the name of the document to be patched. **/
    private static String mDocFile;

    /** Holds the name of the DUL patch file. **/
    private static String mPatchFile;

    /**
     * Shouldn't be instantiated.
     */
    private PatchXML() {
        //Intentionally empty
    }
    /**
     * Parse command line arguments.
     *
     * Sets up file variables and options.
     *
     * @param args array of command line arguments
     */
    public static void parseArgs(final String[] args) {
        
        int i = 0;
        char flag;
        String arg;

        while (i < args.length && args[i].startsWith("-")) {
            
            arg = args[i++];
            
            //Normalize multiple dashes
            //I don't understand differentiating between 1 and 2 dashes
            //We allow 2 in order to mimic patch util
            if (arg.startsWith("--")) {
                arg = arg.substring(1);
            }

            //"wordy" arguments
            if (arg.equals("-version")) {
                printVersion();
            } else if (arg.equals("-help")) {
                printHelp();
            } else if (arg.equals("-dry-run")) {
                dryrun = true;
                /*
            } else if (arg.equals("-reverse")) {
                reverse = true;
                */
            } else if (arg.equals("-debug")) {
                debug = true;
            } else {

                //(series of) flag arguments                
                for (int j = 1; j < arg.length(); j++) {
                    flag = arg.charAt(j);
                    switch (flag) {
                        case 'V':
                            printVersion();
                            break;
                        case 'h':
                            printHelp();
                            break;
                        case 'd':
                            dryrun = true;
                            break;
                        case 'D':
                            debug = true;
                            break;
                            /*
                        case 'R':
                            reverse = true;
                            break;
                            */
                        default:
                            System.err.println("PatchXML: illegal option "
                                    + flag);
                            System.exit(2);
                            break;
                    }
                }
            }
        }
        
        if ((i + 2) != args.length) {
            printUsage();
        }

        mDocFile = args[i];
        mPatchFile = args[++i];
        }

    /**
     * Output usage and exit.
     */
    private static void printUsage() {
        System.err.println("Usage: patch [OPTION]... [ORIGFILE [PATCHFILE]]");
        System.exit(2);
    }

    /**
     * Output help and exit.
     */
    private static void printHelp() {
        System.out.print("\nUsage: patch [OPTION]... [ORIGFILE [PATCHFILE]]\n");
        System.out.print(
                "\nApply a diffxml file to one of the original XML files.\n");
        System.out.print(
                "\n --version  -V  Output version number of program.");
        System.out.print(
                "\n --help     -h  Print summary of options and exit.");
        System.out.print(
                "\n --dry-run  -d  Print results of applying the changes ");
        System.out.print("without modifying any files.");
        /*
        System.out.print(
                "\n --reverse  -R  Assume that the delta file was created ");
        System.out.print("with the old and new files swapped.");
        
        System.out.print("\n\tAttempt to reverse sense of change before ");
        System.out.print("applying it, e.g. inserts become deletes.\n\n");
        */
        System.out.print("\n\n");
        printSoftware();
        System.exit(0);
    }

    /**
     * Output details of other software used in diffxml and patchxml.
     */
    private static void printSoftware() {
     
        System.out.print(
                "\nThis product includes software developed by the ");
        System.out.print("Indiana University Extreme! Lab ");
        System.out.print("(http://www.extreme.indiana.edu/).\n");
        System.out.print(
                "\nThis product includes software developed by the ");
        System.out.print(
                "Apache Software Foundation (http://www.apache.org/).\n\n");
    }

    /**
     * Output version and exit.
     */
    private static void printVersion() {
        
        System.out.println("patchxml Version 0.96 BETA");
        printSoftware();
        System.exit(0);
    }


    /**
    * Checks if input files exist.
    *
    * Outputs error message if input not found.
    *
    * @return True only if both files are found.
    */
    protected static boolean checkFilesExistAndWarn() {

        boolean ret = true;
        File docFile = new File(mDocFile);
        File patchFile = new File(mPatchFile);

        if (!docFile.exists()) {
            System.err.println("Could not find file: " + mDocFile);
            ret = false;
        }
        if (!patchFile.exists()) {
            System.err.println("Could not find file: " + mPatchFile);
            ret = false;
        }
        return ret;
    }

    /**
     * Output the patched document to stdout.
     *
     * @param doc the patched document
     */
    private static void outputDoc(final Document doc) {

        try {
 
            if (dryrun) {
                DOMOps.outputXML(doc, System.out);
            } else {
                DOMOps.outputXML(doc, new FileOutputStream(mDocFile));
            }
        } catch (IOException e) {
            System.err.println("Failed to output new document: " + e);
            System.exit(2);
        }
    }

    /**
     * Attempt to patch given document with given patch file.
     *
     * TODO: Refactor
     * @param args command line arguments
     */
    public static void main(final String[] args) {

        //Set options - instantiates _docFile and _patchFile
        parseArgs(args);

        if (!checkFilesExistAndWarn()) {
            System.exit(2);
        }

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DOMOps.initParser(fac);

        DocumentBuilder parser = null;
        try {
            parser = fac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.err.println("Failed to configure parser: " + e.getMessage());
            System.exit(2);
        }
        
        Document doc = null;
        try {
            doc = parser.parse(mDocFile);
        } catch (SAXException e) {
            System.err.println("Failed to parse document: " + e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("Failed to parse document: " + e.getMessage());
            System.exit(2);
        }

        Document patch = null;
        try {
            patch = parser.parse(mPatchFile);
        } catch (SAXException e) {
            System.err.println("Failed to parse document: " + e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("Failed to parse document: " + e.getMessage());
            System.exit(2);
        }

        doc.normalize();
        patch.normalize();

        if (debug) {
            try {
                System.err.println("Applying patch to: ");
                DOMOps.outputXML(doc, System.err);
                System.err.println();
            } catch (IOException e) {
                System.err.println("Failed to print debug output");
            }
        }

        DULPatch patcher = new DULPatch();
        try {
            patcher.apply(doc, patch);
        } catch (PatchFormatException e) {
            System.err.println("Failed to parse Patch:"); 
            e.printStackTrace();
            System.exit(2);
        }

        outputDoc(doc);
        System.out.println();
    }
}
