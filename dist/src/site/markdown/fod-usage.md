# Usage (FoD)
Contrary to most other SonarQube plug-ins, the Fortify plug-in for SonarQube doesn't actually scan any source 
code. Instead, vulnerability data is loaded from Fortify on Demand. As such, you will need to separately run a 
Fortify on Demand scan before running the SonarQube Fortify plugin.

So in general the steps to run a SonarQube scan with the Fortify plug-in are as follows:

* Upload source code to be scanned to Fortify on Demand
* Wait for Fortify on Demand results to be available
* Invoke SonarQube scan with the Fortify plug-in

Following is an example SonarQube Maven-based scan command that loads vulnerability data from 
Fortify on Demand:

`mvn -X '-Dsonar.fortify.fod.url=https://[region].fortify.com/' -Dsonar.fortify.fod.tenant=[FoD tentant] -Dsonar.fortify.fod.user=[FoD user] '-Dsonar.fortify.fod.password=[FoD password]'  "-Dsonar.fortify.fod.release=[FoD release id or application:release name]" sonar:sonar`
