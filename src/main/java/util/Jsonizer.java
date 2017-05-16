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
package util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.insalyon.dasi.collectif.metier.modele.Activite;
import fr.insalyon.dasi.collectif.metier.modele.Adherent;
import fr.insalyon.dasi.collectif.metier.modele.Demande;
import fr.insalyon.dasi.collectif.metier.modele.Evenement;
import fr.insalyon.dasi.collectif.metier.modele.EvenementPayant;
import fr.insalyon.dasi.collectif.metier.modele.Lieu;
import fr.insalyon.dasi.collectif.metier.service.ServiceMetier;
import fr.insalyon.dasi.collectif.util.MomentJournee;
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import fr.insalyon.dasi.collectif.util.StatutDemande;
import fr.insalyon.dasi.collectif.util.Util;
import java.sql.Date;
import java.text.DateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * The <code>Jsonizer</code> class contains methods for transforming business 
 * objects, (<code>Activite</code>, <code>Demande</code>, ...) into json objects.
 * 
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-13 
 */
public class Jsonizer {
    private static final ServiceMetier SM = new ServiceMetier();
    /**
     * Generates a <code>JsonArray</code> object from the list of activities.
     * @param ac  the list of activities to be sent in response.
     * @return JsonArray.
     */
    public static JsonArray jsonListActivites( List<Activite> ac) {
         
        JsonArray jsonListe = new JsonArray();
        
        for (Activite a : ac)
        {
            JsonObject jsonActivite = new JsonObject();
            jsonActivite.addProperty("id", a.getId());
            jsonActivite.addProperty("denomination", a.getDenomination());
            jsonActivite.addProperty("payed", a.getPayant() ? "Cette activité est payante." : "Cette activité est gratuite.");
            jsonActivite.addProperty("nbParticipants", a.getNbParticipants());
            jsonActivite.addProperty("imgUrl", "img/activite" + a.getId() + ".jpg");
            jsonActivite.addProperty("description", a.getDescription());
            jsonListe.add(jsonActivite);
        }
        
        return jsonListe;
    }
    
    /**
     * Returns a <code>JsonObject</code> containing the name of the user.
     * @param a the user.
     * @return 
     */
    public static JsonObject jsonAdherent(Adherent a) {
        JsonObject adherent = new JsonObject();
        adherent.addProperty("name", a.getNom() + " " + a.getPrenom());
        return adherent;
    }
    
    /**
     * Returns a <code>JsonObject</code> containing a string with either 'success' or 'failure' and an associate message.
     * @param success true for 'success', false for 'failure'.
     * @param message the associated message, which can be a redirection link in case of success.
     * @param redirect the redirection link, if needed. 
     * @return 
     */
    public static JsonObject jsonOperationResult(boolean success, String message, String redirect) {
        JsonObject result = new JsonObject();
        if(success) {
            result.addProperty("result", "success");
        } else {
            result.addProperty("result", "failure");
        }
        result.addProperty("message", message);
        result.addProperty("redirect", redirect);
        return result;
    }
    
