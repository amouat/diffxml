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

import java.util.LinkedList;
import java.util.Queue;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * Implements a First In First Out list.
 *
 * Equivalent to a stack where elements are removed from
 * the *opposite* end to where the are added. Hence the
 * Stack terms "push" and pop" are used.
 * 
 * Only real addition over Java library is method to add children of a node.
 */

public class NodeFifo {
    
    /**
     * Underlying list.
     */
    private final Queue<Node> mFifo;

    /**
     * Default constructor.
     */
    NodeFifo() {
        
        /*
         * TODO: Check if ArrayList is faster.
         */
        mFifo = new LinkedList<Node>();
    }

    /**
     * Adds a Node to the Fifo.
     *
     * @param n the Node to added
     */
    public final void push(final Node n) {
        mFifo.add(n);
    }

    /**
     * Checks if the Fifo contains any objects.
     *
     * @return true if there are any objects in the Fifo
     */

    public final boolean isEmpty() {
        return mFifo.isEmpty();
    }

    /**
     * Remove a Node from the Fifo.
     *
     * This Node is always the oldest item in the array.
     *
     * @return the oldest item in the Fifo
     */
    public final Node pop() {

        Node ret;
        if (mFifo.isEmpty()) {
            ret = null;
        } else {
            ret = mFifo.remove();
        }

        return ret;
    }

    /**
     * Adds the children of a node to the fifo.
     *
     * TODO: Check use of isBanned()
     * 
     * @param x    the node whose children are to be added
     */
    public final void addChildrenOfNode(final Node x) {
        
        NodeList kids = x.getChildNodes();

        if (kids != null) {
            for (int i = 0; i < kids.getLength(); i++) {
                if (Fmes.isBanned(kids.item(i))) {
                    continue;
                }

                push(kids.item(i));
            }
        }
    }

}
