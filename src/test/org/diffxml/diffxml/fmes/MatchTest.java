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

import org.diffxml.diffxml.TestDocHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Class to test matching algorithm.
 * 
 * TODO: Add more tests for similar docs and other node types.
 * 
 * @author Adrian Mouat
 *
 */
public class MatchTest {

    /** Test Doc 1a. */
    private static Document mTestDoc1a;
    
    /** Test Doc 1b. */
    private static Document mTestDoc1b;

    /** Test Doc 2a. */
    private static Document mTestDoc2a;

    /** Test Doc 2b. */
    private static Document mTestDoc2b;

    /** Test Doc 3a. */
    private static Document mTestDoc3a;

    /** Test Doc 3b. */
    private static Document mTestDoc3b;
    
    /** Test Doc 4a. */
    private static Document mTestDoc4a;
    
    /**
     * Set up documents before test.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        
        mTestDoc1a = TestDocHelper.createDocument("<a><b><c/></b></a>");
        mTestDoc1b = TestDocHelper.createDocument("<a><b><c/></b></a>");
        
        mTestDoc2a = TestDocHelper.createDocument(
                "<a>text1<b attr='b'><!-- comment --></b></a>");
        mTestDoc2b = TestDocHelper.createDocument(
                "<a>text1<b attr='b'><!-- comment --></b></a>");
        
        mTestDoc3a = TestDocHelper.createDocument("<x><y><z/></y></x>");
        mTestDoc3b = TestDocHelper.createDocument(
                "<x>different<y><!-- different --></y></x>");
        
        mTestDoc4a = TestDocHelper.createDocument(
                "<a>newtext<b attr='c'><!-- comment --></b></a>");
    }

    /**
     * Just make sure a simple identical document with only elements matches
     * correctly. 
     */
    @Test
    public final void testSimpleIdenticalDoc() {
        
        NodePairs all = Match.easyMatch(mTestDoc1a, mTestDoc1b);
        
        Node aDocEl = mTestDoc1a.getDocumentElement();
        Node partner = all.getPartner(aDocEl);
        
        Node bDocEl = mTestDoc1b.getDocumentElement();
        assertEquals(bDocEl, partner);
        
        Node aB = aDocEl.getFirstChild();
        partner = all.getPartner(aB);
        
        Node bB = bDocEl.getFirstChild();
        assertEquals(bB, partner);
        
        Node aC = aB.getFirstChild();
        partner = all.getPartner(aC);
        
        Node bC = bB.getFirstChild();
        assertEquals(bC, partner);
    }
    
    /**
     * Now test identical doc with comments and text matches correctly. 
     */
    @Test
    public final void testIdenticalDocWithTextAndComments() {

        NodePairs all = Match.easyMatch(mTestDoc2a, mTestDoc2b);
        
        Node aDocEl = mTestDoc2a.getDocumentElement();
        Node partner = all.getPartner(aDocEl);
        
        Node bDocEl = mTestDoc2b.getDocumentElement();
        assertEquals(bDocEl, partner);
        
        Node aText = aDocEl.getFirstChild();
        partner = all.getPartner(aText);
        
        Node bText = bDocEl.getFirstChild();
        assertEquals(bText, partner);

        Node aB = aText.getNextSibling();
        partner = all.getPartner(aB);
        
        Node bB = bText.getNextSibling();
        assertEquals(bB, partner);

        Node aComment = aB.getFirstChild();
        partner = all.getPartner(aComment);
        
        Node bComment = bB.getFirstChild();
        assertEquals(bComment, partner);
    }
    
    /**
     * Test completely different docs - only root nodes should match.
     */
    @Test
    public final void testDifferentDocs() {
        
        //Remember both root and doc elements are forced to match
        NodePairs matches = Match.easyMatch(mTestDoc1a, mTestDoc3a);
        assertEquals(4, matches.size());
        
        matches = Match.easyMatch(mTestDoc2a, mTestDoc3b);
        assertEquals(4, matches.size());
    }

    /**
     * Test similar documents match partly.
     */
    @Test
    public final void testSimilarDocs() {
        
        //<a>text1<b attr='b'><!-- comment --></b></a>
        //<a>newtext<b attr='c'><!-- comment --></b></a>
        
        NodePairs matches = Match.easyMatch(mTestDoc2a, mTestDoc4a);
        
        Node aDocEl = mTestDoc2a.getDocumentElement();
        Node partner = matches.getPartner(aDocEl);
        
        Node bDocEl = mTestDoc4a.getDocumentElement();
        assertEquals(bDocEl, partner);
        
        Node aText = aDocEl.getFirstChild();
        assertNull(matches.getPartner(aText));
        
        Node aB = aText.getNextSibling();
        assertNull(matches.getPartner(aB));

        Node aComment = aB.getFirstChild();
        partner = matches.getPartner(aComment);
        
        Node bComment = bDocEl.getFirstChild().getNextSibling().getFirstChild();
        assertEquals(bComment, partner);
        
    }

