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
package com.netflix.nicobar.core.persistence;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.netflix.nicobar.core.persistence.ArchiveRepository;
import com.netflix.nicobar.core.persistence.PathArchiveRepository;

/**
 * Unit tests for {@link PathArchiveRepository}
 *
 * @author James Kojo
 */
@Test
public class PathArchiveRepositoryTest extends ArchiveRepositoryTest {
    private Path rootArchiveDirectory;

    @Override
    @BeforeClass
    public void setup() throws Exception {
        rootArchiveDirectory = Files.createTempDirectory(PathRepositoryPollerTest.class.getSimpleName()+"_");
        FileUtils.forceDeleteOnExit(rootArchiveDirectory.toFile());
        super.setup();
    }

    @Override
    public ArchiveRepository createRepository() {
        return new PathArchiveRepository.Builder(rootArchiveDirectory).build();
    }
}
