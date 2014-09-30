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

import org.cytoscape.ndex.internal.server.Server;
import org.cytoscape.ndex.internal.singletons.NetworkManager;
import org.cytoscape.ndex.internal.singletons.ServerManager;
import org.cytoscape.ndex.internal.strings.ErrorMessage;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author David
 */
public class FindNetworksDialog extends javax.swing.JDialog {

    private List<NetworkSummary> networkSummaries;
    
    /**
     * Creates new form SimpleSearch
     */
    public FindNetworksDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        prepComponents();
    }
    
    public void setFocusOnDone()
    {
        this.getRootPane().setDefaultButton(done);
        done.requestFocus();
    }
    
    private void prepComponents()
    {
        this.setModal(true);
        this.getRootPane().setDefaultButton(search);
        
        Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
        serverName.setText( selectedServer.display() );
        
        NdexRestClientModelAccessLayer mal = selectedServer.getModelAccessLayer();
        boolean success = mal.checkCredential();
        if( success )
        {
            try
            {
                networkSummaries = mal.findNetworks("*", null, 0, 50);
            }
            catch (IOException ex)
            {         
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "ErrorX", JOptionPane.ERROR_MESSAGE);
                this.setVisible(false);
                return;
            }
            showSearchResults( ); 
        }
        else
        {
            JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "ErrorY", JOptionPane.ERROR_MESSAGE);
            this.setVisible(false);
        }
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

        jScrollPane1 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        selectNetwork = new javax.swing.JButton();
        done = new javax.swing.JButton();
        search = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        administeredByMe = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        serverName = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Find Networks");

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {"Network1", null, null, null, null},
                {"Network2", null, null, null, null},
                {"Network3", null, null, null, null},
                {"Network4", null, null, null, null},
                {"Network5", null, null, null, null},
                {"Network6", null, null, null, null},
                {"Network7", null, null, null, null},
                {"Network8", null, null, null, null},
                {"Network9", null, null, null, null},
                {"Network10", null, null, null, null},
                {"Network11", null, null, null, null},
                {"Network12", null, null, null, null},
                {"Network13", null, null, null, null},
                {"Network14", null, null, null, null},
                {"Network15", null, null, null, null},
                {"Network16", null, null, null, null},
                {"Network17", null, null, null, null},
                {"Network18", null, null, null, null},
                {"Network19", null, null, null, null},
                {"Network20", null, null, null, null}
            },
            new String []
            {
                "Network Title", "Format", "Admins", "Number of Edges", "Number of Nodes"
            }
        ));
        resultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(resultsTable);

        selectNetwork.setText("Select Network");
        selectNetwork.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                selectNetworkActionPerformed(evt);
            }
        });

        done.setText("Done Loading Networks");
        done.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                doneActionPerformed(evt);
            }
        });

        search.setText("Search");
        search.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                searchActionPerformed(evt);
            }
        });

        administeredByMe.setText("Administered By Me");

        jLabel1.setText("Results");

        jLabel2.setText("Current Server: ");

        serverName.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        serverName.setText("Server1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 864, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(searchField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(search))
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverName))
                            .addComponent(administeredByMe))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectNetwork, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(done, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(serverName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(search)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(administeredByMe)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectNetwork)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(done)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void doneActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_doneActionPerformed
    {//GEN-HEADEREND:event_doneActionPerformed
        setVisible(false);
    }//GEN-LAST:event_doneActionPerformed

    private void selectNetworkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectNetworkActionPerformed
    {//GEN-HEADEREND:event_selectNetworkActionPerformed
        int selectedIndex = resultsTable.getSelectedRow();
        if( selectedIndex == -1 )
        {
            JOptionPane.showMessageDialog(this, ErrorMessage.noNetworkSelected, "Error", JOptionPane.ERROR_MESSAGE);
        }
        NetworkSummary ns = networkSummaries.get(selectedIndex);
        NetworkManager.INSTANCE.setSelectedNetworkSummary(ns);
        
        org.cytoscape.ndex.internal.gui.DownloadNetworkDialog dialog = new org.cytoscape.ndex.internal.gui.DownloadNetworkDialog(this, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_selectNetworkActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_searchActionPerformed
    {//GEN-HEADEREND:event_searchActionPerformed
        Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
        
        String me = null;
        if( administeredByMe.isSelected() )
            me = selectedServer.getUsername();
        
        String searchText = searchField.getText();
        if( searchText.isEmpty() )
            searchText = "*";
        
        NdexRestClientModelAccessLayer mal = selectedServer.getModelAccessLayer();
        boolean success = mal.checkCredential();
        if( success )
        {
            try
            {
                networkSummaries = mal.findNetworks(searchText, me, 0, 50);
            }
            catch (IOException ex)
            {         
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            showSearchResults( ); 
        }
        else
        {
            JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "ErrorY", JOptionPane.ERROR_MESSAGE);
            this.setVisible(false);
        }
        
    }//GEN-LAST:event_searchActionPerformed

    private void showSearchResults()
    {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers( new String[]
        {
            "Network Title", "Format", "Admins", "Number of Edges", "Number of Nodes"
        });
        for( NetworkSummary networkSummary : networkSummaries )
        {
            Vector row = new Vector();
            
            //Network Title
            row.add(networkSummary.getName());
            //Format
            row.add("");
            //Admins
            row.add("");
            //Number of Edges
            row.add(networkSummary.getEdgeCount());
            //Number of Nodes
            row.add(networkSummary.getNodeCount());
            
            model.addRow(row);
        }
        resultsTable.setModel(model);
        resultsTable.getSelectionModel().setSelectionInterval(0, 0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FindNetworksDialog dialog = new FindNetworksDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox administeredByMe;
    private javax.swing.JButton done;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable resultsTable;
    private javax.swing.JButton search;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton selectNetwork;
    private javax.swing.JLabel serverName;
    // End of variables declaration//GEN-END:variables

    
}
