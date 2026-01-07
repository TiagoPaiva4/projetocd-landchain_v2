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

public class RentalAcceptanceTransaction implements Serializable {

    private String contractID; // Referência à oferta original
    private String tenantName; // Quem está a aceitar
    private long timestamp;
    private byte[] tenantSignature; // Prova de aceitação

    public RentalAcceptanceTransaction(RealEstateUser tenant, String contractID) throws Exception {
        this.tenantName = tenant.getUserName();
        this.contractID = contractID;
        this.timestamp = System.currentTimeMillis();

        // O inquilino assina o ID do contrato com a sua chave privada
        // Isto prova legalmente que ele aceitou AQUELE contrato específico
        byte[] data = Utils.concatenate(contractID.getBytes(), tenantName.getBytes());
        data = Utils.concatenate(data, Utils.longToBytes(timestamp));

        this.tenantSignature = SecurityUtils.sign(data, tenant.getPrivateKey());
    }

    public String getContractID() { return contractID; }
    public String getTenantName() { return tenantName; }

    @Override
    public String toString() {
        return "[ACEITAÇÃO] O inquilino " + tenantName + " assinou o contrato " + contractID;
    }
    
    private static final long serialVersionUID = 202510152300L;
}
