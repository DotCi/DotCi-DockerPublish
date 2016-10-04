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

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class DockerPublishConfiguration extends GlobalConfiguration {
    private boolean forcePushLatest = true;
    // We need the default to have a trailing slash since it
    // won't get added until setRegistryHost() is called.
    private String registryHost = "hub.docker.com/";

    public DockerPublishConfiguration() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest request, JSONObject json) throws FormException {
        request.bindJSON(this, json);
        save();
        return true;
    }

    public String getRegistryHost() {
        return registryHost;
    }

    public void setRegistryHost(String registryHost) {
        // We need to ensure that the hostname ends with a trailing slash because
        // we expect it later, but we only want to add one if it isn't there.
        if (!registryHost.endsWith("/"))
            registryHost = registryHost + "/";
        this.registryHost = registryHost;
    }

    public static DockerPublishConfiguration get() {
        return GlobalConfiguration.all().get(DockerPublishConfiguration.class);
    }
}