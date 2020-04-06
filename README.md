# Práctica de Redes

## Facultad de Informática de A Coruña. Grado en Ingeniería Informática

## Carlos Torres (carlos.torres@udc.es)

Se ha implementado en java un servidor web sencillo, haciendo uso de las clases disponibles
en el API de Java sólo hasta la capa 4 (clases Socket y ServerSocket)

En mi caso he implementado todos los apartados que aparecen en el enunciado.

El código fuente está todo en el mismo paquete (es.udc.redes.webserver), e incluye:

#### Clase WebServer
Se encarga de escuchar conexiones TCP entrantes y lanzar hilos que atiendan paralelamente a esa petición.
Se encarga también de gestionar los ficheros de log y log de errores, así como el fichero de configuración (server.properties)
Esta es la clase principal, que implementa el método main()

#### Clase HttpThread
Se encarga de leer la petición HTTP que entra por el Socket TCP, y construir un objeto HttpRequest a partir de ella

#### Clase HttpRequest
Se encarga de interpretar la petición HTTP, y generar una respuesta adecuada, haciendo uso de HttpResource.
Se encarga también de almacenar la información de logging y luego escribirla

#### Clase HttpResource
Representa un recurso en el sistema de archivos de la máquina. Los métodos de esta clase incluyen leer el archivo y sus propiedades
para poder enviar HEAD y BODY de la respuesta.

#### Interfaz MiniServlet
Define una interfaz sencilla que todos los servlets deben implementar

#### Clase MiServlet
Gestiona una petición dinámica generando una página que saluda al usuario, cuyo nombre lee de los parámetros de la petición http

#### Clase Calculate
Gestiona una petición dinámica, generando una página que da el resultado de una operación matemática, que se lee e interpreta de
los parámetros de la petición http.
Cabe destacar que todos los parámetros están envueltos en un String, como práctica de programación defensiva, para evitar
la posibilidad de un usuario pueda manualmente editarlas en la uri antes de hacer la petición y provocar problemas
En caso de que las opciones no sean correctas, se devolverá una página indicando que ha habido un error.

#### Clase ServerUtils
Esta clase contiene un método de utilidad para llamar al servlet correspondiente, instanciándolo a partir del nombre de la clase


### Cómo ejecutar la práctica
Al ejecutar la práctica, debe pasarse como parámetro la ruta al fichero server.properties, que contiene parámetros de configuración
que el servidor web necesita para ejecutarse correctamente. En caso de no proporcionar la ruta, o de que esta sea errónea, el servidor
mostrará un error por la consola y terminará su ejecución.

Si el fichero de configuración proporcionado existe pero la sintaxis no es correcta o faltan propiedades por especificar, el comportamiento
del programa no está definido.

El mencionado fichero de configuración tendrá los siguientes parámetros:

- PORT: un entero indicando el número de puerto TCP donde se escucha

- DIRECTORY: la ruta al directorio /p1/resources (puede ser absoluta o relativa). En mi caso es absoluta para poder ejecutarlo tanto
desde netbeans como desde la terminal sin problemas. Esta propiedad debe revisarse antes de arrancar el servidor para evitar problemas

- DIRECTORY_INDEX: el nombre del fichero por defecto para cada directorio (tanto el raíz como posibles subdirectorios), *sin la barra inicial* (p.e. "index.html")

- LOG_FILE y ERROR_LOG_FILE: las rutas al fichero de logs y logs de errores, respectivamente. Si se quiere que todos los logs vayan al mismo fichero, basta
con indicar solamente la propiedad LOG_FILE.

- ALLOW: valor booleano que indica si la directiva ALLOW está activa o no. Se puede consultar el comportamiento en cada caso en el enunciado de la práctica

### Particularidades de la práctica

Además de los ficheros incluídos en p1_files proporcionados por el profesorado, he incluído más ficheros para probar el servidor de forma más exhaustiva
En el directorio p1, se encuentran el fichero de configuración "server.properties", un directorio logs (que contiene ambos ficheros de log), y
un directorio resources (que contiene todos los recursos que se le pueden pedir al servidor).

He decidido dejar el server.properties fuera del directorio /p1/resources porque considero que no tiene sentido que un cliente web pueda pedir el fichero de
configuración del servidor.

En el directorio resources se hallan:

- index.html: una página básica, proporcionada con el enunciado
- saludo.html: la página que recoge los parámetros para las peticiones dinámicas a MiServlet.do, proporcionada con el enunciado
- fic.png: una imagen del logotipo de la facultad, proporcionada con el enunciado
- udc.gif: una imagen del logotipo de la universidad, proporcionada con el enunciado
- LICENSE.txt: un fichero de texto plano, proporcionado con el enunciado
- plain.txt: un fichero de texto plano para probar que los tamaños son correctos
- demo.html: una página simple, que integra texto y dos imágenes para comprobar que el servidor responde bien a varias peticiones seguidas
- favicon.ico: una imagen para usar de icono de las pestañas del navegador
- calculadora.html: una página para recoger los parámetros para las peticiones dinámicas a Calculate.do
- un directorio docs, que contiene a su vez una imagen, un pdf y otro directorio que contiene tres ficheros de texto,
    para probar que la directiva ALLOW funciona correctamente y se aplica recursivamente a los subdirectorios
