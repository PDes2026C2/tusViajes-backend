# language: es
Característica: Gestión y búsqueda de paquetes de viaje
  Como agencia de viajes o usuario visitante
  Quiero crear, modificar, desactivar y buscar paquetes turísticos
  Para publicar ofertas consistentes y encontrar viajes según origen, destino y fechas

  Antecedentes:
    Dado que existe el país "Argentina" con código ISO "AR"
    Y existe el país "Brasil" con código ISO "BR"
    Y existe la ciudad "Buenos Aires" en el país "Argentina"
    Y existe la ciudad "Bariloche" en el país "Argentina"
    Y existe la ciudad "Mendoza" en el país "Argentina"
    Y existe la ciudad "Río de Janeiro" en el país "Brasil"
    Y existe el hotel "Hotel Catedral" en la ciudad "Bariloche"
    Y existe el hotel "Hotel Aconcagua" en la ciudad "Mendoza"
    Y existe el hotel "Hotel Copacabana" en la ciudad "Río de Janeiro"
    Y existe el vuelo 101 de "Aerolíneas Argentinas" desde "Buenos Aires" hacia "Bariloche"
    Y existe el vuelo 102 de "Aerolíneas Argentinas" desde "Bariloche" hacia "Buenos Aires"
    Y existe el vuelo 201 de "GOL" desde "Buenos Aires" hacia "Río de Janeiro"
    Y existe el vuelo 202 de "GOL" desde "Río de Janeiro" hacia "Buenos Aires"

  Escenario: Una agencia autorizada crea un paquete de viaje válido
    Dado que existe una agencia autenticada con razón social "Andes Travel", CUIT "30-10101010-1" y email "ventas@andestravel.com"
    Cuando la agencia crea un paquete llamado "Escapada a Bariloche" con precio 350000, hotel "Hotel Catedral", vuelo de ida 101 y vuelo de vuelta 102 que inicia en 10 días y finaliza en 17 días
    Entonces el código de respuesta HTTP debe ser 201
    Y el paquete "Escapada a Bariloche" existe activo en la base de datos con precio 350000

  Escenario: Creación de paquete rechazada cuando la ciudad del hotel no coincide con el destino del vuelo
    Dado que existe una agencia autenticada con razón social "Andes Travel", CUIT "30-10101010-1" y email "ventas@andestravel.com"
    Cuando la agencia crea un paquete llamado "Paquete Inconsistente" con precio 280000, hotel "Hotel Aconcagua", vuelo de ida 101 y vuelo de vuelta 102 que inicia en 10 días y finaliza en 15 días
    Entonces el código de respuesta HTTP debe ser 400
    Y el mensaje de error de la respuesta contiene "La ciudad del hotel debe coincidir"

  Escenario: Una agencia actualiza los datos de su propio paquete de viaje
    Dado que existe una agencia autenticada con razón social "Agencia Dueña", CUIT "30-11111111-1" y email "duena@agencia.com"
    Y la agencia "Agencia Dueña" tiene publicado el paquete "Bariloche Base" con precio 300000, hotel "Hotel Catedral", vuelo de ida 101 y vuelo de vuelta 102 que inicia en 12 días y finaliza en 19 días
    Cuando la agencia autenticada actualiza el paquete "Bariloche Base" al nuevo nombre "Bariloche Premium" y precio 420000
    Entonces el código de respuesta HTTP debe ser 200
    Y el paquete "Bariloche Premium" existe activo en la base de datos con precio 420000

  Escenario: Una agencia no puede eliminar un paquete perteneciente a otra agencia
    Dado que existe una agencia con razón social "Agencia Dueña", CUIT "30-11111111-1", email "duena@agencia.com", contraseña "Pass123" y estado "AUTHORIZED"
    Y la agencia "Agencia Dueña" tiene publicado el paquete "Nieve en Bariloche" con precio 400000, hotel "Hotel Catedral", vuelo de ida 101 y vuelo de vuelta 102 que inicia en 15 días y finaliza en 22 días
    Y existe una agencia autenticada con razón social "Otra Agencia", CUIT "30-22222222-2" y email "otra@agencia.com"
    Cuando la agencia autenticada intenta eliminar el paquete "Nieve en Bariloche"
    Entonces el código de respuesta HTTP debe ser 403
    Y el paquete "Nieve en Bariloche" sigue activo en la base de datos

  Escenario: Baja lógica de un paquete propio lo oculta de la búsqueda pública pero lo conserva en la base de datos
    Dado que existe una agencia autenticada con razón social "Agencia Dueña", CUIT "30-11111111-1" y email "duena@agencia.com"
    Y la agencia "Agencia Dueña" tiene publicado el paquete "Bariloche Clásico" con precio 320000, hotel "Hotel Catedral", vuelo de ida 101 y vuelo de vuelta 102 que inicia en 12 días y finaliza en 19 días
    Cuando la agencia autenticada elimina su paquete "Bariloche Clásico"
    Entonces el código de respuesta HTTP debe ser 204
    Y el paquete "Bariloche Clásico" existe inactivo en la base de datos
    Cuando un visitante busca paquetes de viaje sin filtros
    Entonces la página de paquetes devuelta no incluye a "Bariloche Clásico"

  Escenario: Búsqueda pública de paquetes filtrando por país de destino
    Dado que existe una agencia con razón social "Global Viajes", CUIT "30-33333333-3", email "global@agencia.com", contraseña "Pass123" y estado "AUTHORIZED"
    Y la agencia "Global Viajes" tiene publicado el paquete "Aventura en la Patagonia" con precio 300000, hotel "Hotel Catedral", vuelo de ida 101 y vuelo de vuelta 102 que inicia en 10 días y finaliza en 15 días
    Y la agencia "Global Viajes" tiene publicado el paquete "Playas de Río" con precio 500000, hotel "Hotel Copacabana", vuelo de ida 201 y vuelo de vuelta 202 que inicia en 10 días y finaliza en 15 días
    Cuando un visitante busca paquetes de viaje con código ISO de país de destino "BR"
    Entonces el código de respuesta HTTP debe ser 200
    Y la página de paquetes devuelta incluye a "Playas de Río"
    Y la página de paquetes devuelta no incluye a "Aventura en la Patagonia"
