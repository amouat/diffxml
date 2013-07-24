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

import org.diffxml.diffxml.fmes.Fmes;


/**
 * DiffFactory creates Diff instances.
 *
 * @author 	Adrian Mouat
 */
public final class DiffFactory {
    /*
     * TODO: Make state fixed for each diff instance. Currently 
     * changes to options affect in-process diffs.
     */

    /**
     * Report only if files differ.
     * Default off.
     */
    private static boolean mBrief = false;

    /**
     * Provide debug output.
     * Default false.
     */
    private static boolean mDebug = false;

    /**
     * Ignore all whitespace.
     * Default off.
     */
    private static boolean mIgnoreAllWhitespace = false;

    /**
     * Ignore leading whitespace.
     * Default off.
     */
    private static boolean mIgnoreLeadingWhitespace = false;

    /**
     * Ignore whitespace only nodes.
     * Default off.
     */
    private static boolean mIgnoreWhitespaceNodes = false;

    /**
     * Ignore changes in case only.
     * Default off.
     */
    private static boolean mIgnoreCase = false;

    /**
     * Ignore comments.
     * Default off.
     */
    private static boolean mIgnoreComments = false;

    /**
     * Ignore processing instructions.
     * Default off.
     */
    private static boolean mIgnoreProcessingInstructions = false;

    /**
     * Output tagnames rather than node numbers.
     * Default off.
     */
    private static boolean mUseTagnames = false;

    /**
     * Output reverse patching context.
     * Default off.
     */
    private static boolean mReversePatch = false;

    /**
     * Whether or not to output context nodes.
     * Default off.
     */
    private static boolean mContext = false;

    /**
     * Amount of sibling context.
     * Default 2.
     */
    private static int mSiblingContext = 2;

    /**
     * Amount of parent context.
     * Default 1.
     */
    private static int mParentContext = 1;

    /**
     * Amount of parent sibling context.
     * Default 0.
     */
    private static int mParentSiblingContext = 0;

    /**
     * Algorithm to use.
     * Default FMES.
     */
    private static boolean mFMES = true;

    /**
     * Use DUL output format.
     * No other format currently supported.
     * default on.
     */
    private static boolean mDUL = true;

    /** Resolving of entities. */
    private static boolean mResolveEntities = true;

    /**
     * Private constructor - shouldn't be called.
     */
    private DiffFactory() {
        //Shouldn't be called
    }

    /**
     * Only report if files differ, do not output differences.
     * 
     * @param brief Sets brief output
     */
    public static void setBrief(final boolean brief) {
        mBrief = brief;
    }
    
    /**
     * If brief mode is on, only reports if files differ, 
     * does not output differences.
     * 
     * @return True if brief output is on
     */
    public static boolean isBrief() {
        return mBrief;
    }

    /**
     * Output extra debug info.
     * 
     * @param debug Sets debug output
     */
    public static void setDebug(final boolean debug) {
        mDebug = debug;
    }
    
    /**
     * If debug mode is on, extra debug info is output.
     * 
     * @return True if debug is on
     */
    public static boolean isDebug() {
        return mDebug;
    }
    /**
     * Sets whether any differences in whitespace should be considered.
     * 
     * @param ignore If true, whitespace is ignored
     */
    public static void setIgnoreAllWhitespace(final boolean ignore) {
        mIgnoreAllWhitespace = ignore;
    }
    
    /**
     * Gets whether any differences in whitespace should be considered.
     * 
     * @return True if whitespace is to be ignored
     */
    public static boolean isIgnoreAllWhitespace() {
        return mIgnoreAllWhitespace;
    }

    /**
     * Sets whether differences in leading whitespace should be considered.
     * 
     * @param ignore If true, leading whitespace is ignored
     */
    public static void setIgnoreLeadingWhitespace(final boolean ignore) {
        mIgnoreLeadingWhitespace = ignore;
    }
    
    /**
     * Gets whether differences in leading whitespace should be considered.
     * 
     * @return True if leading whitespace is to be ignored
     */
    public static boolean isIgnoreLeadingWhitespace() {
        return mIgnoreLeadingWhitespace;
    }

    /**
     * Sets whether nodes with only whitespace should be considered.
     * 
     * @param ignore If true, whitespace only nodes are ignored
     */
    public static void setIgnoreWhitespaceNodes(final boolean ignore) {
        mIgnoreWhitespaceNodes = ignore;
    }
    
    /**
     * Gets whether nodes with only whitespace should be considered.
     * 
     * @return True if whitespace only nodes are to be ignored
     */
    public static boolean isIgnoreWhitespaceNodes() {
        return mIgnoreWhitespaceNodes;
    }

    /**
     * Sets whether differences in case should be considered.
     * 
     * @param ignore If true, case differences are ignored
     */
    public static void setIgnoreCase(final boolean ignore) {
        mIgnoreCase = ignore;
    }
    
    /**
     * Gets whether differences in case should be considered.
     * 
     * @return True if case differences are ignored
     */
    public static boolean isIgnoreCase() {
        return mIgnoreCase;
    }

    /**
     * Sets whether differences in comments should be considered.
     * 
     * @param ignore If true, differences in comments are ignored
     */
    public static void setIgnoreComments(final boolean ignore) {
        mIgnoreComments = ignore;
    }
    
