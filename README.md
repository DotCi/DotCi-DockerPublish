# DotCi-DockerPublish

Publish a docker image via **DotCI** when using the **docker-compose** build type:

  * [Usage](#usage)
    * [The 'latest' label](#the-latest-label)
  * [Configuration](#configuration)
  * [Logging](#logging)
  * [Known Issues](#known-issues)
  * [TODO](#todo)

## Usage

Consider the below `.ci.yml`:

    docker-compose-file: "./docker-compose.yml"

    run:
      mycoolapp: some-cmd-here

    <% if( DOTCI_TAG || DOTCI_BRANCH =~ /publish.*/ ) { %>
    plugins:
      - publish: mycoolapp
    <% } %>

And this `./docker-compose.yml`:

    mycoolapp:
      build: .
      environment:
        RAILS_ENV: production

In the above example, we're publishing our image labeled with a semantic verstion that is derived from a git tag (e.g. `1.34.42`) - or the label will be the commit SHA when the branch name is `publish.*` (e.g. `publish.2015.12.23_16.02.00`)

In any case, an additional label will be published - `latest`. **This will happen any time you trigger this plugin**.

Please note that the above is a best practice example, and although you can very well (attempt to) publish on every build, please don't do that :-)

### The 'latest' label

This plugin operates by force pushing the **latest** label each time it's ran, which will overwrite it.

## Configuration

You may configure the Docker Registry hostname via `/configure` in Jenkins - look for the section labeled **Docker Publish Configuration**.

**TODO: talk about other config options here.**

## Logging

The plugin writes entries into the build master's catalina logs:

    Dec 23, 2015 4:20:20 PM com.groupon.jenkins.DockerComposeDotCi.DockerPublishPlugin getPublishCommands
    INFO: Building 'repofoobarbaz34'
    Dec 23, 2015 4:20:20 PM com.groupon.jenkins.DockerComposeDotCi.DockerPublishPlugin getPublishCommands
    INFO: Publishing image with SHA: your.registry.tld/repo/foo_bar_baz:42840717732e72f8f8fbf8caa5982f0d9b47e372
    Dec 23, 2015 4:20:20 PM com.groupon.jenkins.DockerComposeDotCi.DockerPublishPlugin getPublishCommands
    INFO: Publishing image with 'latest' label: your.registry.tld/repo/foo_bar_baz

These occur each time the plugin is invoked.

## Known Issues

  * None

## TODO

  * Toggle-able force pushing of '`latest`' (?)
    * Make it an option that can be set in `.ci.yml`
  * Provide a way to publish multiple images to multiple registries