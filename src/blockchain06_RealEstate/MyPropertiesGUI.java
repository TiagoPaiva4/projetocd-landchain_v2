/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package blockchain06_RealEstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Tiago Paiva
 */
public class MyPropertiesGUI extends javax.swing.JFrame {
    
    private RemoteNodeInterface node;
    private RealEstateUser myUser;
    private javax.swing.table.DefaultTableModel model;

    /**
     * Creates new form MyPropertiesGUI
     */
   public MyPropertiesGUI(RemoteNodeInterface node, RealEstateUser user) {
        this.node = node;
        this.myUser = user;
        
        initComponents();
        
        // Configurações visuais
        setLocationRelativeTo(null);
        setTitle("Carteira de Ativos - " + user.getUserName());
        
        // Preparar a tabela
        model = (javax.swing.table.DefaultTableModel) tblProperties.getModel();
        
        // Carregar dados automaticamente
        loadWallet();
    }
   
   private void loadWallet() {
        model.setRowCount(0);
        if (node == null) return;

        try {
            Map<String, Integer> wallet = new HashMap<>();
            Map<String, String> typeMap = new HashMap<>();
            Map<String, String> addressMap = new HashMap<>();
            Map<String, SaleProposal> allProposals = new HashMap<>(); // Guardar propostas por ID
            
            BlockChain bc = node.getBlockchain();
            String me = myUser.getUserName().trim();

            // PASSO 1: Ler todas as transações para memória
            // Precisamos de ler primeiro as propostas para depois usar nas aceitações
            List<Object> allTransactions = new ArrayList<>();
            
            for (Block b : bc.getBlocks()) {
                if (b.getTransactions() != null) {
                    for (Object raw : b.getTransactions()) {
                        try {
                            Object obj = raw;
                            if (raw instanceof String) {
                                byte[] data = java.util.Base64.getDecoder().decode((String) raw);
                                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                    obj = ois.readObject();
                                }
                            }
                            allTransactions.add(obj);
                            
                            // Se for proposta, guarda no mapa para referência futura
                            if (obj instanceof SaleProposal) {
                                SaleProposal p = (SaleProposal) obj;
                                allProposals.put(p.getProposalID(), p);
                            }
                        } catch (Exception e) {}
                    }
                }
            }

            // PASSO 2: Calcular Saldos
            for (Object txObj : allTransactions) {
                // A. Registo Inicial (Minting)
                if (txObj instanceof RealEstateTransaction) {
                    RealEstateTransaction tx = (RealEstateTransaction) txObj;
                    String[] parts = tx.getAssetName().split(":");
                    String realID = parts[0];
                    typeMap.put(realID, parts.length > 1 ? parts[1] : "N/A");
                    addressMap.put(realID, parts.length > 2 ? parts[2] : "N/A");
                    
                    if (tx.getTxtReceiver().trim().equals(me)) {
                        wallet.put(realID, wallet.getOrDefault(realID, 0) + tx.getTokenAmount());
                    }
                    if (tx.getTxtSender().trim().equals(me) && !tx.getTxtSender().equals(tx.getTxtReceiver())) {
                        wallet.put(realID, wallet.getOrDefault(realID, 0) - tx.getTokenAmount());
                    }
                }
                
                // B. Transferências (Venda/Compra de Tokens)
                if (txObj instanceof SaleAcceptance) {
                    SaleAcceptance sale = (SaleAcceptance) txObj;
                    SaleProposal original = allProposals.get(sale.getProposalID());
                    
                    if (original != null) {
                        String assetID = original.getPropertyID();
                        int qtd = original.getTokenAmount(); // <--- USA A QUANTIDADE DA PROPOSTA
                        
                        // Se fui eu que comprei (Ganho tokens)
                        if (sale.getBuyer().trim().equals(me)) {
                            wallet.put(assetID, wallet.getOrDefault(assetID, 0) + qtd);
                            // Tentar recuperar info do imóvel se não tiver
                            // (Normalmente já temos se alguém registou, senão fica N/A)
                        }
                        
                        // Se fui eu que vendi (Perco tokens)
                        if (sale.getSeller().trim().equals(me)) {
                            wallet.put(assetID, wallet.getOrDefault(assetID, 0) - qtd);
                        }
                    }
                }
            }

            // PASSO 3: Preencher Tabela
            for (Map.Entry<String, Integer> entry : wallet.entrySet()) {
                if (entry.getValue() > 0) {
                    String id = entry.getKey();
                    model.addRow(new Object[]{
                        id,
                        typeMap.getOrDefault(id, "Desconhecido"),
                        addressMap.getOrDefault(id, "Desconhecida"),
                        entry.getValue(), // Agora mostra p.ex: 100, 500, ou 1000
                        "Livre" // Simplificação do estado
                    });
                }
            }

        } catch (Exception e) { e.printStackTrace(); }
    }
   
   private void viewContractDetails(String assetID) {
        try {
            // Vamos procurar o contrato mais recente para este imóvel
            BlockChain bc = node.getBlockchain();
            RentalTransaction foundContract = null;
            String status = "Pendente";
            
            for (Block b : bc.getBlocks()) {
                 java.util.List transactions = b.getTransactions();
                 if (transactions == null) continue;
                 
                 for (Object raw : transactions) {
                     try {
                        Object obj = raw;
                        if (raw instanceof String) {
                            byte[] data = java.util.Base64.getDecoder().decode((String) raw);
                            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                obj = ois.readObject();
                            }
                        }
                        
                        if (obj instanceof RentalTransaction) {
                            RentalTransaction tx = (RentalTransaction) obj;
                            if (tx.getPropertyID().equals(assetID)) {
                                foundContract = tx;
                            }
                        }
                        
                        // Verificar se está assinado
                        if (obj instanceof RentalAcceptanceTransaction && foundContract != null) {
                            RentalAcceptanceTransaction acc = (RentalAcceptanceTransaction) obj;
                            if (acc.getContractID().equals(foundContract.getContractID())) {
                                status = "ATIVO (Assinado)";
                            }
                        }
                     } catch(Exception e) {}
                 }
            }
            
            if (foundContract != null) {
                String info = "--- DETALHES DO CONTRATO ---\n\n" +
                              "ID Contrato: " + foundContract.getContractID() + "\n" +
                              "Imóvel: " + foundContract.getPropertyID() + "\n" +
                              "Senhorio: " + foundContract.getOwnerName() + "\n" +
                              "Inquilino: " + foundContract.getTenantName() + "\n" +
                              "Renda: " + foundContract.getRentValue() + " €\n" +
                              "Duração: " + foundContract.getDurationMonths() + " meses\n\n" +
                              "ESTADO ATUAL: " + status;
                
                javax.swing.JOptionPane.showMessageDialog(this, info, "Contrato de Arrendamento", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProperties = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        btRefresh = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tblProperties.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID do Imóvel", "Tipo", "Morada", "Tokens", "Estado (Clique para Ver)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProperties.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPropertiesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProperties);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(526, 50));

        jButton1.setText("Fechar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btRefresh.setText("atualizar");
        btRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRefreshActionPerformed(evt);
            }
        });

        jButton2.setText("Contrato de Renda");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(99, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(btRefresh)
                .addGap(49, 49, 49)
                .addComponent(jButton1)
                .addGap(85, 85, 85))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(btRefresh)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        loadWallet();
    }//GEN-LAST:event_btRefreshActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        // 1. Verificar se o utilizador selecionou uma linha na tabela
        int row = tblProperties.getSelectedRow();
        
        if (row == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, selecione um imóvel na tabela.");
            return;
        }

        // 2. Obter o ID do imóvel (Assumindo que o ID está na Coluna 0)
        String selectedAssetID = (String) model.getValueAt(row, 0);

        // 3. Abrir a janela de Renda
        // Passamos o 'node', o 'user' logado e o ID da casa selecionada
        new CreateRentalGUI(node, myUser, selectedAssetID).setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void tblPropertiesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPropertiesMouseClicked
        int row = tblProperties.getSelectedRow();
        int col = tblProperties.getSelectedColumn();
        
        // Se clicar na coluna 2 (Estado) e o estado não for "Livre"
        if (row != -1 && col == 2) {
            String status = (String) tblProperties.getValueAt(row, 2);
            String assetID = (String) tblProperties.getValueAt(row, 0);
            
            if (!status.equals("Livre")) {
                // Abrir janela de detalhes
                viewContractDetails(assetID);
            }
        }
    }//GEN-LAST:event_tblPropertiesMouseClicked

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
            java.util.logging.Logger.getLogger(MyPropertiesGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MyPropertiesGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MyPropertiesGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MyPropertiesGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btRefresh;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProperties;
    // End of variables declaration//GEN-END:variables
}
