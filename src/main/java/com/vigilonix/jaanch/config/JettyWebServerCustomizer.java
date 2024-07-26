package com.vigilonix.jaanch.config;

import org.eclipse.jetty.server.CustomRequestLog;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyWebServerCustomizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {
    @Override
    public void customize(JettyServletWebServerFactory factory) {
        factory.getConfigurations();
        factory.addServerCustomizers(server -> {
            if (server.getRequestLog() instanceof CustomRequestLog) {
                CustomRequestLog existingRequestLog = (CustomRequestLog) server.getRequestLog();
                CustomRequestLog customRequestLog = new CustomRequestLog(existingRequestLog.getWriter(), CustomRequestLog.NCSA_FORMAT + " %{ms}T");
                server.setRequestLog(customRequestLog);
            }
        });
    }
}
