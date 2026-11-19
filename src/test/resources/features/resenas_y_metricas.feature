# language: es
Característica: Reseñas de viajes y métricas administrativas
  Como comprador o administrador de TusViajes
  Quiero publicar reseñas sobre viajes finalizados y consultar los rankings de ventas y calificaciones
  Para evaluar la calidad de los destinos y monitorear el rendimiento del negocio

  Antecedentes:
    Dado que existe el país "Argentina" con código ISO "AR"
    Y existe la ciudad "Buenos Aires" en el país "Argentina"
    Y existe la ciudad "Bariloche" en el país "Argentina"
    Y existe la ciudad "Mendoza" en el país "Argentina"
    Y existe el hotel "Hotel Nahuel Huapi" en la ciudad "Bariloche"
    Y existe el hotel "Hotel Viñedos" en la ciudad "Mendoza"
    Y existe el vuelo 601 de "Aerolíneas Argentinas" desde "Buenos Aires" hacia "Bariloche"
    Y existe el vuelo 602 de "Aerolíneas Argentinas" desde "Bariloche" hacia "Buenos Aires"
    Y existe el vuelo 701 de "Aerolíneas Argentinas" desde "Buenos Aires" hacia "Mendoza"
    Y existe el vuelo 702 de "Aerolíneas Argentinas" desde "Mendoza" hacia "Buenos Aires"
    Y existe una agencia con razón social "Cuyo & Patagonia", CUIT "30-60606060-6", email "info@cuyopatagonia.com", contraseña "Pass123" y estado "AUTHORIZED"
    Y la agencia "Cuyo & Patagonia" tiene publicado el paquete "Experiencia Bariloche" con precio 380000, hotel "Hotel Nahuel Huapi", vuelo de ida 601 y vuelo de vuelta 602 que inició hace 15 días y finalizó hace 8 días
    Y la agencia "Cuyo & Patagonia" tiene publicado el paquete "Ruta del Vino Futuro" con precio 410000, hotel "Hotel Viñedos", vuelo de ida 701 y vuelo de vuelta 702 que inicia en 5 días y finaliza en 10 días
    Y existe un comprador autenticado llamado "Sofía" "López" con email "sofia@comprador.com" y DNI "41223344"

  Escenario: Un comprador publica una reseña sobre un paquete que compró y ya finalizó
    Dado que la compradora "sofia@comprador.com" registró la compra del paquete "Experiencia Bariloche"
    Cuando el comprador publica una reseña para el paquete "Experiencia Bariloche" con puntaje 9 y comentario "Excelente atención y paisajes"
    Entonces el código de respuesta HTTP debe ser 201
    Y existe en la base de datos una reseña para el paquete "Experiencia Bariloche" con puntaje 9 y comentario "Excelente atención y paisajes"

  Escenario: Un comprador no puede reseñar un paquete que no ha adquirido
    Cuando el comprador publica una reseña para el paquete "Experiencia Bariloche" con puntaje 8 y comentario "No viajé pero opino"
    Entonces el código de respuesta HTTP debe ser 403
    Y el mensaje de error de la respuesta es "El comprador aún no ha adquirido este paquete de viaje"

  Escenario: Un comprador no puede reseñar un paquete adquirido que aún no ha finalizado
    Dado que la compradora "sofia@comprador.com" registró la compra del paquete "Ruta del Vino Futuro"
    Cuando el comprador publica una reseña para el paquete "Ruta del Vino Futuro" con puntaje 10 y comentario "Todavía no terminó"
    Entonces el código de respuesta HTTP debe ser 403
    Y el mensaje de error de la respuesta es "No se puede opinar sobre un paquete de viaje antes de su fecha de finalización."

  Escenario: Un comprador no puede publicar una segunda reseña sobre el mismo paquete
    Dado que la compradora "sofia@comprador.com" registró la compra del paquete "Experiencia Bariloche"
    Y el comprador publica una reseña para el paquete "Experiencia Bariloche" con puntaje 9 y comentario "Primera reseña"
    Cuando el comprador publica una reseña para el paquete "Experiencia Bariloche" con puntaje 7 y comentario "Segunda reseña duplicada"
    Entonces el código de respuesta HTTP debe ser 409
    Y el mensaje de error de la respuesta es "El comprador ya opinó sobre este paquete de viaje"

  Escenario: Un administrador consulta los rankings de métricas basados en las compras y reseñas de la base de datos
    Dado que la compradora "sofia@comprador.com" registró la compra del paquete "Experiencia Bariloche"
    Y el comprador publica una reseña para el paquete "Experiencia Bariloche" con puntaje 10 y comentario "Inolvidable"
    Y existe un administrador autenticado con email "admin@tusviajes.com"
    Cuando el administrador consulta el reporte "/api/admin/metrics/top-buyers"
    Entonces el código de respuesta HTTP debe ser 200
    Y el primer comprador del ranking tiene email "sofia@comprador.com" y 1 compra
    Cuando el administrador consulta el reporte "/api/admin/metrics/top-destinations"
    Entonces el código de respuesta HTTP debe ser 200
    Y el primer destino más vendido es "Bariloche" con 1 venta
    Cuando el administrador consulta el reporte "/api/admin/metrics/top-rated-destinations"
    Entonces el código de respuesta HTTP debe ser 200
    Y el primer destino mejor calificado es "Bariloche" con promedio 10
    Cuando el administrador consulta el reporte "/api/admin/metrics/top-agencies"
    Entonces el código de respuesta HTTP debe ser 200
    Y la primera agencia vendedora es "Cuyo & Patagonia" con 1 venta
