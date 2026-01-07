//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
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
package p2p;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created on 27/11/2024, 17:42:15
 *
 * @author manso - computer
 */
public interface IremoteP2P extends Remote {

    //:::: N E T WO R K  :::::::::::
    public String getAdress() throws RemoteException;

    public void addNode(IremoteP2P node) throws RemoteException;

    public List<IremoteP2P> getNetwork() throws RemoteException;

  

}
