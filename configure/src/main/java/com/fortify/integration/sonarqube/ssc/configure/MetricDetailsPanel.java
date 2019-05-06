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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;

import com.fortify.integration.sonarqube.common.SourceSystem;
import com.fortify.integration.sonarqube.common.config.MetricsConfig;
import com.fortify.integration.sonarqube.common.config.MetricsConfig.Direction;
import com.fortify.integration.sonarqube.common.config.MetricsConfig.MetricConfig;
import com.fortify.integration.sonarqube.common.config.MetricsConfig.MetricValueType;

public class MetricDetailsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BindingGroup m_bindingGroup;
	private final SourceSystem sourceSystem;
	private MetricsConfig.MetricConfig metricConfig = new MetricsConfig.MetricConfig();
	private JTextField nameJTextField;
	private JTextField domainJTextField;
	private JCheckBox qualitativeJCheckBox;
	private JTextField exprJTextField;
	private JTextArea descriptionJTextArea;
	private JLabel typeLabel;
	private JComboBox typeJComboBox;
	private JLabel directionLabel;
	private JComboBox directionJComboBox;
	private JLabel keyLabel;
	private JTextField keyJTextField;
	private JPanel panel;
	private JLabel lblExprHelp;

	public MetricDetailsPanel(SourceSystem sourceSystem, MetricsConfig.MetricConfig newMetricConfig) {
		this(sourceSystem);
	}

	/**
	 * @wbp.parser.constructor
	 */
	public MetricDetailsPanel(SourceSystem sourceSystem) {
		this.sourceSystem = sourceSystem;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0, 30};
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		setLayout(gridBagLayout);
		
		keyLabel = new JLabel("Key");
		GridBagConstraints gbc_keyLabel = new GridBagConstraints();
		gbc_keyLabel.anchor = GridBagConstraints.WEST;
		gbc_keyLabel.insets = new Insets(5, 5, 5, 5);
		gbc_keyLabel.gridx = 0;
		gbc_keyLabel.gridy = 0;
		add(keyLabel, gbc_keyLabel);
		
		keyJTextField = new JTextField();
		GridBagConstraints gbc_keyJTextField = new GridBagConstraints();
		gbc_keyJTextField.insets = new Insets(5, 0, 5, 5);
		gbc_keyJTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_keyJTextField.gridx = 1;
		gbc_keyJTextField.gridy = 0;
		add(keyJTextField, gbc_keyJTextField);

		JLabel nameLabel = new JLabel("Name:");
		GridBagConstraints labelGbc_0 = new GridBagConstraints();
		labelGbc_0.anchor = GridBagConstraints.WEST;
		labelGbc_0.insets = new Insets(5, 5, 5, 5);
		labelGbc_0.gridx = 0;
		labelGbc_0.gridy = 1;
		add(nameLabel, labelGbc_0);

		nameJTextField = new JTextField();
		GridBagConstraints componentGbc_0 = new GridBagConstraints();
		componentGbc_0.insets = new Insets(5, 0, 5, 5);
		componentGbc_0.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_0.gridx = 1;
		componentGbc_0.gridy = 1;
		add(nameJTextField, componentGbc_0);
		
		typeLabel = new JLabel("Type:");
		GridBagConstraints gbc_typeLabel = new GridBagConstraints();
		gbc_typeLabel.anchor = GridBagConstraints.WEST;
		gbc_typeLabel.insets = new Insets(5, 5, 5, 5);
		gbc_typeLabel.gridx = 0;
		gbc_typeLabel.gridy = 2;
		add(typeLabel, gbc_typeLabel);
		
		typeJComboBox = new JComboBox();
		typeJComboBox.setModel(new DefaultComboBoxModel(MetricValueType.values()));
		GridBagConstraints gbc_typeJComboBox = new GridBagConstraints();
		gbc_typeJComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_typeJComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_typeJComboBox.gridx = 1;
		gbc_typeJComboBox.gridy = 2;
		add(typeJComboBox, gbc_typeJComboBox);

		JLabel domainLabel = new JLabel("Domain:");
		GridBagConstraints labelGbc_1 = new GridBagConstraints();
		labelGbc_1.anchor = GridBagConstraints.WEST;
		labelGbc_1.insets = new Insets(5, 5, 5, 5);
		labelGbc_1.gridx = 0;
		labelGbc_1.gridy = 3;
		add(domainLabel, labelGbc_1);

		domainJTextField = new JTextField();
		GridBagConstraints componentGbc_1 = new GridBagConstraints();
		componentGbc_1.insets = new Insets(5, 0, 5, 5);
		componentGbc_1.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_1.gridx = 1;
		componentGbc_1.gridy = 3;
		add(domainJTextField, componentGbc_1);
		
				JLabel exprLabel = new JLabel("Expression:");
				GridBagConstraints labelGbc_3 = new GridBagConstraints();
				labelGbc_3.anchor = GridBagConstraints.WEST;
				labelGbc_3.insets = new Insets(5, 5, 5, 5);
				labelGbc_3.gridx = 0;
				labelGbc_3.gridy = 4;
				add(exprLabel, labelGbc_3);
		
				exprJTextField = new JTextField();
				GridBagConstraints componentGbc_3 = new GridBagConstraints();
				componentGbc_3.insets = new Insets(5, 0, 5, 5);
				componentGbc_3.fill = GridBagConstraints.HORIZONTAL;
				componentGbc_3.gridx = 1;
				componentGbc_3.gridy = 4;
				add(exprJTextField, componentGbc_3);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridx = 2;
		gbc_panel.gridy = 4;
		add(panel, gbc_panel);
		
		lblExprHelp = new JLabel("");
		lblExprHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( sourceSystem != null ) {
					new ExpressionHelpDialog("Help - Metric Expression", sourceSystem.getMetricsExpressionFieldsHTMLDescription());
				}
			}
		});
		ImageIcon lblExprHelpIcon = new ImageIcon(MetricDetailsPanel.class.getResource("/javax/swing/plaf/metal/icons/ocean/question.png")); 
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		lblExprHelp.setIcon(lblExprHelpIcon);
		panel.add(lblExprHelp);
		
		

		JLabel qualitativeLabel = new JLabel("Qualitative:");
		GridBagConstraints labelGbc_2 = new GridBagConstraints();
		labelGbc_2.anchor = GridBagConstraints.WEST;
		labelGbc_2.insets = new Insets(5, 5, 5, 5);
		labelGbc_2.gridx = 0;
		labelGbc_2.gridy = 5;
		add(qualitativeLabel, labelGbc_2);

		qualitativeJCheckBox = new JCheckBox();
		GridBagConstraints componentGbc_2 = new GridBagConstraints();
		componentGbc_2.insets = new Insets(5, 0, 5, 5);
		componentGbc_2.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_2.gridx = 1;
		componentGbc_2.gridy = 5;
		add(qualitativeJCheckBox, componentGbc_2);
		
		directionLabel = new JLabel("Direction:");
		GridBagConstraints gbc_directionLabel = new GridBagConstraints();
		gbc_directionLabel.anchor = GridBagConstraints.WEST;
		gbc_directionLabel.insets = new Insets(5, 5, 5, 5);
		gbc_directionLabel.gridx = 0;
		gbc_directionLabel.gridy = 6;
		add(directionLabel, gbc_directionLabel);
		
		directionJComboBox = new JComboBox();
		directionJComboBox.setModel(new DefaultComboBoxModel(Direction.values()));
		GridBagConstraints gbc_directionJComboBox = new GridBagConstraints();
		gbc_directionJComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_directionJComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_directionJComboBox.gridx = 1;
		gbc_directionJComboBox.gridy = 6;
		add(directionJComboBox, gbc_directionJComboBox);

		JLabel descriptionLabel = new JLabel("Description:");
		GridBagConstraints labelGbc_4 = new GridBagConstraints();
		labelGbc_4.anchor = GridBagConstraints.WEST;
		labelGbc_4.insets = new Insets(5, 5, 0, 5);
		labelGbc_4.gridx = 0;
		labelGbc_4.gridy = 7;
		add(descriptionLabel, labelGbc_4);

		descriptionJTextArea = new JTextArea();
		descriptionJTextArea.setRows(5);
		GridBagConstraints componentGbc_4 = new GridBagConstraints();
		componentGbc_4.insets = new Insets(5, 0, 0, 5);
		componentGbc_4.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_4.gridx = 1;
		componentGbc_4.gridy = 7;
		add(descriptionJTextArea, componentGbc_4);

		if (metricConfig != null) {
			m_bindingGroup = initDataBindings();
		}
	}

	public MetricsConfig.MetricConfig getMetricConfig() {
		return metricConfig;
	}

	public void setMetricConfig(MetricsConfig.MetricConfig newMetricConfig) {
		setMetricConfig(newMetricConfig, true);
	}
	
	public void setMetricConfig(MetricsConfig.MetricConfig newMetricConfig, boolean update) {
		metricConfig = newMetricConfig;
		if (update) {
			if (m_bindingGroup != null) {
				m_bindingGroup.unbind();
				m_bindingGroup = null;
			}
			if (metricConfig != null) {
				m_bindingGroup = initDataBindings();
			}
		}
	}
	protected BindingGroup initDataBindings() {
		BeanProperty<MetricConfig, String> nameProperty = BeanProperty.create("name");
		BeanProperty<JTextField, String> textProperty = BeanProperty.create("text");
		AutoBinding<MetricConfig, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, nameProperty, nameJTextField, textProperty);
		autoBinding.bind();
		//
		BeanProperty<MetricConfig, String> domainProperty = BeanProperty.create("domain");
		BeanProperty<JTextField, String> textProperty_1 = BeanProperty.create("text");
		AutoBinding<MetricConfig, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, domainProperty, domainJTextField, textProperty_1);
		autoBinding_1.bind();
		//
		BeanProperty<MetricConfig, Boolean> qualitativeProperty = BeanProperty.create("qualitative");
		BeanProperty<JCheckBox, Boolean> selectedProperty = BeanProperty.create("selected");
		AutoBinding<MetricConfig, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, qualitativeProperty, qualitativeJCheckBox, selectedProperty);
		autoBinding_2.bind();
		//
		BeanProperty<MetricConfig, String> exprProperty = BeanProperty.create("expr");
		BeanProperty<JTextField, String> textProperty_2 = BeanProperty.create("text");
		AutoBinding<MetricConfig, String, JTextField, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, exprProperty, exprJTextField, textProperty_2);
		autoBinding_3.bind();
		//
		BeanProperty<MetricConfig, String> descriptionProperty = BeanProperty.create("description");
		BeanProperty<JTextArea, String> textProperty_3 = BeanProperty.create("text");
		AutoBinding<MetricConfig, String, JTextArea, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, descriptionProperty, descriptionJTextArea, textProperty_3);
		autoBinding_4.bind();
		//
		BeanProperty<MetricConfig, MetricValueType> metricConfigBeanProperty = BeanProperty.create("type");
		BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
		AutoBinding<MetricConfig, MetricValueType, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, metricConfigBeanProperty, typeJComboBox, jComboBoxBeanProperty);
		autoBinding_5.bind();
		//
		BeanProperty<MetricConfig, Direction> metricConfigBeanProperty_1 = BeanProperty.create("direction");
		AutoBinding<MetricConfig, Direction, JComboBox, Object> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, metricConfigBeanProperty_1, directionJComboBox, jComboBoxBeanProperty);
		autoBinding_6.bind();
		//
		BeanProperty<MetricConfig, String> metricConfigBeanProperty_2 = BeanProperty.create("key");
		BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
		AutoBinding<MetricConfig, String, JTextField, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, metricConfig, metricConfigBeanProperty_2, keyJTextField, jTextFieldBeanProperty);
		autoBinding_7.bind();
		//
		BindingGroup bindingGroup = new BindingGroup();
		//
		bindingGroup.addBinding(autoBinding);
		bindingGroup.addBinding(autoBinding_1);
		bindingGroup.addBinding(autoBinding_2);
		bindingGroup.addBinding(autoBinding_3);
		bindingGroup.addBinding(autoBinding_4);
		bindingGroup.addBinding(autoBinding_5);
		bindingGroup.addBinding(autoBinding_6);
		bindingGroup.addBinding(autoBinding_7);
		return bindingGroup;
	}
}
