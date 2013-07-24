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

/**
 * Associates depth with a node.
 *
 * @author Adrian Mouat
 */

public class NodeDepth {
    /**
     * Field holding nodes depth.
     */
    private final int mDepth;

    /**
     * Node we're pointing to.
     */
    private final Node mNode;

    /**
     * Create a NodeDepth for the given node.
     *
     * @param node The node to find the depth of
     */
    NodeDepth(final Node node) {
        
        if (node == null) {
            throw new NullPointerException("Node cannot be null");
        }
        mNode = node;
        mDepth = calculateDepth(mNode);
    }
    
    /**
     * Calculates the depth of a Node.
     * 
     * The root Node is at depth 0.
     * 
     * @param node The Node to calculate the depth of
     * @return The depth of the node
     */
    private int calculateDepth(final Node node) {
        
        int depth = 0;
        Node tmpNode = node;
        Node doc;
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            doc = node;
        } else {
            doc = tmpNode.getOwnerDocument();
        }

        while (!tmpNode.equals(doc)) {
            depth++;
            tmpNode = tmpNode.getParentNode();
        }
        return depth;
    }

    /**
     * Determines if two NodeInfo objects are equal.
     * 
     * Just calls equals on the underlying nodes.
     *
     * @param o NodeInfo to compare with
     * @return True if nodes are equal, otherwise false
     */

    public final boolean equals(final Object o) {

        boolean equals = false;
        
        if (o instanceof NodeDepth) {
            NodeDepth ni = (NodeDepth) o;
            equals = ni.mNode.equals(this.mNode);
        }
        
        return equals;
    }

    /**
     * Hashcode from underlying node.
     * 
     * @return hashcode
     */
    public final int hashCode() {
        return mNode.hashCode();
    }

    /**
     * Returns the depth value.
     *
     * @return The current depth value
     */
    public final int getDepth() {
        return mDepth;
    }

    /**
     * Returns the underlying node.
     *
     * @return The Node.
     */
    public final Node getNode() {
        return mNode;
    }
 
}
