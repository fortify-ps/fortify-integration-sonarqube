# Compatibility
The plugin has been tested with SonarQube 6.7.7 and SonarQube 7.6:

* The plugin will most likely work with other 6.7.x versions
* The plugin will most likely work with any SonarQube version in-between 6.7.7 and 7.6
* The plugin may work with versions later than 7.6
* The plugin is not compatible with versions earlier than 6.7

Note that the plugin will use 7.6-specific SonarQube API features when running on 
SonarQube 7.6 or later. As such, results may be slightly different depending on
which version of SonarQube you are running. 

Note that some editions of SonarQube 7.1 (and possibly other versions) may be lacking the
commons-logging library that is required by this plugin. As SonarQube does not allow plugins
to bundle this library, you may need to manually add this library to your SonarQube lib/common 
directory. The library can be downloaded from here: 
<https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar>  

The plugin should be compatible with all recent SSC and FoD versions.
