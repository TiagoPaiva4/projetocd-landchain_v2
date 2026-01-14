/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package model.transaction;

import java.io.Serializable;
import java.util.UUID;
import model.RealEstateUser;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Representa uma transação de proposta de arrendamento (Aluguer) imobiliário.
 * <p>
 * Esta classe é criada pelo proprietário (Senhorio) de um imóvel quando este deseja
 * iniciar um contrato de aluguer com um inquilino específico. Contém todos os termos
 * financeiros e temporais do acordo.
 * </p>
 * <p>
 * Funciona como uma "Oferta de Contrato" que é assinada digitalmente pelo dono,
 * aguardando posteriormente uma transação de aceitação (`RentalAcceptanceTransaction`)
 * por parte do inquilino para se tornar válida.
 * </p>
 *
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class RentalTransaction implements Serializable {

    /** Identificador único universal (UUID) gerado para este contrato específico. */
    private String contractID; 
    
    /** Identificador do imóvel que está a ser colocado para alugar. */
    private String propertyID;
    
    /** Nome de utilizador do proprietário (Senhorio) que cria a proposta. */
    private String ownerName;
    
    /** Nome de utilizador do inquilino a quem a proposta se destina. */
    private String tenantName;
    
    /** Valor monetário da renda (mensal). */
    private double rentValue;
    
    /** Duração do contrato de arrendamento em meses. */
    private int durationMonths;
    
    /** Carimbo de data/hora (em milissegundos) da criação da proposta. */
    private long timestamp;
    
    /** * Assinatura digital do proprietário.
     * Garante que os termos (valor, duração, imóvel) foram definidos pelo dono
     * e não foram alterados por terceiros.
     */
    private byte[] signature;

    /**
     * Construtor da Transação de Aluguer (Proposta).
     * <p>
     * Gera um ID único para o contrato e assina digitalmente os dados fundamentais
     * usando a chave privada do proprietário.
     * </p>
     *
     * @param owner      O objeto do utilizador Proprietário (necessário para a chave privada).
     * @param propertyID O ID do imóvel a alugar.
     * @param tenantName O nome do utilizador que será o inquilino.
     * @param rentValue  O valor da renda.
     * @param months     A duração do contrato em meses.
     * @throws Exception Se ocorrer um erro na geração da assinatura digital.
     */
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
    
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS (Métodos de acesso)
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Obtém o ID único do contrato.
     * @return String UUID do contrato.
     */
    public String getContractID() { return contractID; }

    /**
     * Obtém o nome do inquilino proposto.
     * @return Nome de utilizador do inquilino.
     */
    public String getTenantName() { return tenantName; }

    /**
     * Obtém o nome do proprietário.
     * @return Nome de utilizador do senhorio.
     */
    public String getOwnerName() { return ownerName; }

    /**
     * Obtém o ID do imóvel.
     * @return Identificador do imóvel.
     */
    public String getPropertyID() { return propertyID; }

    /**
     * Obtém o valor da renda.
     * @return Valor double da renda.
     */
    public double getRentValue() { return rentValue; }

    /**
     * Obtém a duração do contrato.
     * @return Número de meses.
     */
    public int getDurationMonths() {
        return durationMonths;
    }

    /**
     * Retorna uma representação textual da proposta.
     * @return String formatada indicando que é uma [OFERTA].
     */
    @Override
    public String toString() {
        return "[OFERTA] Contrato " + contractID + ": " + ownerName + " propõe alugar a " + tenantName;
    }
    
    /** Versão de serialização para compatibilidade. */
    private static final long serialVersionUID = 202510152200L;
}