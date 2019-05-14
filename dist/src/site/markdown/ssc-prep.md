# Preparing for use with Fortify SSC
The plugin loads all Fortify-related data from Fortify SSC. In order to allow the plug-in to access SSC, you will need to provide
the SSC URL and credentials in the SonarQube configuration or as plugin properties when running a SonarQube scan. To authenticate 
with SSC, you can either provide SSC user name and password credentials, or an SSC authentication token. 

For user-based authentication, no preparations are necessary on the SSC side, other than making sure that the given user has 
sufficient permissions to view vulnerabilities on the appropriate application versions.

For token-based authentication, you will need to define a SonarQube specific token in the SSC configuration, and then generate 
an authentication token for use by the plugin.

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
