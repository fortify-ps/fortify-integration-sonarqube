# Fortify plug-in for SonarQube
This SonarQube plug-in allows for importing Fortify scan results into SonarQube. This includes the following features:
* Load vulnerability data from Fortify SSC and display each vulnerability as a SonarQube issue
* Load various metrics and other meta-data from Fortify SSC, like issue counts and artifact status. 
    * These metrics can be used to define Quality Gates.

# Compatibility
The plugin has been tested with SonarQube 6.7.7 and SonarQube 7.6:

* The plugin will most likely work with other 6.7.x versions
* The plugin will most likely work with any SonarQube version in-between 6.7.7 and 7.6
* The plugin may work with versions later than 7.6
* The plugin is not compatible with versions earlier than 6.7

Note that the plugin will use 7.6-specific SonarQube API features when running on 
SonarQube 7.6 or later. As such, results may be slightly different depending on
which version of SonarQube you are running. 

## Configuring and installing the plugin
Although the plugin can be installed on SonarQube as-is, the plugin distribution provides
a configuration utility that allows for more advanced configurations:

* With the default configuration, all Fortify issues will be mapped onto a single SonarQube rule.
  The configuration utility allows for downloading externalmetadata.xml from SSC, and use one
  of the external groupings defined in this file to define SonarQube rules. For example,
  this allows for selecting 'OWASP Top 10 2017' as the rules source, thereby mapping Fortify
  issues to SonarQube rules A1 - A10 and Other.
* The default configuration provides various standard metrics to be collected from SSC.
  Using the configuration utility, you can add new metrics, or remove predefined metrics.

As such, it is recommended to run the configuration utility before installing the plugin. The
configuration utility will update the plugin jar file based on the selected configuration settings,
after which you can deploy the plugin jar to SonarQube.

The configuration utility can be run using the following command:

`java -jar fortify-ssc-sonarqube-plugin-configure-[version].jar`

It will then ask for the location of the plugin jar, after which you can navigate 
through the various tabs to configure the plugin.

The plugin can then be installed by copying the (configured) fortify-sonarqube-plugin-[version].jar file 
to [SonarQube install dir]\extensions\plugins. If you have used earlier versions of the plugin, please 
make sure that the SonarQube plugins directory only contains one version of the plugin.

## Building the plugin from source
The plugin distribution can be built using Maven:
* Download or clone the source code
* Navigate to the fortify-integration-sonarqube-ssc directory
* Build the plug-in using `mvn clean package`
* Once the build has completed, the full binary distribution will be located in the 
  fortify-integration-sonarqube-ssc\dist\target directory

## Preparing Fortify SSC
The plugin loads all Fortify-related data from Fortify SSC. In order to allow the plug-in to access SSC, you will need to define
a SonarQube specific token in the SSC configuration, and then generate an authentication token for use by the plugin.

* Add the following token definition to [SSC deployment directory]\WEB-INF\internal\serviceContext.xml.
  Note: This has not been updated yet for the new plugin version; you will either need to figure out
  the correct token definition yourself, or for now use username/password authentication.

```xml
	<bean id="sonarQubeToken" class="com.fortify.manager.security.ws.AuthenticationTokenSpec">
		<property name="key" value="SonarQubeToken"/>
		<property name="maxDaysToLive" value="90" />
		<property name="actionPermitted">
			<list value-type="java.lang.String">
				<value>GET=/api/v\d+/artifacts/\d+</value>
				<value>GET=/api/v\d+/jobs</value>
				<value>GET=/api/v\d+/projectVersions</value>
				<value>GET=/api/v\d+/projectVersions/\d+/artifacts</value>
				<value>GET=/api/v\d+/projectVersions/\d+/filterSets</value>
				<value>GET=/api/v\d+/projectVersions/\d+/issues</value>
				<value>GET=/api/v\d+/projectVersions/\d+/performanceIndicatorHistories</value>
				<value>GET=/api/v\d+/projectVersions/\d+/variableHistories</value>
				<value>PUT=/api/v\d+/projectVersions/\d+/issueSearchOptions</value>
				<value>POST=/api/v\d+/fileTokens</value>
				<value>POST=/upload/resultFileUpload.html</value>
			</list>
		</property>
		<property name="terminalActions">
			<list value-type="java.lang.String">
				<value>InvalidateTokenRequest</value>
				<value>DELETE=/api/v\d+/auth/token</value>
			</list>
		</property>
	</bean>
```
* Restart the SSC application server
* Generate a new SSC authentication token for use by the Fortify SonarQube plug-in:
    * `fortifyclient token -gettoken SonarQubeToken -user [user] -url [SSC URL]`
    * Enter the user password, and save the returned token for use in subsequent commands


