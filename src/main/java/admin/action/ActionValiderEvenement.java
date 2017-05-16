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
import fr.insalyon.dasi.collectif.metier.modele.EvenementGratuit;
import fr.insalyon.dasi.collectif.metier.modele.EvenementPayant;
import fr.insalyon.dasi.collectif.metier.modele.Lieu;
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import fr.insalyon.dasi.collectif.util.StatutService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.Action;

/**
 * The <code>ActionValiderEvenement</code> class is used as an adapter between the 
 * <code>HTTPServletRequest</code> to validate the place and the price of an event and the associated business service.
 *
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-30
 */
public class ActionValiderEvenement extends Action {
    /**
     * Executes the <code>affectationLieu</code> or <code>affectationLieuPaf</code> service associated to the 
     * <code>HttpServletRequest</code> and add the updated event as an attribute of the request.
     * @param request
     * @param response
     * @throws fr.insalyon.dasi.collectif.util.ServiceMetierException
     */
    @Override
    public void execute (HttpServletRequest request, HttpServletResponse response) throws ServiceMetierException {
        Long idEv;
        try {
            idEv = Long.parseLong(request.getParameter("idEvent"));
        } catch (NumberFormatException ex) {
            throw new ServiceMetierException(StatutService.UNDEFINED, new NumberFormatException("Mauvais format pour l'id de l'evenement: \'" + request.getParameter("idEvent") + "\' n'est pas un nombre."));
        }
        Long idPl;
        try {
            idPl = Long.parseLong(request.getParameter("idPlace"));
        } catch (NumberFormatException ex) {
            throw new ServiceMetierException(StatutService.UNDEFINED, new NumberFormatException("Mauvais format pour l'id du lieu: \'" + request.getParameter("idPlace") + "\' n'est pas un nombre."));
        }
         

        Evenement evenement = sm.selectEvenementById(idEv);
        Lieu lieu = sm.selectLieuByID(idPl);
            
        if(evenement == null) {
            Logger.getLogger(ActionValiderEvenement.class.getName()).log(Level.SEVERE, "No event found for id: {0}", idEv);
            throw new ServiceMetierException(StatutService.UNDEFINED, new IllegalArgumentException("Cet evenement n'existe pas dans la base de données."));
        }
        if(lieu == null) {
            Logger.getLogger(ActionValiderEvenement.class.getName()).log(Level.SEVERE, "No place found for id: {0}", idPl);
            throw new ServiceMetierException(StatutService.UNDEFINED, new IllegalArgumentException("Ce lieu n'existe pas dans la base de données."));
        }
            
        if(evenement instanceof EvenementGratuit) {
            sm.affectationLieu((EvenementGratuit)evenement, lieu);   
        } else {
            try {
                Double paf = Double.parseDouble(request.getParameter("paf"));
                sm.affectationLieuPaf((EvenementPayant)evenement, lieu, paf);
            } catch(NullPointerException e) {
                Logger.getLogger(ActionValiderEvenement.class.getName()).log(Level.SEVERE, null, e);
                throw new ServiceMetierException(StatutService.UNDEFINED, new NullPointerException("La PAF n'a pas été reçu par le serveur."));
            } catch(NumberFormatException e) {
                Logger.getLogger(ActionValiderEvenement.class.getName()).log(Level.SEVERE, null, e);
                throw new ServiceMetierException(StatutService.UNDEFINED, new NumberFormatException("Mauvais format pour la PAF: \'" + request.getParameter("paf") + "\' n'est pas un nombre."));
            }
        }
        request.setAttribute("eventReady", evenement);
    }  
}
