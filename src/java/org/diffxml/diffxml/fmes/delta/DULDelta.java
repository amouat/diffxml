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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.diffxml.diffxml.DiffFactory;
import org.diffxml.diffxml.DOMOps;
import org.diffxml.diffxml.fmes.ChildNumber;
import org.diffxml.diffxml.fmes.NodeOps;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element; 

import org.diffxml.dul.DULConstants;

/**
 * Handle operations related to creating a DUL delta.
 * 
 * @author Adrian Mouat
 */
public class DULDelta implements DeltaIF {

    /**
     * The EditScript we are creating.
     */
    private Document mEditScript;
    
    /**
     * Creates a new, blank EditScript.
     * 
     * @throws DeltaInitialisationException If the EditScript can't be 
     * initialised.
     */
    public DULDelta() throws DeltaInitialisationException {
        
        try {
            mEditScript = makeEmptyEditScript();
        } catch (ParserConfigurationException e) {
            throw new DeltaInitialisationException(e);
        }
    }
    
    /**
     * Get the XML Document for the EditScript.
     * 
     * @return The EditScript as an XML document.
     */
    public final Document getDocument() {
        return mEditScript;
    }
    
    /** Prepares an empty Edit Script document.
    *
    * Makes document element, appends any necessary attributes
    * and context information.
    *
    * @return a properly formatted, empty edit script
    * @throws ParserConfigurationException If a new document can't be created
    */

   private static Document makeEmptyEditScript() 
   throws ParserConfigurationException {

       DocumentBuilder builder = 
           DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document editScript = builder.newDocument();

       Element docEl = editScript.createElementNS(
               DULConstants.DUL_NAMESPACE, DULConstants.DELTA);

       //Append any context information
       if (DiffFactory.isContext()) {
           docEl.setAttribute(DULConstants.SIBLING_CONTEXT, 
                   Integer.toString(DiffFactory.getSiblingContext()));
           docEl.setAttribute(DULConstants.PARENT_CONTEXT,
                   Integer.toString(DiffFactory.getParentContext()));
           docEl.setAttribute(DULConstants.PARENT_SIBLING_CONTEXT,
                   Integer.toString(DiffFactory.getParentSiblingContext()));
       }

       if (DiffFactory.isReversePatch()) {
           docEl.setAttribute(DULConstants.REVERSE_PATCH, DULConstants.TRUE);
       }

       if (!DiffFactory.isResolveEntities()) {
           docEl.setAttribute(
                   DULConstants.RESOLVE_ENTITIES, DULConstants.FALSE);
       }

       editScript.appendChild(docEl);

       return editScript;
   }
   
    /**
     * Adds inserts for attributes of a node to an EditScript.
     * 
     * @param attrs
     *            the attributes to be added
     * @param path
     *            the path to the node they are to be added to
     */
    public final void addAttrsToDelta(final NamedNodeMap attrs, 
            final String path) {

        int numAttrs;
        if (attrs == null) {
            numAttrs = 0;
        } else {
            numAttrs = attrs.getLength();
        }

        for (int i = 0; i < numAttrs; i++) {
            insert(attrs.item(i), path, 0, 1);
        }
    }

