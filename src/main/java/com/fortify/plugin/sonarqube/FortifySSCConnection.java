package com.fortify.plugin.sonarqube;
/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 *****************************************************************************
package com.hpe.security.fortify.sonarqube.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.springframework.util.Assert;

import com.fortify.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.json.JSONUtil;
import com.fortify.util.rest.ProxyConfiguration;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.propertyaccessor.JsonObjectPropertyAccessor;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.sun.jersey.multipart.Boundary;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.binary.BinaryStreamDriver;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class manages the connection to SSC, allowing to retrieve all kinds of data
 * from SSC.
 * 
 * @author Ruud Senden
 *
 
public class FortifySSCConnection {} /* extends SSCAuthenticatingRestConnection {
	private static final Logger LOG = Loggers.get(FortifySSCConnection.class);
	private final String applicationName;
	private final String versionName;
	private final long applicationVersionId;
	
	private final Supplier<JSONObject> applicationVersionSupplier = Suppliers.memoize(new Supplier<JSONObject>() {
		public JSONObject get() { return getApplicationVersion(); };
	});
	private final Supplier<JSONArray> performanceIndicatorHistoriesSupplier = Suppliers.memoize(new Supplier<JSONArray>() {
		public JSONArray get() { return getMetrics("performanceIndicatorHistories"); };
	});
	private final Supplier<JSONArray> variableHistoriesSupplier = Suppliers.memoize(new Supplier<JSONArray>() {
		public JSONArray get() { return getMetrics("variableHistories"); };
	});
	private final Supplier<JSONObject> mostRecentSuccessfulSCAOrNotCompletedArtifact = Suppliers.memoize(new Supplier<JSONObject>() {
		public JSONObject get() { return findMostRecentSuccessfulSCAOrNotCompletedArtifact(); };
	});
	private final Supplier<JSONArray> filterSetsSupplier = Suppliers.memoize(new Supplier<JSONArray>() {
		public JSONArray get() { return getFilterSets(); };
	});
	private final Supplier<Path> sourceBasePathSupplier = Suppliers.memoize(new Supplier<Path>() {
		public Path get() { return getSourceBasePath(); };
	});
	
	static {
		SpringExpressionUtil.addPropertyAccessors(new JsonObjectPropertyAccessor());
	}
	
	public FortifySSCConnection(String baseUrl, String token, String applicationName, String versionName, ProxyConfiguration proxyConfig) {
		super(baseUrl, token, proxyConfig);
		this.applicationName = applicationName;
		this.versionName = versionName;
		Assert.hasText(this.applicationName, "SSC Application Name cannot be blank");
		Assert.hasText(this.versionName, "SSC Application Version Name cannot be blank");
		this.applicationVersionId = getApplicationVersionId(this.getApplicationName(), this.versionName);
	}
	
	/**
	 * Constructor defining connection information for the given application version id
	 * @param baseUrl
	 * @param token
	 * @param applicationVersionId
	 * @param proxyConfig
	 
	public FortifySSCConnection(String baseUrl, String token, long applicationVersionId, ProxyConfiguration proxyConfig) {
		super(baseUrl, token, proxyConfig);
		this.applicationVersionId = applicationVersionId;
		JSONObject applicationVersion = getMemoizedApplicationVersion();
		this.applicationName = SpringExpressionUtil.evaluateExpression(applicationVersion, "project.name", String.class);
		this.versionName = SpringExpressionUtil.evaluateExpression(applicationVersion, "name", String.class);
	}
	
	/**
	 * @return The SSC application version id used for this connection
	 
	public final long getApplicationVersionId() {
		return applicationVersionId;
	}
	
	
	/**
	 * @return The SSC application name used for this connection
	 
	public String getApplicationName() {
		return applicationName;
	}
	
	/**
	 * @return The SSC application version name used for this connection
	 
	public String getVersionName() {
		return versionName;
	}
	
	/**
	 * @return JSONObject containing application version data
	 
	private final JSONObject getApplicationVersion() {
		return executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/v1/projectVersions")
				.path(""+getApplicationVersionId()), JSONObject.class).optJSONObject("data");
	}

	/**
	 * @param applicationName
	 * @param versionName
	 * @return The application version id for the given application and version name
	 
	private final long getApplicationVersionId(String applicationName, String versionName) {
		JSONArray data = executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/v1/projectVersions")
				.queryParam("q", "project.name:\""+applicationName+"\"+and+name:\""+versionName+"\"")
				.queryParam("fields", "id")
				, JSONObject.class).optJSONArray("data");
		if ( data==null || data.length()!=1 ) {
			throw new RuntimeException("Error retrieving id for application "+applicationName+" version "+versionName);
		}
		return data.optJSONObject(0).optLong("id");
	}
	
	/**
	 * @param type Either performanceIndicatorHistories or variableHistories
	 * @return Performance indicator or variable history data 
	 
	private final JSONArray getMetrics(String type) {
		return executeRequest(HttpMethod.GET, getBaseResource()
    			.path("/api/v1/projectVersions/")
    			.path(""+getApplicationVersionId())
    			.path(type), JSONObject.class).optJSONArray("data");
	}
	
	private final JSONArray getFilterSets() {
		return executeRequest(HttpMethod.GET, getBaseResource()
				.path("/api/v1/projectVersions/")
    			.path(""+getApplicationVersionId())
    			.path("filterSets"), JSONObject.class).optJSONArray("data");
	}
	
	/**
	 * @return The most recent SCA artifact for which processing is complete, or artifact with state 
	 *         PROCESSING, SCHED_PROCESSING, REQUIRE_AUTH or ERROR_PROCESSING, whichever comes first
	 
	public final JSONObject findMostRecentSuccessfulSCAOrNotCompletedArtifact() {
		return findMostRecentArtifact("(_embed.scans?.getJSONObject(0)?.type=='SCA' && status=='PROCESS_COMPLETE') || status matches 'PROCESSING|SCHED_PROCESSING|REQUIRE_AUTH|ERROR_PROCESSING'", true);
	}
	
	/**
	 * @return The most recent artifact that hasn't been processed yet
	 
	public final JSONObject findMostRecentArtifactToBeProcessed() {
		return findMostRecentArtifact("status=='PROCESSING' || status=='SCHED_PROCESS'", true);
	}
	
	/**
	 * @param matchExpression Spring Expression Language expression to evaluate on the artifact data
	 * @param matchValue Value to compare with the outcome of the given matchExpression
	 * @return The most recent artifact data for which the given matchExpression matches the given matchValue
	 
	private final JSONObject findMostRecentArtifact(String matchExpression, Object matchValue) {
		JSONObject artifact = null;
		int start = 0;
		int count = 1;
		int limit = 5;
		while ( start < count && artifact == null ) {
			JSONObject json = executeRequest(HttpMethod.GET, 
				getBaseResource()
    			.path("/api/v1/projectVersions")
    			.path(""+getApplicationVersionId())
    			.path("artifacts")
    			.queryParam("orderBy", "-uploadDate")
    			.queryParam("embed", "scans")
    			.queryParam("start", ""+start)
    			.queryParam("limit", ""+limit), JSONObject.class);
			count = json.optInt("count");
			JSONArray artifacts = json.optJSONArray("data");
			start += artifacts.length();
			artifact = JSONUtil.findJSONObject(artifacts, matchExpression, matchValue);
		}
		return artifact;
	}
	
	public final String getFileToken(FileTokenType type) {
		JSONObject entity = new JSONObject();
		try {
			entity.put("fileTokenType", type.toString());
		} catch (JSONException e) {
			throw new RuntimeException("Error creating entity for fileTokens request", e);
		}
		JSONObject json = executeRequest(HttpMethod.POST, getBaseResource().path("/api/v1/fileTokens").entity(entity), JSONObject.class);
		return SpringExpressionUtil.evaluateExpression(json, "data.token", String.class);
	}
	
	public final long downloadApplicationFile(Path target) {
		InputStream is = executeRequest(HttpMethod.POST, 
				getBaseResource()
				.path("/download/currentStateFprDownload.html")
				.queryParam("id", ""+getApplicationVersionId())
				.queryParam("mat", getFileToken(FileTokenType.DOWNLOAD))
				.accept("*"), InputStream.class);
		
		try {
			return Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException("Error downloading application file", e);
		} finally {
			try {
				is.close();
			} catch ( IOException ioe ) {
				LOG.warn("Error closing response stream, subsequent requests may fail", ioe);
			}
		}
	}
	
	private final Path getSourceBasePath() {
		Path result = null;
		ZipInputStream zis = null;
		try {
			Path tempApplicationFile = Files.createTempFile(null, null);
			downloadApplicationFile(tempApplicationFile);
			zis = new ZipInputStream(new FileInputStream(tempApplicationFile.toFile()));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if ( "audit.fvdl".equals(entry.getName()) ) {
					result = getSourceBasePathFromAuditFvdl(zis);
				}
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Error reading application file");
		} finally {
			try {
				if ( zis !=null ) { zis.close(); }
			} catch ( IOException ioe ) {
				LOG.warn("Error closing application file", ioe);
			}
		}
	}

	private Path getSourceBasePathFromAuditFvdl(ZipInputStream inputStream) {
		Path result = null;
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = factory.createXMLEventReader(inputStream);
			boolean isSourceBasePathElement = false;
			while(eventReader.hasNext() && result==null) {
				XMLEvent event = eventReader.nextEvent();
				switch(event.getEventType()){
				case XMLStreamConstants.START_ELEMENT:
					StartElement startElement = event.asStartElement();
					isSourceBasePathElement = "SourceBasePath".equals(startElement.getName().getLocalPart());       
					break;
				case XMLStreamConstants.CHARACTERS:
					if ( isSourceBasePathElement ) {
						Characters characters = event.asCharacters();
						result = Paths.get(characters.getData());
					}
					break;
				case  XMLStreamConstants.END_ELEMENT:
					break;
				}		    
			}
		} catch (FactoryConfigurationError | XMLStreamException e) {
			throw new RuntimeException("Error reading application file");
		}
		return result;
	}

	public final JSONObject uploadFPR(File fprFile) {
		MultiPart multiPart = new FormDataMultiPart();
		multiPart.type(new MediaType("multipart", "form-data",
	    		Collections.singletonMap(Boundary.BOUNDARY_PARAMETER, Boundary.createBoundary())));
		multiPart.bodyPart(new FormDataBodyPart("Filename", fprFile.getName()));
		multiPart.bodyPart(new FileDataBodyPart(fprFile.getName(), fprFile, MediaType.APPLICATION_OCTET_STREAM_TYPE));
		
		InputStream is = executeRequest(HttpMethod.POST, 
				getBaseResource()
				.path("/upload/resultFileUpload.html")
				.queryParam("entityId", ""+getApplicationVersionId())
				.queryParam("mat", getFileToken(FileTokenType.UPLOAD))
				.accept("application/xml")
				.entity(multiPart, multiPart.getMediaType()), InputStream.class);
		JSONObject json = xml2json(is);
		try {
			is.close();
		} catch ( IOException ioe ) {
			LOG.warn("Error closing response stream, subsequent requests may fail", ioe);
		}
		return json;
	}
	
	private JSONObject xml2json(InputStream is) {
		try {
			HierarchicalStreamCopier copier = new HierarchicalStreamCopier();
			HierarchicalStreamDriver binaryDriver = new BinaryStreamDriver();
			HierarchicalStreamDriver jsonDriver = new JettisonMappedXmlDriver();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copier.copy(new StaxDriver().createReader(is), binaryDriver.createWriter(baos));
			byte[] data = baos.toByteArray();

			// transform binary XStream data into JSON
			StringWriter strWriter = new StringWriter();
			copier.copy(binaryDriver.createReader(new ByteArrayInputStream(data)), jsonDriver.createWriter(strWriter));
			String json = strWriter.toString();

			return new JSONObject(json.toString());
		} catch (JSONException e) {
			throw new RuntimeException("Error converting XML response to JSON", e);
		}
	}
	
	/**
	 * Wait at most the given number of maxSeconds for SSC to finish processing 
	 * the last uploaded SCA artifact.
	 * 
	 * @param maxSeconds maximum seconds to wait for SSC to finish processing
	 * @return true if SSC finished processing the artifact within the given time-out, 
	 *         false if SSC has not finished processing the artifact within the given time-out
	 
	public final boolean waitForProcessingToComplete(int maxSeconds) {
		long startTime = new Date().getTime();
		JSONObject artifact = findMostRecentArtifactToBeProcessed();
		while ( new Date().getTime() < startTime+maxSeconds*1000 && JSONUtil.isMatching(artifact, "status", "PROCESSING") ) {
			try {
				Thread.sleep(1000);
			} catch ( InterruptedException ignore ) {}
			artifact = findMostRecentArtifactToBeProcessed();
		}
		return !JSONUtil.isMatching(artifact, "status", "PROCESSING");
	}
	
	/**
	 * @param issue
	 * @return Browser-accessible deep link for the given issue
	 
	public final String getIssueDeepLink(JSONObject issue) {
		return getApplicationVersionDeepLink()+"/"+issue.optString("id")+"/";
	}
	
	/**
	 * @return Browser-accessible deep link for the current application version
	 
	public final String getApplicationVersionDeepLink() {
		return getBaseUrl()+"html/ssc/index.jsp#!/version/"+getApplicationVersionId()+"/fix";
	}
	
	/**
	 * @return Application version data, may be cached
	 
	public final JSONObject getMemoizedApplicationVersion() {
		return applicationVersionSupplier.get();
	}
	
	/**
	 * @return Performance indicator history data, may be cached
	 
	public final JSONArray getMemoizedPerformanceIndicatorHistories() {
		return performanceIndicatorHistoriesSupplier.get();
	}
	
	/**
	 * @return Variable history data, may be cached
	 
	public final JSONArray getMemoizedVariableHistories() {
		return variableHistoriesSupplier.get();
	}
	
	/**
	 * @return Filter sets, may be cached
	 
	public final JSONArray getMemoizedFilterSets() {
		return filterSetsSupplier.get();
	}
	
	/**
	 * @return Source base path, may be cached
	 
	public final Path getMemoizedSourceBasePath() {
		return sourceBasePathSupplier.get();
	}
	
	/**
	 * @param guidOrTitle filter set GUID or title
	 * @return filter set matching the given GUID or title, or null if not found
	 
	public final JSONObject findFilterSetByGuidOrTitle(String guidOrTitle) {
		String matchExpr = MessageFormat.format("guid==''{0}'' || title==''{0}''", new Object[]{guidOrTitle});
		return JSONUtil.findJSONObject(getMemoizedFilterSets(), matchExpr, true);
	}
	
	/**
	 * @return default filter set
	 
	public final JSONObject findDefaultFilterSet() {
		return JSONUtil.findJSONObject(getMemoizedFilterSets(), "defaultFilterSet", true);
	}

	/**
	 * @return Most recent SCA artifact or artifact with unknown state, whichever comes first
	 
	public JSONObject getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact() {
		return mostRecentSuccessfulSCAOrNotCompletedArtifact.get();
	}
	
	public ArtifactStatus getMostRecentSuccesfulSCAOrNotCompletedArtifactStatus() {
		return Enum.valueOf(ArtifactStatus.class, getMemoizedMostRecentSuccessfulSCAOrNotCompletedArtifact().optString("status"));
	}
	
	/**
	 * List of status codes for artifacts that can be returned by the
	 * various 'mostRecentSuccessfulSCAOrNotCompletedArtifact' methods
	 * in {@link FortifySSCConnection}.
	 * 
	 * @author Ruud Senden
	 *
	public static enum ArtifactStatus
	{
		PROCESS_COMPLETE, 
		PROCESSING,
		SCHED_PROCESSING, 
		REQUIRE_AUTH,
		ERROR_PROCESSING
	}
	
	/**
	 * Enumeration for SSC file token types, to be used for {@link FortifySSCConnection#getFileToken(FileTokenType)}
	 
	public static enum FileTokenType {
		UPLOAD, DOWNLOAD, PREVIEW_FILE, REPORT_FILE
	}
}
*/
