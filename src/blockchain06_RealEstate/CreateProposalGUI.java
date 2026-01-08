/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package blockchain06_RealEstate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import utils.Utils;

/**
 *
 * @author Tiago Paiva
 */
public class CreateProposalGUI extends javax.swing.JFrame {

    private RemoteNodeInterface node;
    private RealEstateUser myUser;

    /**
     * Construtor
     */
    public CreateProposalGUI(RemoteNodeInterface node, RealEstateUser user) {
        this.node = node;
        this.myUser = user;
        
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Colocar Imóvel à Venda");
        
        // Carregar as listas automaticamente
        loadUsers();
        loadMyWallet();
    }

    /**
     * 1. Carregar utilizadores da pasta data_user para a Dropdown
     */
    private void loadUsers() {
        cbTarget.removeAllItems();
        // Opção padrão: Venda pública
        cbTarget.addItem("MERCADO (Qualquer pessoa)");
        
        File folder = new File("data_user");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".pub")) {
                        String userName = f.getName().replace(".pub", "");
                        // Não adicionar o meu próprio nome
                        if (!userName.equalsIgnoreCase(myUser.getUserName())) {
                            cbTarget.addItem(userName);
                        }
                    }
                }
            }
        }
    }

    /**
     * 2. Ler a Blockchain para saber que casas EU tenho (saldo > 0)
     */
    private void loadMyWallet() {
        cbMyAssets.removeAllItems();
        
        if (node == null) return;
        
        try {
            Map<String, Integer> wallet = new HashMap<>();
            BlockChain bc = node.getBlockchain();
            String me = myUser.getUserName().trim();

            // Varrer a blockchain para calcular saldos
            for (Block b : bc.getBlocks()) {
                java.util.List transactions = b.getTransactions();
                if (transactions == null) continue;

                for (Object rawObj : transactions) {
                    try {
                        Object txObj = rawObj;
                        // Decodificar Base64 manualmente
                        if (rawObj instanceof String) {
                            byte[] data = java.util.Base64.getDecoder().decode((String) rawObj);
                            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                txObj = ois.readObject();
                            }
                        }

                        // Processar Tokens de Imóveis
                        if (txObj instanceof RealEstateTransaction) {
                            RealEstateTransaction tx = (RealEstateTransaction) txObj;
                            String assetFull = tx.getAssetName(); // Ex: "RWA-123:T3:Porto"
                            // Usamos o ID como chave, mas vamos guardar o nome completo para mostrar na combo
                            
                            // Truque: Separar ID para a chave do mapa
                            String[] parts = assetFull.split(":");
                            String assetID = parts[0]; 
                            
                            int amount = tx.getTokenAmount();
                            
                            if (tx.getTxtReceiver().trim().equals(me)) {
                                wallet.put(assetFull, wallet.getOrDefault(assetFull, 0) + amount);
                            }
                            if (tx.getTxtSender().trim().equals(me) && !tx.getTxtSender().equals(tx.getTxtReceiver())) {
                                wallet.put(assetFull, wallet.getOrDefault(assetFull, 0) - amount);
                            }
                        }
                        
                        // Processar Vendas Passadas (Se já vendi, não tenho)
                        if (txObj instanceof SaleAcceptance) {
                            SaleAcceptance sale = (SaleAcceptance) txObj;
                            // Se eu fui o vendedor, perdi o imóvel (simplificação: perde tudo)
                            if (sale.getSeller().trim().equals(me)) {
                                // Temos de encontrar a chave no mapa que começa com este ID
                                for(String key : wallet.keySet()){
                                    if(key.startsWith(sale.getPropertyID())){
                                        wallet.put(key, 0); // Saldo a zero
                                    }
                                }
                            }
                            // Se eu fui o comprador, ganhei
                            if (sale.getBuyer().trim().equals(me)) {
                                // (Num sistema real precisávamos de saber o nome completo, 
                                // aqui assumimos que entra com o ID base se não soubermos o resto)
                                // Para simplificar o GUI, focamos apenas no que já tenho registado
                            }
                        }
                        
                    } catch (Exception e) {}
                }
            }

            // Adicionar à Dropdown apenas os que têm saldo positivo
            boolean hasAssets = false;
            for (Map.Entry<String, Integer> entry : wallet.entrySet()) {
                if (entry.getValue() > 0) {
                    cbMyAssets.addItem(entry.getKey()); // Adiciona "RWA-123:T3:Porto"
                    hasAssets = true;
                }
            }
            
            if (!hasAssets) {
                cbMyAssets.addItem("Sem imóveis disponíveis");
                btConfirm.setEnabled(false);
            } else {
                btConfirm.setEnabled(true);
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbMyAssets = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbTarget = new javax.swing.JComboBox<>();
        btConfirm = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Vender Imóvel");

        jLabel2.setText("Selecione o Imóvel:");

        cbMyAssets.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Preço de Venda (€):");

        txtPrice.setText("1000000");

        jLabel4.setText("Destinatário (Comprador):");

        cbTarget.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btConfirm.setText("Publicar Venda");
        btConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConfirmActionPerformed(evt);
            }
        });

        btCancel.setText("Cancelar");
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        jLabel5.setText("Quantidade de tokens");

        txtAmount.setText("1000");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(80, 80, 80)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel2)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(63, 63, 63)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel3))))
                        .addGap(62, 62, 62)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cbMyAssets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cbTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addComponent(btConfirm)
                        .addGap(49, 49, 49)
                        .addComponent(btCancel)))
                .addContainerGap(228, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(90, 90, 90)
                .addComponent(jLabel1)
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cbMyAssets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btConfirm)
                    .addComponent(btCancel))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(552, 50));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 552, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btConfirmActionPerformed
        // TODO add your handling code here:
        try {
            String selectedAssetFull = (String) cbMyAssets.getSelectedItem();
            if (selectedAssetFull == null) return;
            
            // Separar ID
            String assetID = selectedAssetFull.split(":")[0];

            // 1. Ler valores
            double price = Double.parseDouble(txtPrice.getText());
            int amountToSell = Integer.parseInt(txtAmount.getText()); // Ler quantidade

            // 2. Validar se tenho tokens suficientes
            // (Nota: Precisas de garantir que o loadMyWallet preencheu o mapa currentBalances ou ir buscar de novo)
            // Para simplificar, vamos confiar no utilizador ou fazer uma verificação rápida se tiveres o saldo acessível
            
            if (amountToSell <= 0) {
                 JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que 0.");
                 return;
            }

            // 3. Criar Proposta
            String target = (String) cbTarget.getSelectedItem();
            if (target.startsWith("MERCADO")) target = "MERCADO";

            SaleProposal proposal = new SaleProposal(
                myUser, 
                assetID, 
                target, 
                price, 
                amountToSell, // Passar a quantidade
                SaleProposal.TYPE_SELL_OFFER
            );

            node.addTransaction(Utils.ObjectToBase64(proposal));
            JOptionPane.showMessageDialog(this, "Oferta de " + amountToSell + " tokens criada!");
            this.dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }//GEN-LAST:event_btConfirmActionPerformed

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btCancelActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btConfirm;
    private javax.swing.JComboBox<String> cbMyAssets;
    private javax.swing.JComboBox<String> cbTarget;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtPrice;
    // End of variables declaration//GEN-END:variables
}
