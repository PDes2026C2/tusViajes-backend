# language: es
Característica: Administración de agencias y gestión de hoteles
  Como administrador o agencia de TusViajes
  Quiero gestionar el estado de las agencias postulantes y el catálogo de hoteles
  Para mantener la oferta turística actualizada en la base de datos

  Antecedentes:
    Dado que existe un administrador autenticado con email "admin@tusviajes.com"
    Y existe el país "Argentina" con código ISO "AR"
    Y existe la ciudad "Bariloche" en el país "Argentina"

  Escenario: Un administrador consulta y autoriza una agencia pendiente
    Dado que existe una agencia con razón social "Patagonia Tours", CUIT "30-88888888-8", email "info@patagoniatours.com", contraseña "Pass1234" y estado "PENDING"
    Cuando el administrador consulta las agencias pendientes
    Entonces el código de respuesta HTTP debe ser 200
    Y la lista de agencias devuelta contiene a "Patagonia Tours"
    Cuando el administrador autoriza a la agencia "Patagonia Tours"
    Entonces el código de respuesta HTTP debe ser 200
    Y la agencia con email "info@patagoniatours.com" existe en la base de datos con estado "AUTHORIZED"

  Escenario: Un administrador rechaza una agencia pendiente
    Dado que existe una agencia con razón social "Agencia Fantasma", CUIT "30-99999999-9", email "info@fantasma.com", contraseña "Pass1234" y estado "PENDING"
    Cuando el administrador rechaza a la agencia "Agencia Fantasma"
    Entonces el código de respuesta HTTP debe ser 200
    Y la agencia con email "info@fantasma.com" existe en la base de datos con estado "REJECTED"

  Escenario: Una agencia autorizada actualiza su propia razón social
    Dado que existe una agencia autenticada con razón social "Viajes Norte", CUIT "30-44444444-4" y email "norte@agencia.com"
    Cuando la agencia autenticada actualiza su razón social a "Viajes Norte Internacional SA"
    Entonces el código de respuesta HTTP debe ser 200
    Y la agencia con email "norte@agencia.com" tiene la razón social "Viajes Norte Internacional SA" en la base de datos

  Escenario: Un administrador registra un nuevo hotel en una ciudad existente
    Cuando el administrador registra un hotel llamado "Llao Llao Resort" en la ciudad "Bariloche" con servicios "Spa, Golf, Piscina"
    Entonces el código de respuesta HTTP debe ser 201
    Y el hotel "Llao Llao Resort" se encuentra persistido en la base de datos asociado a la ciudad "Bariloche"
