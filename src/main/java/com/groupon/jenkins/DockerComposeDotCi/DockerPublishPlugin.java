/*
The MIT License (MIT)

Copyright (c) 2016, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.groupon.jenkins.DockerComposeDotCi;

import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.dynamic.build.DynamicBuild;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
@Extension
public class DockerPublishPlugin extends DotCiPluginAdapter {
    private static final Logger LOGGER = Logger.getLogger(DockerPublishPlugin.class.getName());
    private DockerPublishConfiguration configuration = DockerPublishConfiguration.get();
    private DockerPublishUtils utils = new DockerPublishUtils();

    public DockerPublishPlugin() {
        super("publish", "");
    }

    @Override
    public boolean perform(DynamicBuild build, Launcher launcher, BuildListener listener) {
        if (build.getResult().equals(Result.SUCCESS)) {

            try {
                ShellCommands publishCommands = getPublishCommands(build, listener);
                Shell execution = new Shell("#!/bin/bash -l \n set -e \n" + publishCommands.toShellScript());
                boolean result;

                try {
                    result = execution.perform(((AbstractBuild) build), launcher, listener);
                } catch (InterruptedException e) {
                    LOGGER.severe(String.format("Got exception: %s", e));
                    throw new RuntimeException(e);
                }
                return result;
            } catch (IOException e) {
                LOGGER.severe(String.format("Got exception: %s", e));
                return false;
            } catch (InterruptedException e) {
                LOGGER.severe(String.format("Got exception: %s", e));
                return false;
            }
        }
        return true;
    }

    private ShellCommands getPublishCommands(DynamicBuild build, BuildListener listener) throws IOException, InterruptedException {

        Map<String, Object> buildEnvironment = build.getEnvironmentWithChangeSet(listener);
        String dotCiBranch = buildEnvironment.get("DOTCI_BRANCH").toString();
        String dotCiTag = null;
        if (dotCiBranch.contains("tags/"))
            dotCiTag = buildEnvironment.get("DOTCI_TAG").toString();
        String orgSlashRepo = build.getParent().getFullName();
        String buildSha = build.getSha();
        int buildNumber = build.getNumber();
        String registryHost = configuration.getRegistryHost();
        String composeBuildName = orgSlashRepo.replaceAll("/", "").replaceAll("\\.", "").replaceAll("-", "")
                .replaceAll("_", "").toLowerCase() + buildNumber;
        ShellCommands publishCommands = new ShellCommands();

        // Read options from .ci.yml and do things with them
        if (this.options instanceof Map) {
            //noinspection unchecked
            for (Map.Entry<String, ArrayList<?>> imageMap : ((Map<String, ArrayList<?>>) this.options).entrySet()) {
                String composeImage = imageMap.getKey();
                ArrayList registryList = imageMap.getValue();

                if (registryList != null) {
                    // Publish image(s) to the configured registryHost(s)
                    LOGGER.info(String.format("Found %d registryHost(s) in .ci.yml ...", registryList.size()));
                    for (Object registryURL : registryList) {
                        registryHost = registryURL.toString();
                        // Ensure we have a trailing slash
                        if (!registryHost.endsWith("/"))
                            registryHost = registryHost + "/";
                        String dockerRepoName = registryHost + orgSlashRepo.toLowerCase().replaceAll("-", "_");
                        // Do the publish!
                        utils.publishImages(publishCommands, composeBuildName, composeImage, dotCiTag,
                                buildSha, dockerRepoName, LOGGER);
                    }
                } else {
                    // Publish image(s) to the default registryHost
                    LOGGER.info("No registryHost found in .ci.yml, we will use the default ...");
                    String dockerRepoName = registryHost + orgSlashRepo.toLowerCase().replaceAll("-", "_");

                    // Do the publish!
                    utils.publishImages(publishCommands, composeBuildName, composeImage, dotCiTag,
                            buildSha, dockerRepoName, LOGGER);
                }
            }
        } else {
            // The plugin was invoked with an invalid syntax!
            String badYamlWarning = "The plugin was invoked but no images or registries were specified! Please review your `.ci.yml` syntax.";
            LOGGER.severe(badYamlWarning);
            throw new InvalidYamlException("ERROR: " + badYamlWarning);
        }
        return publishCommands;
    }
}
