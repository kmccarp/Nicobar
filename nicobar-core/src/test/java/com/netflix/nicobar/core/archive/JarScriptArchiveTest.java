/*
 *
 *  Copyright 2013 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.nicobar.core.archive;

import static com.netflix.nicobar.core.testutil.CoreTestResourceUtil.TestResource.TEST_DEFAULT_MODULE_SPEC_JAR2;
import static com.netflix.nicobar.core.testutil.CoreTestResourceUtil.TestResource.TEST_DEFAULT_MODULE_SPEC_JAR;
import static com.netflix.nicobar.core.testutil.CoreTestResourceUtil.TestResource.TEST_MODULE_SPEC_JAR;
import static com.netflix.nicobar.core.testutil.CoreTestResourceUtil.TestResource.TEST_TEXT_JAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.netflix.nicobar.core.archive.JarScriptArchive;
import com.netflix.nicobar.core.archive.ScriptModuleSpec;

/**
 * Unit Tests for {@link JarScriptArchive}
 *
 * @author James Kojo
 * @author Vasanth Asokan
 */
public class JarScriptArchiveTest {
    @Test
    public void testLoadTextJar() throws Exception {
        URL testJarUrl = getClass().getClassLoader().getResource(TEST_TEXT_JAR.getResourcePath());
        Path jarPath = Paths.get(testJarUrl.toURI()).toAbsolutePath();

        JarScriptArchive scriptArchive = new JarScriptArchive.Builder(jarPath)
            .setModuleSpec(new ScriptModuleSpec.Builder(ModuleId.create("testModuleId")).build())
            .build();
        assertEquals(scriptArchive.getModuleSpec().getModuleId().toString(), "testModuleId");
        Set<String> archiveEntryNames = scriptArchive.getArchiveEntryNames();
        assertEquals(archiveEntryNames, TEST_TEXT_JAR.getContentPaths());
        for (String entryName : archiveEntryNames) {
            URL entryUrl = scriptArchive.getEntry(entryName);
            assertNotNull(entryUrl);
            InputStream inputStream = entryUrl.openStream();
            String content = IOUtils.toString(inputStream, Charsets.UTF_8);
            assertNotNull(content);
        }
    }

    @Test
    public void testDefaultModuleId() throws Exception {
        URL rootPathUrl = getClass().getClassLoader().getResource(TEST_DEFAULT_MODULE_SPEC_JAR.getResourcePath());
        Path rootPath = Paths.get(rootPathUrl.toURI()).toAbsolutePath();
        JarScriptArchive scriptArchive = new JarScriptArchive.Builder(rootPath).build();
        assertEquals(scriptArchive.getModuleSpec().getModuleId(), TEST_DEFAULT_MODULE_SPEC_JAR.getModuleId());

        rootPathUrl = getClass().getClassLoader().getResource(TEST_DEFAULT_MODULE_SPEC_JAR2.getResourcePath());
        rootPath = Paths.get(rootPathUrl.toURI()).toAbsolutePath();
        scriptArchive = new JarScriptArchive.Builder(rootPath).build();
        assertEquals(scriptArchive.getModuleSpec().getModuleId(), TEST_DEFAULT_MODULE_SPEC_JAR2.getModuleId());
    }

    @Test
    public void testLoadWithModuleSpec() throws Exception {
        URL rootPathUrl = getClass().getClassLoader().getResource(TEST_MODULE_SPEC_JAR.getResourcePath());
        Path rootPath = Paths.get(rootPathUrl.toURI()).toAbsolutePath();

        // if the module spec isn't provided, it should be discovered in the jar
        JarScriptArchive scriptArchive = new JarScriptArchive.Builder(rootPath).build();
        ScriptModuleSpec moduleSpec = scriptArchive.getModuleSpec();
        assertEquals(moduleSpec.getModuleId(), TEST_MODULE_SPEC_JAR.getModuleId());
        assertEquals(moduleSpec.getModuleDependencies(), Collections.emptySet());
        Map<String, String> expectedMetadata = new HashMap<>();
        expectedMetadata.put("metadataName1", "metadataValue1");
        expectedMetadata.put("metadataName2", "metadataValue2");
    }
}
