# Fortify plug-in for SonarQube

Deprecation Notice
====
This plugin is not compatible with SonarQube 8.0 and up; see https://github.com/fortify-ps/fortify-integration-sonarqube/issues/11 for details. Given the amount of effort required to keep the plugin up to date with frequent SonarQube API changes, and with customers moving to SonarCloud (which does not support 3rd-party plugins), we no longer consider it feasible to maintain this plugin. 

[Fortify Vulnerability Exporter](https://github.com/fortify/FortifyVulnerabilityExporter) provides an alternative integration by exporting vulnerability data from Fortify on Demand and Fortify Software Security Center (SSC) to a file that can be imported by SonarQube. This lightweight integration is based on the [SonarQube Generic Issue Import Format](https://docs.sonarqube.org/latest/analysis/generic-issue/) that was introduced with SonarQube 7.2 and specifically targeted at importing third-party analysis results. 

The [SonarQube Generic Issue Import Format](https://docs.sonarqube.org/latest/analysis/generic-issue/) does impose some limitations though. The following table illustrates some of these limitations by comparing the plugin-based integration to the import-based integration.

| Plugin-based integration | Import-based integration |
|--------------------------| ------------------------ |
| Load issues from FoD or SSC during a SonarQube scan | FoD or SSC issues must be exported to a file before being imported into SonarQube |
| Try various approaches for matching Fortify-provided source file names and paths to source files being scanned by SonarQube | Requires an exact match between Fortify-provided source file names and paths and source files being scanned by SonarQube |
| Issues that cannot be matched to a source file can be reported at SonarQube project level | Issues that cannot be matched to a source file are silently ignored by SonarQube |
| DAST and other non-SAST issues can be reported at SonarQube project level | DAST and other non-SAST issues are not supported and thus ignored |
| Additional issue details can be provided through a SonarQube ad-hoc rule description | No additional issue details can be imported |

Most of these limitations would require changes on the SonarQube side in order to be resolved, in particular:

* Have SonarQube properly handle third-party issues that cannot be matched to any source file
* Extend the [SonarQube Generic Issue Import Format](https://docs.sonarqube.org/latest/analysis/generic-issue/) to allow additional information like issue details and  recommendations to be reported on third-party issues

Fortify Professional Services may be able to assist you with implementing work-arounds for some of these SonarQube limitations, for example:

* Non-SAST issues could potentially be reported against a specific source file being scanned by SonarQube, for example by adding a dummy `DastIssues.java` file or reporting such issues against an existing `pom.xml`, `build.gradle`, or `package-info.java` file.
* Use a very small, lightweight SonarQube plugin to extend the existing SonarQube functionality with the ability to import third-party SonarQube ad-hoc rules; see https://github.com/fortify-ps-sandbox/sonarqube-scanner-externalissue-rule for a prototype.


Disclaimer
====
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.

Introduction
====
This SonarQube plug-in allows for importing Fortify scan results into SonarQube. This includes the following features:
* Load vulnerability data from Fortify SSC or Fortify on Demand, and display each vulnerability as a SonarQube issue
* Load various metrics and other meta-data from Fortify SSC or FoD, like issue counts and artifact status. 
    * These metrics can be used to define Quality Gates.
    
For more information about configuring and running the plugin, please see the documentation included with the binary distribution.
    
Building from source
====

Prerequisites
----

### Tools
In order to retrieve the source code and build the project, you will need to have the following tools installed:

* Git client
* Maven 3.x

### Fortify Client API dependencies
Development/snapshot versions of this project (i.e. the master branch or other non-release branches) may depend on
a snapshot version of fortify-client-api. You can verify this by searching the root pom.xml file in this project 
for the following dependency declaration:

```xml
<dependency>
	<groupId>com.fortify.client.api</groupId>
	<artifactId>client-api-root</artifactId>
	<version>5.2-SNAPSHOT</version>
	<type>pom</type>
	<scope>import</scope>
</dependency>
```

If, as illustrated in this example, the version of this dependency ends with `-SNAPSHOT`, you will first need to 
build this dependency and install the corresponding artifacts in your local Maven repository by following these steps:

* `git clone https://github.com/fortify-ps/fortify-client-api.git`
* `cd fortify-client-api`
* `mvn clean install`

Notes:

* Non-snapshot versions of fortify-client-api are available at https://github.com/fortify-ps/FortifyMavenRepo,
  which is automatically included during the build of this project. As such, non-snapshot versions of 
  fortify-client-api do not need to be built and installed manually.
* By nature, snapshot versions are unstable and may change at any time. As such, you may need to repeat the
  steps described above if there are any changes in fortify-client-api. Also, there is no guarantee that this 
  project will build without errors with the latest snapshot version of fortify-client-api. In this case, you 
  may need to check out a specific commit of fortify-client-api. 
  
Building the project
----
Once all prerequisites have been met, you can use the following commands to build this project:

* `git clone https://github.com/fortify-ps/fortify-integration-sonarqube.git`
* `cd fortify-integration-sonarqube`
* `git checkout [branch or tag that you want to build]`
* `mvn clean package`

Once completed, build output like plugin JAR file and the binary distribution zip file 
can be found in the dist/target directory.
   

# Licensing

See [LICENSE.TXT](LICENSE.TXT)

