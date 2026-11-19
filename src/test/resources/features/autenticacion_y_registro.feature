# language: es
Característica: Autenticación y registro de usuarios
  Como visitante o usuario de TusViajes
  Quiero poder registrarme e iniciar sesión en la plataforma
  Para acceder a las funcionalidades según mi rol y persistir mi cuenta en la base de datos

  Escenario: Registro exitoso de un nuevo comprador
    Cuando un visitante se registra como comprador con nombre "Lucía", apellido "Martínez", email "lucia@comprador.com", contraseña "Password123", teléfono "1144556677" y DNI "40112233"
    Entonces el código de respuesta HTTP debe ser 201
    Y la respuesta contiene un token de acceso JWT válido para el rol "BUYER"
    Y el comprador con email "lucia@comprador.com" existe en la base de datos con DNI "40112233"

  Escenario: Registro de una nueva agencia queda en estado pendiente de aprobación
    Cuando una empresa se postula como agencia con razón social "Viajes del Sur SA", CUIT "30-71234567-9", email "contacto@viajesdelsur.com" y contraseña "AgencyPass123"
    Entonces el código de respuesta HTTP debe ser 201
    Y la agencia con email "contacto@viajesdelsur.com" existe en la base de datos con estado "PENDING"

  Escenario: Inicio de sesión exitoso de un administrador
    Dado que existe un administrador registrado con email "admin@tusviajes.com" y contraseña "AdminSecret123"
    Cuando el usuario inicia sesión con email "admin@tusviajes.com" y contraseña "AdminSecret123"
    Entonces el código de respuesta HTTP debe ser 200
    Y la respuesta contiene un token de acceso JWT válido para el rol "ADMIN"

  Escenario: Inicio de sesión rechazado para una agencia pendiente de autorización
    Dado que existe una agencia con razón social "Agencia Pendiente", CUIT "30-11223344-5", email "pendiente@agencia.com", contraseña "Secret123" y estado "PENDING"
    Cuando el usuario inicia sesión con email "pendiente@agencia.com" y contraseña "Secret123"
    Entonces el código de respuesta HTTP debe ser 403
    Y el mensaje de error de la respuesta es "La agencia está pendiente de autorización por parte de un administrador."

  Escenario: Inicio de sesión exitoso para una agencia autorizada
    Dado que existe una agencia con razón social "Agencia Autorizada", CUIT "30-55667788-9", email "autorizada@agencia.com", contraseña "Secret123" y estado "AUTHORIZED"
    Cuando el usuario inicia sesión con email "autorizada@agencia.com" y contraseña "Secret123"
    Entonces el código de respuesta HTTP debe ser 200
    Y la respuesta contiene un token de acceso JWT válido para el rol "AGENCY"

  Escenario: Inicio de sesión rechazado por credenciales inválidas
    Dado que existe un administrador registrado con email "admin@tusviajes.com" y contraseña "AdminSecret123"
    Cuando el usuario inicia sesión con email "admin@tusviajes.com" y contraseña "ClaveIncorrecta"
    Entonces el código de respuesta HTTP debe ser 401
    Y el mensaje de error de la respuesta es "Credenciales invalidas"

  Escenario: Renovación exitosa de token mediante refresh token
    Dado que existe un administrador registrado con email "admin@tusviajes.com" y contraseña "AdminSecret123"
    Y el usuario inició sesión con email "admin@tusviajes.com" y contraseña "AdminSecret123"
    Cuando solicita refrescar su sesión utilizando su refresh token
    Entonces el código de respuesta HTTP debe ser 200
    Y la respuesta contiene un token de acceso JWT válido para el rol "ADMIN"
