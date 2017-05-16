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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>ActionDeconnecterAdherent</code> class is used as an adapter between the 
 * <code>HTTPServletRequest</code> to disconnect and the associated business service.
 *
 * @author  Burca Horia
 * @version 1.0
 * @since   2017-04-15
 */
public class ActionDeconnecterAdherent extends Action {
    
    /**
     * Removes the <code>Adherent</code> from the session and invalidates it.
     * @param request
     * @param response 
     */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute("user");
        request.getSession().invalidate();
    }
}
