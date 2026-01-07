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
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package blocchain00;


import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created on 10/12/2025, 17:45:14
 *
 * @author manso - computer
 */
public class RemoteNodeObject extends UnicastRemoteObject implements RemoteNodeInterface {

    public static String OBJECT_NAME = "remoteNode";

    CopyOnWriteArraySet<RemoteNodeInterface> p2p;
    String adress;
    
    Nodelistener listener;

    public RemoteNodeObject(int port, Nodelistener listener) throws Exception {
        super(port);
        this.listener = listener;
        String host = InetAddress.getLocalHost().getHostAddress();
        this.adress = utils.RMI.getRemoteName(host, port, OBJECT_NAME);    
        p2p = new CopyOnWriteArraySet<>();
        listener.onStart(getAdress());
    }

    @Override
    public String getAdress() throws RemoteException {
        return adress;
     }

    @Override
    public void addNode(RemoteNodeInterface node) throws RemoteException {
        //não processar duplicados
        //interromper o fluxo
        if( p2p.contains(node))
            return;
        //fazer o processamento
        p2p.add(node);
        node.addNode(this);
      
        //propogar o serviço pela rede
        for (RemoteNodeInterface remote : p2p) {
            remote.addNode(node);   
        }
        listener.onConect(node.getAdress());
     }

    @Override
    public List<RemoteNodeInterface> getNetwork() throws RemoteException {
        return new ArrayList<>(p2p);
     }
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
