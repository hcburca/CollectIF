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
package admin.servlet;

import admin.action.ActionListerInfosPourAdmin;
import admin.action.ActionValiderEvenement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fr.insalyon.dasi.collectif.dao.JpaUtil;
import fr.insalyon.dasi.collectif.metier.modele.Evenement;
import fr.insalyon.dasi.collectif.metier.modele.EvenementGratuit;
import fr.insalyon.dasi.collectif.metier.modele.EvenementPayant;
import fr.insalyon.dasi.collectif.metier.modele.Lieu;
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
 *
 * @author Horia
 */
@WebServlet(name = "ActionServletAdmin", urlPatterns = {"/ActionServletAdmin"})
public class ActionServletAdmin extends HttpServlet {
    private final static Logger LOGGER = Logger.getLogger(ActionServletAdmin.class.getName());
    
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
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy");
        
        //logging
        LOGGER.log(Level.INFO, "processing action for admin: {0}", todo);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        
        switch(todo) {
            case "listerEvenementsAttente" : {
                try {
                    new ActionListerInfosPourAdmin().execute(request, response);
                    List<Evenement> events = (List<Evenement>) request.getAttribute("eventsWaiting");
                    List<Lieu> places = (List<Lieu>) request.getAttribute("places");
                    JsonObject container = new JsonObject();
                    container.add("events", Jsonizer.jsonEvenementsAdmin(events));
                    container.add("places", Jsonizer.jsonLieux(places));
                    out.println(gson.toJson(container));
                } catch (ServiceMetierException ex) {
                    Logger.getLogger(ActionServletAdmin.class.getName()).log(Level.SEVERE, null, ex);
                    out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Problem de base de données, veuillez réessayer plus tard.", "")));
                }
                break;
            }
            case "validerEvenement"        : {
                try {
                    new ActionValiderEvenement().execute(request, response);
                    Evenement ev = (Evenement) request.getAttribute("eventReady");
                    if (ev instanceof EvenementGratuit) {
                        out.println(gson.toJson(Jsonizer.jsonOperationResult(true, "Succes! L'evenement: " + ev.getActivite().getDenomination() + ", " 
                                            + format.format(ev.getDate()) + " (" + ev.getMoment() + ") aura lieu a l'adresse: " + ev.getLieu().getAdresse() + ".", "admin.html")));
                    } else {
                        out.println(gson.toJson(Jsonizer.jsonOperationResult(true, "Succes! L'evenement: " + ev.getActivite().getDenomination() + ", " 
                                            + format.format(ev.getDate()) + " (" + ev.getMoment() + ") aura lieu a l'adresse: " 
                                            + ev.getLieu().getAdresse() + ", avec une PAF de: " + ((EvenementPayant)ev).getPAF() + ".", "admin.html")));
                    }
                } catch (ServiceMetierException ex) {
                    Logger.getLogger(ActionServletAdmin.class.getName()).log(Level.SEVERE, null, ex);
                    switch(ex.getStatus()) {
                        case UNDEFINED      :
                            out.println(gson.toJson(Jsonizer.jsonOperationResult(false, ex.getCause().getMessage(), "")));
                            break;
                        default             :
                            out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Problem de base de données, veuillez réessayer plus tard.", "")));
                            break;
                    }
                }
                break;
            }
            default                        : {
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
