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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.diffxml.diffxml.TestDocHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Test the NodePairs datatype works correctly.
 * 
 * @author Adrian Mouat
 *
 */
public class NodePairsTest {

    /** Test Doc 1. */
    private static Document mTestDoc1;
    
    /** Test Doc 2. */
    private static Document mTestDoc2;
    
    /**
     * Set up the test docs.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        String docString = "<a><b/><c/><d/></a>";
        mTestDoc1 = TestDocHelper.createDocument(docString);
        docString = "<w><x/><y/><z/></w>";
        mTestDoc2 = TestDocHelper.createDocument(docString);
    }
    
    /**
     * Make sure the correct partner for a node is found.
     */
    @Test
    public void testGetsCorrectPartner() {
        
        NodePairs pairs = new NodePairs();
        assertEquals(0, pairs.size());
        
        Node a = mTestDoc1.getDocumentElement();
        Node w = mTestDoc2.getDocumentElement();
        assertNull(pairs.getPartner(a));
        assertFalse(pairs.isMatched(a));
        assertFalse(pairs.isMatched(w));
        pairs.add(a, w);
        assertEquals(w, pairs.getPartner(a));
        assertEquals(a, pairs.getPartner(w));
        assertEquals(2, pairs.size());
        assertTrue(pairs.isMatched(a));
        assertTrue(pairs.isMatched(w));

        
        Node b = a.getFirstChild();
        Node x = w.getFirstChild();
        pairs.add(x, b);
        
        Node c = b.getNextSibling();
        Node y = x.getNextSibling();
        pairs.add(c, y);
        
        Node d = c.getNextSibling();
        Node z = y.getNextSibling();
        assertNull(pairs.getPartner(z));
        pairs.add(d, z);
        
        assertEquals(b, pairs.getPartner(x));
        assertEquals(x, pairs.getPartner(b));
        assertEquals(c, pairs.getPartner(y));
        assertEquals(y, pairs.getPartner(c));
        assertEquals(d, pairs.getPartner(z));
        assertEquals(z, pairs.getPartner(d));
        assertEquals(8, pairs.size());
    }
    
    /**
     * Check trying to add a null throws an exception.
     */
    @Test
    public final void testHandlesNulls() {
        
        NodePairs pairs = new NodePairs();
        
        try {
            pairs.add(mTestDoc1.getDocumentElement(), null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNull(pairs.getPartner(null));
            assertNull(pairs.getPartner(mTestDoc1.getDocumentElement()));
        }
        
        try {
            pairs.add(null, null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNull(pairs.getPartner(null));
        }   
        
    }

}
