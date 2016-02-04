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

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

public class DockerPublishPluginTest {

    Logger LOGGER = Logger.getLogger(DockerPublishPlugin.class.getName());
    ShellCommands cmds = new ShellCommands();
    String composeBuildName = "htriantafilloumycoolapp42";  // GitHub org + repo + build number
    String composeImage = "app";
    String dotCiTag = "0.34.42";
    String sha = "66630f67158d025cea4be1b2c8a80205e1136c87";  // echo meow | sha1sum
    String dockerRepoName = "docker.domain.tld/htriantafillou/my_cool_app";
    DockerPublishUtils utils = new DockerPublishUtils();

    String expected_tag_cmd_with_sha = String.format("docker tag %s_%s %s:%s",
            composeBuildName, composeImage, dockerRepoName, sha);
    String expected_push_cmd_with_sha = String.format("docker push %s:%s", dockerRepoName, sha);
    String expected_tag_cmd_with_tag = String.format("docker tag %s_%s %s:%s",
            composeBuildName, composeImage, dockerRepoName, dotCiTag);
    String expected_push_cmd_with_tag = String.format("docker push %s:%s", dockerRepoName, dotCiTag);
    String expected_tag_cmd_with_latest = String.format("docker tag %s_%s %s:%s",
            composeBuildName, composeImage, dockerRepoName, "latest");
    String expected_push_cmd_with_latest = String.format("docker push -f %s:%s", dockerRepoName, "latest");
    String expected_first_command_sha = expected_tag_cmd_with_sha;
    String expected_first_command_tag = expected_tag_cmd_with_tag;
    String expected_second_command_sha = expected_push_cmd_with_sha;
    String expected_second_command_tag = expected_push_cmd_with_tag;
    String expected_third_command = expected_tag_cmd_with_latest;
    String expected_fourth_command = expected_push_cmd_with_latest;

    @Test
    public void should_publish_sha_as_label_if_tag_is_null() {
        ShellCommands final_cmds = utils.publishImages(cmds, composeBuildName,
                composeImage, null, sha, dockerRepoName, false, LOGGER);

        String cmd_lines[] = final_cmds.toShellScript().split("\\r?\\n");

        String tag_cmd = cmd_lines[1];
        String push_cmd = cmd_lines[3];

        Assert.assertEquals(tag_cmd, expected_tag_cmd_with_sha);
        Assert.assertEquals(push_cmd, expected_push_cmd_with_sha);
    }

    @Test
    public void should_publish_tag_as_label_if_there_is_one() {
        ShellCommands final_cmds = utils.publishImages(cmds, composeBuildName,
                composeImage, dotCiTag, sha, dockerRepoName, false, LOGGER);

        String cmd_lines[] = final_cmds.toShellScript().split("\\r?\\n");

        String tag_cmd = cmd_lines[1];
        String push_cmd = cmd_lines[3];

        Assert.assertEquals(tag_cmd, expected_tag_cmd_with_tag);
        Assert.assertEquals(push_cmd, expected_push_cmd_with_tag);
    }

    @Test
    public void should_force_push_latest_if_enabled() {
        ShellCommands final_cmds = utils.publishImages(cmds, composeBuildName,
                composeImage, dotCiTag, sha, dockerRepoName, true, LOGGER);

        String cmd_lines[] = final_cmds.toShellScript().split("\\r?\\n");

        String tag_cmd = cmd_lines[5];
        String push_cmd = cmd_lines[7];

        Assert.assertEquals(tag_cmd, expected_tag_cmd_with_latest);
        Assert.assertEquals(push_cmd, expected_push_cmd_with_latest);
    }

    @Test
    public void should_not_force_push_latest_if_disabled() {
        ShellCommands final_cmds = utils.publishImages(cmds, composeBuildName,
                composeImage, dotCiTag, sha, dockerRepoName, false, LOGGER);

        String cmd_lines[] = final_cmds.toShellScript().split("\\r?\\n");

        Assert.assertEquals(cmd_lines.length, 4);  // When pushing 'latest' we have 8 commands
    }

    @Test
    public void should_run_the_correct_commands_for_sha_and_latest_publish() {
        ShellCommands final_cmds = utils.publishImages(cmds, composeBuildName,
                composeImage, null, sha, dockerRepoName, true, LOGGER);

        String cmd_lines[] = final_cmds.toShellScript().split("\\r?\\n");

        String first_cmd = cmd_lines[1];
        String second_cmd = cmd_lines[3];
        String third_cmd = cmd_lines[5];
        String fourth_cmd = cmd_lines[7];

        Assert.assertEquals(first_cmd, expected_first_command_sha);
        Assert.assertEquals(second_cmd, expected_second_command_sha);
        Assert.assertEquals(third_cmd, expected_third_command);
        Assert.assertEquals(fourth_cmd, expected_fourth_command);
    }

    @Test
    public void should_run_the_correct_commands_for_tag_and_no_latest_publish() {
        ShellCommands final_cmds = utils.publishImages(cmds, composeBuildName,
                composeImage, dotCiTag, dotCiTag, dockerRepoName, false, LOGGER);

        String cmd_lines[] = final_cmds.toShellScript().split("\\r?\\n");

        String first_cmd = cmd_lines[1];
        String second_cmd = cmd_lines[3];

        Assert.assertEquals(first_cmd, expected_first_command_tag);
        Assert.assertEquals(second_cmd, expected_second_command_tag);
    }
}
