Tengo 3 servidores Weblogic con nombres HotelMTA, AirlineMTA y AgencyMTA. En AgencyMTA hay un proyecto desplegado en un .war que tiene que conectarse a las colas JMS de los servidores HotelMTA y AirlineMTA. Estoy utilizando la consola remota de Weblogic para administrar y configurar los 3 servidores. Estoy teniendo problemas para configurar las conexiones a los otros servidores porque tengo que configurar las factorías de conexiones y los servidores remotos. Explícame los pasos que debo seguir para poder configurar todo correctamente. Ve paso a paso, y no avances al siguiente paso hasta que te lo diga explícitamente.

Perfecto, vamos a ir paso a paso para configurar correctamente la conexión JMS desde tu servidor **AgencyMTA** hacia las colas ubicadas en **HotelMTA** y **AirlineMTA**.

### Paso 1: Verificar y anotar los recursos JMS remotos

Primero, debes asegurarte de tener acceso a los siguientes detalles de los servidores **HotelMTA** y **AirlineMTA**:

1. **Nombre JNDI** de la cola de destino (por ejemplo, `jms/colaReservasHotel`, `jms/colaReservasVuelo`).
2. **Nombre JNDI** de la *Connection Factory* asociada en esos servidores (por ejemplo, `jms/ConnectionFactoryHotel`).
3. **Dirección del servidor** (host y puerto) donde están desplegados estos recursos.
4. **Nombre del destino (Queue o Topic)** real configurado.
5. **Si hay autenticación**, el usuario y contraseña necesarios para conectarse.

Confirma que tienes estos datos de **HotelMTA** y **AirlineMTA**, o entra a su consola y verifica. Cuando estés listo, dime y avanzamos al siguiente paso.

Okey, tengo todo anotado. Avanza al siguiente paso

Perfecto.

### Paso 2: Configurar *Foreign Server* en AgencyMTA

Ahora, vamos a configurar un **Foreign Server** en **AgencyMTA** para conectarte al servidor remoto (HotelMTA o AirlineMTA). Empecemos solo con uno: **HotelMTA**. Luego repetirás el proceso para AirlineMTA.

#### 2.1. Ingresar a la consola de WebLogic de AgencyMTA

1. Abre la consola de administración de WebLogic.
2. Ve a **"Services"** > **"Messaging"** > **"JMS Modules"**.
3. Selecciona el módulo JMS donde deseas definir el Foreign Server (puedes usar uno existente o crear uno nuevo si prefieres).

> ¿Tienes ya un módulo JMS creado en AgencyMTA para este propósito? ¿Quieres crear uno nuevo o usar uno existente?
> Responde eso y seguimos con la configuración dentro del módulo.

Sí, ya tengo un creado

Perfecto, usaremos ese módulo JMS existente.

### Paso 3: Crear un *Foreign Server* dentro del módulo JMS

Ahora vamos a agregar el **Foreign Server** que representará los recursos JMS remotos de **HotelMTA**.

#### 3.1. Crear el Foreign Server

1. Dentro del módulo JMS, haz clic en **“New”** y selecciona **“Foreign Server”**.

2. Asigna un nombre descriptivo, por ejemplo:
   `HotelMTAForeignServer`

3. En el campo **JNDI Initial Context Factory**, usa:

   ```
   weblogic.jndi.WLInitialContextFactory
   ```

4. En **Connection URL**, coloca la URL de conexión al servidor HotelMTA, en formato:

   ```
   t3://<host>:<puerto>
   ```

   Por ejemplo:

   ```
   t3://hotelserver.ejemplo.com:7001
   ```

5. Si el acceso requiere autenticación, completa los campos **User** y **Password**.

6. En **JNDI Properties**, puedes dejarlo vacío por ahora a menos que necesites alguna propiedad específica (como `java.naming.security.principal`, etc.).

7. Haz clic en **Next**, luego en **Finish**.

Esto crea el Foreign Server base.

> ¿Te aparece creado sin errores? Si sí, dime y avanzamos a agregar los recursos dentro del Foreign Server.


