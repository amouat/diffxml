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

import org.diffxml.diffxml.TestDocHelper;
import org.junit.Test;
import org.w3c.dom.Document;


import org.w3c.dom.Node;

/**
 * Test FindPosition.
 * 
 * @author Adrian Mouat
 */
public class FindPositionTest {

       
    /**
     * Test simple insert of element.
     */
    @Test
    public final void testElementInsert() {

        Document testDoc1 = TestDocHelper.createDocument("<a><b/></a>");
        Document testDoc2 = TestDocHelper.createDocument("<a><b/><c/></a>");
        
        NodePairs pairs = Match.easyMatch(testDoc1, testDoc2);
        
        Node c = testDoc2.getFirstChild().getFirstChild().getNextSibling();
        assertEquals("c", c.getNodeName());
        FindPosition fp = new FindPosition(c, pairs);
        assertEquals(1, fp.getDOMInsertPosition());
        assertEquals(2, fp.getXPathInsertPosition());
        assertEquals(1, fp.getCharInsertPosition());
    }

    /**
     * Test where no leftmost match.
     */
    @Test
    public final void testSimpleInsert() {

        Document testDoc1 = TestDocHelper.createDocument("<a><b/><c/></a>");
        Document testDoc2 = TestDocHelper.createDocument("<a><d/><e/></a>");
        
        NodePairs pairs = Match.easyMatch(testDoc1, testDoc2);
        //Need to mark d out-of-order for the algorithm to work
        NodeOps.setOutOfOrder(testDoc2.getFirstChild().getFirstChild());
        
        Node e = testDoc2.getFirstChild().getFirstChild().getNextSibling();
        assertEquals("e", e.getNodeName());
        FindPosition fp = new FindPosition(e, pairs);
        assertEquals(0, fp.getDOMInsertPosition());
        assertEquals(1, fp.getXPathInsertPosition());
        assertEquals(1, fp.getCharInsertPosition());
    }
    
    /**
     * Test inserting a node after text with a leftmost match.
     */
    @Test
    public final void testInsertingAfterText() {

        Document testDoc1 = TestDocHelper.createDocument("<a>sometext</a>");
        Document testDoc2 = TestDocHelper.createDocument("<a>sometext<b/></a>");
        
        NodePairs pairs = Match.easyMatch(testDoc1, testDoc2);
        
        Node b = testDoc2.getFirstChild().getFirstChild().getNextSibling();
        assertEquals("b", b.getNodeName());
        FindPosition fp = new FindPosition(b, pairs);
        assertEquals(1, fp.getDOMInsertPosition());
        assertEquals(2, fp.getXPathInsertPosition());
        assertEquals(9, fp.getCharInsertPosition());
    }

}
