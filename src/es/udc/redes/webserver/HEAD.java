/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

import java.io.Writer;

/**
 * The HEAD class implements handling of HTTP HEAD requests
 * @author carlos.torres
 */
public class HEAD implements HttpRequest {
    @Override
    public void respond(String request, Writer output) throws HttpException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
