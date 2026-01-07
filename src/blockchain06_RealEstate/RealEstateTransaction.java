package blockchain06_RealEstate;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Base64;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Transação de Ativos Imobiliários (RWA)
 * Permite a transferência de tokens fracionados de um imóvel.
 */
public class RealEstateTransaction implements Serializable {

    // --- Dados do Ativo (RWA) ---
    private String assetName;   // Ex: "Prédio Avenida Lisboa"
    private int tokenAmount;    // Ex: 10 (quantidade de tokens a transferir)

    // --- Dados de Segurança e Identidade ---
    private String txtSender;   // Nome do remetente
    private String txtReceiver; // Nome do destinatário
    private PublicKey sender;   // Chave pública do remetente
    private PublicKey receiver; // Chave pública do destinatário
    private long timestamp;     // Data/Hora da transação
    private byte[] signature;   // Assinatura digital que valida tudo

    /**
     * Construtor da Transação Real Estate
     * @param senderName Nome do utilizador que envia (Vendedor)
     * @param receiverName Nome do utilizador que recebe (Comprador)
     * @param asset Nome do Imóvel (Identificador único do ativo)
     * @param amount Quantidade de tokens a transferir
     * @param pass Password do remetente para assinar a transação
     * @throws Exception Se o login falhar ou a assinatura der erro
     */
    // Adiciona este NOVO construtor na classe blockchain06_RealEstate.RealEstateTransaction

public RealEstateTransaction(RealEstateUser uSender, String receiverName, String asset, int amount) throws Exception {
    // 1. O Sender já vem autenticado (uSender)
    this.txtSender = uSender.getUserName();
    this.sender = uSender.getPublicKey();
    
    // 2. O Receiver (neste caso de minting, é o próprio sender, mas mantemos genérico)
    RealEstateUser uReceiver = RealEstateUser.login(receiverName); // Carrega chave pública do destinatário
    this.txtReceiver = uReceiver.getUserName();
    this.receiver = uReceiver.getPublicKey();

    // 3. Dados do Negócio
    this.assetName = asset;
    this.tokenAmount = amount;
    this.timestamp = System.currentTimeMillis();

    // 4. Assinatura (Usando a chave privada que já está no objeto uSender)
    byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
    allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
    allData = Utils.concatenate(allData, assetName.getBytes());
    allData = Utils.concatenate(allData, Utils.intToBytes(tokenAmount));

    this.signature = SecurityUtils.sign(allData, uSender.getPrivateKey());
}

    /**
     * Verifica se a assinatura da transação é válida
     * @return true se a transação for autêntica
     */
    public boolean verifySignature() {
        try {
            byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
            allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
            allData = Utils.concatenate(allData, assetName.getBytes());
            allData = Utils.concatenate(allData, Utils.intToBytes(tokenAmount));

            return SecurityUtils.verifySign(allData, signature, sender);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        // Formato legível para os logs
        return String.format("%s transferiu %d tokens de [%s] para %s", 
                txtSender, tokenAmount, assetName, txtReceiver);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS (Necessários para a Interface Gráfica e Mineração)
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public String getAssetName() {
        return assetName;
    }

    public int getTokenAmount() {
        return tokenAmount;
    }

    public String getTxtSender() {
        return txtSender;
    }

    public String getTxtReceiver() {
        return txtReceiver;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getSignature() {
        return signature;
    }
    
    // Serial Version UID para garantir compatibilidade na serialização
    private static final long serialVersionUID = 202510151147L;
}