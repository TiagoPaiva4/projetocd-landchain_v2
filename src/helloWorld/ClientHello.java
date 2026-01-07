///*****************************************************************************/
///****     Copyright (C) 2010                                             ****/
///****     António Manuel Rodrigues Manso                                  ****/
///****     e-mail: manso@ipt.pt                                            ****/
///****     url   : http://orion.ipt.pt/~manso    manso@ipt.pt              ****/
///****     Instituto Politécnico de Tomar                                  ****/
///****     Escola Superior de Tecnologia de Tomar                          ****/
///****                                                                     ****/
///*****************************************************************************/
///****     This software was build with the purpose of learning.           ****/
///****     Its use is free and is not provided any guarantee               ****/
///****     or support.                                                     ****/
///****     If you met bugs, please, report them to the author              ****/
///****                                                                     ****/
///*****************************************************************************/
///*****************************************************************************/
package helloWorld;

import java.rmi.Naming;

/**
 *
 * @author manso
 */
public class ClientHello {

    public static void main(String[] args) throws Exception {

        //máquina de apoio à disciplina
        String host = "localhost";
        //host = "193.137.5.177";
        String remoteObject = String.format("//%s:%d/%s", host, ServerHello.remotePort, ServerHello.remoteName);
        RemoteHelloInterface remoteHello = (RemoteHelloInterface) Naming.lookup(remoteObject);
        System.out.println(remoteHello.getMessage());
    }

}
