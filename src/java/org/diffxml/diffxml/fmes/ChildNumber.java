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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to hold and calculate DOM and XPath child numbers of node.
 * 
 * TODO: Needs a refactoring to deal with ignoring nodes in a cleaner way.
 * TODO: Check if this class is a bottleneck
 */
public final class ChildNumber {
    
    /** DOM child number. */
    private int mDOMChildNo = -1;

    /** XPath child number. */
    private int mXPathChildNo = -1;

    /** XPath char position. */
    private int mXPathCharPos = -1;

    /** In-order DOM child number. */
    private int mInOrderDOMChildNo = -1;

    /** In-order XPath child number. */
    private int mInOrderXPathChildNo = -1;
    
    /** In-order XPath text position. */
    private int mInOrderXPathCharPos = -1;
    
    /** The node we are doing the calcs on. */
    private final Node mNode;
    
    /** The siblings of the node and the node itself. */
    private NodeList mSiblings;
    
    
    /**
     * Default constructor.
     * 
     * @param n
     *            Node to find the child numbers of
     */
    public ChildNumber(final Node n) {
        
        if (n == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        if (n.getParentNode() == null) {
            throw new IllegalArgumentException("Node must have parent");
        }
        
        mNode = n;
        mSiblings = mNode.getParentNode().getChildNodes();
    }

    /**
     * Get the DOM child number.
     * 
     * @return DOM child number of associated node.
     */
    public int getDOM() {
        
        if (mDOMChildNo == -1) {
            calculateDOMChildNumber();
        }
        
        return mDOMChildNo;
    }

    /**
     * Get the XPath child number.
     * 
     * @return XPath child number of associated node.
     */
    public int getXPathCharPos() {
        
        if (mXPathCharPos == -1) {
            calculateXPathChildNumberAndPosition();
        }
        return mXPathCharPos;
    }

    /**
     * Get the XPath child number.
     * 
     * @return XPath child number of associated node.
     */
    public int getInOrderXPathCharPos() {
    
        if (mInOrderXPathCharPos == -1) {
            calculateInOrderXPathChildNumberAndPosition();
        }
        return mInOrderXPathCharPos;
    }

    
    /**
     * Get the XPath child number.
     * 
     * @return XPath child number of associated node.
     */
    public int getXPath() {
        
        if (mXPathChildNo == -1) {
            calculateXPathChildNumberAndPosition();
        }
        return mXPathChildNo;
    }

    /**
     * Get the in-order XPath child number.
     * 
     * Only counts nodes marked in-order.
     * 
     * @return In-order XPath child number of associated node.
     */
    public int getInOrderXPath() {

        if (mInOrderXPathChildNo == -1) {
            calculateInOrderXPathChildNumberAndPosition();
        }
        return mInOrderXPathChildNo;
    }

    /**
     * Get the in-order DOM child number.
     * 
     * Only counts nodes marked in-order.
     * 
     * @return In-order DOM child number of associated node.
     */
    public int getInOrderDOM() {

        if (mInOrderXPathChildNo == -1) {
            calculateInOrderDOMChildNumber();
        }
        return mInOrderDOMChildNo;
    }
    
    /**
     * Determines whether XPath index should be incremented.
     * 
     * Handles differences between DOM index and XPath index
     * 
     * @param i The current position in siblings
     * @return true If index should be incremented
     */
    private boolean incIndex(final int i) {

        boolean inc = true;
        Node curr = mSiblings.item(i);
 
        // Handle non-coalescing of text nodes
        if ((i > 0 && nodesAreTextNodes(curr, mSiblings.item(i - 1))) 
                || NodeOps.nodeIsEmptyText(curr)
                || curr.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
            inc = false;
        }

        return inc;
    }
    
    /**
     * Determines whether the given Nodes are all text nodes or not.
     * 
     * @param nodes The Nodes to checks.
     * @return true if all the given Nodes are text nodes
     */
    private static boolean nodesAreTextNodes(final Node... nodes) {

        boolean areText = true;

        for (Node n : nodes) {            
            if (!DOMOps.isText(n)) {
                areText = false;
                break;
            }

        }
        return areText;
    }

    /**
     * Calculates the DOM index of the node.
     * 
     */
    private void calculateDOMChildNumber() {
        
        int cn;
        
        for (cn = 0; cn < mSiblings.getLength(); cn++) {
            if (NodeOps.checkIfSameNode(mSiblings.item(cn), mNode)) {
                break;
            }
        }
        
        mDOMChildNo = cn;
    }

    /**
     * Calculates the "in order" DOM child number of the node.
     * 
     */
    private void calculateInOrderDOMChildNumber() {

        mInOrderDOMChildNo = 0;

        for (int i = 0; i < mSiblings.getLength(); i++) {
            if (NodeOps.checkIfSameNode(mSiblings.item(i), mNode)) {
                break;
            }
            if (NodeOps.isInOrder(mSiblings.item(i))) {
                mInOrderDOMChildNo++;
            }
        }
    }

    /**
     * Sets the XPath child number and text position.
     */
    private void calculateXPathChildNumberAndPosition() {
        
        int domIndex = calculateXPathChildNumber();
        calculateXPathTextPosition(domIndex);   
    }

    /**
     * Sets the XPath child number and text position.
     */
    private void calculateInOrderXPathChildNumberAndPosition() {
        
        int domIndex = calculateInOrderXPathChildNumber();
        calculateInOrderXPathTextPosition(domIndex);   
    }
    
    /**
     * Calculate the character position of the node.
     * 
     * @param domIndex The DOM index of the node in its siblings.
     */
    private void calculateXPathTextPosition(final int domIndex) {
        
        mXPathCharPos = 1;
        for (int i = (domIndex - 1); i >= 0; i--) {
            if (DOMOps.isText(mSiblings.item(i))) {
                mXPathCharPos = mXPathCharPos 
                    + mSiblings.item(i).getTextContent().length();
            } else {
                break;
            }
        }
    }

    /**
     * Set the XPath child number of the node.
     * 
     * @return The DOM index of the node in its siblings
     */
    private int calculateXPathChildNumber() {
        
        int childNo = 1;

        int domIndex;
        for (domIndex = 0; domIndex < mSiblings.getLength(); domIndex++) {
            
            if (NodeOps.checkIfSameNode(mSiblings.item(domIndex), mNode)) {
                
                if (!incIndex(domIndex)) {
                    childNo--;
                }
                break;
            }
            if (incIndex(domIndex)) {
                childNo++;
            }
        }
        
        mXPathChildNo = childNo;
        return domIndex;
    }

    /**
     * Set the in-order XPath child number of the node.
     * 
     * @return The DOM index of the node in its siblings
     */
    private int calculateInOrderXPathChildNumber() {

        int childNo = 0;
        int domIndex;
        Node lastInOrderNode = null;
        Node currNode = null;
        
        for (domIndex = 0; domIndex < mSiblings.getLength(); domIndex++) {
            currNode = mSiblings.item(domIndex);
            if (NodeOps.isInOrder(currNode)
                    && !(nodesAreTextNodes(currNode, lastInOrderNode) 
                        || NodeOps.nodeIsEmptyText(currNode))) {
                childNo++;
            }
            if (NodeOps.checkIfSameNode(currNode, mNode)) {
                break;
            }
            if (NodeOps.isInOrder(currNode)) {
                lastInOrderNode = currNode;
            }
        }
   
        //Add 1 if the given node wasn't in order
        if (currNode != null && !NodeOps.isInOrder(currNode)) {
            childNo++;
        }
   
        mInOrderXPathChildNo = childNo;
        return domIndex;
    }
    

    /**
     * Calculate the character position of the node.
     * 
     * @param domIndex The DOM index of the node in its siblings.
     */
    private void calculateInOrderXPathTextPosition(final int domIndex) {
        
        mInOrderXPathCharPos = 1;
        for (int i = (domIndex - 1); i >= 0; i--) {
            if (DOMOps.isText(mSiblings.item(i))) {
                if (NodeOps.isInOrder(mSiblings.item(i))) {
                    mInOrderXPathCharPos = mInOrderXPathCharPos 
                        + mSiblings.item(i).getTextContent().length();
                }
            } else if (NodeOps.isInOrder(mSiblings.item(i))) {
                break;
            }
        }
    }

    /**
     * Returns the XPath position, ignoring the given node.
     * 
     * @param n The node to ignore
     * @return The XPath position of the node ignoring n
     */
    public int getXPathIgnoring(final Node n) {

        int ret;
        if (n == null) {
            ret = getXPath();
        } else {

            if (n.isSameNode(mNode)) {
                throw new IllegalArgumentException(
                "Can't ignore the position node");
            }
            // Remove the node, run the old method, put it back in
            // *Always* use n to get the parent in case it is somewhere else in
            // the tree (in which case we don't need to remove it, but it's 
            // easier than checking
            Node refNode = n.getNextSibling();
            Node nPar = n.getParentNode();
            nPar.removeChild(n);

            //Invalidate cache
            mXPathChildNo = -1;
            ret = getXPath();
            nPar.insertBefore(n, refNode);
            mXPathChildNo = -1;
        }
        
        return ret;
    }

    /**
     * Gets the DOM index of a node, ignoring the given node.
     * @param n The node to ignore
     * @return The DOM index of the node, ignoring n
     */
    public int getDOMIgnoring(final Node n) {
        
        int ret;
        if (n == null) {
            ret = getDOM();
        } else {
            
            if (n.isSameNode(mNode)) {
                throw new IllegalArgumentException(
                "Can't ignore the position node");
            }
            
            // Remove the node, run the old method, put it back in
            // *Always* use n to get the parent in case it is somewhere else in
            // the tree (in which case we don't need to remove it, but it's 
            // easier than checking
            Node refNode = n.getNextSibling();
            Node nPar = n.getParentNode();
            nPar.removeChild(n);
            //Invalidate cache
            mDOMChildNo = -1;
            ret = getDOM();
            nPar.insertBefore(n, refNode);
            mDOMChildNo = -1;
        }

        return ret;
    }

    /**
     * Gets the XPath character position of a node, ignoring the given node.
     * @param n The node to ignore
     * @return The DOM index of the node, ignoring n
     */
    public int getXPathCharPosIgnoring(final Node n) {

        int ret;
        if (n == null) {
            ret = getXPathCharPos();
        } else {

            if (n.isSameNode(mNode)) {
                throw new IllegalArgumentException(
                "Can't ignore the position node");
            }
            // Remove the node, run the old method, put it back in
            // *Always* use n to get the parent in case it is somewhere else in
            // the tree (in which case we don't need to remove it, but it's 
            // easier than checking
            Node refNode = n.getNextSibling();
            Node nPar = n.getParentNode();
            nPar.removeChild(n);
            //Invalidate cache
            mXPathCharPos = -1;
            ret = getXPathCharPos();
            nPar.insertBefore(n, refNode);
            mXPathCharPos = -1;
        }
        
        return ret;
    }

}
