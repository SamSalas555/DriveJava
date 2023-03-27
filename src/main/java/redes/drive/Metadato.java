/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package redes.drive;

import java.io.Serializable;

public class Metadato implements Serializable {
        private String opcion;
        private String contenido;
        private String adicional;
        
        public Metadato(String opcion, String contenido,String adicional){
            this.opcion = opcion;
            this.contenido = contenido;
            this.adicional = adicional;
        }

    public String getOpcion() {
        return opcion;
    }

    public String getContenido() {
        return contenido;
    }

    public String getAdicional() {
        return adicional;
    }
        
        
        
}
