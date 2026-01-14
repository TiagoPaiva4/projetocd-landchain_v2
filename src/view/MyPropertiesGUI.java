/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import core.Block;
import core.BlockChain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import model.RealEstateUser;
import model.transaction.RealEstateTransaction;
import model.transaction.RentalAcceptanceTransaction;
import model.transaction.RentalTransaction;
import model.transaction.SaleAcceptance;
import model.transaction.SaleProposal;
import network.RemoteNodeInterface;

/**
 *
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class MyPropertiesGUI extends javax.swing.JFrame {

    private RemoteNodeInterface node;
    private RealEstateUser myUser;
    private javax.swing.table.DefaultTableModel model;

    /**
     * Construtor da interface gráfica de "Meus Ativos".
     * Inicializa a janela, configura a tabela e carrega os dados iniciais da blockchain.
     * * @param node Referência para o nó remoto (para aceder à blockchain).
     * @param user O utilizador autenticado que está a ver a sua carteira.
     */
    public MyPropertiesGUI(RemoteNodeInterface node, RealEstateUser user) {
        this.node = node;
        this.myUser = user;

        initComponents();

        // Configurações visuais: Centrar no ecrã e definir título
        setLocationRelativeTo(null);
        setTitle("Carteira de Ativos - " + user.getUserName());

        // Preparar o modelo da tabela para podermos adicionar linhas dinamicamente
        model = (javax.swing.table.DefaultTableModel) tblProperties.getModel();

        // Carregar dados automaticamente (string vazia = sem filtro, mostra tudo)
        loadWallet("");
    }

    /**
     * Método principal que lê a Blockchain, calcula o saldo de tokens do utilizador
     * e preenche a tabela visual. Aplica também um filtro de pesquisa.
     * <p>
     * A lógica funciona em 3 passos:
     * 1. Ler todas as transações para memória.
     * 2. Calcular entradas e saídas de tokens para construir o saldo (Wallet).
     * 3. Filtrar os resultados e adicionar à tabela.
     * </p>
     * * @param filtro Texto a pesquisar (por ID, Tipo ou Morada). Se vazio, mostra tudo.
     */
    private void loadWallet(String filtro) {
        // Limpa a tabela visual para não duplicar dados
        model.setRowCount(0);
        
        // Se não houver conexão ao nó, não faz nada
        if (node == null) {
            return;
        }

        // Normalizar o filtro para minúsculas para a busca não ser sensível a maiúsculas (Case Insensitive)
        String termoPesquisa = (filtro == null) ? "" : filtro.toLowerCase().trim();

        try {
            // --- ESTRUTURAS DE DADOS AUXILIARES ---
            // Mapa: ID do Imóvel -> Saldo de Tokens (Inteiro)
            Map<String, Integer> wallet = new HashMap<>();
            // Mapas para guardar detalhes do imóvel (Tipo e Morada) associados ao ID
            Map<String, String> typeMap = new HashMap<>();
            Map<String, String> addressMap = new HashMap<>();
            // Mapa para acesso rápido a propostas de venda (necessário para calcular transferências)
            Map<String, SaleProposal> allProposals = new HashMap<>();

            BlockChain bc = node.getBlockchain();
            String me = myUser.getUserName().trim();

            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            // :: PASSO 1: Ler e Deserializar todas as transações da Blockchain
            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            List<Object> allTransactions = new ArrayList<>();
            
            for (Block b : bc.getBlocks()) {
                if (b.getTransactions() != null) {
                    for (Object raw : b.getTransactions()) {
                        try {
                            Object obj = raw;
                            // Se o objeto vier como String Base64, convertemo-lo de volta para Objeto Java
                            if (raw instanceof String) {
                                byte[] data = java.util.Base64.getDecoder().decode((String) raw);
                                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                    obj = ois.readObject();
                                }
                            }
                            
                            // Adicionar à lista geral
                            allTransactions.add(obj);
                            
                            // Se for uma Proposta de Venda, guardamos num mapa à parte
                            // Isto é crucial porque a 'SaleAcceptance' (Venda Aceite) só tem o ID da proposta,
                            // e nós vamos precisar de saber quantos tokens essa proposta valia.
                            if (obj instanceof SaleProposal) {
                                SaleProposal p = (SaleProposal) obj;
                                allProposals.put(p.getProposalID(), p);
                            }
                        } catch (Exception e) {
                            // Ignorar transações corrompidas
                        }
                    }
                }
            }

            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            // :: PASSO 2: Calcular Saldos (Reconstruir o estado atual - UTXO Logic)
            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            for (Object txObj : allTransactions) {
                
                // --- CASO A: Registo Inicial ou Transferência Direta ---
                if (txObj instanceof RealEstateTransaction) {
                    RealEstateTransaction tx = (RealEstateTransaction) txObj;
                    
                    // O nome do ativo vem no formato "ID:Tipo:Morada"
                    String[] parts = tx.getAssetName().split(":");
                    String realID = parts[0];
                    
                    // Atualizar mapas de informação (sempre que encontramos uma transação, atualizamos os metadados)
                    typeMap.put(realID, parts.length > 1 ? parts[1] : "N/A");
                    addressMap.put(realID, parts.length > 2 ? parts[2] : "N/A");

                    // Se EU sou o destinatário -> Ganho tokens (+)
                    if (tx.getTxtReceiver().trim().equals(me)) {
                        wallet.put(realID, wallet.getOrDefault(realID, 0) + tx.getTokenAmount());
                    }
                    
                    // Se EU sou o remetente -> Perco tokens (-)
                    // (Verificamos !sender.equals(receiver) para ignorar o 'minting' inicial onde enviamos para nós próprios)
                    if (tx.getTxtSender().trim().equals(me) && !tx.getTxtSender().equals(tx.getTxtReceiver())) {
                        wallet.put(realID, wallet.getOrDefault(realID, 0) - tx.getTokenAmount());
                    }
                }
                
                // --- CASO B: Compra/Venda via Marketplace ---
                if (txObj instanceof SaleAcceptance) {
                    SaleAcceptance sale = (SaleAcceptance) txObj;
                    // Recuperar a proposta original para saber quantos tokens foram trocados
                    SaleProposal original = allProposals.get(sale.getProposalID());
                    
                    if (original != null) {
                        String assetID = original.getPropertyID();
                        int qtd = original.getTokenAmount();
                        
                        // Se fui eu que comprei -> Ganho tokens (+)
                        if (sale.getBuyer().trim().equals(me)) {
                            wallet.put(assetID, wallet.getOrDefault(assetID, 0) + qtd);
                        }
                        // Se fui eu que vendi -> Perco tokens (-)
                        if (sale.getSeller().trim().equals(me)) {
                            wallet.put(assetID, wallet.getOrDefault(assetID, 0) - qtd);
                        }
                    }
                }
            }

            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            // :: PASSO 3: Preencher Tabela com Filtro de Pesquisa
            // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
            for (Map.Entry<String, Integer> entry : wallet.entrySet()) {
                // Só mostramos imóveis onde temos saldo positivo (> 0)
                if (entry.getValue() > 0) {
                    String id = entry.getKey();
                    String tipo = typeMap.getOrDefault(id, "Desconhecido");
                    String morada = addressMap.getOrDefault(id, "Desconhecida");

                    // Criar uma "mega string" com todos os dados pesquisáveis deste imóvel
                    String dadosParaPesquisa = (id + " " + tipo + " " + morada).toLowerCase();

                    // LÓGICA DO FILTRO:
                    // Adiciona à tabela SE o campo de pesquisa estiver vazio
                    // OU SE os dados do imóvel contiverem o texto pesquisado.
                    if (termoPesquisa.isEmpty() || dadosParaPesquisa.contains(termoPesquisa)) {
                        model.addRow(new Object[]{
                            id,
                            tipo,
                            morada,           // Exibimos a morada recuperada
                            entry.getValue(), // Quantidade de tokens
                            "Livre"           // Estado (Pode ser melhorado futuramente)
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Procura na blockchain o estado de arrendamento de um imóvel específico.
     * Mostra um popup com os detalhes do contrato se existir.
     * * @param assetID O ID do imóvel a verificar.
     */
    private void viewContractDetails(String assetID) {
        try {
            BlockChain bc = node.getBlockchain();
            RentalTransaction foundContract = null;
            String status = "Pendente"; // Estado padrão: Proposta criada, mas não aceite

            // Percorrer a blockchain à procura de contratos relacionados com este imóvel
            for (Block b : bc.getBlocks()) {
                java.util.List transactions = b.getTransactions();
                if (transactions == null) {
                    continue;
                }

                for (Object raw : transactions) {
                    try {
                        Object obj = raw;
                        // Deserialização (igual ao loadWallet)
                        if (raw instanceof String) {
                            byte[] data = java.util.Base64.getDecoder().decode((String) raw);
                            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                obj = ois.readObject();
                            }
                        }

                        // 1. Encontrar a Proposta de Contrato (RentalTransaction)
                        if (obj instanceof RentalTransaction) {
                            RentalTransaction tx = (RentalTransaction) obj;
                            if (tx.getPropertyID().equals(assetID)) {
                                foundContract = tx; // Guarda o contrato mais recente encontrado (ou o último no loop)
                            }
                        }

                        // 2. Verificar se existe uma Aceitação (RentalAcceptanceTransaction)
                        // Só validamos a aceitação se já tivermos encontrado a proposta correspondente
                        if (obj instanceof RentalAcceptanceTransaction && foundContract != null) {
                            RentalAcceptanceTransaction acc = (RentalAcceptanceTransaction) obj;
                            // Se a aceitação corresponde ao ID do contrato que encontrámos
                            if (acc.getContractID().equals(foundContract.getContractID())) {
                                status = "ATIVO (Assinado)";
                            }
                        }
                    } catch (Exception e) {
                        // Ignora erros de cast/deserialização
                    }
                }
            }

            // Se encontrámos algum contrato, mostrar popup
            if (foundContract != null) {
                String info = "--- DETALHES DO CONTRATO ---\n\n"
                        + "ID Contrato: " + foundContract.getContractID() + "\n"
                        + "Imóvel: " + foundContract.getPropertyID() + "\n"
                        + "Senhorio: " + foundContract.getOwnerName() + "\n"
                        + "Inquilino: " + foundContract.getTenantName() + "\n"
                        + "Renda: " + foundContract.getRentValue() + " €\n"
                        + "Duração: " + foundContract.getDurationMonths() + " meses\n\n"
                        + "ESTADO ATUAL: " + status;

                javax.swing.JOptionPane.showMessageDialog(this, info, "Contrato de Arrendamento", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Opcional: Avisar que não há contratos
                // javax.swing.JOptionPane.showMessageDialog(this, "Não existem contratos para este imóvel.");
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
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btHistory = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        txtFiltro = new javax.swing.JTextField();

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
                false, false, false, false, false
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 7, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(526, 50));

        jButton1.setText("Fechar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btRefresh.setText("Atualizar");
        btRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRefreshActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multimedia/logo-rwa-50.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 308, Short.MAX_VALUE)
                .addComponent(btRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(16, 16, 16))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setPreferredSize(new java.awt.Dimension(526, 50));

        btHistory.setText("Ver Histórico");
        btHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btHistoryActionPerformed(evt);
            }
        });

        jButton2.setText("Contrato de Renda");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        txtFiltro.setText("Pesquisar...");
        txtFiltro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFiltroActionPerformed(evt);
            }
        });
        txtFiltro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFiltroKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(129, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                        .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        loadWallet("");
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

    private void btHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btHistoryActionPerformed

        int row = tblProperties.getSelectedRow();
        if (row == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecione uma propriedade na tabela!");
            return;
        }

        String selectedID = (String) model.getValueAt(row, 0);

        StringBuilder history = new StringBuilder();
        history.append("HISTÓRICO DO IMÓVEL: ").append(selectedID).append("\n");
        history.append("-------------------------------------------------\n\n");

        try {
            BlockChain bc = node.getBlockchain();

            for (Block b : bc.getBlocks()) {
                java.util.List transactions = b.getTransactions();
                if (transactions == null) {
                    continue;
                }

                for (Object raw : transactions) {
                    try {
                        Object tx = raw;
                        // Descodificar Base64 se necessário
                        if (raw instanceof String) {
                            byte[] data = java.util.Base64.getDecoder().decode((String) raw);
                            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data))) {
                                tx = ois.readObject();
                            }
                        }

                        // --- A. REGISTO ---
                        if (tx instanceof RealEstateTransaction) {
                            RealEstateTransaction rtx = (RealEstateTransaction) tx;
                            if (rtx.getAssetName().startsWith(selectedID)) {
                                history.append("[REGISTO] Criado por ").append(rtx.getTxtSender().trim())
                                        .append("\n          Info: ").append(rtx.getAssetName())
                                        .append("\n\n");
                            }
                        }

                        // --- B. VENDAS (MERCADO) ---
                        if (tx instanceof SaleProposal) {
                            SaleProposal prop = (SaleProposal) tx;
                            if (prop.getPropertyID().equals(selectedID)) {
                                history.append("[MERCADO] ").append(prop.getType())
                                        .append(" por ").append(prop.getProposer())
                                        .append("\n          Preço: ").append(prop.getPrice()).append("€")
                                        .append("\n\n");
                            }
                        }

                        if (tx instanceof SaleAcceptance) {
                            SaleAcceptance sale = (SaleAcceptance) tx;
                            if (sale.getPropertyID().equals(selectedID)) {
                                history.append(">>> VENDA CONCLUÍDA <<<\n")
                                        .append("          De: ").append(sale.getSeller())
                                        .append(" -> Para: ").append(sale.getBuyer())
                                        .append("\n\n");
                            }
                        }

                        // --- C. ALUGUERES (CORRIGIDO AQUI) ---
                        if (tx instanceof RentalTransaction) {
                            RentalTransaction rent = (RentalTransaction) tx;

                            // Verifica se o aluguer é sobre este imóvel
                            if (rent.getPropertyID().equals(selectedID)) {
                                history.append("[ALUGUER] Proposta de Contrato\n")
                                        .append("          Senhorio: ").append(rent.getOwnerName()) // Nome correto
                                        .append("\n          Inquilino: ").append(rent.getTenantName()) // Nome correto
                                        .append("\n          Valor: ").append(rent.getRentValue()).append("€") // Nome correto
                                        .append(" (" + rent.getDurationMonths() + " meses)")
                                        .append("\n\n");
                            }
                        }

                    } catch (Exception e) {
                    }
                }
            }

            // Mostrar Popup
            javax.swing.JTextArea textArea = new javax.swing.JTextArea(history.toString());
            textArea.setEditable(false);
            textArea.setRows(15);
            textArea.setColumns(50);

            javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(textArea);
            javax.swing.JOptionPane.showMessageDialog(this, scroll, "Histórico Completo", javax.swing.JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_btHistoryActionPerformed

    private void txtFiltroKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFiltroKeyReleased
        // TODO add your handling code here:
        loadWallet(txtFiltro.getText());
    }//GEN-LAST:event_txtFiltroKeyReleased

    private void txtFiltroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFiltroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFiltroActionPerformed

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
        //</editor-fold>

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btHistory;
    private javax.swing.JButton btRefresh;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProperties;
    private javax.swing.JTextField txtFiltro;
    // End of variables declaration//GEN-END:variables
}
