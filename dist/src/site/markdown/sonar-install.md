# Configuring and installing the plugin
Although the plugin can be installed on SonarQube as-is, the plugin distribution provides
a configuration utility that allows for more advanced configurations:

* With the default configuration, all Fortify issues will be mapped onto a single SonarQube rule.
  The configuration utility allows for downloading externalmetadata.xml from SSC, and use one
  of the external groupings defined in this file to define SonarQube rules. For example,
  this allows for selecting 'OWASP Top 10 2017' as the rules source, thereby mapping Fortify
  issues to SonarQube rules A1 - A10 and Other.
    * Note that at the moment, FoD doesn't provide any functionality for retrieving
      externalmetadata.xml. However, if you have an SSC instance, you can retrieve
      this file from SSC, and the resulting mappings will be used for FoD as well.
* The default configuration provides various standard metrics to be collected from SSC or FoD.
  Using the configuration utility, you can add new metrics, or remove predefined metrics.

As such, it is recommended to run the configuration utility before installing the plugin. The
configuration utility will update the plugin jar file based on the selected configuration settings,
after which you can deploy the plugin jar to SonarQube.

The configuration utility can be run using the following command:

`java -jar fortify-sonarqube-plugin-configure-[version].jar`

It will then ask for the location of the plugin jar, after which you can navigate 
through the various tabs to configure the plugin.

The plugin can then be installed by copying the (configured) fortify-sonarqube-plugin-[version].jar file 
to [SonarQube install dir]\extensions\plugins. If you have used earlier versions of the plugin, please 
make sure that the SonarQube plugins directory only contains one version of the plugin.
    

