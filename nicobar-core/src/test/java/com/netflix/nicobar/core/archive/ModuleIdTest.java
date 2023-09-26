/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package com.netflix.nicobar.core.archive;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link ModuleId}
 *
 * @author Vasanth Asokan
 * @author Aaron Tull
 */
public class ModuleIdTest {

    @Test
    public void testDefaultVersion() {
        ModuleId moduleId = ModuleId.create("/test/module");
        assertEquals(moduleId.toString(), "/test/module");
    }

    @Test
    public void testWithVersion() {
        ModuleId moduleId = ModuleId.create("test-Module", "v1");
        assertEquals(moduleId.toString(), "test-Module" + ModuleId.MODULE_VERSION_SEPARATOR + "v1");
    }

    @Test
    public void testFromStringDefaultVersion() {
        ModuleId moduleId = ModuleId.fromString("test-Module");
        assertEquals(moduleId.toString(), "test-Module");
    }

    @Test
    public void testFromStringWithVersion() {
        ModuleId moduleId = ModuleId.fromString("test-Module.v2");
        assertEquals(moduleId.toString(), "test-Module.v2");
    }

    @Test
    public void testBadModuleName() {
        // Just to make PMD happy about empty catch blocks,
        // We set a dummy operation.
        @SuppressWarnings("unused")
        boolean passed = false;

        try {
            ModuleId.fromString("test.Module.v2");
            fail("Should disallow dots in module name");
        } catch (IllegalArgumentException e) {
            passed = true;
        }
        try {
            ModuleId.create("test.Module", "v2");
            fail("Should disallow dots in module name");
        } catch (IllegalArgumentException e) {
            passed = true;
        }
        try {
            ModuleId.create("test.Module");
            fail("Should disallow dots in module name");
        } catch (IllegalArgumentException e) {
            passed = true;
        }
        try {
            ModuleId.create("", "v2");
            fail("Should disallow empty module name");
        } catch (IllegalArgumentException e) {
            passed = true;
        }

        char [] disallowedChars = { '#', '!', '(', ')', '.'};
        for (char c: disallowedChars) {
            try {
                ModuleId.create("testModule" + Character.toString(c) + "suffix", "v1");
                fail("Should disallow " + Character.toString(c) +  " in module name");
            } catch (IllegalArgumentException e) {
                passed = true;
            }
        }
    }

    @Test
    public void testModuleVersion() {
        ModuleId moduleId = ModuleId.create("test-Module", "");
        assertEquals(moduleId.toString(), "test-Module");
    }

    @Test
    public void testBadModuleVersion() {
        // Just to make PMD happy about empty catch blocks,
        // We set a dummy operation.
        @SuppressWarnings("unused")
        boolean passed = false;
        try {
            ModuleId.create("test-Module", ".v2");
            fail("Should disallow dots in module version");
        } catch (IllegalArgumentException e) {
            passed = true;
        }

        char [] disallowedChars = { '/', '\\', '#', '!', '(', ')', '.'};
        for (char c: disallowedChars) {
            try {
                ModuleId.create("testModule", "v" + Character.toString(c) + "1");
                fail("Should disallow " + Character.toString(c) +  " in module version");
            } catch (IllegalArgumentException e) {
                passed = true;
            }
        }
    }

    @Test
    public void testNegativeIntegerStringifiedName() {
    	String name = Integer.toHexString(Integer.MIN_VALUE);
	    ModuleId moduleId = ModuleId.create(name);
    	assertEquals(moduleId.toString(), name);
    }
}
