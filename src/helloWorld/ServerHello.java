/**
 * **************************************************************************
 */
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

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author manso
 */
public class ServerHello {

    public static final String remoteName = "RemoteHello";
    public static final int remotePort = 10_011;

    public static void main(String[] args) throws Exception {
        //create object  to listen in the remote port
        RemoteHelloObject helloWorld = new RemoteHelloObject(remotePort);
        //local adress of server
        String host = InetAddress.getLocalHost().getHostAddress();
        //create registry to object
        LocateRegistry.createRegistry(remotePort);
        //create adress of remote object
        String address = String.format("//%s:%d/%s", host, remotePort, remoteName);
        //link adress to object 
        Naming.rebind(address, helloWorld);
        System.out.printf("Ready on %s%n", address);
    }

}
