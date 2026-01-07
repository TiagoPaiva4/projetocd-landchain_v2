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
package blockchain01_network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.RMI;

/**
 * Created on 27/11/2024, 17:48:32
 *
 * @author manso - computer
 */
public class RemoteNodeObject extends UnicastRemoteObject implements RemoteNodeInterface {

    public static String REMOTE_OBJECT_NAME = "remoteNode";

    String address;
    Set<RemoteNodeInterface> network;
    Nodelistener listener;

    public RemoteNodeObject(int port, Nodelistener listener) throws RemoteException {
        super(port);
        try {
            //local adress of server
            String host = InetAddress.getLocalHost().getHostAddress();
            this.address = RMI.getRemoteName(host, port, REMOTE_OBJECT_NAME);
            this.network = new CopyOnWriteArraySet<>();
            // addNode(this);
            this.listener = listener;
            if (listener != null) {
                listener.onStart("Object " + address + " listening");
            } else {
                System.err.println("Object " + address + " listening");
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemoteNodeObject.class.getName()).log(Level.SEVERE, null, ex);
            if (listener != null) {
                listener.onException(ex, "Start remote Object");
            }
        }

    }

    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    @Override
    public void addNode(RemoteNodeInterface node) throws RemoteException {
        //se já tiver o nó  -  não faz nada
        if (network.contains(node)) {
            return;
        }
        //adicionar o no
        network.add(node);
        //adicionar o this ao remoto
        node.addNode(this);

        //propagar o no na rede
        for (RemoteNodeInterface iremoteP2P : network) {
            iremoteP2P.addNode(node);

        }
        if (listener != null) {
            listener.onConect(node.getAdress());
        } else {
            System.out.println("Connected to node.getAdress()");
        }
        //::::::::: DEBUG  ::::::::::::::::
        System.out.println("Rede p2p");
        for (RemoteNodeInterface iremoteP2P : network) {
            System.out.println(iremoteP2P.getAdress());

        }

    }

    @Override
    public List<RemoteNodeInterface> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }
//
//    @Override
//    public int hashCode() {
//        return this.address.hashCode();
//    }
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null || getClass() != obj.getClass()) {
//            return false;
//        }
//        final RemoteNodeObject other = (RemoteNodeObject) obj;
//        return Objects.equals(this.address, other.address);
//    }

}
