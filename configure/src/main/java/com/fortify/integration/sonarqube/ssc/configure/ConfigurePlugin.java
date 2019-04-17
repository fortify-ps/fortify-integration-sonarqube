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
package com.fortify.integration.sonarqube.ssc.configure;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.api.SSCRulepackAPI;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;
import com.google.common.io.PatternFilenameFilter;

public class ConfigurePlugin {
	private Shell shell;
	private Text textPluginJar;
	private Text textSSCUrl;
	private SSCAuthenticatingRestConnection conn;
	
	public static void main(String[] args) {
		try {
			ConfigurePlugin window = new ConfigurePlugin();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell();
		shell.setSize(682, 684);
		shell.setText("Configure Fortify SonarQube Plugin");
		shell.setLayout(null);
		addPluginJarWidgets();
		addSSCUrlWidgets();
	}
	
	private void addPluginJarWidgets() {
		Label lblPluginJar = new Label(shell, SWT.NONE);
		lblPluginJar.setBounds(10, 10, 135, 28);
		lblPluginJar.setText("Plugin Jar:");
		
		textPluginJar = new Text(shell, SWT.BORDER);
		textPluginJar.setBounds(151, 10, 432, 28);
		textPluginJar.setEditable(false);
		String[] pluginJars = new File(".").list(new PatternFilenameFilter("fortify-ssc-sonarqube-plugin-[0-9].*.jar"));
		if ( pluginJars != null && pluginJars.length>0 ) {
			textPluginJar.setText(new File(".", pluginJars[0]).getAbsolutePath());
		}
		Button btnPluginJarBrowse = new Button(shell, SWT.NONE);
		btnPluginJarBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			    dialog.setFilterNames(new String[] { "Fortify SonarQube Plugin JAR" });
			    dialog.setFilterPath(".");
			    dialog.setFilterExtensions(new String[] { "fortify-ssc-sonarqube-plugin-*.jar" }); 
			    String pluginJar = dialog.open();
			    if ( pluginJar != null ) {
			    	textPluginJar.setText(pluginJar);
			    }
			} 
		});
		btnPluginJarBrowse.setBounds(589, 10, 64, 28);
		btnPluginJarBrowse.setText("Browse");
	}
	
	private void addSSCUrlWidgets() {
		Label lblSSCUrl = new Label(shell, SWT.NONE);
		lblSSCUrl.setBounds(10, 40, 135, 28);
		lblSSCUrl.setText("SSC URL:");
		
		textSSCUrl = new Text(shell, SWT.BORDER);
		textSSCUrl.setBounds(151, 40, 432, 28);
		textSSCUrl.setEditable(true);
		textSSCUrl.setText("https://user:pwd@host:port/ssc");
		
		Button btnTestSSCUrl = new Button(shell, SWT.NONE);
		btnTestSSCUrl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				try {
					conn = SSCAuthenticatingRestConnection.builder().baseUrl(textSSCUrl.getText()).build();
					conn.api(SSCApplicationVersionAPI.class).queryApplicationVersions().maxResults(1).paramFields("id").build().getUnique();
					MessageBox dialog = new MessageBox(shell, SWT.OK);
		            dialog.setText("Success");
		            dialog.setMessage("Connection to SSC successful");
		            dialog.open();
		            addSaveButtonWidget();
				} catch ( Exception e ) {
					MessageBox dialog = new MessageBox(shell, SWT.CANCEL);
		            dialog.setText("Error");
		            dialog.setMessage("Unable to connect to SSC\n"+e.toString());
		            dialog.open();
				}
			} 
		});
		btnTestSSCUrl.setBounds(589, 40, 64, 28);
		btnTestSSCUrl.setText("Validate");
	}
	
	private void addSaveButtonWidget() {
		Button btnSave = new Button(shell, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SSCRulepackAPI api = conn.api(SSCRulepackAPI.class);
				JSONMap extMetadata = api.queryRulepacks()
						.preProcessor(new JSONMapFilterSpEL(MatchMode.INCLUDE, "rulepackType=='CATPACK'")).build()
						.getUnique();
				Path externalMetadataPath = Paths.get(".", "externalmetadata.xml");
				String id = extMetadata.get("id", String.class);
				api.downloadRulepack(id, externalMetadataPath);
				
				try (FileSystem zipfs = FileSystems.newFileSystem(Paths.get(textPluginJar.getText()), this.getClass().getClassLoader())) {
					Path pathInZipfile = zipfs.getPath("/externalmetadata.xml");
					Files.copy(externalMetadataPath, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnSave.setBounds(578, 600, 75, 25);
		btnSave.setText("Save");
	}
}
