# Usage (SSC)
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
