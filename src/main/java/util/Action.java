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

import fr.insalyon.dasi.collectif.metier.service.ServiceMetier;
import fr.insalyon.dasi.collectif.util.ServiceMetierException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>Action</code> class is an abstract class that is extended by each of our actions.
 * <p>
 * It has a <code>ServiceMetier</code> as a member variable, and an abstract method 
 * <code>execute(HttpServletRequest request, HttpServletResponse response)</code>.
 *
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-05-03
 */
public abstract class Action {
    protected final ServiceMetier sm = new ServiceMetier();
    
    /**
     * Executes the business service associated with the current request.
     * <p>
     * Adds the results of the business service as an attribute of the request.
     * @param request
     * @param response  
     * @throws fr.insalyon.dasi.collectif.util.ServiceMetierException 
     */
    abstract public void execute(HttpServletRequest request, HttpServletResponse response) throws ServiceMetierException;
    
}
