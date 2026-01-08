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
public class RentalTransaction implements Serializable {

    private String contractID; // ID Único do contrato
    private String propertyID;
    private String ownerName;
    private String tenantName;
    private double rentValue;
    private int durationMonths;
    private long timestamp;
    private byte[] signature;

    public RentalTransaction(RealEstateUser owner, String propertyID, String tenantName, double rentValue, int months) throws Exception {
        this.contractID = UUID.randomUUID().toString(); // Gera um código único (ex: 123e4567-e89b...)
        this.ownerName = owner.getUserName();
        this.propertyID = propertyID;
        this.tenantName = tenantName;
        this.rentValue = rentValue;
        this.durationMonths = months;
        this.timestamp = System.currentTimeMillis();

        // O dono assina o ID do contrato e os termos
        byte[] data = Utils.concatenate(contractID.getBytes(), propertyID.getBytes());
        data = Utils.concatenate(data, tenantName.getBytes());
        data = Utils.concatenate(data, Utils.doubleToBytes(rentValue));
        
        this.signature = SecurityUtils.sign(data, owner.getPrivateKey());
    }
    
    // Getters
    public String getContractID() { return contractID; }
    public String getTenantName() { return tenantName; }
    public String getOwnerName() { return ownerName; }
    public String getPropertyID() { return propertyID; }
    public double getRentValue() { return rentValue; }

    public int getDurationMonths() {
        return durationMonths;
    }

    @Override
    public String toString() {
        return "[OFERTA] Contrato " + contractID + ": " + ownerName + " propõe alugar a " + tenantName;
    }
    
    private static final long serialVersionUID = 202510152200L;
}
