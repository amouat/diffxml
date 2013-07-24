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

import java.util.List;
import java.io.IOException;

import org.diffxml.diffxml.DOMOps;
import org.diffxml.diffxml.DiffFactory;
import org.diffxml.diffxml.fmes.delta.DULDelta;
import org.diffxml.diffxml.fmes.delta.DeltaIF;
import org.diffxml.diffxml.fmes.delta.DeltaInitialisationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Creates the edit script for the fmes algorithm.
 *
 * Uses the algorithm described in the paper
 * "Change Detection in Hierarchically Structure Information".
 *
 * @author Adrian Mouat
 */
public final class EditScript {
        
    /**
     * The original document.
     */
    private final Document mDoc1;
    
    /**
     * The modified document.
     */
    private final Document mDoc2;
    
    /**
     * The set of matching nodes.
     */
    private NodePairs mMatchings;
    
    /**
     * The EditScript.
     */
    private DeltaIF mDelta;
    
    /**
     * Constructor for EditScript.
     * Used to create a list of modifications that will turn doc1 into doc2,
     * given a set of matching nodes.
     * 
     * @param doc1      the original document
     * @param doc2      the modified document
     * @param matchings the set of matching nodes
     */
    public EditScript(final Document doc1, final Document doc2,
            final NodePairs matchings) {
        
        mDoc1 = doc1;
        mDoc2 = doc2;
        mMatchings = matchings;
    }
    
    /**
     * Creates an Edit Script conforming to matchings that transforms
     * doc1 into doc2.
     *
     * Uses algorithm in "Change Detection in Hierarchically Structured
     * Information".
     *
     * @return the resultant Edit Script
     * @throws DocumentCreationException When the output doc can't be made
     */
    public Document create() throws DocumentCreationException {

        try {
            mDelta = new DULDelta();
        } catch (DeltaInitialisationException e) {
            throw new DocumentCreationException("Failed to create edit script",
                    e);
        }

        // Fifo used to do a breadth first traversal of doc2
        NodeFifo fifo = new NodeFifo();
        fifo.addChildrenOfNode(mDoc2);
        
        Node doc2docEl = mDoc2.getDocumentElement();
        //Special case for aligning children of root node
        alignChildren(mDoc1, mDoc2, mMatchings);

        while (!fifo.isEmpty()) {
            
            Node x = fifo.pop();
            fifo.addChildrenOfNode(x);

            Node y = x.getParentNode();
            Node z = mMatchings.getPartner(y);
            Node w = mMatchings.getPartner(x);

            if (!mMatchings.isMatched(x)) {
                w = doInsert(x, z);
            } else {
                // TODO: Update should go here
                // Special case for document element
                if (NodeOps.checkIfSameNode(x, doc2docEl)
                        && !Match.compareElements(w, x)) {
                    w = doUpdate(w, x);
                } else if (!mMatchings.getPartner(y).equals(
                        w.getParentNode())) {
                    doMove(w, x, z, mMatchings);
                }
            }

            alignChildren(w, x, mMatchings);
        }

        deletePhase(mDoc1, mMatchings);

        // TODO: Assert following
        // Post-Condition es is a minimum cost edit script,
        // Matchings is a total matching and
        // doc1 is isomorphic to doc2

        return mDelta.getDocument();
    }

    /**
     * Updates a Node to the value of another node.
     * 
     * @param w The Node to be updated
     * @param x The Node to make it like
     * @return The new Node
     */
    private Node doUpdate(final Node w, final Node x) {
        
        Document doc1 = w.getOwnerDocument();
        Node newW = null;
        if (w.getNodeType() == Node.ELEMENT_NODE) {

            mDelta.update(w, x);

            //Unfortunately, you can't change the node name in DOM, so we need
            //to create a new node and copy it all over
            
            //TODO: Note you don't actually *need* to do this!!!
            //TODO: Only call this when in debug
            newW = doc1.createElementNS(x.getNamespaceURI(), x.getLocalName());
            
            // Copy x's attributes to the new element except xml
            NamedNodeMap attrs = x.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                if (!NodeOps.isNamespaceAttr(attrs.item(i))) {
                    Attr attr2 = (Attr) doc1.importNode(attrs.item(i), true);
                    newW.getAttributes().setNamedItem(attr2);
                }
            }
            
            // Move all *w's* children
            while (w.hasChildNodes()) {
                newW.appendChild(w.getFirstChild());
            }
            
            w.getParentNode().replaceChild(newW, w);
            mMatchings.remove(w);
            mMatchings.add(newW, x);   
        }
        
