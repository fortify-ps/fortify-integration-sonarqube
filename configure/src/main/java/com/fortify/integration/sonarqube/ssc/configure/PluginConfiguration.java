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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import com.fortify.client.ssc.api.SSCRulepackAPI;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;
import com.fortify.integration.sonarqube.ssc.config.AbstractYmlRootConfig;
import com.fortify.integration.sonarqube.ssc.config.MetricsConfig;
import com.fortify.integration.sonarqube.ssc.config.RulesConfig;
import com.fortify.integration.sonarqube.ssc.externalmetadata.FortifyExternalMetadata;
import com.fortify.util.rest.json.JSONMap;
import com.fortify.util.rest.json.preprocessor.filter.AbstractJSONMapFilter.MatchMode;
import com.fortify.util.rest.json.preprocessor.filter.JSONMapFilterSpEL;
import com.google.common.io.PatternFilenameFilter;

import net.miginfocom.swing.MigLayout;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.AbstractListModel;
import java.util.Map;
import com.fortify.integration.sonarqube.ssc.config.MetricsConfig.MetricConfig;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.ObjectProperty;

public class PluginConfiguration {

	private JFrame frmFortifySscSonarqube;
	private SSCConnectionDialog sscConnectionDialog;
	private Path pluginJarPath;
	private Path externalMetadataTempPath;
	private Path rulesYmlTempPath;
	private Path metricsYmlTempPath;
	private RulesConfig rulesConfig;
	private MetricsConfig metricsConfig;
	private JComboBox comboBoxRulesSource;
	private JList listMetricKeys;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PluginConfiguration window = new PluginConfiguration();
					window.frmFortifySscSonarqube.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PluginConfiguration() {
		initialize();
	}
	
	private void initDependencies(JFrame frame) {
		this.sscConnectionDialog = new SSCConnectionDialog(frame);
		this.pluginJarPath = getPluginJarPath(frame);
		this.externalMetadataTempPath = getTempPath("externalmetadata", "xml");
		this.rulesYmlTempPath = getTempPath("rules", "yml");
		this.metricsYmlTempPath = getTempPath("metrics", "yml");
		copyTempFilesFromJar(frame);
		this.rulesConfig = loadConfig(frame, this.rulesYmlTempPath, RulesConfig.class);
		this.metricsConfig = loadConfig(frame, this.metricsYmlTempPath, MetricsConfig.class);
	}
	