## SonarQube configuration
Contrary to earlier versions of the plugin, all Fortify-related rules are now defined under the
'Fortify' language. The plugin defines a default quality profile with all Fortify-related rules 
enabled, so you no longer need to manually enable Fortify-related rules for the various 
SonarQube-supported languages.

In order to allow the plugin to function properly:
* You need to make sure that the SonarQube scan includes all file types for which Fortify vulnerabilities exist
* You need to configure the Fortify plugin, for example the SSC URL and authentication token
  
Failure to do so will result in the Fortify SonarQube plugin not functioning properly, for example not reporting
any Fortify vulnerabilities as SonarQube issues.

### Include all relevant file types
By default, SonarQube only scans file types that are supported by the installed SonarQube language plugins. For example,
if you have the SonarQube Java language plugin installed, SonarQube will scan files with the .java file extension.
However, the Java language plugin for example doesn't support JSP files, so JSP files will not be scanned by 
SonarQube unless you have another SonarQube plugin installed that explicitly supports JSP files.

For SonarQube versions earlier than 7.6, any vulnerabilities for which no corresponding source file
can be found in the SonarQube scan will be ignored. For example, if SonarQube is not configured to scan
.xml files, any Fortify vulnerabilities on .xml files will not be reported in SonarQube. 

For SonarQube versions 7.6 and up, any vulnerabilities for which no corresponding source file can be
found in the SonarQube scan will be reported as project-level issues.

Multiple options exist for including additional file types in the SonarQube scan, such that Fortify vulnerabilities
can be reported on the correct source files:
* Install SonarQube language plugins that supports these file types
* Configure additional file extensions to be included in the SonarQube scan through the Fortify plugin:
    * Navigate to Administration->General Settings->Fortify, and add additional file suffixes 
      in the `Additional file suffixes` field
    * Or set the `sonar.fortify.filesuffixes` property, containing a comma-separated list of
      file extensions, when running the SonarQube scan
* Instruct SonarQube to include unknown file types during the scan:
     * Navigate to Administration->General Settings->Analysis Scope->Files, and enable the
       `Import unknown files` option
     * Or set the `sonar.import_unknown_files` property to true when running the SonarQube scan 

### Fortify plugin configuration
The plug-in provides various configuration settings, both global and project-specific, that can be configured 
through the SonarQube web interface. These settings include SSC URL and credentials, as well as some 
more technical settings. At project level you can define the corresponding SSC application version and SSC 
filter set to use to load issues from SSC. Please see the description for each setting in the SonarQube web 
interface for more information.

Note that all settings can also be specified or overridden on the command line when performing a SonarQube
scan, as we will see in the following section. 

## Running a SonarQube scan with the Fortify plug-in
Contrary to most other SonarQube plug-ins, the Fortify plug-in for SonarQube doesn't actually scan any source 
code. Instead, vulnerability data is loaded from Fortify SSC. As such, you will need to separately run a 
Fortify SCA scan and upload the scan results to Fortify SSC.

So in general the steps to run a SonarQube scan with the Fortify plug-in are as follows:
* Run SCA translation
* Run SCA scan
* Invoke SonarQube scan with the Fortify plug-in

