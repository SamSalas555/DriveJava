/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package redes.drive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ArchivoDrive {
    public static boolean arRemotoRenombrado=false;

    public String verContenidoCarpeta(File direccion, int esp){
        String archivosL="";
        File[] archivos = direccion.listFiles();
        for(File archivo : archivos){
            if(archivo.isDirectory()){
                archivosL += "  ".repeat(esp) +">"+ archivo.getName()+"\n";
                archivosL += verContenidoCarpeta(archivo, esp +1);
            }
            else{
              // this.setArchivoList(this.getArchivoList()+"  ".repeat(espacio) + archivo.getName());
               // System.out.println("  ".repeat(espacio)+archivo.getName());
                archivosL += "  ".repeat(esp) +"|-"+ archivo.getName()+"\n";
            }
        }
        return archivosL;
    }

    public static void renombrarArchivoOCarpeta(String ruta, String nuevoNombre){
      
        File file = new File(ruta);
        String nombreActual = file.getName();
        int index = nombreActual.lastIndexOf(".");

        String nuevaRuta;
        
        if(index == -1){
            //Si se esta renombrando una carpeta
            nuevaRuta = file.getParent() + File.separator + nuevoNombre ;
        }else{
            //Si se esta renombrando un archivo
            nuevaRuta = file.getParent() + File.separator + nuevoNombre + nombreActual.substring(index);
        }

       File newFile = new File(nuevaRuta);
        
        if (file.renameTo(newFile)) {
            System.out.println("Archivo o carpeta renombrado correctamente");
            arRemotoRenombrado = true;
        } else {
            System.out.println("No se pudo renombrar el archivo o carpeta");
        }    

    }

    public static void copiarCarpeta(File origenCarpeta, File destinoCarpeta) throws IOException{

        
        if (origenCarpeta.isDirectory()) {
            
            if (!destinoCarpeta.exists()) {
                destinoCarpeta.mkdir();
            }
    
            String[] archivos = origenCarpeta.list();
            for (String archivo : archivos) {
                File srcArchivo = new File(origenCarpeta, archivo);
                File destArchivo = new File(destinoCarpeta, archivo);
                copiarCarpeta(srcArchivo, destArchivo);
            }
        } else {
            InputStream in = new FileInputStream(origenCarpeta);
            OutputStream out = new FileOutputStream(destinoCarpeta);
    
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
    
            in.close();
            out.close();
        }
    }

    //Funcion para enviar una carpeta y todo su contenido por el socket
    public static void recibirCarpeta(String ruta, DataInputStream dis) throws IOException {
        String nombreAr = dis.readUTF(); //leemos el nombre del archivo
        boolean esDirectorio = dis.readBoolean(); //leeemos si es un directorio
        File archivo = new File(ruta, nombreAr);
        if (esDirectorio) {
            archivo.mkdir();
            int numFiles = dis.readInt();
            for (int i = 0; i < numFiles; i++) {
                recibirCarpeta(archivo.getPath(), dis); // Llamada recursiva para subcarpetas
            }
        } else {
            long archivoTam = dis.readLong();
            byte[] buffer = new byte[4096];
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                    int count;
                    long totalBytesLeer = 0;
                    /*
                     * leemos la cantidad de bytes que faltan para alcanzar el tamaño total del
                     * archivo, o la cantidad de bytes que hay en el buffer si ese tamaño es menor.
                     * De esta manera, nos aseguramos de que leamos exactamente la cantidad correcta
                     * de bytes para cada archivo, lo que evita lanzar java.io.EOFException.
                     */
                    while (totalBytesLeer < archivoTam && (count = dis.read(buffer, 0, (int)Math.min(archivoTam - totalBytesLeer, buffer.length))) != -1) {
                        fos.write(buffer, 0, count);
                        totalBytesLeer += count;
                    }
            }
        }
        }
        
        //Funcion recursiva para enviar carpetas a traves del socket 
        public static void enviarCarpeta(File carpeta, DataOutputStream dos) throws IOException {
        dos.writeUTF(carpeta.getName());
        dos.writeBoolean(carpeta.isDirectory());
        if (carpeta.isDirectory()) {
           
            File[] archivos = carpeta.listFiles();
            dos.writeInt(archivos.length);
            for (File archivo : archivos) {
                enviarCarpeta(archivo, dos); // Llamada recursiva para subcarpetas
            }
        } else {
            dos.writeLong(carpeta.length());
            byte[] buffer = new byte[4096];
            try (FileInputStream fis = new FileInputStream(carpeta)) {
                int contar;
                while ((contar = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, contar);
                }
            }
        }
    }

}
