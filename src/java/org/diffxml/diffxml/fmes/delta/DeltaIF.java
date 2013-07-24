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
package org.diffxml.diffxml.fmes.delta;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Interface for Delta formats.
 * 
 * Implement this to plug-in a new delta format.
 * 
 * @author Adrian Mouat
 *
 */
public interface DeltaIF {
    
    /**
     * Adds a Move operation to the EditScript. 
     * 
     * @param n The node being moved
     * @param parent XPath to the new parent Node
     * @param childno Child number of the parent n will become
     * @param ncharpos The new character position for the Node
     */
    void move(final Node n, final Node parent, final int childno, 
            final int ncharpos);
    
    /**
     * Adds a delete operation to the EditScript for the given Node.
     * 
     * @param n The Node that is to be deleted
     */
    void delete(final Node n);
    
    /**
     * Adds an insert operation to the EditScript.
     * 
     * @param n The node to insert
     * @param parent The Node to be parent of n
     * @param childno The child number of the parent node that n will become
     * @param charpos The character position to insert at
     */
    void insert(final Node n, final Node parent, final int childno,
            final int charpos);

    /**
     * Adds an update operation to the EditScript.
     * 
     * @param w The node to be updated
     * @param x The node it should be equal to
     */
    void update(Node w, Node x);

    /**
     * Get the XML Document for the EditScript.
     * 
     * @return The EditScript as an XML document.
     */
    Document getDocument();

}