	private void withPluginJarFS(Consumer<FileSystem> consumer) {
		try ( FileSystem fs = FileSystems.newFileSystem(this.pluginJarPath, this.getClass().getClassLoader()) ) { 
			consumer.accept(fs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private <T extends AbstractYmlRootConfig> T loadConfig(JFrame frame, Path path, Class<T> type) {
		try {
			if ( Files.exists(path) && Files.size(path)>0 ) {
				return AbstractYmlRootConfig.load(path, type);
			} else {
				return AbstractYmlRootConfig.create(type);
			}
		} catch (Exception e) {
			handleError("Error loading "+type.getSimpleName()+" Yaml configuration, using fresh configuration.", e);
			return AbstractYmlRootConfig.create(type);
		}
	}

	private void handleError(String message, Exception e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(frmFortifySscSonarqube, message+"\n\nError: "+e.getMessage());
	}

	private void copyTempFilesFromJar(JFrame frame) {
		withPluginJarFS(fs->{
			copyFile(frame, fs.getPath("/externalmetadata.xml"), externalMetadataTempPath);
			copyFile(frame, fs.getPath("/rules.yml"), rulesYmlTempPath);
			copyFile(frame, fs.getPath("/metrics.yml"), metricsYmlTempPath);
		});
	}
	
	private void saveConfiguration(JFrame frame) {
		withPluginJarFS(fs->{
			rulesConfig.save(rulesYmlTempPath);
			metricsConfig.save(metricsYmlTempPath);
			copyFile(frame, externalMetadataTempPath, fs.getPath("/externalmetadata.xml"));
			copyFile(frame, rulesYmlTempPath, fs.getPath("/rules.yml"));
			copyFile(frame, metricsYmlTempPath, fs.getPath("/metrics.yml"));
		});
	}
	
	

	private void copyFile(JFrame frame, Path sourcePath, Path targetPath) {
		if ( Files.exists(sourcePath) ) {
			try {
				Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				handleError("Error copying "+sourcePath.toString()+" to "+targetPath.toString()+", skipping", e);
			}
		}
	}
	
	
	
	private Path getTempPath(String prefix, String suffix) {
		try {
			return Files.createTempFile(prefix, suffix);
		} catch (IOException e2) {
			throw new RuntimeException("Error creating temporary file", e2);
		}
	}

	private Path getPluginJarPath(JFrame frame) {
		Path result = null;
		String pluginJar = null;
		File currentDir = new File(".");
		String[] pluginJars = currentDir.list(new PatternFilenameFilter("fortify-ssc-sonarqube-plugin-[0-9].*.jar"));
		if ( pluginJars != null && pluginJars.length==1 ) {
			pluginJar = pluginJars[0];
		}
		
		
		while (result == null ) {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR files", "jar");
			chooser.setFileFilter(filter);
			chooser.setCurrentDirectory(currentDir);
			if ( pluginJar != null ) { chooser.setSelectedFile(new File(currentDir, pluginJar)); }
			int returnVal = chooser.showOpenDialog(frame);
			if( returnVal == JFileChooser.CANCEL_OPTION ) {
				System.exit(1);
			} else {
				result = chooser.getSelectedFile().toPath();
			}
		}
		return result;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFortifySscSonarqube = new JFrame();
		initDependencies(frmFortifySscSonarqube);
		frmFortifySscSonarqube.setTitle("Fortify SSC SonarQube Plugin Configuration");
		frmFortifySscSonarqube.setBounds(100, 100, 450, 300);
		frmFortifySscSonarqube.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFortifySscSonarqube.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmFortifySscSonarqube.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panelRules = new JPanel();
		tabbedPane.addTab("Rules", null, panelRules, null);
		panelRules.setLayout(new MigLayout("", "[69px][grow][115px]", "[23px]"));
		
		JLabel lblRulesSource = new JLabel("Rules Source: ");
		panelRules.add(lblRulesSource, "cell 0 0,alignx left,aligny center");
		
		comboBoxRulesSource = new JComboBox();
		updateComboBoxRulesSource(comboBoxRulesSource);
		panelRules.add(comboBoxRulesSource, "cell 1 0,alignx left,aligny center");
		
		JButton btnUpdateFromSsc = new JButton("Update from SSC");
		btnUpdateFromSsc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SSCAuthenticatingRestConnection conn = sscConnectionDialog.getConnection();
				if ( conn != null ) {
					SSCRulepackAPI api = conn.api(SSCRulepackAPI.class);
					JSONMap extMetadata = api.queryRulepacks()
							.preProcessor(new JSONMapFilterSpEL(MatchMode.INCLUDE, "rulepackType=='CATPACK'")).build()
							.getUnique();
					
					String id = extMetadata.get("id", String.class);
					api.downloadRulepack(id, externalMetadataTempPath);
					updateComboBoxRulesSource(comboBoxRulesSource);
				}
			}
		});
		panelRules.add(btnUpdateFromSsc, "cell 2 0,alignx left,aligny center");
		
		JPanel panelMetrics = new JPanel();
		tabbedPane.addTab("Metrics", null, panelMetrics, null);
		panelMetrics.setLayout(new MigLayout("", "[139px][grow]", "[][]"));
		
		JPanel panelMetricAddRemoveButtons = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelMetricAddRemoveButtons.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelMetrics.add(panelMetricAddRemoveButtons, "cell 0 0,alignx left,aligny top");
		
		JButton btnMetricAdd = new JButton("Add");
		btnMetricAdd.setHorizontalAlignment(SwingConstants.LEFT);
		panelMetricAddRemoveButtons.add(btnMetricAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setHorizontalAlignment(SwingConstants.LEFT);
		panelMetricAddRemoveButtons.add(btnRemove);
		
		listMetricKeys = new JList();
		listMetricKeys.setListData(metricsConfig.getMetrics().keySet().toArray());
		listMetricKeys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panelMetrics.add(listMetricKeys, "cell 0 1,alignx left,aligny top");
		
		MetricDetailsPanel metricDetailsPanel = new MetricDetailsPanel();
		panelMetrics.add(metricDetailsPanel, "cell 1 0 1 2,alignx left,aligny top");
		
		JPanel panelButtons = new JPanel();
		frmFortifySscSonarqube.getContentPane().add(panelButtons, BorderLayout.SOUTH);
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfiguration(frmFortifySscSonarqube);
				JOptionPane.showMessageDialog(frmFortifySscSonarqube, "Configuration saved");
			}
		});
		panelButtons.add(btnSave);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmFortifySscSonarqube.dispose();
			}
		});
		panelButtons.add(btnClose);
		initDataBindings();
	}

	private void updateComboBoxRulesSource(JComboBox comboBoxRulesSource) {
		List<String> entries = new ArrayList<>();
		entries.add("Single Fortify Rule");
		try {
			if ( Files.size(externalMetadataTempPath) > 0 ) {
				FortifyExternalMetadata metadata = FortifyExternalMetadata.parse(externalMetadataTempPath.toUri().toURL());
				entries.addAll(metadata.getExternalListNames());
			}
		} catch (XMLStreamException | IOException e) {
			handleError("Error parsing externalmetadata.xml, try refreshing from SSC", e);
		}
		comboBoxRulesSource.setModel(new DefaultComboBoxModel(entries.toArray(new String[]{})));
	}
	protected void initDataBindings() {
		BeanProperty<RulesConfig, String> rulesConfigBeanProperty = BeanProperty.create("rulesSourceName");
		BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
		AutoBinding<RulesConfig, String, JComboBox, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, rulesConfig, rulesConfigBeanProperty, comboBoxRulesSource, jComboBoxBeanProperty, "rulesSourceName");
		autoBinding.bind();
	}
}
