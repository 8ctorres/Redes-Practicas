/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.udc.redes.webserver;

/**
 * This exception class is used in the HttpRequest implementing classes
 * @author carlos.torres
 */
public class HttpException extends Exception{
    public HttpException(String str){
        super(str);
    }
}
