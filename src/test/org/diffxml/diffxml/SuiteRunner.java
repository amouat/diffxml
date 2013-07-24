package org.diffxml.diffxml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.diffxml.diffxml.fmes.Fmes;
import org.diffxml.patchxml.DULPatch;
import org.diffxml.patchxml.PatchFormatException;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Runs all the test cases in the suite.
 * 
 * Ant seems to insist on the extends :(
 * 
 * @author Adrian Mouat
 *
 */
public class SuiteRunner {

    /** Where the test files are stored. */
    private static final String SUITE_DIR = "suite";

    /**
     * Returns only files ending in "A.xml".
     * 
     */
    class FilesEndAFilter implements FileFilter {
        
        /**
         * Tests whether the given file is a file and ends in "A.xml".
         * 
         * @param f The file to be tested
         * @return True if the file meets the criteria
         */
        public boolean accept(final File f) {
            
            boolean ret = false;
            if (f.isFile() && f.getName().endsWith("A.xml")) {
                ret = true;
            }
            
            return ret;
        }
    }
    
    /**
     * Compares the two given files, applies the patch and checks they are the
     * same afterwards.
     * 
     * @param fA first file to compare 
     * @param fB second file to compare
     */
    public final void runFMESTest(final File fA, final File fB) {
                
        Fmes diffInstance = new Fmes();

        Document dA = null;
        Document dB = null;

        dA = DOMOps.getDocument(fA);
        dB = DOMOps.getDocument(fB);


        Document delta = null;
        try {
            delta = diffInstance.diff(dA, dB);
            DOMOps.outputXML(delta, new FileOutputStream("/tmp/out4.xml"), true);
        } catch (DiffException e) {
            fail("Diff threw exception: " + e.getMessage());
        } catch (IOException e) {
            fail("Caught IO exception:" + e.getMessage());
        }
        
        DULPatch patcher = new DULPatch();
        try {
            //Note the diff will modify dA, so need to read in again
            dA = DOMOps.getDocument(fA);
            patcher.apply(dA, delta);
            
        } catch (PatchFormatException e) {
            e.printStackTrace();
        }

        try {
            delta = diffInstance.diff(DOMOps.getDocument(fB), dA);
            DOMOps.outputXML(delta, new FileOutputStream("/tmp/d.xml"), true);
        } catch (DiffException e) {
            fail("Diff threw exception: " + e.getMessage());
        } catch (FileNotFoundException e) {
            fail("Caught Exception: " + e.getMessage());
        } catch (IOException e) {
            fail("Caught Exception: " + e.getMessage());
        }

        assertFalse(delta.getDocumentElement().hasChildNodes());
    }

    /**
     * Run all the tests in the suite directory.
     */
    @Test
    public final void runSuite() {
        
        
        File suiteDir = new File(SUITE_DIR);
        for (File fA : suiteDir.listFiles(new FilesEndAFilter())) {
                        
            File fB = new File(fA.getAbsolutePath().replace("A.xml", "B.xml"));
            
            //Uncomment following to see list of files being compared
            //System.out.println("Got: " + fA.getAbsolutePath() 
            //        + " " + fB.getAbsolutePath());
            
            //Use the following to run on a single file
            //runFMESTest(new File("/home/adrian/workspace/diffxml_cvs/suite/brianA.xml"), new File("/home/adrian/workspace/diffxml_cvs/suite/brianB.xml"));
            runFMESTest(fA, fB);
        }
    }
    
}

