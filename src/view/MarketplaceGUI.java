/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import core.Block;
import core.BlockChain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.RealEstateUser;
import model.transaction.RealEstateTransaction;
import model.transaction.SaleAcceptance;
import model.transaction.SaleProposal;
import network.RemoteNodeInterface;
import utils.Utils;

/**
 *
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class MarketplaceGUI extends javax.swing.JFrame {

    private RemoteNodeInterface node;
    private RealEstateUser myUser;
    private DefaultTableModel model;
    
    // Lista para guardar as propostas em memória e saber qual é qual ao clicar na tabela
    private List<SaleProposal> displayedProposals = new ArrayList<>();

    public MarketplaceGUI(RemoteNodeInterface node, RealEstateUser user) {
        this.node = node;
        this.myUser = user;
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Mercado Imobiliário - " + user.getUserName());
        
        model = (DefaultTableModel) tblMarket.getModel();
        model.setColumnIdentifiers(new String[]{"ID Imóvel", "Tipo", "De (User)", "Para", "Preço (€)"});
        
        loadMarket("");
    }

    // O método agora aceita o texto do filtro
private void loadMarket(String filtro) {
    model.setRowCount(0);
    displayedProposals.clear();
    
    if (node == null) return;
    
    // Normalizar texto de pesquisa
    String termo = (filtro == null) ? "" : filtro.toLowerCase().trim();

    try {
        BlockChain bc = node.getBlockchain();
        
        // Estruturas de dados auxiliares
        HashSet<String> acceptedIDs = new HashSet<>();
        List<SaleProposal> allProposals = new ArrayList<>();
        
        // Mapa para ligar ID do Imovel -> Morada/Rua
        java.util.Map<String, String> mapImovelRua = new java.util.HashMap<>();

        // --- CICLO ÚNICO: Ler Blockchain para encher as listas e mapas ---
        for (Block b : bc.getBlocks()) {
            List transactions = b.getTransactions();
            if (transactions == null) continue;
            
            for (Object raw : transactions) {
                try {
                    // Deserializar objeto
                    Object obj = raw;
                    if (raw instanceof String) {
                        byte[] data = java.util.Base64.getDecoder().decode((String) raw);
                        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                            obj = ois.readObject();
                        }
                    }
                    
                    // 1. Se for Registo de Imóvel, guardar a Rua
                    if (obj instanceof RealEstateTransaction) {
                        RealEstateTransaction tx = (RealEstateTransaction) obj;
                        // O nome vem como "ID:Tipo:Rua"
                        String[] parts = tx.getAssetName().split(":");
                        if (parts.length >= 3) {
                            mapImovelRua.put(parts[0], parts[2]); // Guarda ID e Rua
                        } else {
                            mapImovelRua.put(parts[0], "N/A");
                        }
                    }

                    // 2. Se for Proposta, adicionar à lista
                    if (obj instanceof SaleProposal) {
                        allProposals.add((SaleProposal) obj);
                    }
                    
                    // 3. Se for Aceitação, marcar como vendida
                    if (obj instanceof SaleAcceptance) {
                        acceptedIDs.add(((SaleAcceptance) obj).getProposalID());
                    }
                    
                } catch (Exception e) {}
            }
        }

        // --- FILTRAGEM E EXIBIÇÃO ---
        for (SaleProposal p : allProposals) {
            // Se já foi aceite, ignorar
            if (acceptedIDs.contains(p.getProposalID())) continue;

            // Obter a rua através do mapa que criámos
            String ruaDoImovel = mapImovelRua.getOrDefault(p.getPropertyID(), "Desconhecida");

            // Lógica de Visibilidade (Público, Para Mim ou Meu)
            boolean isPublic = p.getTargetUser().equals("MERCADO");
            boolean isForMe = p.getTargetUser().equals(myUser.getUserName());
            boolean isMine = p.getProposer().equals(myUser.getUserName());

            if (isPublic || isForMe || isMine) {
                
                // --- AQUI ESTÁ O FILTRO DE PESQUISA ---
                // Cria uma "mega string" com todos os dados pesquisáveis
                String dadosPesquisaveis = (
                        p.getPropertyID() + " " +   // ID
                        p.getProposer() + " " +     // Quem Vende
                        p.getTargetUser() + " " +   // Para quem
                        ruaDoImovel                 // A Rua!
                        ).toLowerCase();

                // Se o filtro estiver vazio OU se os dados conterem o texto
                if (termo.isEmpty() || dadosPesquisaveis.contains(termo)) {
                    
                    displayedProposals.add(p);
                    
                    model.addRow(new Object[]{
                        p.getPropertyID(),
                        p.getTokenAmount(),
                        p.getType(),
                        p.getProposer(),
                        p.getTargetUser(),
                        p.getPrice() + " €",
                        ruaDoImovel // <-- Nova coluna útil
                    });
                }
            }
        }

    } catch (Exception e) { e.printStackTrace(); }
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
        tblMarket = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        btClose = new javax.swing.JButton();
        logo = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btAccept = new javax.swing.JButton();
        btCreateProposal = new javax.swing.JButton();
        txtPesquisa = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tblMarket.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID Imóvel", "Qtd Tokens", "Tipo Proposta", "De", "Para", "Preço", "Localização"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblMarket);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(630, 50));

        jButton2.setText("Atualizar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        btClose.setText("Fechar");
        btClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCloseActionPerformed(evt);
            }
        });

        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multimedia/logo-rwa-50.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 400, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btClose)
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btClose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(logo)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setPreferredSize(new java.awt.Dimension(630, 50));

        btAccept.setText("Aceitar Negócio");
        btAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAcceptActionPerformed(evt);
            }
        });

        btCreateProposal.setText("Vender/Comprar Imóvel");
        btCreateProposal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCreateProposalActionPerformed(evt);
            }
        });

        txtPesquisa.setText("Pesquisar...");
        txtPesquisa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesquisaKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btCreateProposal, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btAccept)
                .addContainerGap(89, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAccept, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btCreateProposal, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 15, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAcceptActionPerformed
        int row = tblMarket.getSelectedRow();
        if (row == -1) return;

        SaleProposal selectedProposal = displayedProposals.get(row);

        // Não posso aceitar a minha própria proposta
        if (selectedProposal.getProposer().equals(myUser.getUserName())) {
            JOptionPane.showMessageDialog(this, "Não pode aceitar a sua própria proposta.");
            return;
        }

        try {
            int confirm = JOptionPane.showConfirmDialog(this, "Tem a certeza que quer aceitar este negócio?");
            if (confirm != JOptionPane.YES_OPTION) return;

            // Criar aceitação
            SaleAcceptance acceptance = new SaleAcceptance(myUser, selectedProposal);
            
            // Enviar
            node.addTransaction(Utils.ObjectToBase64(acceptance));
            JOptionPane.showMessageDialog(this, "Negócio fechado! Aguarde a mineração.");
            this.dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }//GEN-LAST:event_btAcceptActionPerformed

    private void btCreateProposalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCreateProposalActionPerformed
        if (node != null) {
            new CreateProposalGUI(node, myUser).setVisible(true); // Abre a nova janela unificada de criação de propostas
        }
    }//GEN-LAST:event_btCreateProposalActionPerformed

    private void btCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btCloseActionPerformed

    private void txtPesquisaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesquisaKeyReleased
        loadMarket(txtPesquisa.getText());
    }//GEN-LAST:event_txtPesquisaKeyReleased

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        loadMarket("");
    }//GEN-LAST:event_jButton2ActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAccept;
    private javax.swing.JButton btClose;
    private javax.swing.JButton btCreateProposal;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel logo;
    private javax.swing.JTable tblMarket;
    private javax.swing.JTextField txtPesquisa;
    // End of variables declaration//GEN-END:variables
}
