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
package org.diffxml.diffxml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.diffxml.diffxml.fmes.ParserInitialisationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DiffXMLTest {

    @Test
    public final void testSimpleDiff() {
        Diff d = DiffFactory.createDiff();
        try {
            d.diff(new File("test1a.xml"), new File("test2a.xml"));
        } catch (DiffException e) {
            fail("An exception was thrown during the diff: " + e.getMessage());
        }
        //TODO: extend API with method to return document and actually
        //create a patchxml api!

    }
    
	/**
	 * Tests outputXML method.
	 */
    @Test
	public final void testOutputXML()	{
	    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	    DOMOps.initParser(fac);
       
            try {
                String docString = "<?xml version=\"1.0\" encoding=\"UTF-8\""
                    + " standalone=\"yes\"?><x>  <y> "
                    + " <z/> </y></x>";
                ByteArrayInputStream is = new ByteArrayInputStream(
                        docString.getBytes("utf-8"));
                Document doc = fac.newDocumentBuilder().parse(is);
                ByteArrayOutputStream os = new ByteArrayOutputStream(); 
                DOMOps.outputXML(doc, os);
                assertEquals(os.toString(), docString);
            } catch (SAXException e) {
                fail("Caught SAX Exception");
            } catch (IOException e) {
                fail("Caught IOException");
            } catch (ParserConfigurationException e) {
                fail("Caught ParserConfigurationException");
            }

	}
    
    /**
     * Test handling document referencing non-existent DTD.
     */
    @Test
    public final void testNoDTDDiff() {
        Diff d = DiffFactory.createDiff();
        try {
            d.diff(new File("suite/error_handling/nodtd1.xml"), 
                    new File("suite/error_handling/nodtd2.xml"));
            fail("Expected exception due to non resolvable entity");
        } catch (DiffException e) {
            //Expected flow
        }
    }
}
