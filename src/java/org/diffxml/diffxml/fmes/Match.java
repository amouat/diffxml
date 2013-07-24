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

import org.diffxml.diffxml.DOMOps;
import org.diffxml.diffxml.DiffFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.DocumentTraversal;

/**
 * Solves the "good matchings" problem for the FMES algorithm.
 *
 * Essentially pairs nodes that match between documents.
 * Uses the "fast match" algorithm is detailed in the paper
 * "Change Detection in Hierarchically Structure Information".
 *
 * WARNING: Will only work correctly with acylic documents.
 * TODO: Add alternate matching code for cylic documents.
 * See: http://www.devarticles.com/c/a/Development-Cycles/How-to-Strike-a-Match/
 * for information on how to match strings.
 *
 * @author Adrian Mouat
 */

public final class Match {

    /**
     * Private constructor.
     */
    private Match() {
        //Shouldn't be called
    }
    
    /**
     * Performs fast match algorithm on given DOM documents.
     * 
     *  TODO: May want to consider starting at same point in 2nd tree somehow, 
     *  may lead to better matches.
     * 
     * @param doc1
     *            The original document
     * @param doc2
     *            The modified document
     * 
     * @return NodeSet containing pairs of matching nodes.
     */
    public static NodePairs easyMatch(final Document doc1,
            final Document doc2) {

        NodePairs matchSet = new NodePairs();

        doc1.getDocumentElement().normalize();
        doc2.getDocumentElement().normalize();

        List<NodeDepth> list1 = initialiseAndOrderNodes(doc1);
        List<NodeDepth> list2 = initialiseAndOrderNodes(doc2);
        
        //Explicitly add document elements, doctype elements and root
        matchSet.add(doc1, doc2);
        matchSet.add(doc1.getDocumentElement(), doc2.getDocumentElement());
        
        //Kill any doctype nodes - they can't be edited effectively with DOM
        //nor can xpath select them
        if (doc1.getDoctype() != null) {
            doc1.removeChild(doc1.getDoctype());
        }
        if (doc2.getDoctype() != null) {
            doc2.removeChild(doc2.getDoctype());
        }
        
        // Proceed bottom up on List 1
        for (NodeDepth nd1 : list1) {
            Node n1 = nd1.getNode();
            
            for (NodeDepth nd2 : list2) {                   
                Node n2 = nd2.getNode();
                
                if (compareNodes(n1, n2)) {
                    matchSet.add(n1, n2);
                    
                    //Don't want to consider it again
                    list2.remove(nd2);
                    break;
                }
            }
        }

        outputDebug(matchSet, doc1);
        return matchSet;
    }

    /**
     * Outputs information on the matches for debug purposes.
     * 
     * @param matchSet The set of matching Nodes.
     * @param doc The first document being differenced
     */
    private static void outputDebug(final NodePairs matchSet,
            final Document doc) {
        
        if (DiffFactory.isDebug()) {
            NodeIterator ni = ((DocumentTraversal) doc).createNodeIterator(
                    doc.getDocumentElement(), NodeFilter.SHOW_ALL, null, false);
 
            Node n;
            while ((n = ni.nextNode()) != null) {
                System.err.print(DOMOps.getNodeAsString(n));
                if (matchSet.isMatched(n)) {
                    System.err.println(" matches "
                            + DOMOps.getNodeAsString(matchSet.getPartner(n)));
                } else {
                    System.err.println(" unmatched");
                }
            }
            
            ni.detach();
            System.err.println();
        }
    }
    
    /**
     * Compares two elements to determine whether they should be matched.
     * 
     * xmlns attributes are ignored.
     * 
     * TODO: This method is critical in getting good results. Will need to be
     * tweaked. In addition, it may be an idea to allow certain doc types to
     * override it. Consider comparing position, matching of kids etc.
     * 
     * @param a
     *            First element
     * @param b
     *            Potential match for b
     * @return true if nodes match, false otherwise
     */
    public static boolean compareElements(final Node a, final Node b) {

        boolean ret = false;
        
        if (equalsOrBothNullOrEmpty(a.getNamespaceURI(), b.getNamespaceURI())) {
                
            if (NodeOps.getLocalName(a).equals(NodeOps.getLocalName(b))) {

                //Compare attributes

                //Attributes are equal until we find one that doesn't match
                ret = true;

                NamedNodeMap aAttrs = a.getAttributes();
                NamedNodeMap bAttrs = b.getAttributes();

                int noAAttrs = 0;
                if (aAttrs != null) {
                    noAAttrs = aAttrs.getLength();
                }
       
                int i = 0;
                int numANonXMLNSAttrs = 0;
                while (ret && (i < noAAttrs)) {
                    // Check if attr exists in other tag if not xmlns
                    Attr aItem = (Attr) aAttrs.item(i);
                    if (!NodeOps.isNamespaceAttr(aItem)) {
                        numANonXMLNSAttrs++;
                        
                        Attr bItem = (Attr) bAttrs.getNamedItemNS(
                                aItem.getNamespaceURI(), 
                                aItem.getLocalName()); 
                        if (bItem == null || !bItem.getNodeValue().equals(
                                aAttrs.item(i).getNodeValue())) {
                            ret = false;
                        } 
                    }
                    i++;
                }
                
                //Finally we need to check there are no extra attributes in b
                //that are not xmlns attributes
                if (ret != false) {
                    int noBAttrs = 0;
                    int numBNonXMLNSAttrs = 0;
                    if (bAttrs != null) {
                        noBAttrs = bAttrs.getLength();
                    }
                    for (i = 0; i < noBAttrs; i++) {
                        if (!NodeOps.isNamespaceAttr(bAttrs.item(i))) {
                            numBNonXMLNSAttrs++;
                        }
                    }

                    if (numBNonXMLNSAttrs != numANonXMLNSAttrs) {
                        ret = false;
                    }
                }
                
            }
        }
        
        return ret;
    }