    /**
     * Gets whether differences in comments should be considered.
     * 
     * @return True if differences in comments are ignored
     */
    public static boolean isIgnoreComments() {
        return mIgnoreComments;
    }

    /**
     * Sets whether differences in processing instructions should be considered.
     * 
     * @param ignore If true, differences in processing instructions are 
     *               ignored.
     */
    public static void setIgnoreProcessingInstructions(final boolean ignore) {
        mIgnoreProcessingInstructions = ignore;
    }
    
    /**
     * Gets whether differences in processing instructions should be considered.
     * 
     * @return True if differences in processing instructions are ignored
     */
    public static boolean isIgnoreProcessingInstructions() {
        return mIgnoreProcessingInstructions;
    }

    /**
     * Sets whether tagnames should be output instead of node numbers in xpaths.
     * 
     * @param useTagnames If true, tagnames are output in xpaths
     */
    public static void setUseTagnames(final boolean useTagnames) {
        mUseTagnames = useTagnames;
    }
    
    /**
     * Gets whether tagnames should be output instead of node numbers in xpaths.
     * 
     * @return True if differences in processing instructions are ignored
     */
    public static boolean isUseTagnames() {
        return mUseTagnames;
    }

    /**
     * If set, adds information needed to reverse patches.
     * 
     * @param reverse If true, extra output is generated to allow reversing of
     *               patches
     */
    public static void setReversePatch(final boolean reverse) {
        mReversePatch = reverse;
    }
    
    /**
     * Gets whether extra output is generated to allow reversing of patches.
     * 
     * @return True if extra output for reverse patches is generated
     */
    public static boolean isReversePatch() {
        return mReversePatch;
    }

    /**
     * If set, adds extra context nodes to output.
     * 
     * @param context If true, context nodes are output.
     */
    public static void setContext(final boolean context) {
        mContext = context;
    }
    
    /**
     * Gets whether extra context nodes are added to the output.
     * 
     * @return True if context nodes are output.
     */
    public static boolean isContext() {
        return mContext;
    }
    
    /**
     * Sets the number of sibling context nodes used.
     * These are output to each side of the node. 
     * 
     * @param context Number of sibling context nodes.
     */
    public static void setSiblingContext(final int context) {
        
        if (context < 0) {
            throw new IllegalArgumentException("Sibling context must be >= 0");
        }
        mSiblingContext = context;
    }
    
    /**
     * Gets the number of sibling context nodes used.
     * 
     * @return Number of sibling context nodes.
     */
    public static int getSiblingContext() {
        return mSiblingContext;
    }

    /**
     * Sets the number of parent and child context nodes used.
     * This number of both parent and child nodes will be output. 
     * 
     * @param context Number of parent context nodes.
     */
    public static void setParentContext(final int context) {
        
        if (context < 0) {
            throw new IllegalArgumentException("Parent context must be >= 0");
        }
        mParentContext = context;
    }
    
    /**
     * Gets the number of parent and child context nodes used.
     * 
     * @return Number of parent and child context nodes.
     */
    public static int getParentContext() {
        return mParentContext;
    }

    /**
     * Sets the number of parent and child sibling context nodes used.
     * This number of nodes will be output to both sides of parent and child 
     * nodes. 
     * 
     * @param context Number of parent and child sibling context nodes.
     */
    public static void setParentSiblingContext(final int context) {
        
        if (context < 0) {
            throw new IllegalArgumentException(
                    "ParentSibling context must be >= 0");
        }
        mParentSiblingContext = context;
    }
    
    /**
     * Gets the number of parent and child sibling context nodes used.
     * 
     * @return Number of parent and child sibling context nodes.
     */
    public static int getParentSiblingContext() {
        return mParentSiblingContext;
    }

    /**
     * Sets whether the FMES algorithm is used.
     * 
     * @param useFMES If true, the FMES algorithm is used.
     */
    public static void setFMES(final boolean useFMES) {
        mFMES = useFMES;
    }
    
    /**
     * Gets whether the FMES algorithm is used.
     * 
     * @return True if the FMES algorithm is used.
     */
    public static boolean isFMES() {
        return mFMES;
    }

    /**
     * Sets whether the DUL output format is used.
     * 
     * @param useDUL If true, the DUL output format is used.
     */
    public static void setDUL(final boolean useDUL) {
        mDUL = useDUL;
    }
    
    /**
     * Gets whether the DUL output format is used.
     * 
     * @return True if the DUL output format is used.
     */
    public static boolean isDUL() {
        return mDUL;
    }

    /**
     * Sets whether external entities should be resolved.
     * 
     * @param resolve If true, external entities are resolved.
     */
    public static void setResolveEntities(final boolean resolve) {
        mResolveEntities = resolve;
    }
    
    /**
     * Gets whether external entities should be resolved.
     * 
     * @return True if external entities are resolved.
     */
    public static boolean isResolveEntities() {
        return mResolveEntities;
    }
    
    /**
     * Creates an instance of the appropriate Diff engine.
     * 
     * Currently only FMES, may be more in future.
     * 
     * @return a difference engine meeting implementing the Diff interface
     */
    public static Diff createDiff() {
        
        return new Fmes();
    }

}
