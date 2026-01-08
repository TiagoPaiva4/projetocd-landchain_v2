package blockchain06_RealEstate;

import java.io.Serializable;
import java.util.UUID;
import utils.SecurityUtils;
import utils.Utils;

public class SaleProposal implements Serializable {

    public static final String TYPE_SELL_OFFER = "VENDA"; 
    public static final String TYPE_BUY_OFFER = "COMPRA";

    private String proposalID;
    private String propertyID;
    private String proposer;
    private String targetUser;
    private double price;
    private int tokenAmount; // <--- NOVO CAMPO: Quantidade de Tokens
    private String type;
    private long timestamp;
    private byte[] signature;

    public SaleProposal(RealEstateUser user, String propertyID, String targetUser, double price, int tokenAmount, String type) throws Exception {
        this.proposalID = UUID.randomUUID().toString();
        this.proposer = user.getUserName();
        this.propertyID = propertyID;
        this.targetUser = (targetUser == null || targetUser.isEmpty()) ? "MERCADO" : targetUser;
        this.price = price;
        this.tokenAmount = tokenAmount; // Guardar quantidade
        this.type = type;
        this.timestamp = System.currentTimeMillis();

        // Assinar incluindo a quantidade
        String data = proposalID + propertyID + price + tokenAmount + type + this.targetUser;
        this.signature = SecurityUtils.sign(data.getBytes(), user.getPrivateKey());
    }

    // Getters
    public String getProposalID() { return proposalID; }
    public String getPropertyID() { return propertyID; }
    public String getProposer() { return proposer; }
    public String getTargetUser() { return targetUser; }
    public double getPrice() { return price; }
    public int getTokenAmount() { return tokenAmount; } // <--- NOVO GETTER
    public String getType() { return type; }

    @Override
    public String toString() {
        return type + ": " + tokenAmount + " tokens de " + propertyID + " por " + price + "€";
    }
    
    private static final long serialVersionUID = 2025102005L;
}