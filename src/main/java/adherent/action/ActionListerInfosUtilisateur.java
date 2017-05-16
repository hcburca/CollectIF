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
package adherent.action;

import util.Action;
import fr.insalyon.dasi.collectif.metier.modele.Adherent;
import fr.insalyon.dasi.collectif.metier.modele.Demande;
import fr.insalyon.dasi.collectif.metier.modele.Evenement;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>ActionListerInfosUtilisateur</code> class is used as an adapter between the 
 * <code>HTTPServletRequest</code> to list user info together with his
 * current demands and events and the associated business service.
 *
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-13
 */
public class ActionListerInfosUtilisateur extends Action {
    /**
     * Executes the <code>getEvenementsPretsFuturs</code>, <code>getEvenementsEnAttenteEtFuturs</code> 
     * and <code>getDemandesEnAttenteEtFutures</code> methods for the <code>Adherent</code> associated 
     * to the current user and adds these lists of events and demands as attributes of the request.
     * @param request
     * @param response  
     */
    @Override
    public void execute (HttpServletRequest request, HttpServletResponse response) {
        Adherent ad = (Adherent) request.getSession().getAttribute("user");
        List<Evenement> evToSend = ad.getEvenementsPretsFuturs();
        evToSend.addAll(ad.getEvenementsEnAttenteEtFuturs());
        List<Demande> demToSend = ad.getDemandesEnAttenteEtFutures();
        request.setAttribute("eventsReadyOrWaiting", evToSend);
        request.setAttribute("demandsWaiting", demToSend);
    }
}
