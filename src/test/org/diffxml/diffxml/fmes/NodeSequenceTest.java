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

import java.util.List;

import org.diffxml.diffxml.TestDocHelper;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Test for NodeSequence class.
 * 
 * @author Adrian Mouat
 *
 */
public class NodeSequenceTest {

    /**
     * Test getting sequence with all members in common.
     */
    @Test
    public final void testSequenceAllInCommon() {
        
        Document seq1 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");
        Document seq2 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");

        NodePairs pairs = Match.easyMatch(seq1, seq2);
        
        Node[] commSeq = NodeSequence.getSequence(
                seq1.getDocumentElement().getChildNodes(), 
                seq2.getDocumentElement().getChildNodes(), pairs);
        
        assertEquals(4, commSeq.length);
        assertEquals("b", commSeq[0].getNodeName());
        assertEquals("#text", commSeq[1].getNodeName());
        assertEquals("#comment", commSeq[2].getNodeName());
        assertEquals("d", commSeq[3].getNodeName());        
    }

    /**
     * Test getting a sequence with some in common.
     */
    @Test
    public final void testSequenceSomeInCommon() {

        Document seq1 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");
        Document seq2 = TestDocHelper.createDocument(
                "<a><b/>d<!--comment--><d/></a>");
        
        NodePairs pairs = Match.easyMatch(seq1, seq2);
        Node[] commSeq = NodeSequence.getSequence(
                seq1.getDocumentElement().getChildNodes(), 
                seq2.getDocumentElement().getChildNodes(), pairs);

        assertEquals(3, commSeq.length);
        assertEquals("b", commSeq[0].getNodeName());
        assertEquals("#comment", commSeq[1].getNodeName());
        assertEquals("d", commSeq[2].getNodeName());
    }

    /**
     * Test a sequence with no Nodes in common.
     */
    @Test
    public final void testSequenceNoneInCommon() {

        Document seq1 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");
        Document seq2 = TestDocHelper.createDocument(
                "<a><e/>f<!--g--><h/></a>");
        NodePairs pairs = Match.easyMatch(seq1, seq2);
        Node[] commSeq = NodeSequence.getSequence(
                seq1.getDocumentElement().getChildNodes(), 
                seq2.getDocumentElement().getChildNodes(), pairs);
        assertEquals(0, commSeq.length);
    }
    
    /**
     * Test passing null to getSequence.
     */
    @Test
    public final void testSequenceWithNull() {
        
        Document seq1 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");
        Document seq2 = TestDocHelper.createDocument(
                "<a><e/>f<!--g--><h/></a>");
        NodePairs pairs = Match.easyMatch(seq1, seq2);
        
        Node[] commSeq = NodeSequence.getSequence(
                seq1.getDocumentElement().getChildNodes(), null, pairs);
        assertNull(commSeq);
    }
    
    /**
     * Test LCS with all Nodes in common.
     */
    @Test
    public final void testLCSAllInCommon() {
        
        Document set1 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");
        Document set2 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");

        NodePairs pairs = Match.easyMatch(set1, set2);
        assertEquals(12, pairs.size());
        
        Node[] seq1 = NodeSequence.getSequence(
                set1.getDocumentElement().getChildNodes(), 
                set2.getDocumentElement().getChildNodes(), pairs);
        Node[] seq2 = NodeSequence.getSequence(
                set2.getDocumentElement().getChildNodes(), 
                set1.getDocumentElement().getChildNodes(), pairs);
        
        assertEquals(4, seq1.length);
        assertEquals(4, seq2.length);
        
        List<Node> lcs = NodeSequence.getLCS(seq1, seq2, pairs);
        
        assertEquals(4, lcs.size());
        assertEquals("b", lcs.get(0).getNodeName());
        assertEquals("#text", lcs.get(1).getNodeName());
        assertEquals("#comment", lcs.get(2).getNodeName());
        assertEquals("d", lcs.get(3).getNodeName());    
    }

    /**
     * Test LCS with one Node moved.
     */
    @Test
    public final void testLCSNodeMoved() {
        
        Document set1 = TestDocHelper.createDocument(
                "<a><b/>c<!--comment--><d/></a>");
        Document set2 = TestDocHelper.createDocument(
                "<a>c<!--comment--><d/><b/></a>");
        NodePairs pairs = Match.easyMatch(set1, set2);

        Node[] seq1 = NodeSequence.getSequence(
                set1.getDocumentElement().getChildNodes(), 
                set2.getDocumentElement().getChildNodes(), pairs);
        Node[] seq2 = NodeSequence.getSequence(
                set2.getDocumentElement().getChildNodes(), 
                set1.getDocumentElement().getChildNodes(), pairs);

        assertEquals(4, seq1.length);
        assertEquals(4, seq2.length);

        List<Node> lcs = NodeSequence.getLCS(seq1, seq2, pairs);

        assertEquals(3, lcs.size());
        assertEquals("#text", lcs.get(0).getNodeName());
        assertEquals("#comment", lcs.get(1).getNodeName());
        assertEquals("d", lcs.get(2).getNodeName());
    }
    
    /**
     * Test two sequences separated by a common misplaced element.
     */
    @Test
    public final void testLCSWith2Seqs() {
        
        Document set1 = TestDocHelper.createDocument(
                "<a><b/>c<z/><d/>e<f/></a>"); 
        Document set2 = TestDocHelper.createDocument(
                "<a><b/>c<d/>e<f/><z/></a>");
        NodePairs pairs = Match.easyMatch(set1, set2);
        assertEquals(16, pairs.size());

        Node[] seq1 = NodeSequence.getSequence(
                set1.getDocumentElement().getChildNodes(), 
                set2.getDocumentElement().getChildNodes(), pairs);
        Node[] seq2 = NodeSequence.getSequence(
                set2.getDocumentElement().getChildNodes(), 
                set1.getDocumentElement().getChildNodes(), pairs);
        
        assertEquals(6, seq1.length);
        assertEquals(6, seq2.length);

        List<Node> lcs = NodeSequence.getLCS(seq1, seq2, pairs);

        assertEquals(5, lcs.size());
        assertEquals("b", lcs.get(0).getNodeName());
        assertEquals("c", lcs.get(1).getNodeValue());
        assertEquals("d", lcs.get(2).getNodeName());
        assertEquals("e", lcs.get(3).getNodeValue());
        assertEquals("f", lcs.get(4).getNodeName());
    }

    /**
     * Test two sequences with nothing in common.
     */
    @Test
    public final void testLCSWithNoneInCommon() {
        
        Document set1 = TestDocHelper.createDocument(
                "<a><b/></a>"); 
        Document set2 = TestDocHelper.createDocument(
                "<d><e/></d>");
        NodePairs pairs = Match.easyMatch(set1, set2);
        //Remember root nodes and document elements always match
        assertEquals(4, pairs.size());
        
        Node[] seq1 = NodeSequence.getSequence(
                set1.getDocumentElement().getChildNodes(), 
                set2.getDocumentElement().getChildNodes(), pairs);
        Node[] seq2 = NodeSequence.getSequence(
                set2.getDocumentElement().getChildNodes(), 
                set1.getDocumentElement().getChildNodes(), pairs);

        assertEquals(0, seq1.length);
        assertEquals(0, seq2.length);

        List<Node> lcs = NodeSequence.getLCS(seq1, seq2, pairs);
        assertEquals(0, lcs.size());

    }
        
}
