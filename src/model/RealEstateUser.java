//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::      Antonio Manuel Rodrigues Manso                                     ::
//::                                                                         ::
//::      I N S T I T U T O    P O L I T E C N I C O    D E    T O M A R     ::
//::      Escola Superior de Tecnologia de Tomar                             ::
//::      e-mail: manso@ipt.pt                                               ::
//::      url    : http://orion.ipt.pt/~manso                                ::
//::                                                                         ::
//::      This software was build with the purpose of investigate and        ::
//::      learning.                                                          ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package model;

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
 * Representa um utilizador na plataforma de Imobiliário (Real Estate).
 * <p>
 * Esta classe gere a identidade criptográfica do utilizador, incluindo a geração e
 * armazenamento de chaves assimétricas (RSA) e simétricas (AES).
 * Responsável também pelo processo de registo, login (carregamento de chaves) e persistência em ficheiros.
 * </p>
 * Created on 08/10/2025, 16:47:31
 *
 * @author manso - computer
 */
public class RealEstateUser implements Serializable{

    /** Caminho da pasta onde os ficheiros dos utilizadores são guardados. */
    public static final String FILE_PATH = "data_user/";

    /** O nome único do utilizador (usado como identificador). */
    private String userName;
    
    /** A chave pública RSA do utilizador (partilhável). */
    private PublicKey publicKey;
    
    /** * A chave privada RSA do utilizador.
     * Transient: não é serializada automaticamente para segurança. 
     */
    transient private PrivateKey privateKey; // não gravar as chaves nas streams
    
    /** * A chave simétrica AES do utilizador.
     * Transient: não é serializada automaticamente. 
     */
    transient private Key aesKey; // não gravar as chaves nas streams

    /**
     * Construtor protegido.
     * Garante que a estrutura de pastas existe ao instanciar um utilizador.
     */
    protected RealEstateUser() {
        //construtor privado que so pode ser chamado na classe 
        new File(FILE_PATH).mkdirs();
    }

    /**
     * Obtém o nome do utilizador.
     * @return O nome do utilizador.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Define o nome do utilizador.
     * @param userName O novo nome.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Obtém a chave simétrica (AES).
     * @return A chave secreta AES.
     */
    public Key getAesKey() {
        return aesKey;
    }

    /**
     * Obtém a chave pública (RSA).
     * @return A chave pública.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    /**
     * Obtém a chave privada (RSA).
     * <p>Atenção: A chave privada só está disponível se o login for feito com password.</p>
     * @return A chave privada.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Regista um novo utilizador no sistema.
     * <p>
     * 1. Verifica se o utilizador já existe.
     * 2. Gera um par de chaves RSA e uma chave AES.
     * 3. Guarda a Chave Pública em ficheiro (.pub).
     * 4. Encripta a Chave AES com a Pública e guarda em ficheiro (.aes).
     * 5. Encripta a Chave Privada com a password (simétrica) e guarda em ficheiro (.priv).
     * </p>
     * * @param name Nome do utilizador.
     * @param password Password para encriptar a chave privada.
     * @return O objeto RealEstateUser criado e com chaves carregadas.
     * @throws Exception Se o utilizador já existir ou erro na geração de chaves.
     */
    public static RealEstateUser register(String name, String password) throws Exception {
        //verificar se o user já esta registado
        if( new File(FILE_PATH + name + ".pub").exists())
            throw new Exception("User already exists :" + name);
        
        RealEstateUser user = new RealEstateUser();
        user.userName = name;
        //gerar as chaves
        user.aesKey = SecurityUtils.generateAESKey(256);
        KeyPair kp = SecurityUtils.generateRSAKeyPair(2048);
        user.publicKey = kp.getPublic();
        user.privateKey = kp.getPrivate();
        //guardar a publica em claro
        Files.write(Path.of(FILE_PATH + name + ".pub"), user.publicKey.getEncoded());
        //encriptar a Key AES com a publica (que desaencripta com a privada)
        byte[] secretAes = SecurityUtils.encrypt(user.aesKey.getEncoded(), user.publicKey);
        Files.write(Path.of(FILE_PATH + name + ".aes"), secretAes);

        //encriptar a privada com a password
        byte[] secretPriv = SecurityUtils.encrypt(user.privateKey.getEncoded(), password);
        Files.write(Path.of(FILE_PATH + name + ".priv"), secretPriv);
        return user;
    }

    /**
     * Efetua o login completo de um utilizador (recupera a identidade completa).
     * <p>
     * Usa a password fornecida para desencriptar a chave privada armazenada em disco.
     * Com a chave privada, recupera a chave AES.
     * </p>
     * * @param name Nome do utilizador.
     * @param pass Password do utilizador.
     * @return O objeto RealEstateUser com todas as chaves carregadas.
     * @throws Exception Se a password estiver errada ou ficheiros em falta.
     */
    public static RealEstateUser login(String name, String pass) throws Exception {
        RealEstateUser user = new RealEstateUser();
        user.userName = name;
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

    /**
     * Carrega apenas a identidade pública do utilizador (Login parcial).
     * <p>
     * Útil para obter a chave pública de outro utilizador (para lhe enviar mensagens encriptadas
     * ou verificar as suas assinaturas) sem saber a password dele.
     * </p>
     * * @param name Nome do utilizador.
     * @return O objeto RealEstateUser apenas com Nome e Chave Pública.
     * @throws Exception Se o utilizador não existir.
     */
    public static RealEstateUser login(String name) throws Exception {
        RealEstateUser user = new RealEstateUser();
        user.userName = name;
        //ler a publica
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);
        return user;
    }

    /**
     * Retorna uma representação textual do utilizador e das suas chaves (em Base64).
     * @return String com nome e chaves.
     */
    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(userName);
        txt.append("\npub ").append(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        if (privateKey != null) {
            txt.append("\npriv ").append(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            txt.append("\nAES ").append(Base64.getEncoder().encodeToString(aesKey.getEncoded()));
        }
        return txt.toString();
    }
    
    /**
     * Apaga todos os ficheiros de utilizadores registados na pasta de dados.
     * @throws Exception Se ocorrer erro ao apagar.
     */
    public static void deleteAllUsers() throws Exception{
        FolderUtils.cleanFolder(FILE_PATH, true);
        
    }

      /**
      * Lê a lista de todos os utilizadores registados no sistema.
      * <p>Varre a pasta de ficheiros e carrega a parte pública de cada utilizador encontrado.</p>
      *
      * @return Lista de objetos RealEstateUser (apenas com dados públicos).
      */
    public static List<RealEstateUser> getUserList() {
        List<RealEstateUser> lst = new ArrayList<>();
        //Ler os ficheiros da path dos utilizadores
        File[] files = new File(FILE_PATH).listFiles();
        if (files == null) {
            return lst;
        }
        //contruir um user com cada ficheiros
        for (File file : files) {
            //se for uma chave publica
            if (file.getName().endsWith(".pub")) {
                //nome do utilizador
                String userName = file.getName().substring(0, file.getName().lastIndexOf("."));
                try {
                    lst.add(login(userName));
                } catch (Exception e) {
                }
            }
        }
        return lst;

    }
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081647L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::

}