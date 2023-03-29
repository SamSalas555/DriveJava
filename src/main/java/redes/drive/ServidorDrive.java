/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package redes.drive;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorDrive {
    public static void main(String[] args) {
        try {
            //Creamos el socket para metadatos
            ServerSocket servidorMeta = new ServerSocket(8000);
            servidorMeta.setReuseAddress(true);
            System.out.println("Servidor meta iniciado, esperando clientes");
            ServerSocket servidorArchivo = new ServerSocket(8001);
            servidorArchivo.setReuseAddress(true);

            System.out.println("Servidor archivos iniciado, esperando clientes");
            ArchivoDrive arD = new ArchivoDrive();
            File carpeta = new File("");
            File carpeta2 = new File(carpeta.getAbsolutePath() + File.separator + "carpetaServidor");
            carpeta2.setWritable(true);
            File archivo;
            Metadato metEnviar;
            long tam, recibidos;
            byte[] buffer = new byte[1500];
            int l;
            ObjectInputStream objetoEntrada;
            ObjectOutputStream objetoSalida;


            for (; ; ) {
                Socket clienteMeta = servidorMeta.accept();
                System.out.println("Cliente conectado desde: " + clienteMeta.getInetAddress()
                        + " : " + clienteMeta.getPort());
                objetoSalida = new ObjectOutputStream(clienteMeta.getOutputStream());
                objetoEntrada = new ObjectInputStream(clienteMeta.getInputStream());
                Metadato metD1 = (Metadato) objetoEntrada.readObject();
                System.out.println("Cliente solicitando la opcion: " + metD1.getOpcion());
                if (metD1.getOpcion().equals("2")) {
                    metEnviar = new Metadato("2", arD.verContenidoCarpeta(carpeta2, 0), "");
                    objetoSalida.writeObject(metEnviar);
                    objetoSalida.flush();
                } else if (metD1.getOpcion().equals("4")) {
                    File nuevaCarpeta = new File(carpeta2.getAbsoluteFile() + File.separator + metD1.getContenido());
                    if (nuevaCarpeta.mkdirs()) {
                        metEnviar = new Metadato("4", "Carpeta creada con exito", "");
                    } else {
                        metEnviar = new Metadato("4", "No se pudo crear la carpeta", "");
                    }
                    objetoSalida.writeObject(metEnviar);
                    objetoSalida.flush();
                } else if (metD1.getOpcion().equals("6")) {
                    archivo = new File(carpeta2.getAbsolutePath() + File.separator + metD1.getContenido());

                    if (archivo.exists()) {

                        metEnviar = new Metadato("6", "existe", "");
                        objetoSalida.writeObject(metEnviar);
                        objetoSalida.flush();
                    } else {

                        metEnviar = new Metadato("6", "no existe", "");
                        objetoSalida.writeObject(metEnviar);
                        objetoSalida.flush();
                        Socket clA = servidorArchivo.accept();
                        System.out.println("Cliente de archivo conectado desde " + clA.getInetAddress() + ":" + clA.getPort());
                        if (metD1.getAdicional().equals("directorio")) {
                            DataInputStream nuevoDis = new DataInputStream(clA.getInputStream());
                            ArchivoDrive.recibirCarpeta(carpeta2.getAbsolutePath(), nuevoDis);
                            System.out.println("Transferencia finalizada");
                            nuevoDis.close();
                            clA.close();

                        } else {
                            DataInputStream entradaArchivo = new DataInputStream(clA.getInputStream());
                            String nombre = entradaArchivo.readUTF();
                            tam = entradaArchivo.readLong();
                            System.out.println("Comienza descarga del archivo " + nombre + " de " + tam + " bytes\n\n");
                            carpeta2.setWritable(true);

                            DataOutputStream dos = new DataOutputStream(new FileOutputStream(archivo.getAbsolutePath()));
                            recibidos = 0;
                            l = 0;
                            while (recibidos < tam) {
                                byte[] b = new byte[1500];
                                l = entradaArchivo.read(b);

                                dos.write(b, 0, l);
                                dos.flush();
                                recibidos = recibidos + l;

                            }
                            System.out.println("Archivo recibido..");
                            dos.close();
                            entradaArchivo.close();
                            clA.close();
                            metEnviar = new Metadato("6", "archivo subido con exito", "");
                            objetoSalida.writeObject(metEnviar);
                            objetoSalida.flush();
                        }
                    }


                } else if (metD1.getOpcion().equals("7")) {
                    String locacion = metD1.getContenido();
                    File fi = new File(carpeta2.getAbsolutePath() + File.separator + locacion);
                    System.out.println(fi.getAbsolutePath());
                    String existe = "";
                    if (fi.exists()) existe = "existe";
                    else existe = "no existe";
                    metEnviar = new Metadato("7", existe, "");
                    objetoSalida.writeObject(metEnviar);
                    objetoSalida.flush();
                    if (fi.exists()) {
                        System.out.println("enviando archivo...");
                        Socket clA = servidorArchivo.accept();
                        DataOutputStream nuevoDos = new DataOutputStream(clA.getOutputStream());
                        ArchivoDrive.enviarCarpeta(fi, nuevoDos);
                        nuevoDos.close();
                        clA.close();
                        System.out.println("carpeta o archivo enviada");
                    }


                } else if (metD1.getOpcion().equals("9")) {
                    File selecionado = new File(carpeta2.getAbsoluteFile() + File.separator + metD1.getContenido());

                    ArchivoDrive.renombrarArchivoOCarpeta(selecionado.getAbsolutePath(), metD1.getAdicional());
                    if (ArchivoDrive.arRemotoRenombrado) {
                        metEnviar = new Metadato("9", "Carpeta o archivo renombrado con exito", "");
                    } else {
                        metEnviar = new Metadato("9", "No se pudo renombrar la carpeta o archivo", "");
                    }
                    ArchivoDrive.arRemotoRenombrado = false;
                    objetoSalida.writeObject(metEnviar);
                    objetoSalida.flush();
                }
                else if (metD1.getOpcion().equals("11")) {
                    File seleccionado1 = new File(carpeta2.getAbsoluteFile() + File.separator + metD1.getContenido());
                    if (seleccionado1.delete()) {
                        metEnviar = new Metadato("11", "Carpeta o borrado con exito", "");
                    } else {
                        metEnviar = new Metadato("11", "No se pudo borrar la carpeta o archivo", "");
                    }
                    objetoSalida.writeObject(metEnviar);
                    objetoSalida.flush();
                }
                objetoSalida.close();
                objetoEntrada.close();
                clienteMeta.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
