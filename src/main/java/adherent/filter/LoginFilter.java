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
package adherent.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The <code>LoginFilter</code> class is used to control user authentication 
 * for the pages that require it.
 * 
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-15
 */
@WebFilter(urlPatterns = {"/accueil.html", "/ActionServletAdherent", "/demande.html", "/historique.html"})
public class LoginFilter implements Filter {
    private final static Logger LOGGER = Logger.getLogger(LoginFilter.class.getName());
    /**
     * Initializes the parameters of the class, if any.
     * @param fc the filter configuration
     * @throws ServletException 
     */
    @Override
    public void init(FilterConfig fc) throws ServletException {
        //no parameters to initialize
    }
    
    /**
     * Verifies that the <code>ServletRequest</code> passed as parameter has an associated 
     * session and a "user", if it's the case it sends the request to the <code>FilterChain</code>,
     * otherwise responds with a redirection to the login page.
     * @param sr  the request
     * @param sr1 the response
     * @param fc  the <code>FilterChain</code>
     * @throws IOException
     * @throws ServletException 
     */
    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) sr;
        HttpServletResponse response = (HttpServletResponse) sr1;
        HttpSession session = request.getSession(false);
        
        // logging
        LOGGER.log(Level.INFO, "Filtering request with action: {0} on URL: {1}", new Object[]{request.getParameter("action"), request.getRequestURI()});
        LOGGER.log(Level.INFO, "Session exists? {0}", (session != null));
        if (session != null) {
            LOGGER.log(Level.INFO, "Session has user? {0}, email: {1}", new Object[]{(session.getAttribute("user") != null), session.getAttribute("email")});
        }
        
        if ((session == null || session.getAttribute("user") == null)) {
            response.sendRedirect("./index.html");
        } else {
            //        avoid caching of user-resources
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");

            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            //For proxies
            response.setDateHeader("Expires", 0);
            
            fc.doFilter(sr, sr1);
        }
    }
    
    /**
     * Cleans up the allocated resources, if any
     */
    @Override
    public void destroy() {
        //no resources to destroy
    }
    
}
