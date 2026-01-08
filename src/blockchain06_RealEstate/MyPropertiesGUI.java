/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package blockchain06_RealEstate;

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
        // 1. Limpar a tabela antes de carregar
        model.setRowCount(0);
        
        if (node == null) return;

        try {
            // --- ESTRUTURAS DE DADOS ---
            // Mapa para o Saldo: ID -> Quantidade
            java.util.Map<String, Integer> wallet = new java.util.HashMap<>();
            // Mapas para Detalhes: ID -> Texto
            java.util.Map<String, String> typeMap = new java.util.HashMap<>();
            java.util.Map<String, String> addressMap = new java.util.HashMap<>();
            // Mapas para Contratos
            java.util.Map<String, RentalTransaction> myContracts = new java.util.HashMap<>(); // ContractID -> Transação
            java.util.Set<String> acceptedContracts = new java.util.HashSet<>(); // IDs de contratos aceites

            // Obter blocos e nome do utilizador limpo
            java.util.List<Block> blocks = node.getBlockchain().getBlocks();
            String me = myUser.getUserName().trim();

            // 2. VARRER A BLOCKCHAIN
            for (Block b : blocks) {
                java.util.List transactions = b.getTransactions();
                if (transactions == null) continue;

                for (Object rawObj : transactions) {
                    try {
                        Object txObj = rawObj;
                        
                        // Deserializar manualmente (Segurança contra falta de Utils)
                        if (rawObj instanceof String) {
                            byte[] data = java.util.Base64.getDecoder().decode((String) rawObj);
                            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                txObj = ois.readObject();
                            }
                        }

                        // === A. PROCESSAR IMÓVEIS (TOKENS) ===
                        if (txObj instanceof RealEstateTransaction) {
                            RealEstateTransaction tx = (RealEstateTransaction) txObj;
                            
                            // 1. Ler o nome completo (Ex: "RWA-123:T3:Porto")
                            String rawName = tx.getAssetName();
                            String[] parts = rawName.split(":");
                            
                            // 2. Separar as partes
                            String realID = parts[0]; // O ID é sempre o primeiro
                            String type = (parts.length > 1) ? parts[1] : "N/A"; // Se não existir, mete N/A
                            String addr = (parts.length > 2) ? parts[2] : "N/A";
                            
                            // 3. Guardar detalhes nos mapas (sobrepõe antigos, fica o mais recente)
                            typeMap.put(realID, type);
                            addressMap.put(realID, addr);

                            // 4. Calcular Saldos
                            int amount = tx.getTokenAmount();
                            
                            // Se recebi -> SOMA
                            if (tx.getTxtReceiver().trim().equals(me)) {
                                wallet.put(realID, wallet.getOrDefault(realID, 0) + amount);
                            }
                            
                            // Se enviei -> SUBTRAI (Exceto se for para mim mesmo/Minting)
                            if (tx.getTxtSender().trim().equals(me) && !tx.getTxtSender().equals(tx.getTxtReceiver())) {
                                wallet.put(realID, wallet.getOrDefault(realID, 0) - amount);
                            }
                        }

                        // === B. PROCESSAR CONTRATOS (ESTADOS) ===
                        if (txObj instanceof RentalTransaction) {
                            RentalTransaction rtx = (RentalTransaction) txObj;
                            // Se eu sou o dono, guardo este contrato
                            if (rtx.getOwnerName().trim().equals(me)) {
                                myContracts.put(rtx.getContractID(), rtx);
                            }
                        }
                        
                        if (txObj instanceof RentalAcceptanceTransaction) {
                            RentalAcceptanceTransaction acc = (RentalAcceptanceTransaction) txObj;
                            // Guardo o ID do contrato que foi aceite
                            acceptedContracts.add(acc.getContractID());
                        }

                    } catch (Exception e) {
                        // Ignorar erros de leitura em transações individuais
                    }
                }
            }

            // 3. CALCULAR ESTADO FINAL DOS CONTRATOS
            // Mapa final: PropertyID -> Estado Texto
            java.util.Map<String, String> propertyStatus = new java.util.HashMap<>();
            
            for (RentalTransaction rtx : myContracts.values()) {
                if (acceptedContracts.contains(rtx.getContractID())) {
                    propertyStatus.put(rtx.getPropertyID(), "Arrendado (" + rtx.getTenantName() + ")");
                } else {
                    // Só marca como pendente se ainda não estiver marcado como arrendado por outro contrato mais recente
                    propertyStatus.putIfAbsent(rtx.getPropertyID(), "Pendente");
                }
            }

            // 4. PREENCHER A TABELA
            for (java.util.Map.Entry<String, Integer> entry : wallet.entrySet()) {
                // Só mostramos se tivermos tokens (saldo positivo)
                if (entry.getValue() > 0) {
                    String id = entry.getKey();
                    
                    model.addRow(new Object[]{
                        id,                                         // Coluna 0: ID
                        typeMap.getOrDefault(id, "Antigo"),         // Coluna 1: Tipo
                        addressMap.getOrDefault(id, "Desconhecida"),// Coluna 2: Morada
                        entry.getValue(),                           // Coluna 3: Tokens
                        propertyStatus.getOrDefault(id, "Livre")    // Coluna 4: Estado
                    });
                }
            }

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao carregar carteira: " + ex.getMessage());
            ex.printStackTrace();
        }
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
