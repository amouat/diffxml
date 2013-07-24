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

import java.io.File;

import org.w3c.dom.Document;

/**
 * This is the interface all differencing algorithms should implement.
 *
 * The class defines two diff methods for handling File and String input.
 * 
 * The result is returned as an XML document, which unfortunately means DOM
 * needs to be used.
 * 
 * TODO: Add a method that takes URLs
 *
 * @author    Adrian Mouat
 */

public interface Diff {

    /**
     * Differences two files.
     *
     * Returns a patch document representing the differences. 
     * 
     * The document will have only one empty element if the documents are
     * identical.
     *
     * TODO: Consider changing the return type to an interface supporting
     * printing to stream and a boolean areIdentical method.
     *
     * @param f1    Original file
     * @param f2    Modified file
     * @return Document An XML document containing the differences between the 
     *                  2 files.
     * @throws DiffException If something goes wrong
     */

    Document diff(final File f1, final File f2) throws DiffException;


}

