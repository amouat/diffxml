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

import org.diffxml.diffxml.TestDocHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test class for NodeDepth.
 * 
 * @author Adrian Mouat
 *
 */
public class NodeDepthTest {

    /** Test document. */
    private static Document mTestDoc1;
    
    /**
     * Initialises the test doc.
     */
    @BeforeClass
    public static void setUpTest() {
        
        String docString = "<x>text1<y><!-- comment --><z/>text2</y></x>";
        mTestDoc1 = TestDocHelper.createDocument(docString);
        
    }
    
    /** 
     * Helper method for testing nodes.
     *  
     * @param n The node to test
     * @param expectedDepth The expected depth of the node
     */
    private void testCreatingNodeDepth(final Node n, final int expectedDepth) {
        
        NodeDepth depthTest = new NodeDepth(n);
        assertEquals(expectedDepth, depthTest.getDepth());
        assertEquals(n, depthTest.getNode());
    }
    
    /**
     * Test calculating depth of nodes in document.
     */
    @Test
    public void testCorrectDepthCalculated() {
        
        //Try root
        testCreatingNodeDepth(mTestDoc1, 0);
        
        //Try document element
        Element docEl = mTestDoc1.getDocumentElement();
        testCreatingNodeDepth(docEl, 1);
        
        //Try first text node
        Node text1 = docEl.getFirstChild();
        testCreatingNodeDepth(text1, 2);
        
        //y Node
        Node y = text1.getNextSibling();
        testCreatingNodeDepth(y, 2);
        
        //Comment node
        Node comment = y.getFirstChild();
        testCreatingNodeDepth(comment, 3);
        
        //z Node
        Node z = comment.getNextSibling();
        testCreatingNodeDepth(z, 3);
        
        //second text node
        Node text2 = z.getNextSibling();
        testCreatingNodeDepth(text2, 3);                
    }
    
    /**
     * Check a NullPointerException is thrown if handed a null node.
     */
    @Test(expected = NullPointerException.class)  
    public void testNull() {
        NodeDepth nullTest = new NodeDepth(null);
    }
}
