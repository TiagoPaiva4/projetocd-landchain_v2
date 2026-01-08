/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchain06_RealEstate;

import java.io.Serializable;
import utils.SecurityUtils;
import utils.Utils;
/**
 *
 * @author Tiago Paiva
 */

public class SaleAcceptance implements Serializable {

    private String proposalID;
    private String propertyID; // Guardamos para facilitar a leitura no loadWallet
    private String buyer;      // Quem fica com a casa (Novo Dono)
    private String seller;     // Quem era o dono (Antigo Dono)
    private long timestamp;
    private byte[] signature;

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
        String data = proposalID + propertyID + buyer + seller + timestamp;
        this.signature = SecurityUtils.sign(data.getBytes(), signer.getPrivateKey());
    }

    public String getProposalID() { return proposalID; }
    public String getPropertyID() { return propertyID; }
    public String getBuyer() { return buyer; }
    public String getSeller() { return seller; }

    private static final long serialVersionUID = 2025102002L;
}