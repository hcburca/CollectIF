/*
 * Copyright (C) 2017 Horia
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package admin.action;

import fr.insalyon.dasi.collectif.metier.modele.Evenement;
import fr.insalyon.dasi.collectif.metier.modele.Lieu;
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import fr.insalyon.dasi.collectif.util.Util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.Action;

/**
 * The <code>ActionListerEvenementsAttente</code> class is used as an adapter between the 
 * <code>HTTPServletRequest</code> to list the events that need to be assigned a place by the admin 
 * and the associated business service.
 *
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-26
 */
public class ActionListerInfosPourAdmin extends Action {
    
    /**
     * Executes the <code>selectAllEvenementWaiting</code> service associated to the 
     * <code>HttpServletRequest</code> and adds the list of events that need to be validated
     * and the list of places as attributes of the request.
     * @param request
     * @param response 
     * @throws fr.insalyon.dasi.collectif.util.ServiceMetierException 
     */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServiceMetierException {      
        List<Evenement> eventsToSend = new ArrayList<>();
        List<Lieu> placesToSend = new ArrayList<>();
        eventsToSend.addAll(sm.selectAllEvenementWaiting()); 
        placesToSend.addAll(sm.selectAllLieu());

        //remove events that have expired
        for (Iterator<Evenement> iterator = eventsToSend.listIterator(); iterator.hasNext();) {
            Evenement ev = iterator.next();
            if (!Util.dateIsInTheFuture(ev.getDate(), ev.getMoment())) {
                iterator.remove();
            }
        }          
        request.setAttribute("eventsWaiting", eventsToSend);
        request.setAttribute("places", placesToSend);
    }
}