    /**
     * Test documents with same elements but in different order match 
     * completely.
     */
    @Test
    public final void testDifferentOrdering() {
        Document doc1 = TestDocHelper.createDocument(
                "<a><b/>c<z/><d/>e<f/></a>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<a><b/>c<d/>e<f/><z/></a>");
        
        NodePairs matches = Match.easyMatch(doc1, doc2);
        
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        Node bB = bDocEl.getFirstChild();
        assertEquals(bB, matches.getPartner(aB));
        
        Node aC = aB.getNextSibling();
        Node bC = bB.getNextSibling();
        assertEquals(bC, matches.getPartner(aC));
        
        Node aZ = aC.getNextSibling();
        Node bD = bC.getNextSibling();
        Node aD = aZ.getNextSibling();
        assertEquals(bD, matches.getPartner(aD));
        
        Node aE = aD.getNextSibling();
        Node bE = bD.getNextSibling();
        assertEquals(bE, matches.getPartner(aE));
        
        Node aF = aE.getNextSibling();
        Node bF = bE.getNextSibling();
        assertEquals(bF, matches.getPartner(aF));
        
        Node bZ = bF.getNextSibling();
        assertEquals(bZ, matches.getPartner(aZ));
    }

    /**
     * Test elements with different attributes don't match.
     */
    @Test
    public final void testElementsWithDiffAttrs() {
        Document doc1 = TestDocHelper.createDocument(
                "<a><b/><c a=\"1\"/></a>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<a><b a=\"1\"/><c a=\"1\"/></a>");
        
        //a and c should match, b shouldn't
        NodePairs matches = Match.easyMatch(doc1, doc2);
        
        Node decEl1 = doc1.getDocumentElement();
        Node docEl2 = doc2.getDocumentElement();
        assertEquals(decEl1, matches.getPartner(docEl2));

        Node b1 = decEl1.getFirstChild();
        assertNull(matches.getPartner(b1));

        Node c1 = b1.getNextSibling();
        Node c2 = docEl2.getFirstChild().getNextSibling();
        assertEquals(c2, matches.getPartner(c1));
    }

    /**
     * Test documents with two possible matches matches nearest.
     *
     * Currently ignored as not essential and slightly advanced.
     */
    @Test
    @Ignore
    public final void testMatchNearest() {
        Document doc1 = TestDocHelper.createDocument(
                "<a>b<c/>b</a>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<a>z<c/>b</a>");
        
        NodePairs matches = Match.easyMatch(doc1, doc2);
        
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        Node bZ = bDocEl.getFirstChild();
        
        Node aC = aB.getNextSibling();
        Node bC = bZ.getNextSibling();
        assertEquals(bC, matches.getPartner(aC));
        
        Node aB2 = aC.getNextSibling();
        Node bB = bC.getNextSibling();
        assertEquals(bB, matches.getPartner(aB2));
    }

    /**
     * Test elements with different attributes aren't matched.
     *
     */
    @Test
    public final void testDifferingAttributes() {
        Document doc1 = TestDocHelper.createDocument(
                "<a><b a1=\"y\"/></a>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<a><b a2=\"n\"/></a>");
     
        NodePairs matches = Match.easyMatch(doc1, doc2);
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        assertNull(matches.getPartner(aB));
        
    }
    
    /**
     * Elements with different prefixes but same namespace should match.
     *
     */
    @Test
    public final void testDifferingNamespacePrefix() {
        Document doc1 = TestDocHelper.createDocument(
                "<root xmlns:a=\"http://example.com\"><a:a></a:a></root>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<root xmlns:b=\"http://example.com\"><b:a></b:a></root>");
     
        NodePairs matches = Match.easyMatch(doc1, doc2);
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        assertEquals(bDocEl.getFirstChild(), matches.getPartner(aB));
    }

    /**
     * Elements with same prefix but different namespace should not match.
     *
     */
    @Test
    public final void testDifferingNamespace() {
        Document doc1 = TestDocHelper.createDocument(
                "<root xmlns:a=\"http://example.com\"><a:a></a:a></root>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<root xmlns:a=\"http://different.com\"><a:a></a:a></root>");
     
        NodePairs matches = Match.easyMatch(doc1, doc2);
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        assertNull(matches.getPartner(aB));
    }
    
    /**
     * Elements with redundant namespace declarations should match
     *
     */
    @Test
    public final void testRedundantNamespacePrefix() {
        Document doc1 = TestDocHelper.createDocument(
                "<root xmlns:a=\"http://example.com\"><a:a></a:a></root>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<root xmlns:b=\"http://example.com\" " +
                "xmlns:c=\"http://example2.com\"><b:a " +
                "xmlns=\"http://example3.com\"></b:a></root>");
     
        NodePairs matches = Match.easyMatch(doc1, doc2);
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        assertEquals(bDocEl.getFirstChild(), matches.getPartner(aB));
    }

    /**
     * Different styles of declaring namespaces should match
     *
     */
    @Test
    public final void testDeclareNamespaces() {
        Document doc1 = TestDocHelper.createDocument(
                "<root xmlns:a=\"http://example.com\"><a:a></a:a></root>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<root><a xmlns=\"http://example.com\"></a></root>");
     
        NodePairs matches = Match.easyMatch(doc1, doc2);
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        assertEquals(bDocEl.getFirstChild(), matches.getPartner(aB));
    }

    /**
     * Test matching XML namespace.
     *
     */
    @Test
    public final void testXMLNamespaceMatching() {
        Document doc1 = TestDocHelper.createDocument(
                "<root><a xml:lang=\"en\" lang=\"en\"></a></root>"); 
        Document doc2 = TestDocHelper.createDocument(
                "<root><a xml:lang=\"en\" lang=\"en\"></a></root>");
     
        NodePairs matches = Match.easyMatch(doc1, doc2);
        Node aDocEl = doc1.getDocumentElement();
        Node bDocEl = doc2.getDocumentElement();
        assertEquals(bDocEl, matches.getPartner(aDocEl));
        
        Node aB = aDocEl.getFirstChild();
        assertEquals(bDocEl.getFirstChild(), matches.getPartner(aB));
    }
}
