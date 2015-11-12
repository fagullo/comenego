/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.labidiomas.corpus.exception;

/**
 *
 * @author paco
 */
public class LoginException extends Exception {

    public LoginException() {
    }

    public LoginException(String string) {
        super(string);
    }

    public LoginException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public LoginException(Throwable thrwbl) {
        super(thrwbl);
    }

    public LoginException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }
}
