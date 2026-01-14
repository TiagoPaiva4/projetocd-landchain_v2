package model;

import java.io.Serializable;
import java.util.Base64;
import utils.SecurityUtils;

/**
 * Representa uma propriedade imobiliária (Ativo do Mundo Real - RWA) no sistema.
 * <p>
 * Esta classe é responsável por armazenar as características físicas de um imóvel
 * e gerar automaticamente um identificador único (ID) determinístico baseado
 * nessas mesmas características.
 * </p>
 * Implementa {@link Serializable} para permitir que objetos deste tipo sejam
 * transmitidos pela rede na Blockchain ou guardados em ficheiro.
 * 
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class RealEstateProperty implements Serializable {
    
    /**
     * Identificador único do imóvel (gerado automaticamente via Hash).
     */
    private String propertyID;   // O ID será gerado automaticamente
    
    /**
     * Endereço ou localização física do imóvel.
     */
    private String address;
    
    /**
     * Tipo de imóvel (ex: "Apartamento T3", "Vivenda", "Terreno").
     */
    private String type;
    
    /**
     * Área do imóvel em metros quadrados.
     */
    private double sizeM2;
    
    /**
     * Valor patrimonial ou fiscal do imóvel.
     */
    private double taxValue;

    /**
     * Construtor que cria um novo imóvel e gera automaticamente o seu ID único.
     * <p>
     * O ID não é passado como parâmetro; é calculado através de um hash criptográfico
     * combinando a morada, o tipo e o tamanho. Isto garante que o mesmo imóvel
     * (mesmos dados) tenha sempre o mesmo ID no sistema.
     * </p>
     * * @param address  A morada completa do imóvel.
     * @param type     O tipo de imóvel (ex: Casa, Apartamento).
     * @param sizeM2   A área do imóvel em metros quadrados.
     * @param taxValue O valor fiscal do imóvel.
     * @throws Exception Se ocorrer um erro durante a geração do Hash criptográfico (SecurityUtils).
     */
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

    /**
     * Obtém o identificador formatado para apresentação na Blockchain.
     * Adiciona um prefixo "RWA-" ao ID interno gerado.
     * * @return Uma String no formato "RWA-[ID_GERADO]".
     */
    public String getUniqueBlockchainID() {
        // O ID já é único, mas podemos adicionar um prefixo para ficar bonito
        return "RWA-" + propertyID; 
    }

    /**
     * Retorna uma representação textual do imóvel.
     * Útil para logs ou exibição rápida em listas.
     * * @return String com o ID, Tipo e Morada.
     */
    @Override
    public String toString() {
        return "ID: " + getUniqueBlockchainID() + " | " + type + " em " + address;
    }
    
    /**
     * Obtém o ID bruto do imóvel (sem o prefixo RWA).
     * * @return O hash curto de 12 caracteres do imóvel.
     */
    public String getPropertyID() { return propertyID; }

    /**
     * Obtém a morada do imóvel.
     * * @return A morada definida no registo.
     */
    public String getAddress() { return address; }
    
    /**
     * Versão de serialização para garantir compatibilidade entre diferentes versões da classe.
     */
    private static final long serialVersionUID = 202510151900L;
}