# Fortify SSC artifact processing
The Fortify SonarQube plugin allows for uploading an FPR file to SSC before having the plugin process the scan results.
When uploading new scan results to Fortify SSC, these scan artifacts first need to be processed by SSC. Depending on 
various factors, it may take some time for an uploaded artifact to be processed by SSC, and in some cases processing 
may fail or the uploaded artifact may need to be approved. Until an uploaded artifact has been successfully processed, 
the newly uploaded scan results will not be available in SSC. 

The Fortify SonarQube plug-in implements various features for handling artifact state:

* The plug-in will wait for a configured maximum amount of seconds if there is any artifact in the (scheduled for) processing state.
* If the SCA artifact has been processed successfully, results will be loaded from SSC and fed to SonarQube.
* If the time-out has expired while SSC is still processing the results, if the artifact requires approval, or if there was an error during processing, there are two options depending on the `sonar.fortify.ssc.failOnArtifactStates` setting:
    * If the artifact status matches one of the states configured through this setting, the plug-in will throw an exception and cause SonarQube processing to be aborted. No Fortify results will be loaded into SonarQube in this case.
    * If the artifact status doesn't match any of the states configured through this setting (or if no fail states have been configured), processing will continue as usual. Note however that the information loaded into SonarQube may be out of date in this case.
        * In this case you can optionally define quality gates based on the 'Artifact Status' metric. For example you can define a quality gate condition 'Artifact Status is not PROCESS_COMPLETE'.
    

