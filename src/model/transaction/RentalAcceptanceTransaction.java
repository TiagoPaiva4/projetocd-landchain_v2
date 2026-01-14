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
 * Representa a aceitação formal de um contrato de arrendamento por parte de um inquilino.
 * <p>
 * Esta transação é gerada quando um utilizador (Inquilino) decide aceitar uma "RentalTransaction"
 * (Proposta de Contrato) lançada por um proprietário.
 * A classe serve como prova criptográfica de que o inquilino concordou com os termos,
 * contendo a sua assinatura digital sobre o ID do contrato.
 * </p>
 *
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class RentalAcceptanceTransaction implements Serializable {

    /** Identificador único do contrato/oferta original que está a ser aceite. */
    private String contractID; 
    
    /** Nome de utilizador do inquilino que está a aceitar o contrato. */
    private String tenantName; 
    
    /** Carimbo de data/hora (em milissegundos) do momento da aceitação. */
    private long timestamp;
    
    /** * Assinatura digital do inquilino.
     * Garante a autenticidade e o não-repúdio da aceitação do contrato.
     */
    private byte[] tenantSignature; 

    /**
     * Construtor da Transação de Aceitação de Aluguer.
     * <p>
     * Cria o registo de aceitação e gera a assinatura digital usando a chave privada do inquilino.
     * Assina a concatenação do ID do contrato, nome do inquilino e timestamp para garantir
     * que esta aceitação não pode ser falsificada ou reutilizada noutro contexto.
     * </p>
     * * @param tenant O objeto do utilizador Inquilino (contendo a chave privada para assinar).
     * @param contractID O identificador da proposta de contrato que está a ser aceite.
     * @throws Exception Se ocorrer um erro durante o processo de assinatura criptográfica.
     */
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

    /**
     * Obtém o ID do contrato aceite.
     * @return O identificador da proposta original.
     */
    public String getContractID() { return contractID; }

    /**
     * Obtém o nome do inquilino.
     * @return O nome de utilizador do inquilino.
     */
    public String getTenantName() { return tenantName; }

    /**
     * Retorna uma representação textual da aceitação.
     * @return String formatada para logs ou interface.
     */
    @Override
    public String toString() {
        return "[ACEITAÇÃO] O inquilino " + tenantName + " assinou o contrato " + contractID;
    }
    
    /** Versão de serialização. */
    private static final long serialVersionUID = 202510152300L;
}