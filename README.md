# DotCi-DockerPublish

Publish a docker image via **DotCI** when using the **docker-compose** build type:

* [Usage](#usage)
* [Configuration](#configuration)
  * [Docker Registry Host](#docker-registry-host)
  * [Force Push Latest](#force-push-latest)
* [Logging](#logging)
* [Known Issues](#known-issues)
* [TODO](#todo)

## Usage

Consider the below `./docker-compose.yml`:

    mycoolapp:
      build: .
      environment:
        RAILS_ENV: production

With this plugin, publishing one or more Docker images to one or more Docker registries can happen automatically after a successful build (or other condition.)

Here's a sample `.ci.yml` where we publish our image to multiple registries:

	run:
	  app: some-command-here
	
	<% if( DOTCI_TAG || DOTCI_BRANCH =~ /publish.*/ ) { %>
	plugins:
	  - publish:
	    mycoolapp:
	      - "registry-one.domain.tld"
	      - "registry-two.domain.tld"
	<% } %>

You can also publish multiple images in a single job. If your `docker-compose.yml` looks like this:

	mycoolapp:
	  build: .
	  environment:
	    RAILS_ENV: production

	otherimg:
	  build: .

Then configure your `.ci.yml` like this:

	run:
	  app: some-command-here
	
	<% if( DOTCI_TAG || DOTCI_BRANCH =~ /publish.*/ ) { %>
	plugins:
	  - publish:
	    mycoolapp:
	      - "registry-one.domain.tld"
	      - "registry-two.domain.tld"
	    otherimg:
	      - "registry-three.domain.tld"
	      - "registry-four.domain.tld"
	<% } %>

And you will have published both images to your configured registries. Great Job!

## Configuration

You may configure the Docker Registry hostname via `/configure` in Jenkins - look for the section labeled **Docker Publish Configuration**.

### Docker Registry Host

This defaults to the public Docker registry (`hub.docker.com`) and can be overwritten in your `.ci.yml`.

### Force Push Latest

This defaults to ***on***, and will force push the `latest` label upon publish.

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

  * Toggle force pushing of `latest` via `.ci.yml`
