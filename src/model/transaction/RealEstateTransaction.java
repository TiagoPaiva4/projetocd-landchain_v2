package model.transaction;

import java.io.Serializable;
import java.security.PublicKey;
import model.RealEstateUser;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Representa uma transação de Ativos Imobiliários (RWA - Real World Assets) na Blockchain.
 * <p>
 * Esta classe é responsável por registar a transferência de propriedade (ou frações/tokens dela)
 * entre dois utilizadores. Garante a segurança, integridade e não-repúdio através de
 * assinaturas digitais RSA.
 * </p>
 * Implementa {@link Serializable} para permitir o envio pela rede P2P e armazenamento em blocos.
 * 
 * @author Tiago Paiva
 * @author Pedro Laúdo
 */
public class RealEstateTransaction implements Serializable {

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: DADOS DO ATIVO (RWA)
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    /** Identificador único ou nome do imóvel (ex: "Prédio Avenida Lisboa" ou um Hash). */
    private String assetName;   
    
    /** Quantidade de tokens (frações do imóvel) a transferir nesta transação. */
    private int tokenAmount;    

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: DADOS DE SEGURANÇA E IDENTIDADE
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    /** Nome de utilizador do remetente (quem envia/vende). */
    private String txtSender;   
    
    /** Nome de utilizador do destinatário (quem recebe/compra). */
    private String txtReceiver; 
    
    /** Chave Pública do remetente (usada para verificar a assinatura). */
    private PublicKey sender;   
    
    /** Chave Pública do destinatário. */
    private PublicKey receiver; 
    
    /** Carimbo de data/hora (em milissegundos) da criação da transação. */
    private long timestamp;     
    
    /** Assinatura digital que garante que a transação foi criada pelo dono da chave privada do remetente. */
    private byte[] signature;   

    /**
     * Construtor da Transação Real Estate.
     * <p>
     * Cria uma nova transação, identifica o destinatário, define o timestamp e assina
     * digitalmente todos os dados críticos usando a chave privada do utilizador remetente.
     * </p>
     * * @param uSender      O objeto do utilizador remetente (deve estar autenticado e conter a chave privada).
     * @param receiverName O nome do utilizador destinatário (a chave pública será carregada do disco).
     * @param asset        O identificador do imóvel (Ativo).
     * @param amount       A quantidade de tokens a transferir.
     * @throws Exception   Se o destinatário não for encontrado ou ocorrer erro na assinatura criptográfica.
     */
    public RealEstateTransaction(RealEstateUser uSender, String receiverName, String asset, int amount) throws Exception {
        // 1. O Sender já vem autenticado (uSender)
        this.txtSender = uSender.getUserName();
        this.sender = uSender.getPublicKey();
        
        // 2. O Receiver (neste caso de minting, é o próprio sender, mas mantemos genérico)
        // Carrega chave pública do destinatário através do login parcial
        RealEstateUser uReceiver = RealEstateUser.login(receiverName); 
        this.txtReceiver = uReceiver.getUserName();
        this.receiver = uReceiver.getPublicKey();

        // 3. Dados do Negócio
        this.assetName = asset;
        this.tokenAmount = amount;
        this.timestamp = System.currentTimeMillis();

        // 4. Assinatura (Usando a chave privada que já está no objeto uSender)
        // Concatena todos os dados importantes para garantir que nada foi alterado
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        allData = Utils.concatenate(allData, assetName.getBytes());
        allData = Utils.concatenate(allData, Utils.intToBytes(tokenAmount));

        // Gera a assinatura digital
        this.signature = SecurityUtils.sign(allData, uSender.getPrivateKey());
    }

    /**
     * Verifica a validade e autenticidade da transação.
     * <p>
     * Recalcula os dados da transação e verifica se a assinatura corresponde
     * à chave pública do remetente. Isto garante que os dados não foram alterados
     * e que foi realmente o remetente quem criou a transação.
     * </p>
     * * @return true se a assinatura for válida, false caso contrário.
     */
    public boolean verifySignature() {
        try {
            // Reconstruir os dados originais para verificação
            byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
            allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
            allData = Utils.concatenate(allData, assetName.getBytes());
            allData = Utils.concatenate(allData, Utils.intToBytes(tokenAmount));

            // Validar criptograficamente
            return SecurityUtils.verifySign(allData, signature, sender);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retorna uma representação textual da transação.
     * Útil para logs e visualização na interface.
     * * @return String formatada descrevendo a transferência.
     */
    @Override
    public String toString() {
        // Formato legível para os logs
        return String.format("%s transferiu %d tokens de [%s] para %s", 
                txtSender, tokenAmount, assetName, txtReceiver);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS (Métodos de Acesso aos Dados)
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Obtém o nome ou identificador do ativo imobiliário.
     * @return Nome do ativo.
     */
    public String getAssetName() {
        return assetName;
    }

    /**
     * Obtém a quantidade de tokens envolvidos na transação.
     * @return Quantidade de tokens.
     */
    public int getTokenAmount() {
        return tokenAmount;
    }

    /**
     * Obtém o nome do remetente (Vendedor).
     * @return Nome do utilizador remetente.
     */
    public String getTxtSender() {
        return txtSender;
    }

    /**
     * Obtém o nome do destinatário (Comprador).
     * @return Nome do utilizador destinatário.
     */
    public String getTxtReceiver() {
        return txtReceiver;
    }

    /**
     * Obtém a chave pública do remetente.
     * @return Chave pública RSA.
     */
    public PublicKey getSender() {
        return sender;
    }

    /**
     * Obtém a chave pública do destinatário.
     * @return Chave pública RSA.
     */
    public PublicKey getReceiver() {
        return receiver;
    }

    /**
     * Obtém o momento exato da transação.
     * @return Timestamp em milissegundos.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Obtém a assinatura digital da transação.
     * @return Array de bytes da assinatura.
     */
    public byte[] getSignature() {
        return signature;
    }
    
    /**
     * Versão de serialização para compatibilidade entre diferentes versões da classe.
     */
    private static final long serialVersionUID = 202510151147L;
}