    /**
     * Helper method just checks if String are equal or both null or empty.
     * 
     * @param a String to compare to b
     * @param b String to compare to a
     * @return True if Strings equal or both are null or empty
     */
    private static boolean equalsOrBothNullOrEmpty(String a, String b) {
        
        if (a == null || a.trim() == "") {
            return (b == null || b.trim() == "");
        } 
        
        return a.equals(b);
    }

    /**
     * Compares two text nodes to determine if they should be matched.
     * 
     * Takes into account whitespace options.
     * 
     * @param a
     *            First node
     * @param b
     *            Potential match for a
     * @return True if nodes match, false otherwise
     */

    private static boolean compareTextNodes(final Node a, final Node b) {

        String aString = a.getNodeValue();
        String bString = b.getNodeValue();

        if (DiffFactory.isIgnoreAllWhitespace()) {
            // Remove whitespace from nodes before comparison
            // TODO: Check nextToken doesn't skip first
            StringTokenizer st = new StringTokenizer(aString);
            StringBuffer stringBuf = new StringBuffer(aString.length());
            
            while (st.hasMoreTokens()) {
                stringBuf.append(st.nextToken());
            }
            aString = stringBuf.toString();

            st = new StringTokenizer(bString);
            stringBuf = new StringBuffer(bString.length());         
            while (st.hasMoreTokens()) {
                stringBuf.append(st.nextToken());
            }
            bString = stringBuf.toString();
            
        } else if (DiffFactory.isIgnoreLeadingWhitespace()) {
            // Ignore leading ws
            // just call trim
            aString = aString.trim();
            bString = bString.trim();
        }

        // Check case optn
        boolean ret;
        if (DiffFactory.isIgnoreCase()) {
            ret = (aString.equalsIgnoreCase(bString));
        } else {
            ret = (aString.equals(bString));
        }
        
        return ret;
    }

    /**
     * Compares 2 nodes to determine whether they should match.
     * 
     * TODO: Check if more comparisons are needed
     * TODO: Consider moving out to a separate class, implementing an interface
     * 
     * @param a
     *            first node
     * @param b
     *            potential match for a
     * @return true if nodes match, false otherwise
     */
    private static boolean compareNodes(final Node a, final Node b) {

        boolean ret = false;

        if (a.getNodeType() == b.getNodeType()) { 

            switch (a.getNodeType()) {
                case Node.ELEMENT_NODE :
                    ret = compareElements(a, b);
                    break;
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    ret = compareTextNodes(a, b);
                    break;
                case Node.DOCUMENT_NODE :
                    //Always match document nodes
                    ret = true;
                    break;
                default :
                    ret = (a.getNodeValue().equals(b.getNodeValue()));
            }
        }
        
        return ret;
    }

    /**
     * Returns a list of Nodes sorted according to their depths.
     * 
     * Does *NOT* include root or documentElement
     * 
     * TreeSet is sorted in reverse order of depth according to
     * NodeInfoComparator.
     * 
     * @param doc The document to be initialised and ordered.
     * @return A depth-ordered list of the nodes in the doc.
     */
    private static List<NodeDepth> initialiseAndOrderNodes(
            final Document doc) {

        NodeIterator ni = ((DocumentTraversal) doc).createNodeIterator(
                doc, NodeFilter.SHOW_ALL, null, false);

        List<NodeDepth> depthSorted = new ArrayList<NodeDepth>();
             
        Node n;
        while ((n = ni.nextNode()) != null) {
            if (!(NodeOps.checkIfSameNode(doc, n) 
                    || NodeOps.checkIfSameNode(doc.getDocumentElement(), n)
                    || n.getNodeType() == Node.DOCUMENT_TYPE_NODE)) {
                depthSorted.add(new NodeDepth(n));
            }
        }
        
        ni.detach();
        Collections.sort(depthSorted, new NodeDepthComparator());
        
        return depthSorted;
    }
}

