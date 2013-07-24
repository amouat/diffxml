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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.diffxml.diffxml.TestDocHelper;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Test main EditScript class and methods.
 * 
 * TODO: Write helper method for these tests e.g. 
 * checkOpIsInsert(3, b, /node()[1])
 *  
 * @author Adrian Mouat
 *
 */
public class EditScriptTest {
    
   /**
    * Test handling documents with different document elements.
    */
    @Test
    public final void testNonMatchingDocumentElements() {
                
        Document doc1 = TestDocHelper.createDocument("<a><b/></a>");
        Document doc2 = TestDocHelper.createDocument("<c><b/></c>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(6, matchings.size());
        assertNull(matchings.getPartner(doc2.getFirstChild().getFirstChild(
                ).getNextSibling()));
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        //Non-matching document elements can only be corrected via an update
        Node update = res.getFirstChild().getFirstChild();
        assertEquals("update", update.getNodeName());
        assertEquals("/node()[1]", 
                update.getAttributes().getNamedItem("node").getNodeValue());
        assertEquals("c", update.getTextContent());
    }
    
    /**
     * Test handling differences in prolog.
     */
    @Test
    public final void testDifferentProlog() {
        Document doc1 = TestDocHelper.createDocument(
                "<!-- prolog1 --><a><b/></a>");
        Document doc2 = TestDocHelper.createDocument(
                "<!-- prolog2 --><a><b/></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(6, matchings.size());

        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        Node insert = res.getFirstChild().getFirstChild();
        assertEquals("insert", insert.getNodeName());
        NamedNodeMap attrs = insert.getAttributes();
        assertEquals("1", attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/", attrs.getNamedItem("parent").getNodeValue());
        assertEquals(Integer.toString(Node.COMMENT_NODE), 
                attrs.getNamedItem("nodetype").getNodeValue());
        assertEquals(" prolog2 ", insert.getTextContent());

        Node delete = insert.getNextSibling();
        assertEquals("delete", delete.getNodeName());
        attrs = delete.getAttributes();
        assertEquals("/node()[2]", attrs.getNamedItem("node").getNodeValue());
    }
    
    /**
     * Test the simple addition of an element.
     */
    @Test
    public final void testSimpleInsert() {

        Document doc1 = TestDocHelper.createDocument("<a><b/></a>");
        Document doc2 = TestDocHelper.createDocument("<a><b/><c/></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(6, matchings.size());
        assertNull(matchings.getPartner(doc2.getFirstChild().getFirstChild(
                ).getNextSibling()));
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        Node insert = res.getFirstChild().getFirstChild();
        assertEquals("insert", insert.getNodeName());
        NamedNodeMap attrs = insert.getAttributes();
        assertEquals("2", attrs.getNamedItem("childno").getNodeValue());
        assertEquals("c", attrs.getNamedItem("name").getNodeValue());
        assertEquals("/node()[1]", 
                attrs.getNamedItem("parent").getNodeValue());
        assertEquals("1", attrs.getNamedItem("nodetype").getNodeValue());
    }

    /**
     * Test the simple deletion of an element.
     */
    @Test
    public final void testSimpleDeletion() {

        Document doc1 = TestDocHelper.createDocument("<a><b/><c/></a>");
        Document doc2 = TestDocHelper.createDocument("<a><b/></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(6, matchings.size());
        assertNull(matchings.getPartner(doc1.getFirstChild().getFirstChild(
                ).getNextSibling()));
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        Node delete = res.getFirstChild().getFirstChild();
        assertEquals("delete", delete.getNodeName());
        NamedNodeMap attrs = delete.getAttributes();
        assertEquals("/node()[1]/node()[2]", 
                attrs.getNamedItem("node").getNodeValue());
    }
    
    /**
     * Test the simple move of an element.
     */
    @Test
    public final void testSimpleMove() {

        Document doc1 = TestDocHelper.createDocument("<a><b><c/></b><d/></a>");
        Document doc2 = TestDocHelper.createDocument("<a><b/><d><c/></d></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(10, matchings.size());
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        Node move = res.getFirstChild().getFirstChild();
        assertEquals("move", move.getNodeName());
        NamedNodeMap attrs = move.getAttributes();
        assertEquals("/node()[1]/node()[1]/node()[1]", 
                attrs.getNamedItem("node").getNodeValue());
        assertEquals("1", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]/node()[2]", 
                attrs.getNamedItem("parent").getNodeValue());    

    }

    /**
     * Test insert after text.
     */
    @Test
    public final void testInsertAfterText() {

        Document doc1 = TestDocHelper.createDocument("<a>text</a>");
        Document doc2 = TestDocHelper.createDocument("<a>text<b/></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(6, matchings.size());
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        Node insert = res.getFirstChild().getFirstChild();
        assertEquals("insert", insert.getNodeName());
        NamedNodeMap attrs = insert.getAttributes();
        assertEquals("2", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]", 
                attrs.getNamedItem("parent").getNodeValue());    
        assertEquals("5",  
                attrs.getNamedItem("charpos").getNodeValue());    
        assertEquals(Integer.toString(Node.ELEMENT_NODE),
                attrs.getNamedItem("nodetype").getNodeValue());    
    }

    /**
     * Test mis-aligned nodes.
     */
    @Test
    public final void testMisalignedNodes() {

        Document doc1 = TestDocHelper.createDocument("<a>b<c/>b</a>");
        Document doc2 = TestDocHelper.createDocument("<a>z<c/>b</a>");

        NodePairs matchings = new NodePairs();
        Node docEl1 = doc1.getDocumentElement();
        Node docEl2 = doc2.getDocumentElement();
        matchings.add(doc1, doc2);
        matchings.add(docEl1, docEl2);
        matchings.add(docEl1.getFirstChild(), 
                docEl2.getFirstChild().getNextSibling().getNextSibling());
        matchings.add(docEl1.getFirstChild().getNextSibling(),
                docEl2.getFirstChild().getNextSibling());

        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        Node move = res.getFirstChild().getFirstChild();
        assertEquals("move", move.getNodeName());
        NamedNodeMap attrs = move.getAttributes();
        assertEquals("2", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]", 
                attrs.getNamedItem("parent").getNodeValue());    
        assertEquals("/node()[1]/node()[1]",
                attrs.getNamedItem("node").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("new_charpos").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("old_charpos").getNodeValue());

        Node insert = move.getNextSibling();
        assertEquals("insert", insert.getNodeName());
        attrs = insert.getAttributes();
        assertEquals("1", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]", 
                attrs.getNamedItem("parent").getNodeValue());    
        assertEquals("3",
                attrs.getNamedItem("nodetype").getNodeValue());    
        assertEquals("z", insert.getTextContent());

        Node delete = insert.getNextSibling();
        assertEquals("delete", delete.getNodeName());
        attrs = delete.getAttributes();
        assertEquals("/node()[1]/node()[3]",
                attrs.getNamedItem("node").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("length").getNodeValue());
        assertEquals("2",
                attrs.getNamedItem("charpos").getNodeValue());

    }
    /**
     * Test inserting and moving where marked order of nodes is important.
     */
    @Test
    public final void testOrdering() {

        Document doc1 = TestDocHelper.createDocument("<a><c>6</c><b>7</b></a>");
        Document doc2 = TestDocHelper.createDocument("<a><b>6</b><b>7</b></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(10, matchings.size());
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }
        Node insert = res.getFirstChild().getFirstChild();
        assertEquals("insert", insert.getNodeName());
        NamedNodeMap attrs = insert.getAttributes();
        assertEquals(Integer.toString(Node.ELEMENT_NODE), 
                attrs.getNamedItem("nodetype").getNodeValue());
        assertEquals("3", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("b", 
                attrs.getNamedItem("name").getNodeValue());
        assertEquals("/node()[1]", 
                attrs.getNamedItem("parent").getNodeValue());    

        Node move1 = insert.getNextSibling();
        assertEquals("move", move1.getNodeName());
        attrs = move1.getAttributes();
        assertEquals("1", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]/node()[2]", 
                attrs.getNamedItem("parent").getNodeValue());    
        assertEquals("/node()[1]/node()[1]/node()[1]",
                attrs.getNamedItem("node").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("new_charpos").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("old_charpos").getNodeValue());

        Node move2 = move1.getNextSibling();
        assertEquals("move", move2.getNodeName());
        attrs = move2.getAttributes();
        assertEquals("1", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]/node()[3]", 
                attrs.getNamedItem("parent").getNodeValue());    
        assertEquals("/node()[1]/node()[2]/node()[1]",
                attrs.getNamedItem("node").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("new_charpos").getNodeValue());
        assertEquals("2",
                attrs.getNamedItem("old_charpos").getNodeValue());

        Node delete = move2.getNextSibling();
        assertEquals("delete", delete.getNodeName());
        attrs = delete.getAttributes();
        assertEquals("/node()[1]/node()[1]", 
                attrs.getNamedItem("node").getNodeValue());
    }
    
    /**
     * Test for irritating bug where unmatched node breaks text.
     */
    @Test
    public final void testNumberingBug() {
        Document doc1 = TestDocHelper.createDocument(
                "<a>x<b/>y</a>");
        Document doc2 = TestDocHelper.createDocument(
                "<a>x<p/>y<b/></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(10, matchings.size());
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }

        Node move = res.getFirstChild().getFirstChild();
        assertEquals("move", move.getNodeName());
        NamedNodeMap attrs = move.getAttributes();
        assertEquals("2", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]", 
                attrs.getNamedItem("parent").getNodeValue());    
        assertEquals("/node()[1]/node()[2]",
                attrs.getNamedItem("node").getNodeValue());
        assertEquals("3",
                attrs.getNamedItem("new_charpos").getNodeValue());
        assertEquals("2",
                attrs.getNamedItem("old_charpos").getNodeValue());

    }
    
    /**
     * Test for DocumentType node handling.
     * 
     * Note DocumentType nodes can't be differenced, as can't be referenced by
     * XPath.
     */
    @Test
    public final void testDocumentType() {
        Document doc1 = TestDocHelper.createDocument(
        "<!DOCTYPE a [ ]><a></a>");
        Document doc2 = TestDocHelper.createDocument(
        "<a></a>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(4, matchings.size());
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }

        assertEquals(res.getFirstChild().getChildNodes().getLength(), 0);
    }

    /**
     * Test for irritating bug where unmatched node breaks text.
     */
    @Test
    public final void testCBug() {
        Document doc1 = TestDocHelper.createDocument(
                "<p><b>xxx</b>yyy<br/></p>");
        Document doc2 = TestDocHelper.createDocument(
                "<p><br/>yyy</p>");
        NodePairs matchings = Match.easyMatch(doc1, doc2);
        assertEquals(8, matchings.size());
        EditScript es = new EditScript(doc1, doc2, matchings);
        Document res = null;
        try {
            res = es.create();
        } catch (DocumentCreationException e) {
            fail("Caught Exception");
        }

        Node move = res.getFirstChild().getFirstChild();
        assertEquals("move", move.getNodeName());
        NamedNodeMap attrs = move.getAttributes();
        assertEquals("3", 
                attrs.getNamedItem("childno").getNodeValue());
        assertEquals("/node()[1]", 
                attrs.getNamedItem("parent").getNodeValue());    
        assertEquals("/node()[1]/node()[2]",
                attrs.getNamedItem("node").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("new_charpos").getNodeValue());
        assertEquals("1",
                attrs.getNamedItem("old_charpos").getNodeValue());

    }
}
