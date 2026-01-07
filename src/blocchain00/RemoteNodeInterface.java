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
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//////////////////////////////////////////////////////////////////////////////


package blocchain00;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Created on 10/12/2025, 17:43:34 
 * @author manso - computer
 */
public interface RemoteNodeInterface extends java.rmi.Remote{
     //:::: N E T WO R K  :::::::::::
    public String getAdress() throws RemoteException;

    public void addNode(RemoteNodeInterface node) throws RemoteException;

    public List<RemoteNodeInterface> getNetwork() throws RemoteException;


}