As an example of a Maven-based SCA and SonarQube scan:
* Navigate to [SCA Install]\plugins\maven
* Unzip either the binary or source Maven plugin, and build/deploy the plugin to your local Maven repository
* Navigate to the samples\EightBall directory inside the Maven plugin directory
* Run a Maven-based scan for the EightBall example (see README.txt)
* Create application EightBall and version 1.0 in SSC
* Run the following command to perform SonarQube analysis, upload the FPR file to SSC, wait for SSC to process
  the artifact, and then import vulnerabilities and metrics from SSC:
    * `mvn -Dsonar.fortify.ssc.url=http[s]://[credentials]@[host][:port]/ssc -Dsonar.fortify.ssc.appversion=EightBall:1.0 -Dsonar.fortify.ssc.uploadFPR=target\fortify\EightBall-1.0.fpr -Dsonar.fortify.ssc.failOnArtifactStates=SCHED_PROCESSING,PROCESSING,REQUIRE_AUTH,ERROR_PROCESSING -Dsonar.fortify.ssc.processing.timeout=120 sonar:sonar`
        * [credentials] can be either 'authToken:[authtoken]' or '[username]:[password]'
    * Apart from regular SonarQube processing, this will invoke the Fortify SonarQube plug-in to retrieve vulnerability data and metrics from Fortify SSC. 
    * The plug-in will start with uploading the FPR file to SSC (`-Dsonar.fortify.ssc.uploadFPR=target\fortify\EightBall-1.0.fpr`)
    * The plug-in will wait for at most 120 seconds (`-Dsonar.fortify.ssc.processing.timeout` setting) for SSC to process the uploaded SCA scan results. 
    * If the uploaded scan results have not been processed within that time-frame, if the upload requires approval, or if there was an error processing the uploaded artifact, the scan will fail (`-Dsonar.fortify.ssc.failOnArtifactStates` setting).
    * For a full list of available settings, the SonarQube settings pages.
    
## Some notes

### SonarQube Fortify Rules
TODO Update this section

For every SonarQube language, the Fortify SonarQube plug-in defines a single 'Fortify' rule. 
There are multiple reasons for having a single Fortify rule, instead of having individual 
SonarQube rules for every Fortify vulnerability category:
* SonarQube requires the full set of available rules to be available at plug-in load time. For Fortify this approach is not viable:
    * Fortify doesn't provide a public API for retrieving a list of all available vulnerability categories.
    * Every individual Fortify scan may use a different set of (standard or custom) Fortify rules, so possibly there is no single source for all possible vulnerability categories.
* Disabling individual Fortify vulnerability categories in SonarQube may result in inconsistencies between vulnerabilities shown in SonarQube , and vulnerabilities shown in Fortify SSC or reports generated by Fortify SSC, possibly causing confusion.
* Disabling individual Fortify vulnerability categories in SonarQube may result in inconsistencies between the number of individual vulnerabilities shown in SonarQube, and the various metrics shown on the Fortify custom dashboard in SonarQube (since these metrics are calculated by SSC), again possibly causing confusion.


### Fortify SSC artifact processing
When uploading new SCA scan results to Fortify SSC, these scan artifacts need to be processed by SSC. Depending on various
factors, it may take some time for an uploaded artifact to be processed by SSC. In some cases, processing may fail, 
or the uploaded artifact may need to be approved before the results will be processed by SSC. Until an uploaded
artifact has been successfully processed, the newly uploaded scan results will not be available in SSC. 

The Fortify SonarQube plug-in implements various features for handling artifact state:
* The plug-in will wait for a configured maximum amount of seconds if there is any artifact in the (scheduled for) processing state.
* If the SCA artifact has been processed successfully, results will be loaded from SSC and fed to SonarQube.
* If the time-out has expired while SSC is still processing the results, if the artifact requires approval, or if there was an error during processing, there are two options depending on the `sonar.fortify.ssc.failOnArtifactStates` setting:
    * If the artifact status matches one of the states configured through this setting, the plug-in will throw an exception and cause SonarQube processing to be aborted. No Fortify results will be fed to SonarQube in this case.
    * If the artifact status doesn't match any of the states configured through this setting (or if no fail states have been configured), processing will continue as usual. Note however that the information fed to SonarQube may be out of date in this case.
        * In this case you can optionally define quality gates based on the 'Artifact Status' metric. For example you can define a quality gate condition 'Artifact Status is not PROCESS_COMPLETE'.
    

   
