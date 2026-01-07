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
package template;

import java.rmi.Naming;

/**
 *
 * @author manso
 */
public class Client {
    public static final String host = "localhost";
    public static final String remoteName = "RemoteObj";
    public static final int remotePort = 10011;
    

    public static void main(String[] args) throws Exception {
        //adress of remote object
        String remoteObject = String.format("//%s:%d/%s", host,remotePort, remoteName);     
        // get reference to the remote object
        IRemote remoteHello = (IRemote) Naming.lookup(remoteObject);
        //execute remote method
        System.out.println(remoteHello.sayHello());
    }

}
