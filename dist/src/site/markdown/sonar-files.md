# SonarQube files to be scanned
By default, SonarQube only scans file types that are supported by the installed SonarQube language plugins. For example,
if you have the SonarQube Java language plugin installed, SonarQube will scan files with the .java file extension.
However, the Java language plugin for your SonarQube version may not support JSP files, so JSP files will by default not 
be scanned by SonarQube.

Depending on the SonarQube version that you are running, the Fortify plugin exhibits different behaviours:

* For SonarQube versions earlier than 7.6, any vulnerabilities for which no corresponding source file
can be found in the SonarQube scan will be ignored. For example, if SonarQube is not configured to scan
.xml or .jsp files, any Fortify vulnerabilities on .xml or .jsp files will not be reported in SonarQube.
The same is true for any vulnerabilities for which no source file is available, like Dynamic Application
Security Testing (DAST) results.
* For SonarQube versions 7.6 and up, any vulnerabilities for which no corresponding source file can be
found in the SonarQube scan will be reported as SonarQube project-level issues. This includes any vulnerabilities
for which no source file is available, like DAST results.

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
