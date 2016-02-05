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

import com.groupon.jenkins.buildtype.util.shell.ShellCommands;

import java.util.logging.Logger;

public class DockerPublishUtils {

    public DockerPublishUtils() {
        super();
    }

    private ShellCommands buildTagAndPublishCommands(ShellCommands cmds, String composeBuild, String composeImage,
                                                     String hostOrgRepo, String label, String msg, Logger LOGGER) {
        LOGGER.info(msg);
        if (label.equals("latest")) {
            cmds.add(String.format("docker tag -f %s_%s %s:%s", composeBuild, composeImage, hostOrgRepo, label));
        } else {
            cmds.add(String.format("docker tag %s_%s %s:%s", composeBuild, composeImage, hostOrgRepo, label));
        }
        cmds.add(String.format("docker push %s:%s", hostOrgRepo, label));
        return cmds;
    }

    public ShellCommands publishImages(ShellCommands cmds, String composeBuild, String composeImage, String ciTag,
                                        String sha, String hostOrgRepo, boolean forcePushLatest, Logger LOGGER) {

        if (ciTag != null) {
            // Publish with label as tag
            buildTagAndPublishCommands(cmds, composeBuild, composeImage, hostOrgRepo, ciTag,
                    String.format("Publishing tagged image '%s': %s:%s", composeImage, hostOrgRepo, ciTag), LOGGER);
        } else {
            // Publish with label as sha
            buildTagAndPublishCommands(cmds, composeBuild, composeImage, hostOrgRepo, sha,
                    String.format("Publishing image '%s' with SHA: %s:%s", composeImage, hostOrgRepo, sha), LOGGER);
        }

        if (forcePushLatest) {
            // Publish as 'latest'
            buildTagAndPublishCommands(cmds, composeBuild, composeImage, hostOrgRepo, "latest",
                    String.format("Publishing image '%s' with 'latest' label: %s", composeImage, hostOrgRepo), LOGGER);
        } else {
            LOGGER.info("Force pushing 'latest' is disabled, skipping ...");
        }
        return cmds;
    }
}
