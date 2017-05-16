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
import fr.insalyon.dasi.collectif.metier.modele.Activite;
import fr.insalyon.dasi.collectif.metier.modele.Adherent;
import fr.insalyon.dasi.collectif.metier.modele.Demande;
import fr.insalyon.dasi.collectif.util.MomentJournee;
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import fr.insalyon.dasi.collectif.util.StatutService;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>ActionEnregistrerDemande</code> class is used as an adapter between the 
 * <code>HTTPServletRequest</code> to register a demand and the associated business service.
 *
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-21 
 */
public class ActionEnregistrerDemande extends Action {    
    /**
     * Executes the <code>createDemande</code> service associated to the 
     * <code>HttpServletRequest</code> and adds the added demand as an attribute of the request.
     * @param request
     * @param response
     * @throws fr.insalyon.dasi.collectif.util.ServiceMetierException
     */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServiceMetierException {        
        // get user
        Adherent user = (Adherent) request.getSession().getAttribute("user");
        // get the activity
        Long idActivite;
        try {
            idActivite = Long.parseLong(request.getParameter("idActivite"));
        } catch (NumberFormatException ex) {
            throw new ServiceMetierException(StatutService.UNDEFINED, new NumberFormatException("Mauvais format pour l'id de l'activité: \'" + request.getParameter("idActivite") + "\' n'est pas un nombre."));
        }
        Activite activite;
        activite = sm.selectActiviteById(idActivite);
        if (activite == null) {
            throw new ServiceMetierException(StatutService.UNDEFINED, new IllegalArgumentException("Cette activité n'existe pas dans la base de données."));
        }
        // get date
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date;
        try {
            date = formatter.parse(request.getParameter("date"));
        } catch (ParseException ex) {
            Logger.getLogger(ActionEnregistrerDemande.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServiceMetierException(StatutService.UNDEFINED, new ParseException("Le format de la date n'est pas respecté (dd/MM/yyy).", ex.getErrorOffset()));
        }
        // get day period
        String momentString = request.getParameter("moment");
        MomentJournee moment;
        try {
            //moment = MomentJournee.valueOf(momentString.toUpperCase());
            moment = MomentJournee.values()[Integer.parseInt(momentString)];
        } catch (NumberFormatException ex) {
            Logger.getLogger(ActionEnregistrerDemande.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServiceMetierException(StatutService.UNDEFINED, new NumberFormatException("Le format du moment de la journée n'est pas respecté (entier entre 0 et " + (MomentJournee.values().length - 1) + ")."));
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.getLogger(ActionEnregistrerDemande.class.getName()).log(Level.SEVERE, null, e);
            throw new ServiceMetierException(StatutService.UNDEFINED, new NumberFormatException("Ce moment n'existe pas (moments valides: 0 - " + (MomentJournee.values().length - 1) + ")."));
        }

        Demande aAjouter = new Demande(date, moment, activite);
        sm.createDemande(user, aAjouter);
        request.setAttribute("demandAdded", aAjouter);
    }
}