Ya he creado los dos foreign servers y no me ha dado ningún error

Perfecto, excelente trabajo.

### Paso 4: Agregar *Connection Factories* y *Destinations* al Foreign Server

Ahora, dentro de cada **Foreign Server** (por ejemplo, `HotelMTAForeignServer`), vamos a crear los recursos que permiten acceder a las colas y factorías remotas desde **AgencyMTA**.

Comencemos con **HotelMTA**.

#### 4.1. Agregar una *Foreign Connection Factory*

1. Dentro del `HotelMTAForeignServer`, haz clic en **"New"** > **"Foreign Connection Factory"**.
2. Asigna un nombre, por ejemplo:
   `HotelConnectionFactory`
3. En **Remote JNDI Name**, introduce el nombre JNDI del *Connection Factory* en **HotelMTA**, por ejemplo:

   ```
   jms/ConnectionFactoryHotel
   ```
4. En **Local JNDI Name**, coloca el nombre con el que tu aplicación en **AgencyMTA** lo va a buscar, por ejemplo:

   ```
   jms/RemoteHotelConnectionFactory
   ```

#### 4.2. Agregar una *Foreign Destination* (Queue o Topic)

1. Vuelve al Foreign Server y haz clic en **"New"** > **"Foreign Destination"**.
2. Asigna un nombre, por ejemplo:
   `HotelReservationQueue`
3. En **Remote JNDI Name**, introduce el nombre JNDI real de la cola en HotelMTA, por ejemplo:

   ```
   jms/colaReservasHotel
   ```
4. En **Local JNDI Name**, coloca el nombre con el que la aplicación lo buscará, por ejemplo:

   ```
   jms/RemoteHotelReservationQueue
   ```

> Cuando hayas creado estos dos recursos (Connection Factory y Destination) dentro de `HotelMTAForeignServer`, dime y seguimos con la activación y prueba.

Ya he configurado las factorías de conexiones y destinos remotos en ambos Foreign Servers

¡Perfecto! Ya estás muy cerca de tener todo funcionando.

### Paso 5: Activar los cambios y probar la conectividad

#### 5.1. Guardar y activar los cambios

1. Asegúrate de hacer clic en **"Save"** y luego en **"Activate Changes"** en la consola de administración.
2. Reinicia **AgencyMTA** si es necesario (aunque no siempre hace falta, depende de la versión y cambios hechos).

#### 5.2. Verificar acceso desde tu aplicación

Ahora debes asegurarte de que tu aplicación desplegada en **AgencyMTA** esté usando los **nombres JNDI locales** que configuraste en los Foreign Servers. Es decir:

* Para acceder a la connection factory de HotelMTA:

  ```java
  ctx.lookup("jms/RemoteHotelConnectionFactory")
  ```
* Para acceder a la cola remota de HotelMTA:

  ```java
  ctx.lookup("jms/RemoteHotelReservationQueue")
  ```

Y lo mismo con los recursos remotos de AirlineMTA.

> ¿Tu aplicación ya está utilizando esos JNDI locales? ¿Tienes algún mensaje de error cuando la pruebas? Podemos validar la conexión con una prueba sencilla si lo necesitas.


Ya he configurado ambas conexiones y me salta el siguiente error:
<10 Jun 2025, 09:39:52,957 Central European Summer Time> <Error> <RJVM> <BEA-000503> <Incoming message header or abbreviation processing failed.
 java.io.InvalidClassException: filter status: REJECTED
java.io.InvalidClassException: filter status: REJECTED
        at java.base/java.io.ObjectInputStream.filterCheck(ObjectInputStream.java:1414)
        at java.base/java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:2055)
        at java.base/java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1909)
        at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1722)
        at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:514)
        Truncated.
>
MSATravel - 09:39:52.986 ERROR - domainevent.publisher.airline.JMSAirlineReservationPublisher - Error al publicar el mensaje: Failed to initialize JNDI context, tried 2 time or times totally, the interval of each time is 0ms.
; nested exception is:
        java.io.EOFException

Gracias por compartir el error. Este tipo de error:

```
java.io.InvalidClassException: filter status: REJECTED
```

