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
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>ActionConnecterAdherent</code> class is used as an adapter between the 
 * <code>HTTPServletRequest</code> to login and the associated business service.
 *
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-13 
 */
public class ActionConnecterAdherent extends Action {
    
    /**
     * Executes the <code>selectAdherentByEmail</code> service associated to the 
     * <code>HttpServletRequest</code> and connects this <code>Adherent</code> to the server
     * by creating a new <code>HttpSession</code> for him.
     * @param request
     * @param response
     * @throws fr.insalyon.dasi.collectif.util.ServiceMetierException
     */
    @Override
    public void execute (HttpServletRequest request, HttpServletResponse response) throws ServiceMetierException {
        String email = request.getParameter("email");
        
        Adherent adherent = sm.selectAdherentByEmail(email);
        request.getSession().setAttribute("user", adherent);
        request.getSession().setAttribute("email", adherent.getMail());
    }
}
