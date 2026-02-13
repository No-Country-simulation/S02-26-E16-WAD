package com.elevideo.backend.service;

import java.util.Map;

/**
 * Servicio para el envío de correos electrónicos.
 */
public interface EmailService {

    /**
     * Envía un correo electrónico usando una plantilla HTML.
     *
     * @param to           Dirección de correo del destinatario
     * @param subject      Asunto del correo
     * @param templateName Nombre del archivo de plantilla (sin extensión)
     * @param variables    Variables para renderizar en la plantilla
     */
    void sendEmailTemplate(String to, String subject, String templateName, Map<String, Object> variables);

    /**
     * Envía un correo electrónico simple con texto HTML.
     *
     * @param to      Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param html    Contenido HTML del correo
     */
    void sendHtmlEmail(String to, String subject, String html);
}