    /**
     * Returns a <code>JsonArray</code> containing the demands and the events that are passed
     * as parameter.
     * @param d the list of demands.
     * @param e the list of events.
     * @return 
     */
    public static JsonArray jsonListDemandesEtEvenements(List<Demande> d, List<Evenement> e) {
        JsonArray result = new JsonArray();
        
        for (Demande dem : d) {
            if (dem.getStatut() != StatutDemande.VALIDE) {
                JsonObject jsonDemande = new JsonObject();
                jsonDemande.addProperty("type", "demand");
                jsonDemande.addProperty("denomination", dem.getActivite().getDenomination());
                jsonDemande.addProperty("imgUrl", "img/activite" + dem.getActivite().getId() + ".jpg");
                jsonDemande.addProperty("date", DateFormat.getDateInstance(DateFormat.MEDIUM).format(dem.getDate()));
                jsonDemande.addProperty("moment", dem.getMoment().name().toLowerCase());
                jsonDemande.addProperty("momentOrder", dem.getMoment().ordinal());
                if (Util.dateIsInTheFuture(dem.getDate(), dem.getMoment())) {
                    try {
                        //get nr of participants
                        List<Demande> same = SM.selectDemandesByActiviteEtDate(dem.getActivite().getId(), dem.getDate(), dem.getMoment());
                        jsonDemande.addProperty("status", "Demande en attente de participants (" + same.size() + "/" + dem.getActivite().getNbParticipants() + ").");
                    } catch (ServiceMetierException ex) {
                        Logger.getLogger(Jsonizer.class.getName()).log(Level.SEVERE, null, ex);
                        jsonDemande.addProperty("status", "Demande en attente de participants (N/A).");
                    }
                } else {
                    jsonDemande.addProperty("status", "Demande expiré. Il n'y avait pas assez de personnes pour créer un événement.");
                }
                result.add(jsonDemande);
            }
        }
        
        for (Evenement ev : e) {
            JsonObject jsonEvent = new JsonObject();
            jsonEvent.addProperty("type", "event");
            jsonEvent.addProperty("denomination", ev.getActivite().getDenomination());
            jsonEvent.addProperty("imgUrl", "img/activite" + ev.getActivite().getId() + ".jpg");
            jsonEvent.addProperty("date", DateFormat.getDateInstance(DateFormat.MEDIUM).format(ev.getDate()));
            jsonEvent.addProperty("moment", ev.getMoment().name().toLowerCase());
            jsonEvent.addProperty("momentOrder", ev.getMoment().ordinal());
            
            switch (ev.getStatut()) {
                case PRET         :
                    JsonArray participants = new JsonArray();
                    for (Adherent a: ev.getParticipants()) {
                        JsonObject participant = new JsonObject();
                        participant.addProperty("name", a.getNom() + " " + a.getPrenom());
                        participant.addProperty("lat", a.getLatitude());
                        participant.addProperty("long", a.getLongitude());
                        participants.add(participant);
                    }
                    jsonEvent.add("participants", participants);
                    jsonEvent.addProperty("place", ev.getLieu().getDenomination());
                    if (ev instanceof EvenementPayant) {
                        jsonEvent.addProperty("price", ((EvenementPayant)ev).getPAF());
                    } else {
                        jsonEvent.addProperty("price", "gratuit");
                    }
                    if (Util.dateIsInTheFuture(ev.getDate(), ev.getMoment())) {
                        jsonEvent.addProperty("status", "L'événement est prêt.");
                    } else {
                        jsonEvent.addProperty("status", "L'événement est terminé.");
                    }
                    break;
                case ATTENTE_LIEU :
                    if (Util.dateIsInTheFuture(ev.getDate(), ev.getMoment())) {
                        int nr = ev.getActivite().getNbParticipants();
                        jsonEvent.addProperty("status", "Evénement en attente d'attribution de lieu (" + nr + "/" + nr + ").");
                    } else {
                        jsonEvent.addProperty("status", "Evénement expiré. L'admin n'a pas attribué un lieu.");
                    } 
                    break;
                case ANNULE       :
                    jsonEvent.addProperty("status", "Evénement annulé.");
                    break;
                default           :
                    jsonEvent.addProperty("status", "Statut inconnu.");
                    break;
            }
            result.add(jsonEvent);
        }
        return result;
    }
    
    /**
     * Returns a <code>JsonArray</code> containing the events that are passed as parameter
     * with all the information needed by the admin.
     * @param events - list of <code>Evenement</code>, with status ATTENTE_LIEU.
     * @return 
     */
    public static JsonArray jsonEvenementsAdmin(List<Evenement> events) {
        JsonArray result = new JsonArray();
        
        for (Evenement ev: events) {
            JsonObject jsonEvent = new JsonObject();
            jsonEvent.addProperty("id", ev.getId());
            jsonEvent.addProperty("denomination", ev.getActivite().getDenomination());
            jsonEvent.addProperty("imgUrl", "img/activite" + ev.getActivite().getId() + ".jpg");
            jsonEvent.addProperty("date", DateFormat.getDateInstance(DateFormat.MEDIUM).format(ev.getDate()));
            jsonEvent.addProperty("moment", ev.getMoment().name().toLowerCase());
            jsonEvent.addProperty("momentOrder", ev.getMoment().ordinal());
            jsonEvent.addProperty("placeType", Util.getTypeLieuFromActiviteDenomination(ev.getActivite().getDenomination()));
            if (ev.getActivite().getPayant()) {
                jsonEvent.addProperty("payed", "Cette activité est payante.");
            } else {
                jsonEvent.addProperty("payed", "Cette activité est gratuite.");
            }
            JsonArray participants = new JsonArray();
            for (Adherent a: ev.getParticipants()) {
                JsonObject participant = new JsonObject();
                participant.addProperty("name", a.getNom() + " " + a.getPrenom());
                participant.addProperty("lat", a.getLatitude());
                participant.addProperty("long", a.getLongitude());
                participants.add(participant);
            }
            jsonEvent.add("participants", participants);
            result.add(jsonEvent);
        }
        
        return result;
    }
    
