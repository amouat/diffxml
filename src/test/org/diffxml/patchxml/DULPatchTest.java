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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.diffxml.diffxml.TestDocHelper;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Class to test applying DUL Patches.
 * 
 * @author Adrian Mouat
 *
 */
public class DULPatchTest {

    /**
     * Simple insert operation.
     */
    @Test
    public final void testSimpleInsert() {
        
        Document doc1 = TestDocHelper.createDocument("<a></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"1\" "
                + "childno=\"1\" name=\"b\"/>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("b", 
                    doc1.getDocumentElement().getFirstChild().getNodeName());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Insert element after text.
     */
    @Test
    public final void testInsertAfterText() {
        
        Document doc1 = TestDocHelper.createDocument("<a>text</a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"1\" "
                + "childno=\"2\" name=\"b\" charpos=\"5\" />"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node textNode = doc1.getDocumentElement().getFirstChild();
            assertEquals("text", textNode.getNodeValue());
            assertEquals("b", textNode.getNextSibling().getNodeName());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Insert element before text.
     */
    @Test
    public final void testInsertBeforeText() {
        
        Document doc1 = TestDocHelper.createDocument("<a>text</a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"1\" "
                + "childno=\"1\" name=\"b\" charpos=\"1\" />"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node b = doc1.getDocumentElement().getFirstChild();
            assertEquals("b", b.getNodeName());
            assertEquals("text", b.getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Insert element into text.
     */
    @Test
    public final void testInsertIntoText() {
        
        Document doc1 = TestDocHelper.createDocument("<a>text</a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"1\" "
                + "childno=\"2\" name=\"b\" charpos=\"2\" />"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node text1 = doc1.getDocumentElement().getFirstChild();
            assertEquals("t", text1.getNodeValue());
            assertEquals("b", text1.getNextSibling().getNodeName());
            assertEquals("ext", 
                    text1.getNextSibling().getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Insert element into text.
     */
    @Test
    public final void testInsertIntoText2() {
        
        Document doc1 = TestDocHelper.createDocument("<a>xy<b/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert charpos=\"2\" childno=\"2\" name=\"p\" "
                + "nodetype=\"1\" parent=\"/node()[1]\"/>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node text1 = doc1.getDocumentElement().getFirstChild();
            assertEquals("x", text1.getNodeValue());
            assertEquals("p", text1.getNextSibling().getNodeName());
            assertEquals("y", 
                    text1.getNextSibling().getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test inserting attribute.
     */
    @Test
    public final void testInsertingAttr() {
        
        Document doc1 = TestDocHelper.createDocument("<a><b/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a/node()[1]\" nodetype=\"2\" "
                + "name=\"attr\">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Element b = (Element) doc1.getDocumentElement().getFirstChild();
            assertEquals("b", b.getNodeName());
            assertEquals("val", b.getAttribute("attr"));
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test inserting comment.
     */
    @Test
    public final void testInsertingComment() {
        
        Document doc1 = TestDocHelper.createDocument("<a></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" + Node.COMMENT_NODE + "\""
                + ">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node comment = doc1.getDocumentElement().getFirstChild();
            assertEquals(Node.COMMENT_NODE, comment.getNodeType());
            assertEquals("val", comment.getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test inserting CDATA section.
     */
    @Test
    public final void testInsertingCDATA() {
        
        Document doc1 = TestDocHelper.createDocument("<a></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" + Node.CDATA_SECTION_NODE 
                + "\"" + ">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node comment = doc1.getDocumentElement().getFirstChild();
            assertEquals(Node.CDATA_SECTION_NODE, comment.getNodeType());
            assertEquals("val", comment.getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test simple delete operation.
     */
    @Test
    public final void testSimpleDelete() {

        Document doc1 = TestDocHelper.createDocument("<a><b/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[1]\" />" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertNull(doc1.getDocumentElement().getFirstChild());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test deleting attribute.
     */
    @Test
    public final void testDeleteAttribute() {

        Document doc1 = TestDocHelper.createDocument(
                "<a><b attr=\"val\"/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[1]/@attr\" />" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("", ((Element) doc1.getDocumentElement(
                    ).getFirstChild()).getAttribute("attr"));
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test deleting comment.
     */
    @Test
    public final void testDeleteComment() {

        Document doc1 = TestDocHelper.createDocument(
                "<a><!-- comment --></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[1]\" />" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertNull(doc1.getDocumentElement().getFirstChild());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test deleting text.
     *
     * Test deleting whole text node and part of.
     */
    @Test
    public final void testDeleteText() {

        Document doc1 = TestDocHelper.createDocument(
                "<a>12<b/>3456</a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[1]\" />" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("b", doc1.getDocumentElement().getFirstChild(
                        ).getNodeName());
        } catch (PatchFormatException e) {
            e.printStackTrace();
            fail("Caught exception " + e.getMessage());
        }

        patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[2]\" "
                + "charpos=\"2\" length=\"2\"/>" 
                + "</delta>");
        try {
            (new DULPatch()).apply(doc1, patch);
            assertTrue(doc1.getDocumentElement().getFirstChild(
                        ).getNextSibling() != null);
            assertEquals("36", doc1.getDocumentElement().getFirstChild(
                        ).getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }

        patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[2]\" "
                + "charpos=\"1\" length=\"1\"/>" 
                + "</delta>");
        try {
            (new DULPatch()).apply(doc1, patch);
            assertTrue(doc1.getDocumentElement().getFirstChild(
                        ).getNextSibling() != null);
            assertEquals("6", doc1.getDocumentElement().getFirstChild(
                        ).getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test simple move operation.
     */
    @Test
    public final void testSimpleMove() {

        Document doc1 = TestDocHelper.createDocument("<a><b/><c><d/></c></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<move node=\"/a/node()[2]/node()[1]\" " 
                + "parent=\"/a/node()[1]\" childno=\"1\" />" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("d", 
                    doc1.getDocumentElement().getFirstChild().getFirstChild(
                        ).getNodeName());
            assertNull(doc1.getDocumentElement().getFirstChild(
                        ).getNextSibling().getFirstChild());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test moving into text.
     */
    @Test
    public final void testMoveIntoText() {

        Document doc1 = TestDocHelper.createDocument(
                "<a><b>text</b><c><d/></c></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<move node=\"/a/node()[2]/node()[1]\" " 
                + "parent=\"/a/node()[1]\" childno=\"2\" "
                + "new_charpos=\"3\"/>" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node b = doc1.getDocumentElement().getFirstChild();
            assertNull(b.getNextSibling().getFirstChild());
            assertEquals("te", b.getFirstChild().getNodeValue());
            assertEquals("d", 
                    b.getFirstChild().getNextSibling().getNodeName());
            assertEquals("xt", b.getFirstChild().getNextSibling(
                        ).getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test moving part of text.
     */
    @Test
    public final void testMovePartOfText() {

        Document doc1 = TestDocHelper.createDocument(
                "<a><b></b><c>text</c></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<move node=\"/a/node()[2]/node()[1]\" " 
                + "parent=\"/a/node()[1]\" childno=\"1\" "
                + "old_charpos=\"2\" length=\"2\"/>" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node b = doc1.getDocumentElement().getFirstChild();
            assertEquals("tt", 
                    b.getNextSibling().getFirstChild().getNodeValue());
            assertEquals("ex", b.getFirstChild().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test moves don't count moved node.
     */
    @Test
    public final void testMoveCount() {

        Document doc1 = TestDocHelper.createDocument(
                "<a><b/><c/><d/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<move node=\"/a/node()[1]\" " 
                + "parent=\"/a\" childno=\"2\"/>" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node c = doc1.getDocumentElement().getFirstChild();
            assertEquals("c", c.getNodeName());
            assertEquals("b", c.getNextSibling().getNodeName());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test update of element.
     */
    @Test
    public final void testUpdateElement() {

        Document doc1 = TestDocHelper.createDocument(
                "<a><b/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<update node=\"/a/b\">c</update>" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node c = doc1.getDocumentElement().getFirstChild();
            assertEquals("c", c.getNodeName());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test update of attribute.
     */
    @Test
    public final void testUpdateAttribute() {

        Document doc1 = TestDocHelper.createDocument(
                "<a><b attr=\"test\"/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<update node=\"/a/b/@attr\">newval</update>"  
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            Node c = doc1.getDocumentElement().getFirstChild();
            assertEquals("newval", ((Element) c).getAttribute("attr"));
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Simple insert operation.
     */
    @Test
    public final void testInsertAtRoot() {
        
        Document doc1 = TestDocHelper.createDocument("<a></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/\" nodetype=\"" + Node.COMMENT_NODE + "\" "
                + "childno=\"1\">comment</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("comment", 
                    doc1.getFirstChild().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test deleting text with CDATA in middle.
     */
    @Test
    public final void testCDATAInText() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[1]\" " 
                + "charpos=\"3\" length=\"4\"/>" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("teext2text3", 
                    doc1.getDocumentElement().getTextContent());
            //Shouldn't remove CDATA section
            assertEquals(Node.CDATA_SECTION_NODE, doc1.getDocumentElement(
                    ).getFirstChild().getNextSibling().getNodeType());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test deleting from end of CDATA into text.
     */
    @Test
    public final void testCDATAInText2() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[1]\" " 
                + "charpos=\"9\" length=\"4\"/>" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("text1texxt3", 
                    doc1.getDocumentElement().getTextContent());
            //Shouldn't remove CDATA section
            assertEquals(Node.CDATA_SECTION_NODE, doc1.getDocumentElement(
                    ).getFirstChild().getNextSibling().getNodeType());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test removing CDATA from centre of text.
     */
    @Test
    public final void testRemoveCDATAFromText() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/a/node()[1]\" " 
                + "charpos=\"6\" length=\"5\"/>" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            doc1.normalize();
            assertEquals("text1text3", 
                    doc1.getDocumentElement().getTextContent());
            //Should only have 1 text node
            assertEquals(1, 
                    doc1.getDocumentElement().getChildNodes().getLength());
            assertEquals(Node.TEXT_NODE, 
                    doc1.getDocumentElement().getFirstChild().getNodeType());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test insert after CDATA.
     */
    @Test
    public final void testInsertAfterCDATA() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" + Node.COMMENT_NODE + "\""
                + " childno=\"2\" charpos=\"11\""
                + ">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            
            Node cdata = doc1.getDocumentElement().getFirstChild(
                    ).getNextSibling();
            assertEquals(Node.CDATA_SECTION_NODE, cdata.getNodeType());
            assertEquals("text2", cdata.getNodeValue());
            assertEquals(Node.COMMENT_NODE, 
                    cdata.getNextSibling().getNodeType());
            assertEquals("text3", 
                    cdata.getNextSibling().getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    /**
     * Test insert before CDATA.
     */
    @Test
    public final void testInsertBeforeCDATA() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" + Node.COMMENT_NODE + "\""
                + " childno=\"2\" charpos=\"6\""
                + ">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            
            assertEquals("text1", doc1.getDocumentElement().getFirstChild(
                    ).getNodeValue());
            Node comment = doc1.getDocumentElement().getFirstChild(
                    ).getNextSibling();
            assertEquals(Node.COMMENT_NODE, comment.getNodeType());
            assertEquals(Node.CDATA_SECTION_NODE, comment.getNextSibling(
                    ).getNodeType());
            assertEquals("text2", 
                    comment.getNextSibling().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test insert into CDATA.
     */
    @Test
    public final void testInsertCommentIntoCDATA() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" + Node.COMMENT_NODE + "\""
                + " childno=\"2\" charpos=\"8\""
                + ">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            
            assertEquals("text1", doc1.getDocumentElement().getFirstChild(
                    ).getNodeValue());
            Node cdata1 = doc1.getDocumentElement().getFirstChild(
                    ).getNextSibling();
            assertEquals(Node.CDATA_SECTION_NODE, cdata1.getNodeType());
            assertEquals("te", cdata1.getNodeValue());
            Node comment = cdata1.getNextSibling();
            assertEquals(Node.COMMENT_NODE, comment.getNodeType());
            Node cdata2 = comment.getNextSibling();
            assertEquals(Node.CDATA_SECTION_NODE, cdata2.getNodeType());
            assertEquals("xt2", cdata2.getNodeValue());
            
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test insert text into CDATA.
     * 
     * *Should* insert a new node.
     * 
     */
    @Test
    public final void testInsertTextIntoCDATA() {
       
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" + Node.TEXT_NODE + "\""
                + " childno=\"2\" charpos=\"8\""
                + ">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            
            assertEquals("text1", doc1.getDocumentElement().getFirstChild(
                    ).getNodeValue());
            Node cdata1 = doc1.getDocumentElement().getFirstChild(
                    ).getNextSibling();
            assertEquals(Node.CDATA_SECTION_NODE, cdata1.getNodeType());
            assertEquals("te", cdata1.getNodeValue());
            Node text = cdata1.getNextSibling();
            assertEquals(Node.TEXT_NODE, text.getNodeType());
            assertEquals("val", text.getNodeValue());
            Node cdata2 = text.getNextSibling();
            assertEquals(Node.CDATA_SECTION_NODE, cdata2.getNodeType());
            assertEquals("xt2", cdata2.getNodeValue());
            
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test insert CDATA into CDATA.
     * 
     * Should *not* insert a new node.
     */
    @Test
    public final void testInsertCDATAIntoCDATA() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>text3</a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" + Node.CDATA_SECTION_NODE
                + "\" childno=\"2\" charpos=\"8\""
                + ">val</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            doc1.normalize();
            
            assertEquals("text1", doc1.getDocumentElement().getFirstChild(
                    ).getNodeValue());
            Node cdata = doc1.getDocumentElement().getFirstChild(
                    ).getNextSibling();
            assertEquals(Node.CDATA_SECTION_NODE, cdata.getNodeType());
            assertEquals("tevalxt2", cdata.getNodeValue());
            Node text = cdata.getNextSibling();
            assertEquals(Node.TEXT_NODE, text.getNodeType());
            assertEquals("text3", text.getNodeValue());
            
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    
    /**
     * Test move CDATA.
     */
    @Test
    public final void testMoveCDATA() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>t<b/></a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<move node=\"/a/node()[1]\" " 
                + "parent=\"/a/node()[2]\" childno=\"1\" "
                + "old_charpos=\"6\" length=\"5\"/>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            doc1.normalize();
            
            assertEquals("text1t", doc1.getDocumentElement().getFirstChild(
                    ).getNodeValue());
            Node b = doc1.getDocumentElement().getFirstChild().getNextSibling();
            assertEquals("b", b.getNodeName());
            assertEquals(Node.CDATA_SECTION_NODE, 
                    b.getFirstChild().getNodeType());
            assertEquals("text2", b.getFirstChild().getNodeValue());
            
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }

    /**
     * Test move part of CDATA.
     * 
     * Would be reasonable to throw an exception, as it is really an attempt
     * to move two nodes.
     * 
     * However, I'd rather make sure a text or CDATA node is inserted to keep
     * things robust.
     */
    @Test
    public final void testMovePartOfCDATA() {
        
        Document doc1 = TestDocHelper.createDocument(
        "<a>text1<![CDATA[text2]]>t<b/></a>");
        
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<move node=\"/a/node()[1]\" " 
                + "parent=\"/a/node()[2]\" childno=\"1\" "
                + "old_charpos=\"9\" length=\"3\"/>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            doc1.normalize();
            
            assertEquals("text1", doc1.getDocumentElement().getFirstChild(
                    ).getNodeValue());
            Node cdata = doc1.getDocumentElement().getFirstChild(
                    ).getNextSibling();
            assertEquals(Node.CDATA_SECTION_NODE, 
                    cdata.getNodeType());
            assertEquals("tex", cdata.getNodeValue());
            Node b = cdata.getNextSibling();
            assertEquals("b", b.getNodeName());
            assertEquals("t2t", b.getFirstChild().getNodeValue());
            
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    @Test
    public final void testInsertPI() {
        
        Document doc1 = TestDocHelper.createDocument("<a></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/\" nodetype=\"" 
                + Node.PROCESSING_INSTRUCTION_NODE + "\" "
                + "childno=\"1\" name=\"piname\">pivalue</insert>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals("pivalue", 
                    doc1.getFirstChild().getNodeValue());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    @Test
    public final void testInsertWithNamespace() {
        
        Document doc1 = TestDocHelper.createDocument(
                "<a xmlns:n=\"http://example.com\"><n:b/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<insert parent=\"/a\" nodetype=\"" 
                + Node.ELEMENT_NODE + "\" "
                + "childno=\"2\" name=\"c\" ns=\"http://new.com\"/>"
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            assertEquals(1, 
                    doc1.getDocumentElement().getElementsByTagNameNS(
                            "http://example.com", "b").getLength());
            assertEquals(1, 
                    doc1.getDocumentElement().getElementsByTagNameNS(
                            "http://new.com", "c").getLength());

        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
    @Test
    public final void testDeletePI() {
        
        Document doc1 = TestDocHelper.createDocument(
                "<?Processing Instruction?><a><b/></a>");
        Document patch = TestDocHelper.createDocument(
                "<delta>"
                + "<delete node=\"/node()[1]\" />" 
                + "</delta>");

        try {
            (new DULPatch()).apply(doc1, patch);
            //Now document element should be only child
            assertEquals(1, doc1.getChildNodes().getLength());
        } catch (PatchFormatException e) {
            fail("Caught exception " + e);
        }
    }
    
}
