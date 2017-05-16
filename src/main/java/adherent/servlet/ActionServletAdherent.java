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
package adherent.servlet;

import adherent.action.ActionDeconnecterAdherent;
import adherent.action.ActionEnregistrerDemande;
import adherent.action.ActionListerActivites;
import adherent.action.ActionListerHistorique;
import adherent.action.ActionListerInfosUtilisateur;
import adherent.action.ActionListerUneActivite;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fr.insalyon.dasi.collectif.dao.JpaUtil;
import fr.insalyon.dasi.collectif.metier.modele.Activite;
import fr.insalyon.dasi.collectif.metier.modele.Adherent;
import fr.insalyon.dasi.collectif.metier.modele.Demande;
import fr.insalyon.dasi.collectif.metier.modele.Evenement;
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.Jsonizer;

/**
 * The <code>ActionServlet</code> class is part of the "Controller" in the 
 * Model-View-Controller architecture of our web application.
 * <p>
 * It is used for interpreting the <code>HTTPServletRequest</code>, calling the 
 * associated <code>Action</code> and sending a <code>HTTPServletResponse</code>.
 * It is associated with the requests that come from the customer area.
 * 
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-13 
 */
@WebServlet(name = "ActionServletAdherent", urlPatterns = {"/ActionServletAdherent"})
public class ActionServletAdherent extends HttpServlet {
    private final static Logger LOGGER = Logger.getLogger(ActionServletAdherent.class.getName());
    
    /**
     * Initializes the JPA utility is it wasn't already initialized in a different servlet.
     * @throws ServletException 
     */
    @Override
    public void init() throws ServletException {
        //intialise the entity manager factory
        if (!JpaUtil.isInitialized()) {
            JpaUtil.init();
        }
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request.
     * @param response servlet response.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String todo = request.getParameter("action");
        Adherent user = (Adherent) request.getSession().getAttribute("user"); 
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy");
        
        //logging
        LOGGER.log(Level.INFO, "processing action: {0}", todo);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        JsonObject container = new JsonObject();
        
        switch(todo) {
            case "listerInfos"        : {
                new ActionListerInfosUtilisateur().execute(request, response);
                List<Evenement> events = (List<Evenement>) request.getAttribute("eventsReadyOrWaiting");
                List<Demande> demands = (List<Demande>) request.getAttribute("demandsWaiting");
                container.add("adherent", Jsonizer.jsonAdherent(user));
                container.add("demandes", Jsonizer.jsonListDemandesEtEvenements(demands, events));
                out.println(gson.toJson(container));
                break;
            }
            case "listerActivites"    : {
                try {
                    new ActionListerActivites().execute(request, response);
                    List<Activite> activites = (List<Activite>) request.getAttribute("activities");
                    container.add("activites",Jsonizer.jsonListActivites(activites));
                    out.println(gson.toJson(container));
                } catch (ServiceMetierException ex) {
                    Logger.getLogger(ActionServletAdherent.class.getName()).log(Level.SEVERE, null, ex);
                    out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Problem de base de données, veuillez réessayer plus tard.", "")));
                }
                break;
            }
            case "listerHistorique"   : {
                new ActionListerHistorique().execute(request, response);
                List<Demande> demands = (List<Demande>) request.getAttribute("histoDemands");
                List<Evenement> events = (List<Evenement>) request.getAttribute("histoEvents");
                out.println(gson.toJson(Jsonizer.jsonListDemandesEtEvenements(demands, events)));
                break;
            }
            case "listerUneActivite"  : {   
                try {
                    int nrDays = Integer.parseInt(request.getParameter("nrDays"));
                    new ActionListerUneActivite().execute(request, response);
                    Activite ac = (Activite) request.getAttribute("activity");
                    container.add("activite", Jsonizer.jsonActivite(user, ac, nrDays));
                    out.println(gson.toJson(container));
                } catch (ServiceMetierException ex) {
                    Logger.getLogger(ActionServletAdherent.class.getName()).log(Level.SEVERE, null, ex);
                    switch(ex.getStatus()) {
                        case UNDEFINED      :
                            out.println(gson.toJson(Jsonizer.jsonOperationResult(false, ex.getCause().getMessage(), "")));
                            break;
                        default             :
                            out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Problem de base de données, veuillez réessayer plus tard.", "")));
                            break;
                    }
                } catch (NumberFormatException ex) {
                    Logger.getLogger(ActionServletAdherent.class.getName()).log(Level.SEVERE, null, ex);
                    out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Le nombre de jours: \'" + request.getParameter("nrDays") + "\' n\'est pas un nombre valid.", "")));
                }
                break;
            }
            case "enregistrerDemande" : {
                try {
                    new ActionEnregistrerDemande().execute(request, response);
                    Demande d = (Demande) request.getAttribute("demandAdded");
                    out.println(gson.toJson(Jsonizer.jsonOperationResult(true, "Votre demande pour \'" + d.getActivite().getDenomination() + "\', pour la date " 
                                            + format.format(d.getDate()) + " a été enregistrée! Consulter votre historique pour voir son progrès.", "accueil.html")));
                } catch (ServiceMetierException ex) {
                    Logger.getLogger(ActionServletAdherent.class.getName()).log(Level.SEVERE, null, ex);
                    switch(ex.getStatus()) {
                        case UNDEFINED      :
                            out.println(gson.toJson(Jsonizer.jsonOperationResult(false, ex.getCause().getMessage(), "")));
                            break;
                        case DOUBLE_DEMANDE :
                            out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Vous avez déjà une demande sur cette activité et cette date. Veuillez changer la date ou l\'activité.", "")));
                            break;
                        default             :
                            out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Problem de base de données, veuillez réessayer plus tard.", "")));
                            break;
                    }
                }
                break;
            }
            case "seDeconnecter"      : {
                new ActionDeconnecterAdherent().execute(request, response);
                response.sendRedirect("./index.html");
                break;
            }
            default                   : {
                out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Aucune implementation trouvée pour l'action demandé.", "")));
                break;
            }
        }
        
        out.close();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    /**
     * Destroys the JPA utility if it wasn't already destroyed in a different servlet.
     */
    @Override
    public void destroy() {
        //destroy the entity manager factory 
        if (JpaUtil.isInitialized()) {
            JpaUtil.destroy();
        }
    }
}
