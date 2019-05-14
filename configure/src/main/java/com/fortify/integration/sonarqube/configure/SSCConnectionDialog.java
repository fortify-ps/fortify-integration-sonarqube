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
package com.fortify.integration.sonarqube.configure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.fortify.client.ssc.api.SSCApplicationVersionAPI;
import com.fortify.client.ssc.connection.SSCAuthenticatingRestConnection;

import net.miginfocom.swing.MigLayout;

public class SSCConnectionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldSSCURL;
	private JTextField textFieldUserName;
	private JPasswordField passwordField;
	private SSCAuthenticatingRestConnection connection = null;

	/**
	 * Create the dialog.
	 */
	public SSCConnectionDialog(JFrame frame) {
		super(frame);
		setModal(true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setTitle("Connect to SSC");
		setBounds(100, 100, 450, 165);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow]", "[][][]"));
		{
			JLabel lblSscUrl = new JLabel("SSC URL:");
			contentPanel.add(lblSscUrl, "cell 0 0,alignx left");
		}
		{
			textFieldSSCURL = new JTextField();
			textFieldSSCURL.setText("http://localhost:1820/ssc");
			contentPanel.add(textFieldSSCURL, "cell 1 0,growx");
			textFieldSSCURL.setColumns(10);
		}
		{
			JLabel lblUsername = new JLabel("Username:");
			contentPanel.add(lblUsername, "cell 0 1,alignx left");
		}
		{
			textFieldUserName = new JTextField();
			textFieldUserName.setText("ssc");
			contentPanel.add(textFieldUserName, "cell 1 1,growx");
			textFieldUserName.setColumns(10);
		}
		{
			JLabel lblPassword = new JLabel("Password:");
			contentPanel.add(lblPassword, "cell 0 2,alignx left");
		}
		{
			passwordField = new JPasswordField();
			passwordField.setText("Fortify123!");
			contentPanel.add(passwordField, "cell 1 2,growx");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							SSCAuthenticatingRestConnection newConn = SSCAuthenticatingRestConnection.builder()
									.baseUrl(textFieldSSCURL.getText())
									.userName(textFieldUserName.getText())
									.password(new String(passwordField.getPassword()))
									.build();
							newConn.api(SSCApplicationVersionAPI.class).queryApplicationVersions().maxResults(1).paramFields("id").build().getUnique();
							connection = newConn;
							setVisible(false);
						} catch ( Exception exc ) {
							JOptionPane.showMessageDialog(getOwner(), "Error connection to SSC:\n"+exc.getMessage());
						}
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public SSCAuthenticatingRestConnection getConnection() {
		if ( connection==null ) {
			setVisible(true);
		}
		return connection;
	}

}
