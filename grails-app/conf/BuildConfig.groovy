grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// specify dependency exclusions here; for example, uncomment this to disable ehcache:
		// excludes 'ehcache'
	}
	repositories {
		inherits true // Whether to inherit repository definitions from plugins

		grailsPlugins()
		grailsHome()
		mavenLocal()
		grailsCentral()
		mavenCentral()
	}

	plugins {
		// plugins for the build system only
		build ":tomcat:7.0.55"
		
		// plugins needed at runtime but not for compilation
		runtime ":hibernate4:4.3.6.1" // or ":hibernate:3.6.10.18"
	}
}
