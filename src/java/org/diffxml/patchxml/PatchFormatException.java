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

/**
 * Indicates a formatting error in a delta.
 * 
 * @author Adrian Mouat
 *
 */
public class PatchFormatException extends Exception {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param e Chained exception
     */
    public PatchFormatException(final Exception e) {
        super(e);
    }
    
     /**
     * Constructor.
     * 
     * @param s Description of error
     */
    public PatchFormatException(final String s) {
        super(s);
    }
       
    /**
     * Constructor.
     * 
     * @param s Description of error
     * @param e Chained exception
     */
    public PatchFormatException(final String s, final Exception e) {
        super(s, e);
    }
}

