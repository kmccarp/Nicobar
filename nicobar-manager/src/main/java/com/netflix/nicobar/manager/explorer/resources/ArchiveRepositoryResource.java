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
package com.netflix.nicobar.manager.explorer.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.netflix.nicobar.core.archive.JarScriptArchive;
import com.netflix.nicobar.core.archive.ModuleId;
import com.netflix.nicobar.core.archive.ScriptModuleSpec;
import com.netflix.nicobar.core.persistence.ArchiveRepository;
import com.netflix.nicobar.core.persistence.ArchiveSummary;
import com.netflix.nicobar.core.persistence.RepositorySummary;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.multipart.FormDataParam;

/**
 * REST sub-resource for a single {@link ArchiveRepository}
 *
 * @author James Kojo
 * @author Vasanth Asokan
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArchiveRepositoryResource {
    private final ArchiveRepository repository;

    /**
     * @param repository
     */
    public ArchiveRepositoryResource(ArchiveRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable showView() {
        Map<String, Object> model = new HashMap<>();
        model.put("repositoryId", repository.getRepositoryId());
        return new Viewable( "/scriptmanager/repository_view.ftl", model);
    }

    /**
     * @return a summary for this repository
     */
    @GET
    @Path("summary")
    public RepositorySummary getRepositorySummary() {
        RepositorySummary repositorySummary;
        try {
            repositorySummary = repository.getDefaultView().getRepositorySummary();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
        return repositorySummary;
    }
    /**
     * @return summaries for the archives in the repo
     */
    @GET
    @Path("archivesummaries")
    public List<ArchiveSummary> getSummaries() {
        try {
            return repository.getDefaultView().getArchiveSummaries();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void insertArchive(
            @FormDataParam("moduleSpec") ScriptModuleSpec moduleSpec,
            @FormDataParam("archiveJar") InputStream file) {
        validateModuleSpec(moduleSpec);
        String moduleId = moduleSpec.getModuleId().toString();
        try {
            java.nio.file.Path tempFile = Files.createTempFile(moduleId, ".jar");
            Files.copy(file, tempFile, StandardCopyOption.REPLACE_EXISTING);
            JarScriptArchive jarScriptArchive = new JarScriptArchive.Builder(tempFile)
                .setModuleSpec(moduleSpec)
                .build();
            repository.insertArchive(jarScriptArchive);
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("{moduleId}")
    public void deleteArchive(@PathParam("moduleId") String moduleId) {
        try {
            repository.deleteArchive(ModuleId.fromString(moduleId));
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    public void validateModuleSpec(ScriptModuleSpec moduleSpec) {
        Set<String> missing = new HashSet<>(1);
        if (moduleSpec == null) {
            missing.add("moduleSpec");
        } else {
            if (moduleSpec.getCompilerPluginIds() == null) {
                missing.add("compilerPluginIds");
            }
            if (moduleSpec.getMetadata() == null) {
                missing.add("metadata");
            }
            if (moduleSpec.getModuleDependencies() == null) {
                missing.add("moduleDependencies");
            }
            if (moduleSpec.getModuleId() == null) {
                missing.add("moduleId");
            }
        }
        if (!missing.isEmpty()) {
            throw new WebApplicationException(
                Responses
                    .clientError()
                    .entity(Collections.singletonMap("missing", missing))
                    .build());
        }
    }
}
