# SonarQube Fortify plugin configuration
The plug-in provides various configuration settings, both global and project-specific, that can be configured 
through the SonarQube web interface. These settings include SSC or FoD URL and credentials, as well as some 
more technical settings. At project level you can define the corresponding SSC application version and SSC 
filter set, or FoD application release from which to import vulnerabilities and metrics into SonarQube. Please 
see the description for each setting in the SonarQube web interface for more information.

Note that all settings can also be specified or overridden on the command line when performing a SonarQube
scan. The setting names can be found in the various SonarQube settings pages. For instructions on how to 
specify these settings during a SonarQube scan, please see the SonarQube documentation. For example, when 
running a Maven-based SonarQube scan, these settings can be provided on the Maven command line using the
`-D[setting name]=[value]` command line options. 
