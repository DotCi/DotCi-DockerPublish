# DotCi-DockerPublish

Publish a docker image via **DotCI** when using the **docker-compose** build type:

* [Usage](#usage)
* [Configuration](#configuration)
  * [Build Tags](#build-tags)
  * [Docker Registry Host](#docker-registry-host)
* [Logging](#logging)

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

### Build Tags

In order to push images named after git tags, you must first enable **Build Tags** under your job's configuration. This will trigger a build when you push a git tag, as well as a docker image push that is labeled with the git tag.

### Docker Registry Host

You may configure the Docker Registry hostname via `/configure` in Jenkins - look for the section labeled **Docker Publish Configuration**.

This defaults to the public Docker registry (`hub.docker.com`) and can be overwritten in your `.ci.yml`.

## Logging

The plugin writes entries into the build master's catalina logs:

    Dec 23, 2015 4:20:20 PM com.groupon.jenkins.DockerComposeDotCi.DockerPublishPlugin getPublishCommands
    INFO: Building 'repofoobarbaz34'
    Dec 23, 2015 4:20:20 PM com.groupon.jenkins.DockerComposeDotCi.DockerPublishPlugin getPublishCommands
    INFO: Publishing image with SHA: your.registry.tld/repo/foo_bar_baz:42840717732e72f8f8fbf8caa5982f0d9b47e372
    Dec 23, 2015 4:20:20 PM com.groupon.jenkins.DockerComposeDotCi.DockerPublishPlugin getPublishCommands
    INFO: Publishing image with 'latest' label: your.registry.tld/repo/foo_bar_baz

These occur each time the plugin is invoked.
