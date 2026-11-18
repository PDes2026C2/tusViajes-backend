# language: es
Característica: Compras de paquetes turísticos y gestión de favoritos
  Como comprador registrado en TusViajes
  Quiero guardar paquetes en mis favoritos y comprar paquetes de viaje
  Para planificar mis vacaciones y asegurar mis pasajes y estadía

  Antecedentes:
    Dado que existe el país "Argentina" con código ISO "AR"
    Y existe la ciudad "Buenos Aires" en el país "Argentina"
    Y existe la ciudad "Bariloche" en el país "Argentina"
    Y existe el hotel "Hotel Llao Llao" en la ciudad "Bariloche"
    Y existe el vuelo 501 de "Aerolíneas Argentinas" desde "Buenos Aires" hacia "Bariloche"
    Y existe el vuelo 502 de "Aerolíneas Argentinas" desde "Bariloche" hacia "Buenos Aires"
    Y existe una agencia con razón social "Turismo Sur", CUIT "30-50505050-5", email "sur@agencia.com", contraseña "Pass123" y estado "AUTHORIZED"
    Y la agencia "Turismo Sur" tiene publicado el paquete "Bariloche Invernal" con precio 450000, hotel "Hotel Llao Llao", vuelo de ida 501 y vuelo de vuelta 502 que inicia en 10 días y finaliza en 17 días
    Y existe un comprador autenticado llamado "Carlos" "Gómez" con email "carlos@comprador.com" y DNI "38123456"

  Escenario: Un comprador agrega, consulta y elimina un paquete de su lista de favoritos
    Cuando el comprador agrega el paquete "Bariloche Invernal" a sus favoritos
    Entonces el código de respuesta HTTP debe ser 200
    Cuando el comprador consulta su lista de paquetes favoritos
    Entonces el código de respuesta HTTP debe ser 200
    Y la lista de favoritos devuelta contiene el paquete "Bariloche Invernal"
    Cuando el comprador elimina el paquete "Bariloche Invernal" de sus favoritos
    Entonces el código de respuesta HTTP debe ser 204
    Cuando el comprador consulta su lista de paquetes favoritos
    Entonces la lista de favoritos devuelta está vacía

  Escenario: Compra exitosa de un paquete de viaje con cupo en vuelos
    Dado que el servicio externo de vuelos tiene cupo disponible para todos los vuelos
    Cuando el comprador adquiere el paquete "Bariloche Invernal"
    Entonces el código de respuesta HTTP debe ser 201
    Y se registra en la base de datos la compra del paquete "Bariloche Invernal" para el comprador "carlos@comprador.com" por un monto de 450000
    Y se emitieron los pasajes en el servicio externo para los vuelos 501 y 502

  Escenario: Un comprador no puede comprar dos veces el mismo paquete de viaje
    Dado que el servicio externo de vuelos tiene cupo disponible para todos los vuelos
    Y el comprador ya adquirió el paquete "Bariloche Invernal"
    Cuando el comprador adquiere el paquete "Bariloche Invernal"
    Entonces el código de respuesta HTTP debe ser 409
    Y el mensaje de error de la respuesta es "El comprador ya adquirió este paquete de viaje"

  Escenario: No se puede comprar un paquete cuya fecha de inicio ya pasó
    Dado que la agencia "Turismo Sur" tiene publicado el paquete "Viaje Ya Iniciado" con precio 250000, hotel "Hotel Llao Llao", vuelo de ida 501 y vuelo de vuelta 502 que inició hace 2 días y finaliza en 5 días
    Cuando el comprador adquiere el paquete "Viaje Ya Iniciado"
    Entonces el código de respuesta HTTP debe ser 400
    Y el mensaje de error de la respuesta es "No se puede comprar un paquete de viaje que ya ha comenzado"

  Escenario: Compensación de pasajes y rollback cuando el vuelo de regreso está lleno
    Dado que el vuelo de ida 501 tiene cupo pero el vuelo de vuelta 502 está completo
    Cuando el comprador adquiere el paquete "Bariloche Invernal"
    Entonces el código de respuesta HTTP debe ser 409
    Y el mensaje de error de la respuesta es "Uno de los vuelos asociados no cuenta con cupo disponible"
    Y se compensó la reserva del vuelo 501 en el servicio externo
    Y el comprador "carlos@comprador.com" no registra compras en la base de datos
