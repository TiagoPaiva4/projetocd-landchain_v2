package model.transaction;

import java.io.Serializable;
import java.util.UUID;
import model.RealEstateUser;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Representa uma proposta comercial (Oferta) no Marketplace imobiliário.
 * <p>
 * Esta classe encapsula a intenção de um utilizador de iniciar um negócio.
 * Pode ser uma oferta de <b>VENDA</b> (o utilizador tem tokens e quer dinheiro) ou
 * uma oferta de <b>COMPRA</b> (o utilizador tem dinheiro e quer tokens).
 * </p>
 * <p>
 * As propostas podem ser direcionadas a um utilizador específico (Negócio Privado)
 * ou lançadas para "MERCADO" (Negócio Público).
 * </p>
 * 
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class SaleProposal implements Serializable {

    /** Constante que define o tipo de proposta como VENDA (Quero vender tokens). */
    public static final String TYPE_SELL_OFFER = "VENDA"; 
    
    /** Constante que define o tipo de proposta como COMPRA (Quero comprar tokens). */
    public static final String TYPE_BUY_OFFER = "COMPRA";

    /** Identificador único universal (UUID) desta proposta. */
    private String proposalID;
    
    /** Identificador do imóvel (Ativo RWA) envolvido no negócio. */
    private String propertyID;
    
    /** Nome do utilizador que criou a proposta (Propositor). */
    private String proposer;
    
    /** * Nome do utilizador destinatário da proposta.
     * Se for "MERCADO", a proposta é pública e qualquer pessoa pode aceitar.
     */
    private String targetUser;
    
    /** Preço total proposto em euros (valor monetário da transação). */
    private double price;
    
    /** Quantidade de tokens (frações do imóvel) que estão a ser negociados. */
    private int tokenAmount; 
    
    /** O tipo de operação: "VENDA" ou "COMPRA". */
    private String type;
    
    /** Carimbo de data/hora da criação da proposta. */
    private long timestamp;
    
    /** Assinatura digital do propositor para garantir a integridade da oferta. */
    private byte[] signature;

    /**
     * Construtor da Proposta de Venda/Compra.
     * <p>
     * Inicializa uma nova oferta no sistema. Gera um ID único e assina digitalmente
     * todos os termos do acordo (quem, o quê, quanto e por quanto) usando a chave privada do utilizador.
     * </p>
     *
     * @param user        O utilizador que está a criar a proposta (contém a chave privada).
     * @param propertyID  O ID do imóvel que se pretende transacionar.
     * @param targetUser  O nome do destinatário (ou deixe null/vazio para ser pública "MERCADO").
     * @param price       O valor monetário (Euros) proposto.
     * @param tokenAmount A quantidade de tokens do imóvel envolvidos.
     * @param type        O tipo de proposta (Use as constantes TYPE_SELL_OFFER ou TYPE_BUY_OFFER).
     * @throws Exception  Se ocorrer erro na geração da assinatura criptográfica.
     */
    public SaleProposal(RealEstateUser user, String propertyID, String targetUser, double price, int tokenAmount, String type) throws Exception {
        this.proposalID = UUID.randomUUID().toString();
        this.proposer = user.getUserName();
        this.propertyID = propertyID;
        // Se não for especificado um alvo, assume-se que é para o mercado aberto
        this.targetUser = (targetUser == null || targetUser.isEmpty()) ? "MERCADO" : targetUser;
        this.price = price;
        this.tokenAmount = tokenAmount; // Guardar quantidade
        this.type = type;
        this.timestamp = System.currentTimeMillis();

        // Assinar incluindo a quantidade e todos os dados relevantes
        // Isto impede que alguém altere o preço ou a quantidade de tokens após a assinatura
        String data = proposalID + propertyID + price + tokenAmount + type + this.targetUser;
        this.signature = SecurityUtils.sign(data.getBytes(), user.getPrivateKey());
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS (Métodos de acesso aos dados da proposta)
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Obtém o ID único da proposta.
     * @return String UUID.
     */
    public String getProposalID() { return proposalID; }

    /**
     * Obtém o ID do imóvel em questão.
     * @return String ID do imóvel.
     */
    public String getPropertyID() { return propertyID; }

    /**
     * Obtém o nome de quem fez a proposta.
     * @return Nome de utilizador.
     */
    public String getProposer() { return proposer; }

    /**
     * Obtém o alvo da proposta.
     * @return Nome de utilizador ou "MERCADO".
     */
    public String getTargetUser() { return targetUser; }

    /**
     * Obtém o preço proposto.
     * @return Valor em euros.
     */
    public double getPrice() { return price; }

    /**
     * Obtém a quantidade de tokens envolvida.
     * @return Número inteiro de tokens.
     */
    public int getTokenAmount() { return tokenAmount; } 

    /**
     * Obtém o tipo de proposta.
     * @return "VENDA" ou "COMPRA".
     */
    public String getType() { return type; }

    /**
     * Retorna uma representação textual resumida da proposta.
     * Útil para listagens rápidas ou logs.
     * @return String descritiva (ex: "VENDA: 10 tokens de [ID] por 500€").
     */
    @Override
    public String toString() {
        return type + ": " + tokenAmount + " tokens de " + propertyID + " por " + price + "€";
    }
    
    /** Versão de serialização para compatibilidade. */
    private static final long serialVersionUID = 2025102005L;
}