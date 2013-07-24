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

/**
 * Finds the position to insert a Node at.
 * 
 * Calculates XPath, DOM and character position.
 * @author Adrian Mouat
 *
 */
public class FindPosition {
 
    /** The DOM position. */
    private int mInsertPositionDOM;
    
    /** The XPath position. */
    private int mInsertPositionXPath;
    
    /** The character position. */
    private int mCharInsertPosition;
    
    /**
     * Finds the child number to insert a node as.
     *
     * (Equivalent to the current child number of the node to insert
     * before)
     *
     * @param x         the node with no partner
     * @param matchings the set of matching nodes
     */
    public FindPosition(final Node x, final NodePairs matchings) {

        Node v = getInOrderLeftSibling(x);

        if (v == null) {
            
            mInsertPositionDOM = 0;
            mInsertPositionXPath = 1;
            mCharInsertPosition = 1;
            
        } else {

            /**
             * Get partner of v and return index after
             * (we want to insert after the previous in-order node, so that
             * w's position is equivalent to x's).
             */
            Node u = matchings.getPartner(v);
            assert (u != null);

            ChildNumber uChildNo = new ChildNumber(u);
            Node w = matchings.getPartner(x);

            //Need position after u
            //NOTE: This is different from the FMES algorithm, which *wrongly*
            //indicates we should use the "in-order" child number.
            if (w != null) {
                //Doing a move, need to be careful not to count node being moved
                mInsertPositionDOM = uChildNo.getDOMIgnoring(w) + 1;
                mInsertPositionXPath = uChildNo.getXPathIgnoring(w) + 1;
            } else {
                mInsertPositionDOM = uChildNo.getDOM() + 1;
                mInsertPositionXPath = uChildNo.getXPath() + 1;
            }

            //For xpath, character position is used if node is text node
            if (DOMOps.isText(u)) {
                if (w != null) {
                    mCharInsertPosition = uChildNo.getXPathCharPosIgnoring(w)
                        + u.getTextContent().length();
                } else {
                    mCharInsertPosition = uChildNo.getXPathCharPos()
                        + u.getTextContent().length();
                }
            } else {
                mCharInsertPosition = 1;
            }
        }

    }
    
    /**
     * Gets the rightmost left sibling of n marked "inorder".
     *
     * @param n Node to find "in order" left sibling of
     * @return  Either the "in order" left sibling or null if none
     */
    private static Node getInOrderLeftSibling(final Node n) {
        
        Node curr = n.getPreviousSibling();
        while (curr != null && !NodeOps.isInOrder(curr)) {
            curr = curr.getPreviousSibling();
        }

        return curr;
    }

    /**
     * Returns the DOM number the node should have when inserted.
     * 
     * @return the DOM number to insert the node as
     */
    public final int getDOMInsertPosition() {
        return mInsertPositionDOM;
    }
    
    /**
     * Returns the XPath number the node should have when inserted.
     * 
     * @return The XPath number to insert the node as
     */
    public final int getXPathInsertPosition() {
        return mInsertPositionXPath;
    }
    
    /**
     * Returns the character position to insert the node as.
     * 
     * @return The character position to insert the node at
     */
    public final int getCharInsertPosition() {
        return mCharInsertPosition;
    }
}
