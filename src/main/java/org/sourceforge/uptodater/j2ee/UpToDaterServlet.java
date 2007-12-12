package org.sourceforge.uptodater.j2ee;

import javax.servlet.*;
import java.io.IOException;

/* 
 * Created Date: Jan 10, 2005
 */

/**
 * A servlet wrapper for UpToDater.  See documentation for a sample deployment descriptor.
 * @author rapruitt
 */
public class UpToDaterServlet extends ServletContextRunner implements Servlet {

    ServletConfig cfg;
    public ServletConfig getServletConfig() {
        return cfg;
    }

    ServletContext getServletContext() {
        return cfg.getServletContext();
    }

    public void service(ServletRequest servletRequest, ServletResponse servletResponse)
            throws ServletException, IOException {
/*
        String action = servletRequest.getParameter("action");
        if ("getFailed".equals(action)){            
        } else if ("update".equals(action)){
        } else if ("getUnapplied".equals(action)){
        }
*/
    }

    public String getServletInfo() {
        return "UpToDater";
    }

    public void destroy() {
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        this.cfg = servletConfig;
        doUpdate();
    }
}
