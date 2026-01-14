/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import core.Block;
import core.BlockChain;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.RealEstateUser;
import model.transaction.RentalAcceptanceTransaction;
import model.transaction.RentalTransaction;
import network.RemoteNodeInterface;
import utils.Utils;

/**
 *
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class TenantContractsGUI extends javax.swing.JFrame {

    // Variáveis de instância para gestão da janela e dados
    private RemoteNodeInterface node;
    private RealEstateUser myUser;
    private DefaultTableModel model;

    /**
     * Construtor da interface de Contratos de Arrendamento (Inquilino).
     * Inicializa a janela e carrega automaticamente os contratos destinados ao utilizador logado.
     * * @param node Referência para o nó da rede (para ler a blockchain).
     * @param user O utilizador atual (Inquilino) que quer ver as suas ofertas.
     */
    public TenantContractsGUI(RemoteNodeInterface node, RealEstateUser user) {
        this.node = node;
        this.myUser = user;
        initComponents();
        
        // Configurações de janela
        setLocationRelativeTo(null);
        setTitle("Meus Contratos de Arrendamento - " + user.getUserName());
        
        // Configurar as colunas da tabela
        model = (DefaultTableModel) tblContracts.getModel();
        model.setColumnIdentifiers(new String[]{"ID Contrato", "Imóvel", "Valor (€)", "Estado"});
        
        // Iniciar carregamento dos dados
        loadContracts();
    }

    /**
     * Método principal que varre a Blockchain à procura de contratos.
     * <p>
     * Lógica de funcionamento:
     * 1. Cria duas listas: uma para as "Ofertas Recebidas" e outra para os "Contratos já Aceites".
     * 2. Percorre todos os blocos e todas as transações.
     * 3. Se encontrar uma {@code RentalTransaction} dirigida a mim, adiciona à lista de Ofertas.
     * 4. Se encontrar uma {@code RentalAcceptanceTransaction} assinada por mim, guarda o ID na lista de Aceites.
     * 5. No final, cruza as duas listas para determinar o estado (Pendente vs Ativo) e preenche a tabela.
     * </p>
     */
    private void loadContracts() {
        // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
        // :: 1. PREPARAÇÃO
        // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
        model.setRowCount(0); // Limpa a tabela visual
        
        if (node == null) return;

        try {
            // Listas para armazenar temporariamente os dados encontrados
            java.util.List<RentalTransaction> myOffers = new java.util.ArrayList<>();
            java.util.Set<String> acceptedContractIDs = new java.util.HashSet<>();
            String myName = myUser.getUserName().trim(); // Garante que não há espaços extra no nome

            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            // :: 2. LEITURA DA BLOCKCHAIN
            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            BlockChain bc = node.getBlockchain();
            java.util.List<Block> blocks = bc.getBlocks(); 

            // Varrer todos os blocos
            for (Block b : blocks) {
                java.util.List transactions = b.getTransactions(); 
                
                if (transactions == null) continue;

                // Varrer todas as transações dentro do bloco
                for (Object rawObj : transactions) {
                    try {
                        Object decoded = rawObj;

                        // Se a transação estiver serializada em Base64 (String), converte para Objeto
                        if (rawObj instanceof String) {
                            byte[] data = java.util.Base64.getDecoder().decode((String) rawObj);
                            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                decoded = ois.readObject();
                            }
                        }

                        // --- CASO A: É UMA PROPOSTA DE ALUGUER? ---
                        if (decoded instanceof RentalTransaction) {
                            RentalTransaction tx = (RentalTransaction) decoded;
                            // Verifica se o destinatário (Inquilino) sou eu
                            if (tx.getTenantName().trim().equals(myName)) {
                                myOffers.add(tx);
                            }
                        }
                        
                        // --- CASO B: É UMA ACEITAÇÃO DE ALUGUER? ---
                        else if (decoded instanceof RentalAcceptanceTransaction) {
                            RentalAcceptanceTransaction tx = (RentalAcceptanceTransaction) decoded;
                            // Verifica se fui eu que assinei a aceitação
                            if (tx.getTenantName().trim().equals(myName)) {
                                acceptedContractIDs.add(tx.getContractID());
                            }
                        }
                        
                    } catch (Exception e) {
                        // Transações inválidas ou de outros tipos são ignoradas silenciosamente
                    }
                }
            }

            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            // :: 3. APRESENTAÇÃO DOS DADOS
            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            // Percorre todas as ofertas encontradas e verifica o seu estado
            for (RentalTransaction offer : myOffers) {
                String status = "PENDENTE"; // Estado inicial: Proposta existe mas não foi assinada
                
                // Se o ID deste contrato estiver no conjunto de IDs aceites, muda o estado
                if (acceptedContractIDs.contains(offer.getContractID())) {
                    status = "ATIVO (Assinado)";
                }

                // Adiciona a linha à tabela
                model.addRow(new Object[]{
                    offer.getContractID(),
                    offer.getPropertyID(),
                    offer.getRentValue() + " €",
                    status
                });
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao carregar contratos: " + e.getMessage());
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
        tblContracts = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btClose = new javax.swing.JButton();
        btRefresh = new javax.swing.JButton();
        logo = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btSignContract = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        tblContracts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID Contrato", "Imóvel", "Valor", "Estado"
            }
        ));
        jScrollPane1.setViewportView(tblContracts);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(590, 50));

        btClose.setText("Fechar");
        btClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCloseActionPerformed(evt);
            }
        });

        btRefresh.setText("Atualizar");
        btRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRefreshActionPerformed(evt);
            }
        });

        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multimedia/logo-rwa-50.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 348, Short.MAX_VALUE)
                .addComponent(btRefresh)
                .addGap(18, 18, 18)
                .addComponent(btClose)
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(logo)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btClose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setPreferredSize(new java.awt.Dimension(590, 50));

        btSignContract.setText("Assinar Contrato");
        btSignContract.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSignContractActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(223, 223, 223)
                .addComponent(btSignContract)
                .addContainerGap(249, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btSignContract, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btSignContractActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSignContractActionPerformed
        // TODO add your handling code here:
        int row = tblContracts.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um contrato pendente.");
            return;
        }

        String status = (String) model.getValueAt(row, 3);
        if (!status.equals("PENDENTE")) {
            JOptionPane.showMessageDialog(this, "Este contrato já está assinado!");
            return;
        }

        String contractID = (String) model.getValueAt(row, 0);

        try {
            // Confirmar com o utilizador
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Deseja assinar digitalmente este contrato?\nEsta ação é irreversível na Blockchain.", 
                    "Assinar Contrato", JOptionPane.YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) return;

            // Criar a transação de Aceitação
            RentalAcceptanceTransaction acceptance = new RentalAcceptanceTransaction(myUser, contractID);

            // Enviar para a rede
            node.addTransaction(Utils.ObjectToBase64(acceptance));

            JOptionPane.showMessageDialog(this, "Contrato assinado e enviado para a Blockchain!");
            loadContracts(); // Atualizar tabela

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao assinar: " + e.getMessage());
        }
    }//GEN-LAST:event_btSignContractActionPerformed

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        // TODO add your handling code here:
        loadContracts();
    }//GEN-LAST:event_btRefreshActionPerformed

    private void btCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCloseActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btCloseActionPerformed

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btClose;
    private javax.swing.JButton btRefresh;
    private javax.swing.JButton btSignContract;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel logo;
    private javax.swing.JTable tblContracts;
    // End of variables declaration//GEN-END:variables
}
