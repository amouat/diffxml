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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Class to handle general diffxml operations on Nodes.
 *
 */
public final class NodeOps {
    
    /**
     * Key for user data on whether the node is in order.
     */
    private static final String INORDER = "inorder";
    
    /**
     * XML Namepscae URI. Probably a better place to get this from.
     */
    public static final String XMLNS = "http://www.w3.org/2000/xmlns/";
    
    /**
     * Disallow instantiation.
     */
    private NodeOps() {
    }
    
    /**
     * Mark the node as being "inorder".
     *
     * @param n the node to mark as "inorder"
     */
    public static void setInOrder(final Node n) {

        n.setUserData(INORDER, true, null);
    }

    /**
     * Mark the node as not being "inorder".
     *
     * @param n the node to mark as not "inorder"
     */
    public static void setOutOfOrder(final Node n) {
        n.setUserData(INORDER, false, null);
    }

    /**
     * Check if node is marked "inorder".
     *
     * Note that nodes are inorder by default.
     *
     * @param n node to check
     * @return false if UserData set to False, true otherwise
     */
    public static boolean isInOrder(final Node n) {
        
        boolean ret;
        Object data = n.getUserData(INORDER);
        if (data == null) {
            ret = true;
        } else {
            ret = (Boolean) data;
        }
        return ret;
    }


    /**
     * Check if nodes are the same.
     *
     * Does not test if data equivalent, but if same node in same doc.
     * TODO: Handle null cases.
     *
     * @param x first node to check
     * @param y second node to check
     * @return true if same node, false otherwise
     */

    public static boolean checkIfSameNode(final Node x, final Node y) {
        return x.isSameNode(y);
    }

    /**
     * Calculates an XPath that uniquely identifies the given node.
     * For text nodes note that the given node may only be part of the returned
     * node due to coalescing issues; use an offset and length to identify it
     * unambiguously.
     * 
     * @param n The node to calculate the XPath for.
     * @return The XPath to the node as a String
     */
    public static String getXPath(final Node n) {

 
        String xpath;
        
        if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
            //Slightly special case for attributes as they are considered to
            //have no parent
            ((Attr) n).getOwnerElement();
            xpath = getXPath(((Attr) n).getOwnerElement())
                 + "/@" + n.getNodeName();
            
        } else if (n.getNodeType() == Node.DOCUMENT_NODE) {
            
            xpath = "/";
        } else if (n.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
            
            throw new IllegalArgumentException(
                    "DocumentType nodes cannot be identified with XPath");
            
        } else if (n.getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
            
            ChildNumber cn = new ChildNumber(n);
            xpath = "/node()[" + cn.getXPath() + "]"; 
            
        } else {

            ChildNumber cn = new ChildNumber(n);

            xpath = getXPath(n.getParentNode()) 
                + "/node()[" + cn.getXPath() + "]";
        }
        
        return xpath;
    }
    
    /**
     * Check if node is an empty text node.
     * 
     * @param n The Node to test.
     * @return True if it is a 0 sized text node
     */
    public static boolean nodeIsEmptyText(final Node n) {
        return (n.getNodeType() == Node.TEXT_NODE 
            && n.getNodeValue().length() == 0);
    }

    /**
     * Copies a node from one Document to another, including attributes but
     * no children.
     * 
     * Required as importNode does not handle namespaces well for element nodes
     * 
     * @param mDoc1 Document that x is to be copied to
     * @param x The node to copy
     * @return A copy of the node in mDoc1
     */
    public static Node copyNodeToDoc(Document doc, Node x) {
        
        Node copy;
        if (x.getNodeType() == Node.ELEMENT_NODE) {
            Element copyEl = doc.createElementNS(
                    x.getNamespaceURI(), x.getNodeName());
            NamedNodeMap attrs = ((Element) x).getAttributes();
            
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr a = (Attr) attrs.item(i);
                copyEl.setAttributeNS(
                        a.getNamespaceURI(), a.getNodeName(), a.getNodeValue());    
            }
            
            copy = copyEl;
            
        } else {
            copy = doc.importNode(x, false);
        }
        
        return copy;
    }

    /**
     * Gets the local name of the node if not null, else just the node name.
     * 
     * Avoids issues with getLocalName returning null.
     * 
     * @param n Node to get the name of
     * @return The local name of the node
     */
    public static String getLocalName(Node a) {
        String ret = a.getLocalName();
        if (ret == null) {
            ret = a.getNodeName();
        }
        
        return ret;
    }

    /**
     * Returns true if the attribute is namespace declaration.
     * 
     * Takes a node to avoid casts.
     * 
     * @param n Attribute to check if namespace declaration
     * @return True if namespace declaration
     */
    public static boolean isNamespaceAttr(Node n) {
        
        boolean ret = false;
        
        if (n.getNamespaceURI() != null) {
            if (n.getNamespaceURI().equals(NodeOps.XMLNS)) {
                ret = true;
            } else if (n.getLocalName().equals("xmlns")) {
                ret = true;
            }
        } else if (n.getNodeName().equals("xmlns")) {
            ret = true;
        }
        
        return ret;
    }
}
