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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.fortify.integration.sonarqube.common.SourceSystem;
import com.fortify.integration.sonarqube.common.config.MetricsConfig;
import com.fortify.integration.sonarqube.common.config.MetricsConfig.MetricConfig;

import net.miginfocom.swing.MigLayout;

public class MetricsPanel extends JPanel {
	private JList listMetrics;
	private MetricDetailsPanel metricDetailsPanel;
	private final MetricsConfig metricsConfig;
	private final SourceSystem sourceSystem;

	public MetricsPanel(SourceSystem sourceSystem, MetricsConfig metricsConfig) {
		this.sourceSystem = sourceSystem;
		this.metricsConfig = metricsConfig;
		
		setLayout(new MigLayout("", "[150px:n,grow 20][grow]", "[][]"));
		
		JPanel panelMetricAddRemoveButtons = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelMetricAddRemoveButtons.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		add(panelMetricAddRemoveButtons, "cell 0 0,alignx right,aligny top");
		
		JButton btnMetricAdd = new JButton("Add");
		btnMetricAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				metricsConfig.addMetricConfig(new MetricConfig());
				listMetrics.setSelectedIndex(listMetrics.getModel().getSize()-1);
				listMetrics.repaint();
			}
		});
		btnMetricAdd.setHorizontalAlignment(SwingConstants.LEFT);
		panelMetricAddRemoveButtons.add(btnMetricAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				metricsConfig.removeMetricConfig(metricsConfig.getMetrics().get(listMetrics.getSelectedIndex()));
				listMetrics.repaint();
			}
		});
		btnRemove.setHorizontalAlignment(SwingConstants.LEFT);
		panelMetricAddRemoveButtons.add(btnRemove);
		
		listMetrics = new JList();
		listMetrics.setSelectedIndex(0);
		listMetrics.setBorder(new LineBorder(new Color(0, 0, 0)));
		listMetrics.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(listMetrics), "cell 0 1,grow");
		
		metricDetailsPanel = new MetricDetailsPanel(sourceSystem);
		add(metricDetailsPanel, "cell 1 1,grow");
		
		initDataBindings();
	}
	
	protected void initDataBindings() {
		//
		BeanProperty<MetricsConfig, List<MetricConfig>> metricsConfigBeanProperty = BeanProperty.create("metrics");
		JListBinding<MetricConfig, MetricsConfig, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, metricsConfig, metricsConfigBeanProperty, listMetrics);
		//
		BeanProperty<MetricConfig, String> metricConfigBeanProperty = BeanProperty.create("name");
		jListBinding.setDetailBinding(metricConfigBeanProperty);
		//
		jListBinding.bind();
		//
		BeanProperty<JList, Object> jListBeanProperty = BeanProperty.create("selectedElement");
		BeanProperty<MetricDetailsPanel, MetricConfig> metricDetailsPanelBeanProperty = BeanProperty.create("metricConfig");
		AutoBinding<JList, Object, MetricDetailsPanel, MetricConfig> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, listMetrics, jListBeanProperty, metricDetailsPanel, metricDetailsPanelBeanProperty);
		autoBinding_1.bind();
	}
}