    /**
     * Appends an insert operation to the EditScript given the inserted node, 
     * XPath to parent, character position & child number.
     * 
     * Set charpos to 1 if not needed.
     * 
     * @param n The node to insert
     * @param parent The path to the node to be parent of n
     * @param childno The child number of the parent node that n will become
     * @param charpos The character position to insert at
     */
    public final void insert(final Node n, final String parent, 
            final int childno, final int charpos) {

        Element ins = mEditScript.createElement(DULConstants.INSERT);
        
        ins.setAttribute(DULConstants.PARENT, parent);
        ins.setAttribute(DULConstants.NODETYPE, 
                Integer.toString(n.getNodeType()));

        if (n.getNodeType() != Node.ATTRIBUTE_NODE) {
            ins.setAttribute(DULConstants.CHILDNO, Integer.toString(childno));
        }

        if (n.getNodeType() == Node.ATTRIBUTE_NODE 
                || n.getNodeType() == Node.ELEMENT_NODE 
                || n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            if (n.getLocalName() != null) {
                ins.setAttribute(DULConstants.NAMESPACE, n.getNamespaceURI());
                ins.setAttribute(DULConstants.NAME, n.getLocalName());
            } else {
                ins.setAttribute(DULConstants.NAME, n.getNodeName());
            }
        }
        
        if (charpos > 1) {
            ins.setAttribute(DULConstants.CHARPOS, Integer.toString(charpos));
        }

        String value = n.getNodeValue();
        if (value != null) {
            Node txt = mEditScript.createTextNode(value);
            ins.appendChild(txt);
        }

        mEditScript.getDocumentElement().appendChild(ins);

        outputDebug(ins);
        
        // Add any attributes
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            //TODO: Update for using element names instead of node()
            addAttrsToDelta(n.getAttributes(), 
                    parent + "/node()[" + childno + "]");
        }
    }

    /**
     * Appends an insert operation to the EditScript given the inserted node, 
     * parent Node, character position & child number.
     * 
     * @param n The node to insert
     * @param parent The Node to be parent of n
     * @param childno The child number of the parent node that n will become
     * @param charpos The character position to insert at
     */
    public final void insert(final Node n, final Node parent, 
            final int childno, final int charpos) {

        insert(n, NodeOps.getXPath(parent), childno, charpos);
    }
    
    /**
     * Adds a delete operation to the EditScript for the given Node.
     * 
     * @param n The Node that is to be deleted
     */
    public final void delete(final Node n) {
        
        Element del = mEditScript.createElement(DULConstants.DELETE);
        del.setAttribute(DULConstants.NODE, NodeOps.getXPath(n));
        
        if (DOMOps.isText(n)) {
            
            ChildNumber cn = new ChildNumber(n);
            int charpos = cn.getXPathCharPos();
            
            if (charpos >= 1) {
                del.setAttribute(DULConstants.CHARPOS, 
                        Integer.toString(charpos));
            }

            del.setAttribute(DULConstants.LENGTH, 
                    Integer.toString(n.getTextContent().length()));
        }

        mEditScript.getDocumentElement().appendChild(del);

        outputDebug(del);
    }

    /**
     * Adds a Move operation to the EditScript. 
     * 
     * @param n The node being moved
     * @param parent XPath to the new parent Node
     * @param childno Child number of the parent n will become
     * @param ncharpos The new character position for the Node
     */
    public final void move(final Node n, final Node parent,
            final int childno, final int ncharpos) {
        
        if (ncharpos < 1) {
            throw new IllegalArgumentException(
                    "New Character position must be >= 1");
        }
        
        Element mov = mEditScript.createElement(DULConstants.MOVE);
        mov.setAttribute(DULConstants.NODE, NodeOps.getXPath(n));
        
        int ocharpos = new ChildNumber(n).getXPathCharPos();
        mov.setAttribute(DULConstants.OLD_CHARPOS, Integer.toString(ocharpos));
        mov.setAttribute(DULConstants.NEW_CHARPOS, Integer.toString(ncharpos));

        if (DOMOps.isText(n)) {
            mov.setAttribute(DULConstants.LENGTH, 
                    Integer.toString(n.getNodeValue().length()));
        }

        mov.setAttribute(DULConstants.PARENT, NodeOps.getXPath(parent));
        mov.setAttribute(DULConstants.CHILDNO, Integer.toString(childno));

        mEditScript.getDocumentElement().appendChild(mov);

        outputDebug(mov);
    }

    /**
     * Adds an update operation to the delta.
     * 
     * @param w The node to update
     * @param x The node to update it to
     */
    public final void update(final Node w, final Node x) {
        
        Element update = mEditScript.createElement(DULConstants.UPDATE);
        update.setAttribute(DULConstants.NODE, NodeOps.getXPath(w));
        
        if (w.getNodeType() == Node.ELEMENT_NODE) {
            update.setTextContent(x.getNodeName());
            updateAttributes((Element) w, (Element) x);
        } else {
            update.setTextContent(x.getNodeValue());
        } 
        
        mEditScript.getDocumentElement().appendChild(update);
        
        outputDebug(update);
    }
    
    /**
     * Updates the attributes of element w to be the same as x's.
     * 
     * Ignores xmlns attributes - these are assumed to be part of the document
     * structure rather than the content. Different namespaces will cause
     * comparison of elements etc to fail.
     * 
     * @param w The Element to update the attributes of
     * @param x The element holding the correct attributes
     */
    private void updateAttributes(final Element w, final Element x) {
    
        NamedNodeMap wAttrs = w.getAttributes();
        NamedNodeMap xAttrs = x.getAttributes();
        
        //Delete any attrs of w not in x, update others
        for (int i = 0; i < wAttrs.getLength(); i++) {
            
            Node wAttr = wAttrs.item(i);
            
            if (!NodeOps.isNamespaceAttr(wAttr)) { 
                Node xAttr = xAttrs.getNamedItemNS(wAttr.getNamespaceURI(), wAttr.getLocalName());
                if (xAttr == null) {
                    delete(wAttrs.item(i));
                } else if (!wAttr.getNodeValue().equals(xAttr.getNodeValue())) {
                    update(wAttr, xAttr);
                }
            }
        }
        
        //Add any attrs in x but not w
        for (int j = 0; j < xAttrs.getLength(); j++) {

            Node xAttr = xAttrs.item(j);
            
            if (!NodeOps.isNamespaceAttr(xAttr)) {
                
                if (wAttrs.getNamedItemNS(xAttr.getNamespaceURI(), 
                        xAttr.getLocalName()) == null) {
                    insert(xAttr, NodeOps.getXPath(w), 0, 1);
                }
            }
        }
    }
    
    /**
      * Outputs debug message for node.
      * 
      * @param n The Node to output debug on
      */
    private void outputDebug(final Node n) {

        if (DiffFactory.isDebug()) {
            System.err.print("Applying: ");
            System.err.println(DOMOps.getNodeAsStringDeep(n));
        }
    }
}
