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
package org.diffxml.patchxml;

import java.io.IOException;

import org.diffxml.diffxml.DOMOps;
import org.diffxml.dul.DULConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

public class DULPatch {

    private XPath mXPath;

    /**
     * Perform update operation.
     *
     * @param doc The document being patched
     * @param op The update operation node
     * @throws PatchFormatException If the operation is malformed
     */
    private void doUpdate(final Document doc, final Node op) 
    throws PatchFormatException {

        NamedNodeMap opAttrs = op.getAttributes();
        Node updateNode = getNamedNode(doc, opAttrs);

        if (updateNode.getNodeType() == Node.ELEMENT_NODE) {
            Node newNode = doc.createElementNS(getNameSpaceFromAttr(opAttrs),
                    op.getTextContent());
            
            // Copy attributes to the new element
            NamedNodeMap attrs = updateNode.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr2 = (Attr) doc.importNode(attrs.item(i), true);
                newNode.getAttributes().setNamedItem(attr2);
            }
            
            // Move all the children over
            while (updateNode.hasChildNodes()) {
                newNode.appendChild(updateNode.getFirstChild());
            }
            updateNode.getParentNode().replaceChild(newNode, updateNode);
            
        } else {
            updateNode.setNodeValue(op.getTextContent());
        }

    }

    /**
     * Get the parent node pointed to by the parent attribute.
     *
     * @param doc   document being patched
     * @param attrs attributes of operation node
     * @return the parent node
     * @throws PatchFormatException If the patch is not formatted correctly
     */
    private Node getParentFromAttr(final Document doc, 
            final NamedNodeMap attrs) throws PatchFormatException {
        
        Node parent = null;
        try {
            parent = (Node) mXPath.evaluate(
                    attrs.getNamedItem(DULConstants.PARENT).getNodeValue(),
                    doc.getDocumentElement(), 
                    XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new PatchFormatException(
                    "Could not resolve XPath for parent attribuute", e);
        }
        
        return parent;
    }

    /**
     * Get value of nodetype attribute.
     *
     * @param attrs attributes of operation node
     * @return the value of nodetype
     * @throws PatchFormatException If the nodetype is missing or malformed
     */
    private int getNodeTypeFromAttr(final NamedNodeMap attrs)
    throws PatchFormatException {

        int val;
        Node type = attrs.getNamedItem(DULConstants.NODETYPE);
        if (type != null) {
            try {
                val = Integer.valueOf(type.getNodeValue());
            } catch (NumberFormatException e) {
                throw new PatchFormatException("Invalid nodetype", e);
            }
        } else {
            throw new PatchFormatException("No nodetype specified");
        }

        return val;
    }

    /**
     * Get value of length attribute.
     *
     * @param attrs attributes of operation node
     * @return the value of the length attr
     * @throws PatchFormatException If the length is missing or malformed
     */
    private int getLengthFromAttr(final NamedNodeMap attrs)
    throws PatchFormatException {

        int val;
        Node type = attrs.getNamedItem(DULConstants.LENGTH);
        if (type != null) {
            try {
                val = Integer.valueOf(type.getNodeValue());
                if (val < 1) {
                    throw new PatchFormatException("Invalid length");
                }
            } catch (NumberFormatException e) {
                throw new PatchFormatException("Invalid length", e);
            }
        } else {
            throw new PatchFormatException("No length specified");
        }

        return val;
    }

    /**
     * Get value of name attribute.
     *
     * @param attrs attributes of operation node
     * @return the value of the name  attribute
     * @throws PatchFormatException If the name is missing
     */
    private String getNameFromAttr(final NamedNodeMap attrs)
    throws PatchFormatException {

        String val;
        Node name = attrs.getNamedItem(DULConstants.NAME);
        if (name != null) {
            val = name.getNodeValue();
        } else {
            throw new PatchFormatException("No name specified");
        }

        return val;
    }

    /**
     * Get value of namespace attribute.
     * 
     * Returns null if not existent, *does not* throw exception.
     *
     * @param attrs attributes of operation node
     * @return the value of the name  attribute
     */
    private String getNameSpaceFromAttr(final NamedNodeMap attrs)
    throws PatchFormatException {

        String val;
        Node name = attrs.getNamedItem(DULConstants.NAMESPACE);
        if (name != null) {
            val = name.getNodeValue();
        } else {
            val = null;
        }

        return val;
    }
    
    /**
     * Get the DOM Child Number equivalent of the XPath childnumber.
     *
     * @param siblings the NodeList we are interested in
     * @param xpathcn  the XPath child number
     * @return the equivalent DOM child number
     */
    private int getDOMChildNoFromXPath(final NodeList siblings,
            final int xpathcn) {

        int domIndex = 0;
        int xPathIndex = 1;
        while ((xPathIndex < xpathcn) && (domIndex < siblings.getLength())) {
            if (!((prevNodeIsATextNode(siblings, domIndex))
                    && (DOMOps.isText(siblings.item(domIndex))))) {
                xPathIndex++;
            }
            domIndex++;
        }
        //Handle appending nodes
        if (xPathIndex < xpathcn) {
            domIndex++;
            xPathIndex++;
        }

        assert domIndex == xPathIndex;
        return domIndex;
    }

    /**
     * Get the value associated with the operation node.
     *
     * Returns an empty sting if no value.
     *
     * @param op the operation node
     * @throws PatchFormatException if there is an error parsing the node
     * @return the string value of the node
     */
    private String getOpValue(final Node op) 
        throws PatchFormatException {
        
        NodeList opKids = op.getChildNodes();

        String value = "";
        if (opKids.getLength() > 1) {
            throw new PatchFormatException(
                    "Unexpected children in insert operation");
        } else if ((opKids.getLength() == 1)
                && (DOMOps.isText(opKids.item(0)))) {
            value = opKids.item(0).getNodeValue();
        }

        return value;
    }

    /**
     * Get value of old_charpos attribute.
     *
     * Defaults to 1.
     *
     * @param opAttrs attributes of operation node
     * @return the value of new_charpos
     */
    private int getOldCharPos(final NamedNodeMap opAttrs) {
        
        int oldCharPos = 1;

        Node a = opAttrs.getNamedItem(DULConstants.OLD_CHARPOS);
        if (a != null) {
            oldCharPos = Integer.valueOf(a.getNodeValue());
        }

        return oldCharPos;
    }

    /**
     * Get value of new_charpos attribute.
     *
     * Defaults to 1.
     *
     * @param opAttrs attributes of operation node
     * @return the value of new_charpos
     */
    private int getNewCharPos(final NamedNodeMap opAttrs) {
        
        int newCharPos = 1;

        Node a = opAttrs.getNamedItem(DULConstants.NEW_CHARPOS);
        
        if (a != null) {
            newCharPos = Integer.valueOf(a.getNodeValue());
        }

        return newCharPos;
    }

    /**
     * Get value of charpos attribute.
     *
     * Defaults to 1 if not present.
     *
     * @param opAttrs attributes of operation node
     * @return the value of charpos
     * @throws PatchFormatException for illegal charpos values
     */
    private int getCharPos(final NamedNodeMap opAttrs)
    throws PatchFormatException {
        
        int charpos = 1;

        Node a = opAttrs.getNamedItem(DULConstants.CHARPOS);
        if (a != null) {
            charpos = Integer.valueOf(a.getNodeValue());
            if (charpos < 1) {
                throw new PatchFormatException("charpos must be >= 1");
            }
        } 

        return charpos;
    }

    /**
     * Tests if previous node is a text node.
     *
     * @param siblings siblings of current node
     * @param index    index of current node
     * @return true if previous node is a text node, false otherwise
     */
    private boolean prevNodeIsATextNode(final NodeList siblings,
            final int index) {
        
        return (index > 0 && DOMOps.isText(siblings.item(index - 1)));
    }

    /**
     * Inserts a node at the given character position.
     *
     * @param charpos  the character position to insert at
     * @param siblings the NodeList to insert the node into
     * @param domcn    the child number to insert the node as
     * @param ins      the node to insert
     * @param parent   the node to become the parent of the inserted node
     */
    private void insertAtCharPos(final int charpos, final NodeList siblings,
            final int domcn, final Node ins, final Node parent, 
            final Document doc) throws PatchFormatException {

        //we know text node at domcn -1
        int cp = charpos;
        int textNodeIndex = domcn - 1;
        boolean append = false;

        while (prevNodeIsATextNode(siblings, textNodeIndex)) {
            textNodeIndex--;
        }

        while (DOMOps.isText(siblings.item(textNodeIndex))
                && cp > siblings.item(textNodeIndex).getNodeValue().length()) {
            cp = cp - siblings.item(textNodeIndex).getNodeValue().length();
            textNodeIndex++;

            if (textNodeIndex == siblings.getLength()) {
                if (cp > 1) {
                    throw new PatchFormatException("charpos past end of text");
                }
                append = true;
                parent.appendChild(ins);
                break;
            }
        }

        Node sibNode = siblings.item(textNodeIndex);

        if (!append) {
            if (cp == 1) {
                parent.insertBefore(ins, sibNode);
            } else if (cp > sibNode.getNodeValue().length()) {
                Node nextSib = sibNode.getNextSibling();
                if (nextSib != null) {
                    parent.insertBefore(ins, nextSib);
                } else {
                    parent.appendChild(ins);
                }
            } else {
                String text = sibNode.getNodeValue();
                Node nextSib = sibNode.getNextSibling();
                parent.removeChild(sibNode);
                Node text1, text2;
                
                if (sibNode.getNodeType() == Node.CDATA_SECTION_NODE
                        && ins.getNodeType() == Node.CDATA_SECTION_NODE) {
                 
                    Node cdata = doc.createCDATASection(
                            text.substring(0, cp - 1) + ins.getNodeValue()
                            + text.substring(cp - 1));
                    if (nextSib != null) {
                        parent.insertBefore(cdata, nextSib);
                    } else {
                        parent.appendChild(cdata);
                    }
                    
                } else {
                    if (sibNode.getNodeType() == Node.TEXT_NODE) {
                        text1 = doc.createTextNode(text.substring(0, cp - 1));
                        text2 = doc.createTextNode(text.substring(cp - 1));
                    } else { //CDATA
                        text1 = doc.createCDATASection(
                                text.substring(0, cp - 1));
                        text2 = doc.createCDATASection(text.substring(cp - 1));
                    }
                    if (nextSib != null) {
                        parent.insertBefore(text1, nextSib);
                        parent.insertBefore(ins, nextSib);
                        parent.insertBefore(text2, nextSib);
                    } else {
                        parent.appendChild(text1);
                        parent.appendChild(ins);
                        parent.appendChild(text2);
                    }
                }
            }
        }
    }

    /**
     * Insert a node under parent node at given position.
     *
     * @param siblings the NodeList to insert the node into
     * @param parent   the parent to insert the node under
     * @param domcn    the child number to insert the node as
     * @param charpos  the character position at which to insert the node
     * @param ins      the node to be inserted
     * @param doc      the document we are inserting into
     * @throws PatchFormatException In case of invalid data
     */
    private void insertNode(final NodeList siblings, final Node parent,
            final int domcn, final int charpos, final Node ins, 
            final Document doc) throws PatchFormatException {
        
        //siblings(domcn) is the node currently at the position we want to put 
        //the node

        if (domcn > siblings.getLength()) {
            throw new PatchFormatException(
                    "Child number past end of nodes");
        }
        if (parent.getNodeType() != Node.ELEMENT_NODE
                && parent.getNodeType() != Node.DOCUMENT_NODE) {
            throw new PatchFormatException(
                    "Parent must be an element");
        }

        if ((siblings.getLength() > 0)) {

            //Check if inserting into text
            if (prevNodeIsATextNode(siblings, domcn)) {
                insertAtCharPos(charpos, siblings, domcn, ins, parent, doc);
            } else if (domcn < siblings.getLength()) {
                parent.insertBefore(ins, siblings.item(domcn));
            } else {
                parent.appendChild(ins);
            }
        } else {
            parent.appendChild(ins);
        }
    }
    
    /**
     * Get the DOM Child number of a node using "childno" attribute.
     *
     * If attribute doesn't exist, assumes childno = 1.
     *
     * @param opAttrs  the attributes of the operation
     * @param nodeType the nodeType to be inserted
     * @param siblings the siblings of the node
     * @return the DOM Child number of the node
     */
    private int getDOMChildNo(final NamedNodeMap opAttrs,
            final int nodeType, final NodeList siblings) 
    throws PatchFormatException {
        
        // First XPath child is 1, first DOM is 0
        int xpathcn = 1;
        int domcn = 0;

        if (opAttrs.getNamedItem(DULConstants.CHILDNO) != null) {
            try {
                xpathcn = Integer.valueOf(opAttrs.getNamedItem(
                            DULConstants.CHILDNO).getNodeValue());
            } catch (NumberFormatException e) {
                throw new PatchFormatException("Invalid childno", e);
            }
        }

        //Convert xpath childno to DOM childno
        if (nodeType != Node.ATTRIBUTE_NODE) {
            domcn = getDOMChildNoFromXPath(siblings, xpathcn);
        }

        return domcn;
    }

    /**
     * Apply insert operation to document.
     *
     * @param doc the document to be patched
     * @param op  the insert operation node
     * @throws PatchFormatException if there is an error parsing the op
     */
    private void doInsert(final Document doc, final Node op) 
    throws PatchFormatException {
        
        Node ins;

        //Get various variables need for insert
        NamedNodeMap opAttrs = op.getAttributes();
        int charpos = getCharPos(opAttrs);

        //Element parent = null;
        Node parentNode = getNamedParent(doc, opAttrs);
        if (parentNode == null) {
            throw new PatchFormatException(
                    "Insert operation must specify valid parent.");
        } 

        NodeList siblings = parentNode.getChildNodes();
        int nodeType = getNodeTypeFromAttr(opAttrs);

        int domcn = getDOMChildNo(opAttrs, nodeType, siblings);

        switch (nodeType) {
            case Node.TEXT_NODE:

                ins = doc.createTextNode(getOpValue(op));
                insertNode(siblings, parentNode, domcn, charpos, ins, doc);
                break;

            case Node.CDATA_SECTION_NODE:
                
                ins = doc.createCDATASection(getOpValue(op));
                insertNode(siblings, parentNode, domcn, charpos, ins, doc);
                break;
                
            case Node.ELEMENT_NODE:

                ins = doc.createElementNS(getNameSpaceFromAttr(opAttrs),
                        getNameFromAttr(opAttrs));
                insertNode(siblings, parentNode, domcn, charpos, ins, doc);
                break;

            case Node.COMMENT_NODE:

                ins = doc.createComment(getOpValue(op));
                insertNode(siblings, parentNode, domcn, charpos, ins, doc);
                break;

            case Node.ATTRIBUTE_NODE:

                if (parentNode.getNodeType() != Node.ELEMENT_NODE) {
                    throw new PatchFormatException("Parent not an element");
                }
                ((Element) parentNode).setAttributeNS(
                        getNameSpaceFromAttr(opAttrs),
                        getNameFromAttr(opAttrs), getOpValue(op));
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                
                ins = doc.createProcessingInstruction(
                        getNameFromAttr(opAttrs), getOpValue(op));
                insertNode(siblings, parentNode, domcn, charpos, ins, doc);
                break;
                
            case Node.DOCUMENT_TYPE_NODE:
                
                throw new PatchFormatException(
                        "Cannot insert doctype nodes into existing documents");
                
            default:
                throw new PatchFormatException("Unknown NodeType " + nodeType);
        }
    }

    /**
     * Delete the appropriate amount of text from a Node.
     * 
     * Assumes delNode is the start of the text.
     *
     * @param delNode the text node to delete text from
     * @param charpos the character position at which to delete
     * @param length the number of characters to delete
     * @param doc the document being deleted from
     * @throws PatchFormatException if there is a problem with the patch
     * @return A node with the deleted text (CDATA or text as appropriate)
     */
    private Node deleteText(final Node delNode, final int charpos, 
            final int length, final Document doc) 
    throws PatchFormatException {
        
        if (!DOMOps.isText(delNode)) {
            throw new PatchFormatException(
                    "Attempt to delete text from non-text node.");
        }
        
        if (charpos < 1) {
            throw new PatchFormatException(
                "charpos must be >= 1");
        }

        String text = delNode.getNodeValue();
        Node deleted;
        
        if (charpos > text.length()) {
            if (DOMOps.isText(delNode.getNextSibling())) {
                deleted = deleteText(delNode.getNextSibling(), 
                        charpos - text.length(), length, doc);
            } else {
                throw new PatchFormatException(
                "charpos not within text");
            }
        } else {

            int leftover = (length + charpos - 1) - text.length();

            String newText = text.substring(0, charpos - 1);
            String deletedText = text.substring(charpos - 1);
            if (leftover < 0) {
                newText = newText + text.substring(charpos - 1 + length);
                deletedText = deletedText.substring(0, length);
            }

            if (delNode.getNodeType() == Node.TEXT_NODE) {
                deleted = doc.createTextNode(deletedText);
            } else {
                deleted = doc.createCDATASection(deletedText);
            }
            
            if (newText.length() > 0) {
                Node newTextNode;
                if (delNode.getNodeType() == Node.TEXT_NODE) {
                    newTextNode = doc.createTextNode(newText);
                } else if (delNode.getNodeType() == Node.CDATA_SECTION_NODE) {
                    newTextNode = doc.createCDATASection(newText);
                } else {
                    throw new PatchFormatException(
                    "Illegal NodeType");
                }
                delNode.getParentNode().insertBefore(newTextNode, delNode);
            }

            if (leftover > 0) {
                if (DOMOps.isText(delNode.getNextSibling())) {
                    deleted.setNodeValue(deleted.getNodeValue() 
                            + deleteText(delNode.getNextSibling(), 1, leftover,
                                    doc).getNodeValue());
                } else {
                    throw new PatchFormatException(
                    "length past end of text");
                }
            }
            delNode.getParentNode().removeChild(delNode);
        }

        return deleted;
    }

    /**
     * Delete the appropriate amount of text from a Node.
     *
     * @param delNode the text node to delete text from
     * @param charpos the character position at which to delete
     * @param doc the document being deleted from
     * @throws PatchFormatException if there is a problem with the patch
     * @return A CDATA or text node with the deleted text
     */
    private Node deleteText(final Node delNode, final int charpos, 
            final Document doc) 
    throws PatchFormatException {

        int totalLength = DOMOps.getTextLength(delNode);
        int length = totalLength - charpos + 1;
        return deleteText(delNode, charpos, length, doc);
    }

    /**
     * Gets the node pointed to by the "parent" attribute.
     *
     * @param doc     document being patched
     * @param opAttrs attributes of operation node
     * @throws PatchFormatException if there is an error parsing the attribute
     * @return        node pointed to by "parent" attribute
     */
    private Node getNamedParent(final Document doc, 
            final NamedNodeMap opAttrs) throws PatchFormatException {
        
        String xPath;
        Node n = opAttrs.getNamedItem(DULConstants.PARENT);
        if (n != null) {
            xPath = n.getNodeValue();
        } else {
            throw new PatchFormatException("No parent attribute");
        }

        Node ret = getNodeFromXPath(doc, xPath);
        if (ret == null) {
            throw new PatchFormatException(
                    "Failed to find parent node: " + xPath);
        }
        return ret;
    }

    /**
     * Returns the node pointed to by a given xPath.
     *
     * @param doc   document being patched
     * @param xPath xPath to the node
     * @throws PatchFormatException if there is an error parsing the xpath
     * @return      the node pointed to by the xPath
     */
    private Node getNodeFromXPath(final Document doc, final String xPath) 
    throws PatchFormatException {

        Node n = null;
        try {
            //According to API returns *first* match,
            //so should be first text node if text node matched
            n = (Node) mXPath.evaluate(
                    xPath,
                    doc.getDocumentElement(), 
                    XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new PatchFormatException(
                    "Could not resolve XPath for node");
        }
        return n;
    }

    /**
     * Gets the node pointed to by the "node" attribute.
     *
     * @param doc     document being patched
     * @param opAttrs attributes of operation node
     * @throws PatchFormatException if there is an error parsing the attribute
     * @return        node pointed to by "node" attribute
     */
    private Node getNamedNode(final Document doc, final NamedNodeMap opAttrs) 
        throws PatchFormatException {

        String xPath = opAttrs.getNamedItem(DULConstants.NODE).getNodeValue();
        return getNodeFromXPath(doc, xPath);
    }

    /**
     * Apply delete operation.
     *
     * @param doc document to be patched
     * @param op  node holding details of delete
     * @throws PatchFormatException if there is an error parsing the op
     */
    private void doDelete(final Document doc, final Node op) 
    throws PatchFormatException {

        NamedNodeMap opAttrs = op.getAttributes();
        Node delNode = getNamedNode(doc, opAttrs);

        if (delNode == null) {
            throw new PatchFormatException("Could not resolve XPath for node");
        }
        
        if (delNode.getNodeType() == Node.ATTRIBUTE_NODE) {
            Attr delAttr = (Attr) delNode;
            delAttr.getOwnerElement().removeAttributeNode(delAttr);
        } else if (DOMOps.isText(delNode)) {
            
            //TODO Refactor - unnecessarily complex
            int charpos = getCharPos(opAttrs);
            boolean haveLength = true;
            int length = 0;
            
            try {    
                length = getLengthFromAttr(opAttrs);
            } catch (PatchFormatException e) {
                haveLength = false;
            }
            
            if (haveLength) {
                deleteText(delNode, charpos, length, doc);
            } else {
                deleteText(delNode, charpos, doc);
            }
        } else {
            delNode.getParentNode().removeChild(delNode);
        }
    }

    /**
     * Apply move operation.
     *
     * @param doc document to be patched
     * @param op  node holding details of move
     * @throws PatchFormatException if there is an error parsing the op
     */
    private void doMove(final Document doc, final Node op) 
        throws PatchFormatException {
        
        NamedNodeMap opAttrs = op.getAttributes();

        Node moveNode = getNamedNode(doc, opAttrs);
        if (moveNode == null) {
            throw new PatchFormatException("Error applying patch.\n"
                    + "Node to move doesn't exist.");
        }

        int oldCharPos = getOldCharPos(opAttrs);

        //Find position to move to
        //Get parent
        Element parent = (Element) getNamedParent(doc, opAttrs);

        NodeList newSiblings = parent.getChildNodes();
        int domcn = getDOMChildNo(opAttrs, moveNode.getNodeType(), newSiblings);

        //Get new charpos
        int newCharPos = getNewCharPos(opAttrs);

        //Perform delete
        if (DOMOps.isText(moveNode)) {
            Node text;
            try {
                int length = getLengthFromAttr(opAttrs);
                text = deleteText(moveNode, oldCharPos, length, doc); 
            } catch (PatchFormatException e) {
                text = deleteText(moveNode, oldCharPos, doc);
            }
            moveNode = text;
        } else {
            moveNode = moveNode.getParentNode().removeChild(moveNode);
        }
        

        //Perform insert
        insertNode(newSiblings, parent, domcn, newCharPos, moveNode, doc);
    }
  
    /**
     * Apply DUL patch to XML document.
     *
     * @param doc   the XML document to be patched
     * @param patch the DUL patch
     * @throws PatchFormatException if there is an error parsing the patch
     */
    public final void apply(final Document doc, final Document patch) 
        throws PatchFormatException {

        mXPath = XPathFactory.newInstance().newXPath();

        NodeIterator ni = ((DocumentTraversal) patch).createNodeIterator(
                patch.getDocumentElement(), NodeFilter.SHOW_ELEMENT,
                null, false);

        Node op = ni.nextNode();

        //Check we have a delta
        if (!op.getNodeName().equals(DULConstants.DELTA)) {
            throw new PatchFormatException("All deltas must begin with a "
                    + DULConstants.DELTA + " element.");
        }

        //Cycle through elements applying ops
        op = ni.nextNode();

        while (op != null) {
            //Normalize essential for deletes to work
            doc.normalize();
            String opName = op.getNodeName();

            try {
                if (opName.equals(DULConstants.UPDATE)) {
                    doUpdate(doc, op);
                } else if (opName.equals(DULConstants.INSERT)) {
                    doInsert(doc, op);
                } else if (opName.equals(DULConstants.DELETE)) {
                    doDelete(doc, op);
                } else if (opName.equals(DULConstants.MOVE)) {
                    doMove(doc, op);
                } else {
                    throw new PatchFormatException(
                            "Invalid element: " + opName);
                }

                if (PatchXML.debug) {
                    try {
                        System.err.print("At operation: ");
                        System.err.println(DOMOps.getNodeAsStringDeep(op));
                        System.err.println("Result: ");
                        DOMOps.outputXML(doc, System.err);
                        System.err.println();
                        System.err.println();
                    } catch (IOException e) {
                        System.err.println("Failed to print debug output");
                        System.exit(1);
                    }
                }
            } catch (PatchFormatException e) {
                throw new PatchFormatException(
                        "Error at operation:\n"
                        + DOMOps.getNodeAsStringDeep(op), e);
            }
            op = ni.nextNode();
        }
    }

}
