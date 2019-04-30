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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import com.fortify.integration.sonarqube.ssc.config.MetricsConfig;
import java.awt.Color;
import javax.swing.UIManager;

public class ExpressionHelpDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public ExpressionHelpDialog() {
		setTitle("Help - Metric Expressions");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTextPane txtpnDescription = new JTextPane();
			txtpnDescription.setBackground(UIManager.getColor("Label.background"));
			txtpnDescription.setEnabled(true);
			txtpnDescription.setContentType("text/html");
			txtpnDescription.setText(getExpressionsDescription());
			txtpnDescription.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(txtpnDescription);
			contentPanel.add(scrollPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		setVisible(true);
	}
	
	private String getExpressionsDescription() {
		StringBuffer sb = new StringBuffer("<html>");
		sb.append("<p>Expressions define how to calculate the metric values."
				+ " For general information about these expressions, see the"
				+ " Spring Expression Language (SpEL) reference at"
				+ " https://docs.spring.io/spring/docs/4.3.22.RELEASE/spring-framework-reference/html/expressions.html."
				+ "</p>"
				+ "<p>The following fields can be used in these expressions:</p>"
				+ "<ul>"
				+ "<li>All fields returned by the /api/v1/projectVersions endpoint</li>");
		for ( MetricsConfig.ExpressionField field : MetricsConfig.ExpressionField.values() ) {
			sb.append("<li>").append(field.name()).append(" - ").append(field.getDescription()).append("</li>");
		}
		sb.append("</ul>");
		sb.append("<p>Following are some example expressions: </p><ul>");
		sb.append("<li>name - Application version name</li>");
		sb.append("<li>project.name - Application name</li>");
		sb.append("<li>deepLink - Deep link to application version</li>");
		sb.append("<li>pi['Fortify Security Rating'] - Performance Indicator 'Fortify Security Rating' value</li>");
		sb.append("<li>var['CFPO'] - Variable 'CFPO' value</li>");
		sb.append("<li>var['CFPO']+var['HFPO'] - Sum of variable values 'CFPO' and 'HFPO'</li>");
		sb.append("</ul>");
		
		sb.append("</html>");
		return sb.toString();
	}

}
