/*
 * Copyright (c) 2014, the Cytoscape Consortium and the Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.cytoscape.ndex.internal.gui;

import javax.swing.JOptionPane;
import org.cytoscape.ndex.internal.strings.ErrorMessage;
import org.cytoscape.ndex.internal.server.Server;
import org.cytoscape.ndex.internal.server.ServerList;
import org.cytoscape.ndex.internal.singletons.ServerManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JCheckBox;


/**
 *
 */
public class SignInDialog extends javax.swing.JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private final Frame parent;
    
    /**
     * Creates new form AddNDExServer
     */
    public SignInDialog(Frame parent) {
        super(parent, true);
        this.parent = parent;
        initComponents();
        prepComponents();
    }
    
    /**
     * This method should be called when the mode is set to EDIT so that
     * existing server data may be modified.
     */
    private void populateFieldsWithSelectedServer()
    {
       
    }
    
    private void prepComponents()
    {
        getRootPane().setDefaultButton(save);    
    }
    
 

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jSplitPane1 = new javax.swing.JSplitPane();
        jTextField9 = new javax.swing.JTextField();
        username = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        save = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        password = new javax.swing.JPasswordField();

        jTextField9.setText("jTextField9");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sign in to NDEx");

        jLabel3.setText("Server:");

     //   jLabel4.setText("public.ndexbio.org");

        jLabel5.setText("Username:");

        jLabel6.setText("Password:");

        save.setText("Sign in");
        save.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveActionPerformed(evt);
            }
        });

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelActionPerformed(evt);
            }
        });
        
        Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
        
        
        jLabel4.setText(selectedServer.getUrl());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addContainerGap()
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addGroup(layout.createSequentialGroup()
        							.addComponent(jLabel3)
        							.addGap(42)
        							.addComponent(jLabel4))
        						.addGroup(layout.createSequentialGroup()
        							.addGroup(layout.createParallelGroup(Alignment.LEADING)
        								.addComponent(jLabel5)
        								.addComponent(jLabel6))
        							.addPreferredGap(ComponentPlacement.UNRELATED)
        							.addGroup(layout.createParallelGroup(Alignment.LEADING)
        								.addComponent(password, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
        								.addComponent(username, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)))))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(178)
        					.addComponent(cancel)
        					.addGap(31)
        					.addComponent(save)))
        			.addContainerGap())
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGap(16)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel3)
        				.addComponent(jLabel4))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel5)
        				.addComponent(username, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel6)
        				.addComponent(password, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(18)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(cancel)
        				.addComponent(save))
        			.addContainerGap())
        );
        getContentPane().setLayout(layout);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveActionPerformed
    {
    	
        Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
        
    //    if ( this.chckbxRemeberMe.isSelected()) {
            selectedServer.setUsername(username.getText().trim());
            selectedServer.setPassword(password.getText().trim());
   //     }	
        
        
        NdexRestClientModelAccessLayer mal = selectedServer.getModelAccessLayer();
        try {
			if( selectedServer.check(mal) )
			{
			    if( !selectedServer.isRunningNdexServer(mal) )
			    {
			        JOptionPane.showMessageDialog(this, ErrorMessage.notValidNdexServer, "Error", JOptionPane.ERROR_MESSAGE);
			        return;
			    }

			    try
			        {
			            NdexStatus status = mal.getServerStatus();
			            String description = "Number of Networks: " + status.getNetworkCount();
			            selectedServer.setDescription(description);
			            ServerList availableServers = ServerManager.INSTANCE.getAvailableServers();
			            availableServers.serverDescriptionChanged(selectedServer);
			            availableServers.save();
			            // TODO Fix this description later.
			        }
			        catch( IOException ex )
			        {
			            Logger.getLogger(ChangeNdexServerDialog.class.getName()).log(Level.SEVERE, null, ex);
			        }

			        String name = selectedServer.getName();
			        JOptionPane.showMessageDialog(this, "Successfully connect to: " + name, "Connected", JOptionPane.INFORMATION_MESSAGE);
			        this.setVisible(false);
			        selectedServer.setAuthenticated(true);
			}
			else
			{
			    // TODO Need error from server
			    JOptionPane.showMessageDialog(this, ErrorMessage.failedToConnect, "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (HeadlessException | IOException e) {
			JOptionPane.showMessageDialog(this,  ErrorMessage.failedToConnect + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
        
    }//GEN-LAST:event_saveActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelActionPerformed
    {//GEN-HEADEREND:event_cancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JPasswordField password;
    private javax.swing.JButton save;
    private javax.swing.JTextField username;
}
