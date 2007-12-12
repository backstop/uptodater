package org.sourceforge.uptodater.j2ee;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

/**
 * Configure in your web.xml.
 */
public class UpToDaterApplicationListener extends ServletContextRunner implements ServletContextListener {
    private ServletContext ctx;

    public void contextInitialized(ServletContextEvent event) {
        ctx = event.getServletContext();
        doUpdate();
    }

    ServletContext getServletContext() {
        return ctx;
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