es común en versiones recientes de Java (Java 8u121+, Java 11+) y WebLogic cuando estás usando **serialización de objetos a través de RMI/JMS** entre servidores y la clase que se está intentando deserializar **no está permitida por el filtro de serialización de Java**.

Esto ocurre porque **Java activó un mecanismo de seguridad** que **filtra qué clases pueden ser deserializadas** remotamente, y WebLogic aplica estos filtros también para proteger de ataques.

---

### ✅ Paso 6: Resolver el error "filter status: REJECTED"

Debemos agregar explícitamente las clases necesarias a la lista de clases permitidas para deserialización.

#### Opción A: Agregar al filtro global de WebLogic (recomendado para pruebas y clusters controlados)

1. Edita el archivo `java.security` en tu JDK (no recomendado en producción si compartes JDK con otras aplicaciones).
2. O mejor: añade la siguiente propiedad **en la configuración de la JVM** del servidor **AgencyMTA** (desde la consola de WebLogic):

   Ve a:

   * **Servers** > **AgencyMTA** > **Server Start** > **Arguments** (o “Startup Parameters”)
   * Añade esta línea al final de los argumentos de JVM:

   ```bash
   -Djdk.serialFilter=java.rmi.*,javax.jms.*,com.tuempresa.*,com.otropaquete.*,!*
   ```

   Donde `com.tuempresa.*` y otros paquetes son los que contienen las clases que estás enviando por JMS.

#### Opción B: Configurar `SerializedSystemIni` o `weblogic.serialFilter` (más específica)

También puedes usar una propiedad específica de WebLogic:

```bash
-Dweblogic.serialFilter=java.rmi.*,javax.jms.*,com.tuempresa.*,!*
```

Esto permite clases usadas por RMI/JMS. El `!*` final es importante porque bloquea todo lo no permitido explícitamente.

---

### 🧪 Qué hacer ahora

1. Identifica qué clase está causando el rechazo. Si no está claro, puedes habilitar un log más detallado temporalmente o agregar un comodín más abierto para pruebas, como:

   ```bash
   -Djdk.serialFilter=*
   ```

   *(Úsalo solo en entornos de desarrollo, no en producción.)*

2. Agrega los paquetes necesarios como se explicó.

3. **Reinicia el servidor AgencyMTA** tras aplicar los cambios en los parámetros JVM.

> Aplica esto y dime si el error desaparece o si el mensaje cambia. También puedo ayudarte a identificar qué clase concreta está causando el rechazo si me muestras más del stack trace o el tipo de mensaje JMS que estás enviando.


No puedo acceder a esas propiedades. ¿Cómo se llama el archivo de configuración de Weblogic para añadir esas propiedades?

Perfecto, si no puedes modificarlas desde la consola de WebLogic, puedes hacerlo directamente editando los archivos de configuración del dominio WebLogic.

---

### ✅ Archivo que debes modificar: `setDomainEnv.sh` (Linux/Unix) o `setDomainEnv.cmd` (Windows)

Este archivo es el lugar correcto para añadir propiedades JVM a nivel de dominio o servidor.

#### Ruta típica:

```
<MW_HOME>/user_projects/domains/<nombre_dominio>/bin/setDomainEnv.sh
```

Ejemplo concreto:

```
/opt/weblogic/user_projects/domains/AgencyDomain/bin/setDomainEnv.sh
```

---

### 🔧 Qué hacer

1. Abre el archivo `setDomainEnv.sh` en un editor de texto:

   ```bash
   vi setDomainEnv.sh
   ```

2. Busca esta línea:

   ```bash
   JAVA_OPTIONS="${JAVA_OPTIONS} ...
   ```

3. Añade tu filtro justo después de esa línea o al final de la definición de `JAVA_OPTIONS`. Por ejemplo:

   ```bash
   JAVA_OPTIONS="${JAVA_OPTIONS} -Djdk.serialFilter=java.rmi.*,javax.jms.*,com.tuempresa.*,!*"
   export JAVA_OPTIONS
   ```

   o si usas WebLogic 12.2.1.4+ puedes usar:

   ```bash
   JAVA_OPTIONS="${JAVA_OPTIONS} -Dweblogic.serialFilter=java.rmi.*,javax.jms.*,com.tuempresa.*,!*"
   export JAVA_OPTIONS
   ```

