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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test class for ChildNumber.
 * 
 * @author Adrian Mouat
 *
 */
public class ChildNumberTest {

    /** Test XML document. */
    private Document testDoc;
    
    /** Test XML Element. */
    private Element parent;
    
    /** Factory for docs. */
    private DocumentBuilderFactory mFac;
    
    /** Factory for XPath Expressions. */
    private XPathFactory mXPathFac;
    
    /**
     * Prepares commonly used test elements etc.
     * 
     * @throws Exception
     */
    @Before
    public final void setUp() throws Exception {

        mFac = DocumentBuilderFactory.newInstance();
        mXPathFac = XPathFactory.newInstance();
        testDoc = mFac.newDocumentBuilder().newDocument();
        parent = testDoc.createElement("parent");
        testDoc.appendChild(parent); 
    }
    
    /**
     * Check straightforward case.
     */
    @Test
    public final void testSimpleChildNo() {

        Element a = testDoc.createElement("a");
        Element b = testDoc.createElement("b");
        Element c = testDoc.createElement("c");
        
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        
        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        
        assertEquals(0, aChildNo.getDOM());
        assertEquals(1, bChildNo.getDOM());
        assertEquals(2, cChildNo.getDOM());
        
        XPath xpath = mXPathFac.newXPath();
        try {
            String pre = "//parent/node()[";
            Object ret = xpath.evaluate(
                    pre + aChildNo.getXPath() + "]", testDoc, 
                    XPathConstants.NODE);
            assertTrue(a.isSameNode(((Node) ret)));
            
            ret = xpath.evaluate(pre + bChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue(b.isSameNode(((Node) ret)));
            
            ret = xpath.evaluate(pre + cChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue(c.isSameNode(((Node) ret)));
            
        } catch (XPathExpressionException e) {
            fail("Caught XPathExpressionException: " + e.getMessage());
        }
        
    }
    
    /**
     * Test handling of text nodes.
     */
    @Test
    public final void testTextNodeChildNo() {
        
        //<parent><a/>12<!--d-->3</parent>
        Node blank = testDoc.createTextNode("");
        Element a = testDoc.createElement("a");
        Node b = testDoc.createTextNode("1");
        Node c = testDoc.createTextNode("2");
        Node d = testDoc.createComment("d");
        Node e = testDoc.createTextNode("3");
        
        parent.appendChild(blank); //Should be ignored in XPaths
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        parent.appendChild(d);
        parent.appendChild(e);
        
        ChildNumber blankChildNo = new ChildNumber(blank);
        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        ChildNumber dChildNo = new ChildNumber(d);
        ChildNumber eChildNo = new ChildNumber(e);
        
        assertEquals(0, blankChildNo.getDOM());
        assertEquals(1, aChildNo.getDOM());
        assertEquals(2, bChildNo.getDOM());
        assertEquals(3, cChildNo.getDOM());
        assertEquals(4, dChildNo.getDOM());
        assertEquals(5, eChildNo.getDOM());
        
        //Force evaluation of xpaths before normalize
        aChildNo.getXPath();
        bChildNo.getXPath();
        cChildNo.getXPath();
        dChildNo.getXPath();
        eChildNo.getXPath();

        testDoc.normalize();

        XPath xpath = mXPathFac.newXPath();
        try {
            String pre = "//parent/node()[";
            Object ret = xpath.evaluate(
                    pre + aChildNo.getXPath() + "]", testDoc, 
                    XPathConstants.NODE);
            assertTrue(a.isSameNode(((Node) ret)));
            
            ret = xpath.evaluate(
                    "substring(" + pre + bChildNo.getXPath()
                    + "]," + bChildNo.getXPathCharPos() + ",1)",
                    testDoc, XPathConstants.STRING);
            assertEquals("1", ret.toString());
            
            ret = xpath.evaluate(
                    "substring(" + pre + cChildNo.getXPath() + "],"
                    + cChildNo.getXPathCharPos() + ",1)", testDoc, 
                    XPathConstants.STRING);
            assertEquals("2", ret.toString());
            
            ret = xpath.evaluate(pre + dChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue("Got: " + ret.toString(), d.isSameNode(((Node) ret)));

            ret = xpath.evaluate(
                    "substring(" + pre + eChildNo.getXPath() + "],"
                    + eChildNo.getXPathCharPos() + ",1)", testDoc, 
                    XPathConstants.STRING);
            assertEquals(e.getTextContent(), ret.toString());

        } catch (XPathExpressionException ex) {
            fail("Caught XPathExpressionException: " + ex.getMessage());
        }

    }

    /**
     * Test two initial text nodes are counted properly.
     */
    @Test
    public final void testTwoInitialTextNodes() {
        
        Node a = testDoc.createTextNode("1234");
        Node b = testDoc.createTextNode("5");
        Element c = testDoc.createElement("a");
        
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        
        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        
        assertEquals(0, aChildNo.getDOM());
        assertEquals(1, bChildNo.getDOM());
        assertEquals(2, cChildNo.getDOM());
        
        XPath xpath = mXPathFac.newXPath();
        try {
            String pre = "//parent/node()[";
            Object ret = xpath.evaluate(
                    "substring(" + pre + aChildNo.getXPath()
                    + "]," + aChildNo.getXPathCharPos() + ",4)",
                    testDoc, XPathConstants.STRING);
            assertEquals(a.getTextContent(), ret.toString());
            
            ret = xpath.evaluate(
                    "substring(" + pre + bChildNo.getXPath() + "],"
                    + bChildNo.getXPathCharPos() + ",1)", testDoc, 
                    XPathConstants.STRING);
            assertEquals(b.getTextContent(), ret.toString());
            
            ret = xpath.evaluate(pre + cChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue("Got: " + ret.toString(), c.isSameNode(((Node) ret)));
            
        } catch (XPathExpressionException e) {
            fail("Caught XPathExpressionException: " + e.getMessage());
        }
    }

    /**
     * Test in-order counting of DOM nodes.
     */
    @Test
    public final void testDOMInOrder() {
        
        Node a = testDoc.createTextNode("1234");
        NodeOps.setOutOfOrder(a);
        Node b = testDoc.createTextNode("5");
        NodeOps.setInOrder(b);
        Element c = testDoc.createElement("a");
        NodeOps.setInOrder(c);
        
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        
        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        
        assertEquals(0, aChildNo.getInOrderDOM());
        assertEquals(0, bChildNo.getInOrderDOM());
        assertEquals(1, cChildNo.getInOrderDOM());
        
        NodeOps.setInOrder(a);
        NodeOps.setOutOfOrder(b);
        NodeOps.setInOrder(c);

        aChildNo = new ChildNumber(a);
        bChildNo = new ChildNumber(b);
        cChildNo = new ChildNumber(c);

        assertEquals(0, aChildNo.getInOrderDOM());
        assertEquals(1, bChildNo.getInOrderDOM());
        assertEquals(1, bChildNo.getInOrderDOM());        
    }
    
    /**
     * Test counting of in-order XPath nodes.
     */
    @Test
    public final void testXPathInOrder() {
        
        Node a = testDoc.createTextNode("1234");
        NodeOps.setOutOfOrder(a);
        Node b = testDoc.createCDATASection("56");
        NodeOps.setInOrder(b);
        Node c = testDoc.createTextNode("78");
        NodeOps.setInOrder(c);
        Element d = testDoc.createElement("nine");
        NodeOps.setInOrder(d);

        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        parent.appendChild(d);

        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        ChildNumber dChildNo = new ChildNumber(d);

        assertEquals(1, aChildNo.getInOrderXPath());
        assertEquals(1, aChildNo.getInOrderXPathCharPos());
        assertEquals(1, bChildNo.getInOrderXPath());
        assertEquals(1, bChildNo.getInOrderXPathCharPos());
        assertEquals(1, cChildNo.getInOrderXPath());
        assertEquals(3, cChildNo.getInOrderXPathCharPos());
        assertEquals(2, dChildNo.getInOrderXPath());
        //assertEquals(5, dChildNo.getInOrderXPathCharPos());
        
    }

    /**
     * Test counting of text position with intervening nodes.
     */
    @Test
    public final void testTextPositionInOrder() {
    
        Node a = testDoc.createTextNode("12");
        NodeOps.setInOrder(a);
        Node b = testDoc.createElement("three");
        NodeOps.setOutOfOrder(b);
        Node c = testDoc.createTextNode("45");
        NodeOps.setInOrder(c);
        Element d = testDoc.createElement("six");
        NodeOps.setInOrder(d);
        Node e = testDoc.createTextNode("78");
        NodeOps.setInOrder(e);
        Node f = testDoc.createCDATASection("9");

        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        parent.appendChild(d);
        parent.appendChild(e);
        parent.appendChild(f);

        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        ChildNumber dChildNo = new ChildNumber(d);
        ChildNumber eChildNo = new ChildNumber(e);
        ChildNumber fChildNo = new ChildNumber(f);

        assertEquals(1, aChildNo.getInOrderXPath());
        assertEquals(1, aChildNo.getInOrderXPathCharPos());
        assertEquals(2, bChildNo.getInOrderXPath());
        assertEquals(3, bChildNo.getInOrderXPathCharPos());
        assertEquals(1, cChildNo.getInOrderXPath());
        assertEquals(3, cChildNo.getInOrderXPathCharPos());
        assertEquals(2, dChildNo.getInOrderXPath());
        assertEquals(5, dChildNo.getInOrderXPathCharPos());
        assertEquals(3, eChildNo.getInOrderXPath());
        assertEquals(1, eChildNo.getInOrderXPathCharPos());
        assertEquals(3, fChildNo.getInOrderXPath());
        assertEquals(3, fChildNo.getInOrderXPathCharPos());
    }

    /**
     * Test counting of text position with intervening nodes.
     */
    @Test
    public final void testIgnoringNodes() {
    
        Node a = testDoc.createTextNode("12");
        Node b = testDoc.createElement("three");
        Node c = testDoc.createTextNode("45");
        
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);

        ChildNumber cn = new ChildNumber(c);

        try {
            cn.getDOMIgnoring(c);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            //Normal execution
        }
        
        assertEquals(2, cn.getDOMIgnoring(null));
        assertEquals(1, cn.getDOMIgnoring(b));
        assertEquals(1, cn.getDOMIgnoring(a));
        
        assertEquals(3, cn.getXPathIgnoring(null));
        assertEquals(1, cn.getXPathIgnoring(b));
        assertEquals(2, cn.getXPathIgnoring(a));
       
        assertEquals(1, cn.getXPathCharPosIgnoring(null));
        assertEquals(3, cn.getXPathCharPosIgnoring(b));
        assertEquals(1, cn.getXPathCharPosIgnoring(a));
 
    }

    /**
     * Check exception thrown if given null.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testNull() {
        
        new ChildNumber(null);
    }
    
    /**
     * Test exception thrown if no parent.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testChildWithNoParent() {
        
        Node child = testDoc.createElement("noparent");
        new ChildNumber(child);
    }
}