    /**
     * Returns a <code>JsonArray</code> containing the places that are passed as parameter.
     * @param places - a list of <code>Lieu</code>
     * @return 
     */
    public static JsonArray jsonLieux(List<Lieu> places) {
        JsonArray result = new JsonArray();
        
        for(Lieu l: places) {
            JsonObject place = new JsonObject();
            place.addProperty("id", l.getId());
            place.addProperty("name", l.getDenomination());
            place.addProperty("address", l.getAdresse());
            place.addProperty("lat", l.getLatitude());
            place.addProperty("long", l.getLongitude());
            place.addProperty("type", l.getType());
            result.add(place);
        }
        return result;
    }
    
    /**
     * Generates a Json object from the particular activity
     * associated with the <code>HttpServletResponse</code>
     * @param adherent
     * @param a   the activity to be sent in response
     * @param nrJours number of days ahead in which to look for equals demands
     * @return 
     */
    public static JsonObject jsonActivite(Adherent adherent, Activite a, int nrJours) {
        JsonObject jsonActivite = new JsonObject();
        jsonActivite.addProperty("denomination", a.getDenomination());
        jsonActivite.addProperty("nbParticipants",a.getNbParticipants());
        
        // generate array of number of participants for nrJours days ahead 
        JsonArray participantsParDate = new JsonArray();
        for (LocalDate d = LocalDate.now(); d.compareTo(LocalDate.now().plusDays(nrJours)) < 0; d = d.plusDays(1)) {
            for (MomentJournee m: MomentJournee.values()) {
                if (!(d.equals(LocalDate.now()) && LocalDateTime.now().getHourOfDay() >= 12 && m == MomentJournee.MATIN ) &&
                    !(d.equals(LocalDate.now()) && LocalDateTime.now().getHourOfDay() >= 18 && (m == MomentJournee.MATIN || m == MomentJournee.APRES_MIDI))) {
                    JsonObject participantsUneDate = new JsonObject();
                    participantsUneDate.addProperty("date", d.toString());
                    participantsUneDate.addProperty("moment", m.name());
                    participantsUneDate.addProperty("momentOrder", m.ordinal());
                    try {
                        List<Demande> dems = SM.selectDemandesByActiviteEtDate(a.getId(), java.sql.Date.valueOf(d.toString()), m);
                        //add the participants
                        JsonArray listeParticipantsParDate = new JsonArray();
                        for(Demande de: dems){
                            JsonObject lesParticipants = new JsonObject();
                            lesParticipants.addProperty("nom",de.getUtilisateur().getNom()+" "+ de.getUtilisateur().getPrenom());
                            listeParticipantsParDate.add(lesParticipants);
                        }
                        participantsUneDate.addProperty("nr", dems.size());
                        participantsUneDate.add("participants",listeParticipantsParDate);
                        //check if the moment is free or not
                        List<Demande> demsUser = adherent.getDemandesEnAttenteEtFutures();
                        List<Evenement> eventsUser = adherent.getEvenementsEnAttenteEtFuturs();
                        eventsUser.addAll(adherent.getEvenementsPretsFuturs());
                        String dejaParticipe = "Ce moment et cette date sont disponibles.";
                        for (Demande demande: demsUser) {
                            if ((demande.getDate().equals(Date.valueOf(d.toString()))) && (demande.getMoment() == m) && (demande.getActivite().getId().equals(a.getId()))) {
                                dejaParticipe = "Vous avez déjà fait une demande pour cette actvité, pour ce moment et cette journée.";
                                break;
                            } else if ((demande.getDate().equals(Date.valueOf(d.toString()))) && (demande.getMoment() == m)) {
                                dejaParticipe = "Vous avez fait une autre demande pour ce moment et cette journée: " + demande.getActivite().getDenomination() + ".";
                                break;
                            }
                        }
                        if (dejaParticipe.equals("Ce moment et cette date sont disponibles.")) {
                            for (Evenement evenement: eventsUser) {
                                if ((evenement.getDate().equals(Date.valueOf(d.toString()))) && (evenement.getMoment() == m) && (evenement.getActivite().getId().equals(a.getId()))) {
                                    dejaParticipe = "Vous avez déjà un évènement planifié pour cette actvité, pour ce moment et cette journée." ;
                                    break;
                                } else if ((evenement.getDate().equals(Date.valueOf(d.toString()))) && (evenement.getMoment() == m)) {
                                    dejaParticipe = "Vous avez un autre évènement planifié pour ce moment et cette journée: " + evenement.getActivite().getDenomination() + ".";
                                    break;
                                }
                            }
                        }
                        participantsUneDate.addProperty("statusMoment", dejaParticipe);
                    } catch (ServiceMetierException ex) {
                        Logger.getLogger(Jsonizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    participantsParDate.add(participantsUneDate);
                }
            }
        }
        jsonActivite.add("participantsParDate", participantsParDate);
        return jsonActivite;
    }
}
