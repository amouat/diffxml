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

import org.w3c.dom.Node;
import java.util.ArrayList;

/**
 * Class to hold pairs of nodes.
 * TODO: Test if any performance benefit from using UserData.
 */
public class NodePairs {

    /**
     * Key for user data on whether the node is matched.
     */
    private static final String MATCHED = "matched";
    
    /**
     * Internal list to store nodes.
     */
    private ArrayList<Node> mPairs = new ArrayList<Node>();

    /**
     * Adds a pair of nodes to the set.
     * Sets UserData as matched.
     * 
     * @param x
     *            first node
     * @param y
     *            partner of first node
     */
    public final void add(final Node x, final Node y) {
        
        if (x == null || y == null) {
            throw new NullPointerException("Nodes cannot be null");
        }
        
        mPairs.add(x);
        mPairs.add(y);
        setMatched(x, y);
    }

    /**
     * Mark the node as being "matched".
     *
     * @param n the node to mark as "matched"
     */
    private static void setMatched(final Node n) {
        n.setUserData(MATCHED, true, null);
    }

    /**
     * Check if node is marked "matched".
     *
     * Made static so that I can use a instance method later if it is faster or
     * better.
     * 
     * @param n node to check
     * @return true if marked "matched", false otherwise
     */
    public final boolean isMatched(final Node n) {
        
        boolean ret;
        Object data = n.getUserData(MATCHED);
        if (data == null) {
            ret = false;
        } else {
            ret = (Boolean) data;
        }
        return ret;
    }
    
    /**
     * Mark a pair of nodes as matched.
     *
     * @param nodeA  The unmatched partner of nodeB
     * @param nodeB  The unmatched partner of nodeA
     */
    private static void setMatched(final Node nodeA, final Node nodeB) {
        setMatched(nodeA);
        setMatched(nodeB);
    }
    
    /**
     * Returns the partner of a given node. Returns null if the node does not
     * exist.
     * 
     * @param n
     *            the node to find the partner of.
     * @return the partner of n.
     */
    public final Node getPartner(final Node n) {

        Node ret = null;
        int in = mPairs.indexOf(n);

        if (in != -1) {

            if ((in % 2) == 1) {
                ret = mPairs.get(--in);
            } else {
                ret = mPairs.get(++in);
            }
        }

        return ret;
    }

    /**
     * Get the number of nodes stored. 
     * 
     * Note that this includes both nodes and partners.
     * 
     * @return The number of nodes stored.
     */
    public final int size() {
        return mPairs.size();
    }

    /**
     * Remove a node and it's partner from the list of matchings.
     * 
     * @param n The Node to remove
     */
    public final void remove(final Node n) {
        
        Node nMatch = getPartner(n);
        
        nMatch.setUserData(MATCHED, null, null);
        n.setUserData(MATCHED, null, null);
        
        mPairs.remove(getPartner(n));
        mPairs.remove(n);
    }
}
