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
 ******************************************************************************/
package com.fortify.integration.sonarqube.ssc.scanner;

import java.util.Collection;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerByteArray;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.Startable;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.InputFile;

import com.fortify.client.ssc.api.SSCIssueAPI;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.processor.AbstractJSONMapProcessor;
import com.fortify.util.spring.SpringExpressionUtil;

// TODO Test this
@ScannerSide
@ExtensionPoint
public class FortifyIssueHelper implements Startable {
	/**
	DB db = DBMaker.tempFileDB()
			.closeOnJvmShutdown().fileDeleteAfterClose()
			.fileMmapEnableIfSupported()
			.make();
	Map<String, List<Object>> groups = db.hashMap("groups", Serializer.STRING, Serializer.JAVA).create();
	*/
	
	private DB db = null;
	private BTreeMap<byte[], JSONMap> issuesByReversePath = null;
	private final FortifySSCScannerSideConnectionHelper connHelper;
	private final String[] issueFields;
	
	public FortifyIssueHelper(FortifySSCScannerSideConnectionHelper connHelper, String... issueFields) {
		this.connHelper = connHelper;
		this.issueFields = issueFields;
	}
	
	private synchronized DB getDB() {
		if ( db==null ) {
			db = DBMaker.tempFileDB()
					.closeOnJvmShutdown().fileDeleteAfterClose()
					.fileMmapEnableIfSupported()
					.make();
		}
		return db;
	}
	
	private synchronized BTreeMap<byte[], JSONMap> getIssuesByReversePath() {
		if ( issuesByReversePath==null && connHelper.isConnectionAvailable() ) {
			issuesByReversePath = getDB().treeMap("issuesByReversePath").keySerializer(new SerializerByteArray()).valueSerializer(Serializer.JAVA).create();
			connHelper.getConnection().api(SSCIssueAPI.class).queryIssues(connHelper.getApplicationVersionId())
				.paramFields(issueFields).build().processAll(new AbstractJSONMapProcessor() {
					
					@Override
					public void process(JSONMap json) {
						byte[] key = new StringBuilder(SpringExpressionUtil.evaluateExpression(json, "id+'.'+fullFileName", String.class)).reverse().toString().getBytes();
						issuesByReversePath.put(key, json);						
					}
				});
		}
		return issuesByReversePath;
	}
	
	public Collection<JSONMap> getIssuesForInputFile(InputFile inputFile) {
		// This uses deprecated SQ API, but there seems to be no non-deprecated methods for getting
		// the full file name; uri() is not deprecated but not guaranteed to return the actual file
		// location.
		byte[] reverseInputFilePathString = new StringBuilder(inputFile.absolutePath()).reverse().toString().getBytes();
		return getIssuesByReversePath().prefixSubMap(reverseInputFilePathString).values();
	}
	
	@Override
	public void start() {
		// Nothing to do, we load issue data on demand
	}
	
	@Override
	public void stop() {
		if ( issuesByReversePath!=null ) {
			issuesByReversePath.close();
		}
		if ( db!=null ) {
			db.close();
		}
	}
}
