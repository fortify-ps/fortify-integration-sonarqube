# Fortify plug-in for SonarQube

Deprecation Notice
====
As detailed in https://github.com/fortify-ps/fortify-integration-sonarqube/issues/11, this plugin is not compatible with SonarQube 8.0 and up. In order to support recent SonarQube versions, a full rewrite of the plugin would be required. Given the amount of effort required to keep the plugin up to date with frequent SonarQube API changes, and with customers moving to SonarCloud (which does not support 3rd-party plugins), we no longer consider it feasible to maintain this plugin. 

Existing customers may continue using this plugin if they don't plan on upgrading to SonarQube 8.0 or higher in the near future. Although Fortify Professional Services could assist customers with developing an updated plugin that does support SonarQube 8.x, it is likely that a future SonarQube version again breaks the plugin.

As such, it is strongly recommended to consider alternative options. For example, a more lightweight integration could be considered based on [SonarQube Generic Issue Import Format](https://docs.sonarqube.org/latest/analysis/generic-issue/). Such a solution would also work for SonarCloud and would likely require much less maintenance effort, at the cost of reduced functionality due to SonarQube limitations (see for example https://community.sonarsource.com/t/generic-issue-data-ad-hoc-rules/9624). 

Fortify Professional Services can assist you with implementing such alternative solutions; please contact your Fortify sales representative to discuss the options.

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

