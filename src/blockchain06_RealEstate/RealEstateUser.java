//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Projeto RWA - Real Estate Blockchain (Adaptação)                    ::
//::                                                                         ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

package blockchain06_RealEstate;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import utils.FolderUtils;
import utils.SecurityUtils;

/**
 * Adaptado para incluir dados reais de identificação (NIF)
 */
public class RealEstateUser implements Serializable {

    // Pasta separada para não misturar com os users da TemplarCoin
    public static final String FILE_PATH = "data_realestate_user/";

    private String userName;
    private String taxID; // NIF ou Cartão de Cidadão (Novo campo para RWA)
    private PublicKey publicKey;
    
    transient private PrivateKey privateKey; // não gravar as chaves nas streams
    transient private Key aesKey; // não gravar as chaves nas streams

    protected RealEstateUser() {
        //construtor privado que so pode ser chamado na classe 
        new File(FILE_PATH).mkdirs();
    }

    public String getUserName() {
        return userName;
    }
    
    public String getTaxID() {
        return taxID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Key getAesKey() {
        return aesKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Regista um novo utilizador com NIF (Identificação Fiscal)
     * @param name Nome do utilizador
     * @param taxID Número de Identificação Fiscal (NIF)
     * @param password Password para encriptar a chave privada
     * @return Utilizador registado
     */
    public static RealEstateUser register(String name, String taxID, String password) throws Exception {
        //verificar se o user já esta registado
        if (new File(FILE_PATH + name + ".pub").exists()) {
            throw new Exception("User already exists :" + name);
        }

        RealEstateUser user = new RealEstateUser();
        user.userName = name;
        user.taxID = taxID; // Guardar o NIF
        
        //gerar as chaves
        user.aesKey = SecurityUtils.generateAESKey(256);
        KeyPair kp = SecurityUtils.generateRSAKeyPair(2048);
        user.publicKey = kp.getPublic();
        user.privateKey = kp.getPrivate();
        
        // --- GRAVAR FICHEIROS ---
        
        // 1. Guardar o Objeto User Público (com NIF e Public Key)
        // Nota: Ao contrário do TemplarUser original que guardava só a chave pública em bytes,
        // aqui podemos serializar o objeto para manter o NIF associado à chave, 
        // OU manter o padrão simples e guardar a PubKey + um ficheiro extra de meta-dados.
        // Para manter compatibilidade com a lógica simples do Blockchain 1.0, vamos manter a estrutura de ficheiros 
        // mas podemos guardar o NIF num ficheiro de texto auxiliar ou assumir que o nome do ficheiro é o ID.
        
        // Opção Simples: Guardar a chave pública normal
        Files.write(Path.of(FILE_PATH + name + ".pub"), user.publicKey.getEncoded());
        
        // Opção RWA: Guardar o NIF num ficheiro .txt para consulta
        Files.writeString(Path.of(FILE_PATH + name + ".nif"), taxID);

        // 2. Encriptar a Key AES com a publica
        byte[] secretAes = SecurityUtils.encrypt(user.aesKey.getEncoded(), user.publicKey);
        Files.write(Path.of(FILE_PATH + name + ".aes"), secretAes);

        // 3. Encriptar a privada com a password
        byte[] secretPriv = SecurityUtils.encrypt(user.privateKey.getEncoded(), password);
        Files.write(Path.of(FILE_PATH + name + ".priv"), secretPriv);
        
        return user;
    }

    public static RealEstateUser login(String name, String pass) throws Exception {
        RealEstateUser user = new RealEstateUser();
        user.userName = name;
        
        // Tentar ler o NIF se existir
        try {
            user.taxID = Files.readString(Path.of(FILE_PATH + name + ".nif"));
        } catch (Exception e) {
            user.taxID = "Unknown";
        }

        //ler a chave privada
        byte[] secretPriv = Files.readAllBytes(Path.of(FILE_PATH + name + ".priv"));
        //desencriptar com a password
        byte[] plainPriv = SecurityUtils.decrypt(secretPriv, pass);
        user.privateKey = SecurityUtils.getPrivateKey(plainPriv);
        //ler a AES
        byte[] secretAes = Files.readAllBytes(Path.of(FILE_PATH + name + ".aes"));
        //desencriptar com a chave privada
        byte[] plainAes = SecurityUtils.decrypt(secretAes, user.privateKey);
        user.aesKey = SecurityUtils.getAESKey(plainAes);
        //ler a publica
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);
        return user;
    }

    public static RealEstateUser login(String name) throws Exception {
        RealEstateUser user = new RealEstateUser();
        user.userName = name;
        // Tentar ler o NIF
        try {
            user.taxID = Files.readString(Path.of(FILE_PATH + name + ".nif"));
        } catch (Exception e) {
            user.taxID = "Unknown";
        }
        
        //ler a publica
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);
        return user;
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(userName);
        txt.append(" [NIF: ").append(taxID).append("]"); // Mostrar NIF no toString
        txt.append("\npub ").append(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        if (privateKey != null) {
            txt.append("\n(Authenticated)");
        }
        return txt.toString();
    }
    
    public static void deleteAllUsers() throws Exception{
        FolderUtils.cleanFolder(FILE_PATH, true);
    }

    public static List<RealEstateUser> getUserList() {
        List<RealEstateUser> lst = new ArrayList<>();
        File[] files = new File(FILE_PATH).listFiles();
        if (files == null) {
            return lst;
        }
        for (File file : files) {
            if (file.getName().endsWith(".pub")) {
                String userName = file.getName().substring(0, file.getName().lastIndexOf("."));
                try {
                    lst.add(login(userName));
                } catch (Exception e) {
                }
            }
        }
        return lst;
    }
    
    private static final long serialVersionUID = 202510151647L;
}