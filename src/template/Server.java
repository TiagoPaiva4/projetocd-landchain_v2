//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2024   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//////////////////////////////////////////////////////////////////////////////
package template;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created on 20/11/2024, 11:41:29
 *
 * @author manso - computer
 */
public class Server {

    public static final String remoteName = "RemoteObj";
    public static final int remotePort = 10011;

    public static void main(String[] args) throws Exception {
        //create object  to listen in the remote port
        ORemote rmtObj = new ORemote(remotePort);
        //local adress of server
        String host = InetAddress.getLocalHost().getHostAddress();
        //create registry to object
        LocateRegistry.createRegistry(remotePort);
        //create adress of remote object
        String address = String.format("//%s:%d/%s", host, remotePort, remoteName);
        //link adress to object 
        Naming.rebind(address, rmtObj);
        System.out.printf("Remote object ready at %s", address);
    }
}
