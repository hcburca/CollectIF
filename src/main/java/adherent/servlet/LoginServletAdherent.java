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

import adherent.action.ActionConnecterAdherent;
import adherent.action.ActionEnregistrerAdherent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.insalyon.dasi.collectif.dao.JpaUtil;
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import fr.insalyon.dasi.collectif.util.StatutService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.Jsonizer;

/**
 * The <code>LoginServlet</code> class is part of the "Controller" in the 
 * Model-View-Controller architecture of our web application.
 * <p>
 * It is used for interpreting the <code>HTTPServletRequest</code>, calling the 
 * associated <code>Action</code> and sending a <code>HTTPServletResponse</code>.
 * It is associated with the requests of login or register.
 * 
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-17 
 */
@WebServlet(name = "LoginServletAdherent", urlPatterns = {"/LoginServletAdherent"})
public class LoginServletAdherent extends HttpServlet {
    private final static Logger LOGGER = Logger.getLogger(LoginServletAdherent.class.getName());
    
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
        String action = request.getParameter("action");
        // logging
        LOGGER.log(Level.INFO, "Processing action: {0}", action);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
        
        switch(action) {
            case "seConnecter"  : {
                try {
                    new ActionConnecterAdherent().execute(request, response);
                    out.println(gson.toJson(Jsonizer.jsonOperationResult(true, "Connecté avec succès.", "accueil.html")));
                } catch (ServiceMetierException ex) {
                    Logger.getLogger(LoginServletAdherent.class.getName()).log(Level.SEVERE, null, ex);
                    if (ex.getStatus() == StatutService.INVALIDE_IDANTIFIANT) {
                        out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Email non-valide.", "")));
                    } else {
                        out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Problem de base de données, veuillez réessayer plus tard.", "")));
                    }
                }
                break;
            }
            case "sEnregistrer" : {
                try {
                    new ActionEnregistrerAdherent().execute(request, response);
                    out.println(gson.toJson(Jsonizer.jsonOperationResult(true, "Veuillez verifier votre email pour savoir l'etat de votre inscription.", "index.html")));
                } catch (ServiceMetierException ex) {
                    Logger.getLogger(LoginServletAdherent.class.getName()).log(Level.SEVERE, null, ex);
                    if (ex.getStatus() == StatutService.EMAIL_DEJA_UTILISER) {
                        out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Il existe déjà un compte associé à ce email.", "")));
                    } else {
                        out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Problem de base de données, veuillez réessayer plus tard.", "")));
                    }
                } 
                break;
            }
            default             :
                out.println(gson.toJson(Jsonizer.jsonOperationResult(false, "Aucune implementation trouvée pour l'action demandé.", "")));
                break;
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
