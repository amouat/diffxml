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

package org.diffxml.diffxml.fmes;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.diffxml.diffxml.DOMOps;
import org.diffxml.diffxml.Diff;
import org.diffxml.diffxml.DiffException;
import org.diffxml.diffxml.DiffFactory;

import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Fmes finds the differences between two DOM documents.
 *
 * Uses the Fast Match Edit Script algorithm (fmes).
 * Output is a DOM document representing the differences in
 * DUL format.
 *
 * @author     Adrian Mouat
 */
public class Fmes implements Diff {

    /**
     * Determines if the given node should be ignored.
     *
     * Examines the node's type against settings.
     *
     * @return True if the node is banned, false otherwise
     * @param  n   The node to be checked
     */

    public static boolean isBanned(final Node n) {
        
        boolean ret = false;
        // Check if ignorable whitespace
        if (DiffFactory.isIgnoreWhitespaceNodes() && DOMOps.isText(n)) {
            StringTokenizer st = new StringTokenizer(n.getNodeValue());
            if (!st.hasMoreTokens()) {
                ret = true;
            }
        }

        // Check if ignorable comment
        if (DiffFactory.isIgnoreComments()
                && (n.getNodeType() == Node.COMMENT_NODE)) {
            ret = true;
        }

        // Check if ignorable pi
        if (DiffFactory.isIgnoreProcessingInstructions()
                && (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)) {
            ret = true;
        }

        return ret;
    }

    /**
     * Calls fmes diff on two files.
     *
     * @return       The delta
     * @param file1  The original file
     * @param file2  The modified file
     * @throws DiffException If something goes wrong during the diff
     **/
    public final Document diff(final File file1, final File file2) 
    throws DiffException {
        
        
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DOMOps.initParser(fac);

        DocumentBuilder db = null;
        Document doc1 = null;
        Document doc2 = null;

        try {
            db = fac.newDocumentBuilder();
            doc1 = db.parse(file1);
        } catch (ParserConfigurationException e) {
            throw new DiffException("Failed to set up XML parser", e);
        } catch (IOException e) {
            throw new DiffException("Failed to parse file "
                    + file1.getAbsolutePath(), e);            
        } catch (SAXException e) {
            throw new DiffException("Failed to parse file "
                    + file1.getAbsolutePath(), e);                        
        }

        try {
            doc2 = db.parse(file2);
        } catch (IOException e) {
            throw new DiffException("Failed to parse file "
                    + file2.getAbsolutePath(), e);            
        } catch (SAXException e) {
            throw new DiffException("Failed to parse file "
                    + file2.getAbsolutePath(), e);                        
        }

        return diff(doc1, doc2);
    }

    /**
     * Differences two DOM documents and returns the delta.
     *
     * The delta is in DUL format.
     *
     * @param doc1    The original document
     * @param doc2    The new document
     *
     * @return        A document describing the changes
     *                required to make doc1 into doc2.
     * @throws DiffException If something goes wrong during the diff
     */

    public final Document diff(final Document doc1, final Document doc2) 
    throws DiffException  {

        NodePairs matchings = Match.easyMatch(doc1, doc2);

        Document delta = null;
        try {
            delta = (new EditScript(doc1, doc2, matchings)).create();
        } catch (DocumentCreationException e) {
            throw new DiffException("Failed to create Edit Script ", e); 
        }

        return delta;
    }
}