        return newW;
    }
    
    /**
     * Inserts (the import of) node x as child of z according to the algorithm 
     * and updates the Edit Script.
     *
     * @param x          current node
     * @param z          partner of x's parent
     * @return           the inserted node
     */
    private Node doInsert(final Node x, final Node z) {

        assert (x != null);
        assert (z != null);

        if (x.getNodeType() == Node.DOCUMENT_TYPE_NODE) {

            //Doctype nodes can't be effectively (or at least easily) 
            //edited with XPath and DOM
            throw new IllegalArgumentException("Doctype nodes not supported");

        }

        //Find the child number (k) to insert w as child of z 
        FindPosition pos = new FindPosition(x, mMatchings);

        //Apply insert to doc1
        //The node we want to insert is the copy of x with attributes but no
        //children
        Node w = NodeOps.copyNodeToDoc(mDoc1, x); 

        //Need to set in order as won't be revisited
        NodeOps.setInOrder(w);
        NodeOps.setInOrder(x);

        mDelta.insert(w, z, pos.getXPathInsertPosition(), 
                pos.getCharInsertPosition());

        //Take match of parent (z), and insert
        w = DOMOps.insertAsChild(pos.getDOMInsertPosition(), z, w);

        outputDebug();
        //Add to matching set
        mMatchings.add(w, x);

        return w;
    }

    /**
     * Performs a move operation according to the algorithm and updates
     * the EditScript.
     *
     * @param w          the node to be moved
     * @param x          the matching node
     * @param z          the partner of x's parent
     * @param matchings  the set of matching nodes
     */
    private void doMove(final Node w, final Node x, final Node z, 
            final NodePairs matchings) {

        Node v = w.getParentNode();
        Node y = x.getParentNode();

        //Apply move if parents not matched and not null

        Node partnerY = matchings.getPartner(y);
        assert !NodeOps.checkIfSameNode(v, partnerY);

        FindPosition pos = new FindPosition(x, matchings);

        NodeOps.setInOrder(w);
        NodeOps.setInOrder(x);

        mDelta.move(w, z, pos.getXPathInsertPosition(), 
                pos.getCharInsertPosition());

        //Apply move to T1
        DOMOps.insertAsChild(pos.getDOMInsertPosition(), z, w);
        outputDebug();
    }

    /**
     * Performs the deletePhase of the algorithm.
     *
     * @param n          the current node
     * @param matchings  the set of matching nodes
     */
    private void deletePhase(final Node n, final NodePairs matchings) {
        
        // Deletes nodes in Post-order traversal
        NodeList kids = n.getChildNodes();
        if (kids != null) {
            // Note that we loop *backward* through kids
            for (int i = (kids.getLength() - 1); i >= 0; i--) {
                deletePhase(kids.item(i), matchings);
            }
        }

        // If node isn't matched, delete it
        if (!matchings.isMatched(n) 
                && n.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
            mDelta.delete(n);
            n.getParentNode().removeChild(n);
         
        }
    }

    /**
     * Mark the children of a node out of order.
     *
     * @param n the parent of the nodes to mark out of order
     */
    private static void markChildrenOutOfOrder(final Node n) {

        NodeList kids = n.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            NodeOps.setOutOfOrder(kids.item(i));
        }
    }

    /**
     * Mark the children of a node in order.
     *
     * @param n the parent of the nodes to mark in order
     */
    private static void markChildrenInOrder(final Node n) {

        NodeList kids = n.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            NodeOps.setInOrder(kids.item(i));
        }
    }
    
    /**
     * Marks the Nodes in the given list and their partners "inorder".
     *
     * @param seq  the Nodes to mark "inorder"
     * @param matchings the set of matching Nodes
     */
    private static void setNodesInOrder(final List<Node> seq,
            final NodePairs matchings) {

        for (Node node : seq) {
            NodeOps.setInOrder(node);
            NodeOps.setInOrder(matchings.getPartner(node));
        }
    }

    /**
     * Moves nodes that are not in order to correct position.
     *
     * @param w Node with potentially misaligned children
     * @param wSeq Sequence of children of w that have matches in the children
     *             of x
     * @param stay The List of nodes not to be moved
     * @param matchings The set of matching nodes
     */
    private void moveMisalignedNodes(final Node w, final Node[] wSeq, 
            final List<Node> stay, final NodePairs matchings) {
        
        //Get Nodes that are not in LCS but are in wSeq (or xSeq)
        for (Node a : wSeq) {
            if (!stay.contains(a)) {

                Node b = matchings.getPartner(a);
                FindPosition pos = new FindPosition(b, matchings);

                mDelta.move(a, w, pos.getXPathInsertPosition(),
                        pos.getCharInsertPosition());

                DOMOps.insertAsChild(pos.getDOMInsertPosition(), w, a);

                NodeOps.setInOrder(a);
                NodeOps.setInOrder(b);
                outputDebug();
            }
        }
    }

    /**
     * Aligns children of current node that are not in order.
     *
     * @param w  the match of the current node.
     * @param x  the current node

     * @param matchings  the set of matchings
     */
    private void alignChildren(final Node w, final Node x,
            final NodePairs matchings) {
        
        //Order of w and x is important
        markChildrenOutOfOrder(w);
        markChildrenOutOfOrder(x);

        NodeList wKids = w.getChildNodes();
        NodeList xKids = x.getChildNodes();

        Node[] wSeq = NodeSequence.getSequence(wKids, xKids, matchings);
        Node[] xSeq = NodeSequence.getSequence(xKids, wKids, matchings);

        List<Node> lcsSeq = NodeSequence.getLCS(wSeq, xSeq, matchings);
        setNodesInOrder(lcsSeq, matchings);
        
        moveMisalignedNodes(w, wSeq, lcsSeq, matchings);
        
        //The following is missing from the algorithm, but is important
        markChildrenInOrder(w);
        markChildrenInOrder(x);
    }

    /**
     * Outputs debug information.
     */
    private void outputDebug() {

        if (DiffFactory.isDebug()) {
            System.err.println("Result:");
            try {
                DOMOps.outputXML(mDoc1, System.err);
            } catch (IOException e) {
                System.err.println("Failed to print debug info");
            }
            System.err.println();
            System.err.println();
        }
    }
}
