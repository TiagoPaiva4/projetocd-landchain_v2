package blockchain06_RealEstate;

import java.io.Serializable;
import java.util.Base64;
import utils.SecurityUtils;

public class RealEstateProperty implements Serializable {
    
    private String propertyID;   // O ID será gerado automaticamente
    private String address;
    private String type;
    private double sizeM2;
    private double taxValue;

    // Construtor modificado: NÃO pede o ID, ele gera-o.
    public RealEstateProperty(String address, String type, double sizeM2, double taxValue) throws Exception {
        this.address = address;
        this.type = type;
        this.sizeM2 = sizeM2;
        this.taxValue = taxValue;
        
        // --- GERAÇÃO AUTOMÁTICA DO ID ---
        // Criamos uma string única com os dados vitais do imóvel
        String uniqueData = address + type + sizeM2;
        
        // Geramos um Hash (impressão digital) desses dados
        byte[] hash = SecurityUtils.getHash(uniqueData.getBytes());
        
        // Convertemos para texto e ficamos com os primeiros 12 caracteres para ser um ID curto e legível
        // Exemplo de resultado: "Xy7zQq9LmN2k"
        this.propertyID = Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 12);
    }

    public String getUniqueBlockchainID() {
        // O ID já é único, mas podemos adicionar um prefixo para ficar bonito
        return "RWA-" + propertyID; 
    }

    @Override
    public String toString() {
        return "ID: " + getUniqueBlockchainID() + " | " + type + " em " + address;
    }
    
    public String getPropertyID() { return propertyID; }
    public String getAddress() { return address; }
    
    private static final long serialVersionUID = 202510151900L;
}