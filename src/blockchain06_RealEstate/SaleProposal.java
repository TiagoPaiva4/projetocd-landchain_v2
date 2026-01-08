/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchain06_RealEstate;

import java.io.Serializable;
import java.util.UUID;
import utils.SecurityUtils;
import utils.Utils;
/**
 *
 * @author Tiago Paiva
 */

public class SaleProposal implements Serializable {

    public static final String TYPE_SELL_OFFER = "VENDA"; // Dono quer vender
    public static final String TYPE_BUY_OFFER = "COMPRA"; // Alguém quer comprar

    private String proposalID;
    private String propertyID;
    private String proposer;      // Quem criou a proposta (Dono ou Interessado)
    private String targetUser;    // Específico (ex: "Ana") ou "MERCADO" (qualquer um)
    private double price;
    private String type;          // VENDA ou COMPRA
    private long timestamp;
    private byte[] signature;

    public SaleProposal(RealEstateUser user, String propertyID, String targetUser, double price, String type) throws Exception {
        this.proposalID = UUID.randomUUID().toString();
        this.proposer = user.getUserName();
        this.propertyID = propertyID;
        this.targetUser = (targetUser == null || targetUser.isEmpty()) ? "MERCADO" : targetUser;
        this.price = price;
        this.type = type;
        this.timestamp = System.currentTimeMillis();

        // Assinar a proposta
        String data = proposalID + propertyID + price + type + this.targetUser;
        this.signature = SecurityUtils.sign(data.getBytes(), user.getPrivateKey());
    }

    public String getProposalID() { return proposalID; }
    public String getPropertyID() { return propertyID; }
    public String getProposer() { return proposer; }
    public String getTargetUser() { return targetUser; }
    public double getPrice() { return price; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return type + ": " + propertyID + " por " + price + "€ (" + proposer + ")";
    }
    
    private static final long serialVersionUID = 2025102001L;
}