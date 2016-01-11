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
import java.util.logging.Logger;
import java.util.Map;

@Extension
public class DockerPublishPlugin extends DotCiPluginAdapter {
    private static final Logger LOGGER = Logger.getLogger(DockerPublishPlugin.class.getName());
    DockerPublishConfiguration configuration = DockerPublishConfiguration.get();

    public DockerPublishPlugin() {
        super("publish", "");
    }

    @Override
    public boolean perform(DynamicBuild build, Launcher launcher, BuildListener listener) {
        if (build.getResult().equals(Result.SUCCESS)) {

            try {
                ShellCommands publishCommands = getPublishCommands(build, listener);
                Shell execution = new Shell("#!/bin/bash -l \n set -e \n" + publishCommands.toShellScript());
                LOGGER.info(String.format("Execution string will be: %s: ", execution));
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

    public ShellCommands getPublishCommands(DynamicBuild build, BuildListener listener) throws IOException, InterruptedException {
        Map<String, Object> buildEnvironment = build.getEnvironmentWithChangeSet(listener);
        String dotCiBranch = buildEnvironment.get("DOTCI_BRANCH").toString();
        String dotCiTag = null;
        if (dotCiBranch.contains("tags/"))
            dotCiTag = buildEnvironment.get("DOTCI_TAG").toString();
        String repoName = build.getParent().getFullName();
        String sha = build.getSha();
        int buildNumber = build.getNumber();
        boolean forcePushLatest = configuration.isForcePushLatest();
        String registryHost = configuration.getRegistryHost();
        String dockerRegistryImageName = registryHost + repoName.toLowerCase().replaceAll("-", "_");
        String publishProjectName = repoName.replaceAll("/", "").replaceAll("\\.", "").replaceAll("-", "").replaceAll("_", "").toLowerCase() + buildNumber;
        ShellCommands publishCommands = new ShellCommands();

        // Build the image
        LOGGER.info(String.format("Building '%s'", publishProjectName));
        publishCommands.add(String.format("docker-compose -p %s pull", publishProjectName));
        publishCommands.add(String.format("docker-compose -p %s build", publishProjectName));

        if (dotCiTag != null) {
            // Publish with label as tag
            LOGGER.info(String.format("Publishing tagged image: %s:%s", dockerRegistryImageName, dotCiTag));
            publishCommands.add(String.format("docker tag %s_%s %s:%s", publishProjectName, getComposeImageToPublish(), dockerRegistryImageName, dotCiTag));
            publishCommands.add(String.format("docker push %s:%s", dockerRegistryImageName, dotCiTag));
        } else {
            // Publish with label as sha
            LOGGER.info(String.format("Publishing image with SHA: %s:%s", dockerRegistryImageName, sha));
            publishCommands.add(String.format("docker tag %s_%s %s:%s", publishProjectName, getComposeImageToPublish(), dockerRegistryImageName, sha));
            publishCommands.add(String.format("docker push %s:%s",dockerRegistryImageName, sha));
        }

        if (forcePushLatest)
        // Publish as 'latest'
            LOGGER.info(String.format("Publishing image with 'latest' label: %s", dockerRegistryImageName));
            publishCommands.add(String.format("docker tag -f %s_%s %s:latest", publishProjectName, getComposeImageToPublish(), dockerRegistryImageName));
            publishCommands.add(String.format("docker push %s:latest", dockerRegistryImageName));

        return publishCommands;
    }

    private Object getComposeImageToPublish() {
        return this.options;
    }
}