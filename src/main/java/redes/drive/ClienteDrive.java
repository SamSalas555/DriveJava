/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package redes.drive;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JFileChooser;

public class ClienteDrive {
    public static void main(String[] args) {
        File carpeta = new File("");
        File carpeta2 = new File(carpeta.getAbsolutePath() + File.separator + "carpetaCliente");

        JFileChooser fileChooser = new JFileChooser(carpeta2);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        BufferedReader entradaTeclado = new BufferedReader(new InputStreamReader(System.in));
        long tam, enviados = 0;
        int opcion = 0, opcion2 = 0, l = 0, fichero;
        boolean salir = false;
        String nombreCar = " "; //nombre para crear y elimar carpetas
        String nombreArchivo = " "; //nombre para crear y eliminar archivos
        File nuevaCarpeta, archivo; // objeto archivoexistente para crear y elimar carpetas
        Socket clienteMeta; //objeto Socket para usar en las diferentes opciones
        Socket clienteArchivos; //Socket para usar al enviar archivos a la carpeta local
        ObjectInputStream objetoEntrada; // para recibir metadatos
        ObjectOutputStream objetoSalida; // para enviar metadatos
        FileInputStream archivoEntrada;
        FileOutputStream archivoSalida;
        DataOutputStream datoSalida;
        DataInputStream datoEntrada;
        Metadato metRecibido;
        Metadato metD; //metadato para enviar     
        byte[] buffer = new byte[1500];


        try {
            do {
                menu();
                String op = entradaTeclado.readLine();
                opcion = Integer.parseInt(op);

                switch (opcion) {
                    //Visualizar carpeta local
                    case 1:
                        System.out.println("1. Visualizar carpeta local");
                        ArchivoDrive arD;
                        System.out.println(carpeta2.getAbsolutePath());
                        arD = new ArchivoDrive();
                        System.out.println(arD.verContenidoCarpeta(carpeta2, opcion));

                        break;
                    //Visualizar carpeta remota
                    case 2:
                        System.out.println("2. Visualizar carpeta remota");
                        clienteMeta = new Socket("127.0.0.1", 8000);
                        System.out.println("Conexion exitosa con el servidor");
                        metD = new Metadato("2", "", "");
                        objetoSalida = new ObjectOutputStream(clienteMeta.getOutputStream());
                        objetoEntrada = new ObjectInputStream(clienteMeta.getInputStream());
                        objetoSalida.writeObject(metD);
                        objetoSalida.flush();
                        metRecibido = (Metadato) objetoEntrada.readObject();
                        System.out.println("Contenido carpeta del servidor");
                        System.out.println(metRecibido.getContenido());
                        objetoSalida.close();
                        objetoEntrada.close();
                        clienteMeta.close();
                        break;
                    //Crear carpeta local
                    case 3:
                        System.out.println("3. Crear carpeta local");
                        System.out.println("Si quieres crear una carpeta dentro de una carpeta de tu carpeta local\n"
                                + "por ejemplo si hay una carpeta llamada dos y quieres crear una carpeta llamada dosA dentro"
                                + "debes introducir lo siguiente: dos\\dosA, de lo contrario solo introduce el nombre de la carpeta ");
                        System.out.println("Introduce el nombre:  ");
                        nombreCar = entradaTeclado.readLine();
                        nuevaCarpeta = new File(carpeta2.getAbsoluteFile() + File.separator + nombreCar);
                        if (nuevaCarpeta.mkdirs()) {
                            System.out.println(" Carpeta creada con exito ");
                        } else {
                            System.out.println("No se pudo crear la carpeta");
                        }
                        break;
                    //Crear carpeta remota
                    case 4:
                        System.out.println("4. Crear carpeta remota");
                        System.out.println("Si quieres crear una carpeta dentro de una carpeta la carpeta del servidorl\n"
                                + "por ejemplo si hay una carpeta llamada dos y quieres crear una carpeta llamada dosA dentro"
                                + "debes introducir lo siguiente: dos\\dosA, de lo contrario solo introduce el nombre de la carpeta ");
                        System.out.println("Introduce el nombre:  ");
                        nombreCar = entradaTeclado.readLine();
                        clienteMeta = new Socket("127.0.0.1", 8000);
                        System.out.println("Conexion exitosa con el servidor");
                        objetoSalida = new ObjectOutputStream(clienteMeta.getOutputStream());
                        objetoEntrada = new ObjectInputStream(clienteMeta.getInputStream());
                        metD = new Metadato("4", nombreCar, "");
                        objetoSalida.writeObject(metD);
                        objetoSalida.flush();
                        metRecibido = (Metadato) objetoEntrada.readObject();
                        System.out.println(metRecibido.getContenido());
                        objetoSalida.close();
                        objetoEntrada.close();
                        clienteMeta.close();
                        break;
                    //Subir archivo o carpeta a la carpeta local
                    case 5:
                        System.out.println("5. Subir archivo o carpeta a la carpeta local");
                        menu2();
                        String op2 = entradaTeclado.readLine();
                        opcion2 = Integer.parseInt(op2);
                        switch (opcion2) {
                            //Crear un archivo nuevo
                            case 1:
                                System.out.println("Introduce el nombre del archivo a crear en la carpeta local:  ");
                                nombreArchivo = entradaTeclado.readLine();
                                archivo = new File(carpeta2.getAbsoluteFile() + File.separator + nombreArchivo);
                                if (archivo.createNewFile()) {
                                    System.out.println("Nuevo archivo creado con éxito.");
                                } else {
                                    System.out.println("El archivo ya existe.");
                                }
                                break;
                            //Subir archivo o carpeta existente
                            case 2:
                                fichero = fileChooser.showOpenDialog(null);
                                if (fichero == JFileChooser.APPROVE_OPTION) {

                                    File file = fileChooser.getSelectedFile();
                                    //subir carpeta
                                    if (file.isDirectory()) {
                                        String nomCar = file.getName();
                                        File nuevacarpeta = new File(carpeta2, nomCar);
                                        if (nuevacarpeta.mkdir()) {
                                            ArchivoDrive.copiarCarpeta(file, nuevacarpeta);
                                            System.out.println("Carpeta copiada con exito");
                                        } else {
                                            System.out.println("Error");
                                        }

                                        //subir archivo
                                    } else {
                                        archivoEntrada = new FileInputStream(file.getAbsolutePath());
                                        archivoSalida = new FileOutputStream(carpeta2.getAbsolutePath() + File.separator + file.getName());
                                        tam = file.length();
                                        enviados = 0;
                                        while (enviados < tam) {
                                            l = archivoEntrada.read(buffer);
                                            archivoSalida.write(buffer, 0, l);
                                            archivoSalida.flush();
                                            enviados = enviados + l;


                                        }

                                        archivoEntrada.close();
                                        archivoSalida.close();
                                        System.out.println("Archivo o carpeta copiado con éxito.");
                                    }

                                } else {
                                    System.out.println("No se ha seleccionado ningún archivo o carpeta.");
                                }

                                break;
                        }
                        break;
                    //Subir archivo o carpeta a la carpeta remota
                    case 6:
                        System.out.println("6. Subir archivo o carpeta de la carpeta local a la carpeta remota");
                        clienteMeta = new Socket("127.0.0.1", 8000);
                        System.out.println("Conexion exitosa con el servidor");
                        objetoSalida = new ObjectOutputStream(clienteMeta.getOutputStream());
                        objetoEntrada = new ObjectInputStream(clienteMeta.getInputStream());


                        fichero = fileChooser.showOpenDialog(null);
                        if (fichero == JFileChooser.APPROVE_OPTION) {

                            File file = fileChooser.getSelectedFile();
                            String nomCar = file.getName();
                            String tipo = "";
                            if (file.isDirectory()) tipo = "directorio";
                            else tipo = "archivo";
                            metD = new Metadato("6", nomCar, tipo);
                            objetoSalida.writeObject(metD);
                            objetoSalida.flush();
                            metRecibido = (Metadato) objetoEntrada.readObject();


                            if (metRecibido.getContenido().equals("existe")) {
                                System.out.println("La carpeta o el archivo ya existen en la carpeta remota");
                            } else {

                                if (file.isDirectory()) {

                                    clienteArchivos = new Socket("127.0.0.1", 8001);
                                    DataOutputStream nuevoDos = new DataOutputStream(clienteArchivos.getOutputStream());
                                    ArchivoDrive.enviarCarpeta(file, nuevoDos);
                                    nuevoDos.close();
                                    clienteArchivos.close();
                                    System.out.println("carpeta enviada");


                                    //subir archivo
                                } else {
                                    System.out.println("subiendo archivo");
                                    clienteArchivos = new Socket("127.0.0.1", 8001);
                                    DataOutputStream salidaSocketArchivos = new DataOutputStream(clienteArchivos.getOutputStream());
                                    System.out.println("archivo enviar: " + file.getAbsolutePath());
                                    DataInputStream leerArchivo = new DataInputStream(new FileInputStream(file.getAbsolutePath()));
                                    salidaSocketArchivos.writeUTF(file.getName());
                                    salidaSocketArchivos.flush();
                                    tam = file.length();
                                    salidaSocketArchivos.writeLong(tam);
                                    salidaSocketArchivos.flush();

                                    enviados = 0;
                                    l = 0;
                                    while (enviados < tam) {
                                        byte[] b = new byte[1500];
                                        l = leerArchivo.read(b);
                                        salidaSocketArchivos.write(b, 0, l);
                                        salidaSocketArchivos.flush();
                                        enviados = enviados + l;

                                    }
                                    System.out.println("\nArchivo enviado..");
                                    leerArchivo.close();
                                    salidaSocketArchivos.close();
                                    clienteArchivos.close();
                                    metRecibido = (Metadato) objetoEntrada.readObject();

                                    System.out.println(metRecibido.getContenido());


                                }
                            }
                        } else {
                            System.out.println("No se ha seleccionado ningún archivo o carpeta.");
                        }

                        objetoSalida.close();
                        objetoEntrada.close();
                        clienteMeta.close();


                        break;
                    //Descargar archivo o carpeta de la carpeta remota
                    case 7:
                        System.out.println("7. Descargar archivo o carpeta");

                        System.out.println("Si quieres descargar una carpeta o archivo que esta dentro de una carpeta la carpeta del servidor\n"
                                + "por ejemplo si hay una carpeta llamada Carpeta1 y quieres descargar una carpeta o archivo llamada Carpeta2 dentro"
                                + "debes introducir lo siguiente: Carpeta1 + Separator + Carpeta2, de lo contrario solo introduce el nombre de la carpeta o archivo ");

                        System.out.println("Para descargar un archivo debes introducir la extención!!");
                        System.out.println("Introduce el nombre:  ");
                        nombreCar = entradaTeclado.readLine();

                        File carloc = new File(carpeta2.getAbsolutePath() + File.separator + nombreCar);
                        if (carloc.exists()) {
                            System.out.println("Ya hay un archivo en la carpeta o archivo local con ese nombre");
                        } else {
                            clienteMeta = new Socket("127.0.0.1", 8000);
                            System.out.println("Conexion exitosa con el servidor");
                            objetoSalida = new ObjectOutputStream(clienteMeta.getOutputStream());
                            objetoEntrada = new ObjectInputStream(clienteMeta.getInputStream());
                            metD = new Metadato("7", nombreCar, "");
                            objetoSalida.writeObject(metD);
                            objetoSalida.flush();
                            metRecibido = (Metadato) objetoEntrada.readObject();
                            if (metRecibido.getContenido().equals("existe")) {
                                System.out.println("Descargando archivo...");
                                clienteArchivos = new Socket("127.0.0.1", 8001);
                                DataInputStream nuevoDis = new DataInputStream(clienteArchivos.getInputStream());
                                ArchivoDrive.recibirCarpeta(carpeta2.getAbsolutePath(), nuevoDis);
                                System.out.println("Transferencia finalizada");
                                nuevoDis.close();
                                clienteArchivos.close();
                            } else {
                                System.out.println("no existe el archivo o carpeta");
                            }

                            objetoSalida.close();
                            objetoEntrada.close();
                            clienteMeta.close();
                        }
                        break;
                    //Remobrar archivos o carpetas locales
                    case 8:
                        fichero = fileChooser.showOpenDialog(null);
                        if (fichero == JFileChooser.APPROVE_OPTION) {
                            System.out.println("Selecciona el archivo o carpeta a renombrar:  ");
                            File seleccionado = fileChooser.getSelectedFile();
                            System.out.println("Introduce el nuevo nombre del archivo con la extencion del archivo  :  ");
                            nombreArchivo = entradaTeclado.readLine();

                            ArchivoDrive.renombrarArchivoOCarpeta(seleccionado.getAbsolutePath(), nombreArchivo);

                        } else {
                            System.out.println("No se ha seleccionado ningún archivo o carpeta.");
                        }
                        break;
                    //Renombrar archivos o carpetas remotas
                    case 9:
                        System.out.println("9. Renombrar archivo remoto");
                        System.out.println("Si quieres renombrar una carpeta o archivo que esta dentro de una carpeta la carpeta del servidor\n"
                                + "por ejemplo si hay una carpeta llamada dos y quieres renombrar una carpeta o archivo llamada dosA dentro"
                                + "debes introducir lo siguiente: dos\\dosA, de lo contrario solo introduce el nombre de la carpeta o archivo ");

                        System.out.println("Para renombrar un archivo debes introducir la extención!!");
                        System.out.println("Introduce el nombre:  ");
                        nombreCar = entradaTeclado.readLine();

                        System.out.println("Introduce el nuevo nombre:  ");
                        String nuevoNombre = entradaTeclado.readLine();
                        clienteMeta = new Socket("127.0.0.1", 8000);
                        System.out.println("Conexion exitosa con el servidor");
                        objetoSalida = new ObjectOutputStream(clienteMeta.getOutputStream());
                        objetoEntrada = new ObjectInputStream(clienteMeta.getInputStream());
                        metD = new Metadato("9", nombreCar, nuevoNombre);
                        objetoSalida.writeObject(metD);
                        objetoSalida.flush();
                        metRecibido = (Metadato) objetoEntrada.readObject();
                        System.out.println(metRecibido.getContenido());
                        objetoSalida.close();
                        objetoEntrada.close();
                        clienteMeta.close();
                        break;
                    case 10:
                        fichero = fileChooser.showOpenDialog(null);
                        if (fichero == JFileChooser.APPROVE_OPTION) {
                            System.out.println("Selecciona el archivo o carpeta a borrar:  ");
                            File seleccionado = fileChooser.getSelectedFile();
                            ArchivoDrive.borrarCarpeta(seleccionado.getAbsolutePath());

                        } else {
                            System.out.println("No se ha seleccionado ningún archivo o carpeta.");
                        }
                        break;
                    case 11:
                        System.out.println("11. Borrar archivo remoto");
                        System.out.println("Si quieres borrar una carpeta o archivo que esta dentro de una carpeta la carpeta del servidor\n"
                                + "por ejemplo si hay una carpeta llamada dos y quieres renombrar una carpeta o archivo llamada dosA dentro"
                                + "debes introducir lo siguiente: dos\\dosA, de lo contrario solo introduce el nombre de la carpeta o archivo ");

                        System.out.println("Para borrar un archivo debes introducir la extención!!");
                        System.out.println("Introduce el nombre:  ");
                        nombreCar = entradaTeclado.readLine();
                        clienteMeta = new Socket("127.0.0.1", 8000);
                        System.out.println("Conexion exitosa con el servidor");
                        objetoSalida = new ObjectOutputStream(clienteMeta.getOutputStream());
                        objetoEntrada = new ObjectInputStream(clienteMeta.getInputStream());
                        metD = new Metadato("11", nombreCar,"Vacio");
                        objetoSalida.writeObject(metD);
                        objetoSalida.flush();
                        metRecibido = (Metadato) objetoEntrada.readObject();
                        System.out.println(metRecibido.getContenido());
                        objetoSalida.close();
                        objetoEntrada.close();
                        clienteMeta.close();
                        break;
                    default:
                        System.out.println("Opcion no valida");
                        break;


                }

                if (salir) {
                    break;
                }
                System.out.println("\n \n \n");
            } while (opcion != 12);

            entradaTeclado.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void menu() {
        System.out.println("1.  Visualizar carpeta local");
        System.out.println("2.  Visualizar carpetas servidor");
        System.out.println("3.  Crear carpeta local");
        System.out.println("4.  Crear carpeta servidor");
        System.out.println("5.  Subir archivo o carpeta local");
        System.out.println("6.  Subir archivo o carpeta remoto");
        System.out.println("7.  Descargar archivo o carpeta del servidor");
        System.out.println("8.  Renombrar archivos o carpetas locales");
        System.out.println("9.  Renombrar archivos o carpetas remotos");
        System.out.println("10  Borrar Carpeta Local");
        System.out.println("11  Borrar Carpeta Remota");
        System.out.println("12. Salir");
        System.out.println("Introduzca la opción que desea realizar (1-10): ");
    }

    public static void menu2() {
        System.out.println("1.  Crear nuevo archivo");
        System.out.println("2.  Subir archivo o carpeta existente");
    }
}   
        
    

    