4. Guarda los cambios.

5. **Reinicia el servidor AgencyMTA** para que los cambios surtan efecto.

---

### 🧪 Después de reiniciar

* Vuelve a probar tu aplicación.
* Si el error desaparece o cambia, vamos avanzando.
* Si no, puedo ayudarte a identificar la clase rechazada o ajustar el filtro más finamente.

> ¿Puedes aplicar este cambio? Avísame cuando lo hayas hecho o si necesitas ayuda con el contenido exacto del filtro para tus clases.


He aplicado el cambio con la opción -Dweblogic.serialFilter=* y me sigue lanzando el mismo error. En cambio, si utiliza -Djdk.serialFilter=* me salta el error de que no se puede configurar el serialFilter más de una vez (aunque en el archivo setDomain no he encontrado otra instrución que configure el serialFilter)

Gracias por los detalles, estás muy cerca de solucionarlo. Vamos a aclarar bien lo que está pasando:

---

### 🔍 Diagnóstico del problema

1. **WebLogic internamente** puede estar configurando su propio `jdk.serialFilter` por defecto.
2. Cuando tú intentas establecerlo de nuevo en `JAVA_OPTIONS`, **Java lanza error porque esa propiedad no puede redefinirse una vez fijada.**
3. El filtro `-Dweblogic.serialFilter=*` **es específico de WebLogic**, pero **no tiene efecto sobre el filtro del JDK (`jdk.serialFilter`)**, que es el que está provocando tu error `filter status: REJECTED`.

---

### ✅ Solución segura y efectiva

#### Paso 1: **Evitar conflicto de definición duplicada**

Busca en el mismo archivo `setDomainEnv.sh` o en otros archivos de configuración de WebLogic (como los siguientes) si hay una definición previa de `jdk.serialFilter`:

* `startWebLogic.sh`
* `startManagedWebLogic.sh`
* Archivos `.properties` en el dominio:
  `<DOMAIN_HOME>/config/startup.properties` (si existe)

🔎 Busca con:

```bash
grep -r "serialFilter" .
```

Si encuentras una línea como esta:

```bash
-Djdk.serialFilter=default
```

Debes **eliminarla** o comentarla antes de establecer la tuya propia.

---

#### Paso 2: Usar solo **-Djdk.serialFilter** con los paquetes correctos

Una vez eliminado cualquier otro filtro existente, añade en `setDomainEnv.sh`:

```bash
JAVA_OPTIONS="${JAVA_OPTIONS} -Djdk.serialFilter=java.rmi.*,javax.jms.*,com.tuempresa.*,com.airline.*,com.hotel.*,!*"
export JAVA_OPTIONS
```

⚠️ Importante: reemplaza `com.tuempresa.*` por los paquetes reales de tus clases serializadas.

---

### 🧪 Alternativa temporal solo para pruebas

Para verificar que efectivamente este es el problema, puedes probar (en un entorno no productivo):

```bash
-Djdk.serialFilter=*
```

y asegurarte de que:

* Ningún otro `serialFilter` se está definiendo antes.
* Has reiniciado **por completo** el servidor (`AgencyMTA`), no solo redeploy.

---

### ¿Y si no se encuentra ningún otro filtro definido?

En versiones de WebLogic con JDK 11+, **el filtro puede estar siendo definido por el propio JDK** desde archivos de seguridad (`java.security`), ubicados normalmente en:

```
<JAVA_HOME>/conf/security/java.security
```

Busca esta línea:

```properties
jdk.serialFilter=...
```

Puedes modificarla **temporalmente** (otra vez: solo en entornos de pruebas), o dejarla vacía si vas a pasar todo por parámetro JVM.

---

¿Quieres que te ayude a componer un filtro correcto y seguro para producción, basándome en los paquetes que usas?
Solo dime qué paquetes contienen tus clases serializables JMS.
