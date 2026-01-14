/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.transaction;

import java.io.Serializable;
import model.RealEstateUser;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Representa a aceitação de uma proposta comercial (Compra ou Venda) de um imóvel.
 * <p>
 * Esta transação é gerada quando um utilizador decide aceitar uma `SaleProposal` existente no mercado.
 * É a peça final que concretiza a transferência de propriedade/tokens.
 * </p>
 * <p>
 * A classe é inteligente o suficiente para determinar quem é o Comprador e quem é o Vendedor
 * baseando-se no tipo da proposta original (se foi uma oferta de venda ou uma oferta de compra).
 * </p>
 *
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class SaleAcceptance implements Serializable {

    /** O ID da proposta original que está a ser aceite. */
    private String proposalID;
    
    /** * O ID do imóvel transacionado.
     * <p>Guardamos aqui explicitamente para facilitar a leitura e indexação (ex: no método loadWallet)
     * sem ter de ir buscar a proposta original à blockchain.</p>
     */
    private String propertyID; 
    
    /** Nome de utilizador que adquire o imóvel (Novo Dono). */
    private String buyer;      
    
    /** Nome de utilizador que aliena o imóvel (Antigo Dono). */
    private String seller;     
    
    /** Momento em que o negócio foi fechado (timestamp em milissegundos). */
    private long timestamp;
    
    /** Assinatura digital de quem aceitou o negócio, validando a transação. */
    private byte[] signature;

    /**
     * Construtor da Aceitação de Venda.
     * <p>
     * Este método determina automaticamente os papéis de Comprador e Vendedor:
     * <ul>
     * <li>Se a proposta original era de <b>VENDA</b> (alguém queria vender), quem aceita (signer) torna-se o <b>COMPRADOR</b>.</li>
     * <li>Se a proposta original era de <b>COMPRA</b> (alguém queria comprar), quem aceita (signer) é o dono atual, logo torna-se o <b>VENDEDOR</b>.</li>
     * </ul>
     * </p>
     *
     * @param signer           O utilizador que está a clicar no botão "Aceitar" (contém a chave privada).
     * @param originalProposal O objeto da proposta que está a ser aceite.
     * @throws Exception Se ocorrer erro na assinatura digital.
     */
    public SaleAcceptance(RealEstateUser signer, SaleProposal originalProposal) throws Exception {
        this.proposalID = originalProposal.getProposalID();
        this.propertyID = originalProposal.getPropertyID();
        this.timestamp = System.currentTimeMillis();
        
        // Definir quem é quem baseado no tipo de proposta
        if (originalProposal.getType().equals(SaleProposal.TYPE_SELL_OFFER)) {
            // Se era oferta de VENDA, quem aceita é o COMPRADOR
            this.buyer = signer.getUserName();
            this.seller = originalProposal.getProposer();
        } else {
            // Se era oferta de COMPRA, quem aceita é o DONO (VENDEDOR)
            this.seller = signer.getUserName();
            this.buyer = originalProposal.getProposer();
        }

        // Assinar a aceitação
        // Garante que o aceitante concordou com os termos (quem compra, quem vende, qual a proposta e quando)
        String data = proposalID + propertyID + buyer + seller + timestamp;
        this.signature = SecurityUtils.sign(data.getBytes(), signer.getPrivateKey());
    }

    /**
     * Obtém o ID da proposta aceite.
     * @return String ID.
     */
    public String getProposalID() { return proposalID; }

    /**
     * Obtém o ID do imóvel.
     * @return String ID.
     */
    public String getPropertyID() { return propertyID; }

    /**
     * Obtém o nome do Comprador (novo proprietário).
     * @return Nome de utilizador.
     */
    public String getBuyer() { return buyer; }

    /**
     * Obtém o nome do Vendedor (antigo proprietário).
     * @return Nome de utilizador.
     */
    public String getSeller() { return seller; }

    /** Versão de serialização para compatibilidade. */
    private static final long serialVersionUID = 2025102002L;
}