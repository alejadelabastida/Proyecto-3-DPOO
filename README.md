# Club Deportivo Séneca – Proyecto 3, Entrega única

## Participantes

1. Alejandra de la Bastida
2. Juan Jose Castro
3. Tomas Rivera

## Proyecto 3 – Interfaz gráfica del administrador (Persona 1)

1. Abra `src/interfaz/administrador/VentanaAdministrador.java`, clic derecho > **Run As > Java Application**.
2. Entre con usuario `admin` y clave `admin123` (3 intentos; si entra otro rol, se le avisa y se cierra).
3. El menú lateral tiene: Inicio (resumen y pendientes), Torneos, Equipos y jugadores, Instalaciones, Inventario y sugerencias, Turnos del personal, Competencias oficiales, Consultas y reportes y **Gráfica de ingresos**.
4. La gráfica de barras compara tienda, cafetería y mensualidades en 5 días, sin IVA ni impuesto al consumo. Filtros: fecha inicial, rubros y vista por día o total del rango.
5. Usa los mismos datos de `datos/club.dat`. La primera vez se agregan ventas, mensualidades, sugerencias y solicitudes de turno de ejemplo (`DatosAdministrador`), solo si no existen.

Pruebas nuevas (JUnit 5): `IngresosNetosTest` e `InterfazAdministradorTest`.

Documento de diseño, bocetos, diagramas UML (fuentes `.mmd`) y capturas: `Diagramas y documento/Proyecto 3 - Administrador/`.

## Proyecto 3 – Consola del administrador

1. Abra `src/consola/ConsolaAdministrador.java`, clic derecho > **Run As > Java Application**.
2. Entre con usuario `admin` y clave `admin123`. Si entra un socio o un empleado, el programa le avisa que use la consola de su rol y se cierra.
3. El menú principal tiene seis partes:
   - **Torneos internos**: crear, ver por estado, ver inscritos, asignar entrenador a un equipo, iniciar, registrar resultados, posiciones, finalizar (premios o bonos) e historial.
   - **Equipos y jugadores**: ver equipos, crear equipo, inscribir un socio en un equipo, dar de baja un jugador o un equipo.
   - **Inventario, tienda e instalaciones**: ver inventario (todo o por ubicación), ver instalaciones y si están libres en una fecha y hora, reabastecer la tienda, mover artículos entre Tienda y Bodega, y aprobar o rechazar sugerencias de los empleados.
   - **Turnos del personal**: ver los turnos y aprobar o rechazar solicitudes de cambio (no deja aprobar si quedan menos de 2 entrenadores y 1 fisioterapeuta).
   - **Reportes e historiales**: ventas por rubro (tienda, cafetería y mensualidades), asistencia, resultados, evolución del ranking e historial de reservas, partidos y sesiones, siempre entre dos fechas.
   - **Competencias oficiales**: registrar una competencia (liga o ASCUN), inscribir un equipo con su nómina y ver la tabla de posiciones.
4. Los datos se cargan y se guardan solos en `datos/club.dat`. La primera vez se crean datos de ejemplo (torneos, inventario, instalaciones y turnos).
5. Para probar un torneo completo: inscriba al menos dos socios desde `ConsolaParticipante` (deben estar en la disciplina y el nivel del torneo), vuelva a esta consola, inicie el torneo, registre los resultados y finalícelo.

### Pruebas JUnit del administrador

Clic derecho sobre cada archivo > **Run As > JUnit Test**. Ninguna escribe archivos.
- `GestionTorneosTest`: 15 pruebas de torneos (crear, iniciar, resultados, premios, bonos e historial).
- `AdministracionTest`: 12 pruebas de inventario (reabastecer y mover) y de reportes (ventas con mensualidades, asistencia, resultados y ranking).
- `ReservasPerfilTest`: 6 pruebas de reservas, de que el administrador no puede reservar y de la consulta del perfil.

## Proyecto 3 – Inscripciones y participantes

### Cómo ejecutar
1. Abra `src/presentacion/VentanaParticipante.java`, clic derecho > **Run As > Java Application**.
2. Los datos se cargan y se guardan solos en `datos/club.dat`. La primera vez se crean datos de ejemplo. Para empezar de cero, cierre la ventana y borre la carpeta `datos`.
3. Para salir, cierre la ventana (se guardan los cambios)

### Usuarios de prueba (todos con clave `1234`)
- Socios: `sofia` y `juan` (tenis intermedio), `maria` y `pedro` (pádel avanzado).
- Estudiante: `diana`.
- También puede crear un usuario nuevo con **Registrarme como estudiante** o **Registrarme como persona externa**.
- El administrador y los empleados no entran por esta ventana.

**Consola de empleados**
- Entrenadores: `carlos` (responsable del equipo Halcones Sub-15) y `laura`.
- Fisioterapeuta: `mateo`.

También se pueden registrar nuevos estudiantes y personas externas desde el menú inicial de la consola de participantes. El usuario no se puede repetir y la clave debe tener mínimo 4 caracteres.

Si alguien entra a una consola que no es de su rol, el programa se lo avisa. El login da 3 intentos y no dice cuál dato falló.

### Qué hace cada pestaña
- **Torneos:** ver torneos, inscribirse, inscribir pareja (por el usuario de la pareja), retirarse y ver clasificación y resultados.
- **Academias y reservas (solo socios):** inscribirse en una disciplina, reservar una instalación, programar una práctica y ver convocatorias.
- **Compras (solo socios):** comprar en tienda o cafetería con propina, puntos de fidelidad, código de descuento o bono; ver bonos e historial
- **Gráficas (solo socios):**
  - Líneas: evolución del ELO por semanas. Filtros de disciplina y número de semanas
  - Pastel: victorias, derrotas y empates. Filtros de disciplina y tipo (todas, prácticas o torneos)
- **Perfil:** datos del usuario

### Prueba rápida
1. Entre como `sofia` > pestaña **Torneos** > elija "Copa Tenis…" > **Inscribirme**.
2. En **Gráficas**, cambie la disciplina o las semanas y vea cómo cambia la línea.

### Pruebas JUnit
Clic derecho sobre `src/pruebas/GraficasParticipanteTest.java` > **Run As > JUnit Test** (6 pruebas de los datos de las gráficas).
