# SoporteHub · Reparto del código por persona

Todo el código vive en UN solo proyecto (así compila junto). Cada persona es dueña de los archivos listados abajo.
Rutas relativas a `src/main/java/pa/soportehub/` (java), `src/main/resources/` (res) y `src/test/java/pa/soportehub/` (test).

## Común (se hace primero, entre todos)
pom.xml · application.yml · .env.example · .gitignore · docker-compose.yml · SoporteHubApplication
common/* (excepciones, GlobalExceptionHandler, Dates) · domain/ticket/Priority
res: db/migration/V1__base.sql · messages.properties · static/css/app.css

## Persona 1 · Seguridad y acceso
java: domain/user/* (User, Role, UserStatus, UserRepository) · security/* (JwtService, JwtAuthenticationFilter,
SecurityConfig, TenantContext, CurrentUser, AuthenticatedUser, AuthService, AuthCookies, LoginAttemptLimiter,
TokenRevocationStore) · web/AuthController, ApiAuthController, HomeController
res: V2__user_roles.sql · templates/fragments.html, login.html, error.html

## Persona 2 · Empresas, usuarios e invitaciones
java: domain/organization/* · domain/notification/EmailSender · infrastructure/email/ResendEmailSender ·
infrastructure/redis/InvitationTokenStore · application/command/{RegisterOrganization*, InviteUser*, AcceptInvitation*,
SettingsCommandHandler} · application/event/{UserInvitedEvent, InvitationEmailListener} ·
web/{OrganizationController, UserController, InvitationController, SettingsController}
res: V3__sla_and_assignment_strategy.sql · templates/{organizations, users, accept-invitation, settings}.html

## Persona 3 · Núcleo del ticket (escritura)
java: domain/ticket/{Ticket, TicketStatus, TicketComment, TicketStatusHistory, TicketFactory, TicketRepository} ·
infrastructure/persistence/* · application/assignment/* · application/event/TicketChangedEvent ·
application/command/{CreateTicket*, AssignTicket*, ChangeTicketStatus*, AddComment*, ReopenTicket*} ·
web/{TicketController, TicketApiController}
res: V4__tickets.sql · test: domain/ticket/TicketTest

## Persona 4 · Lectura (CQRS), pantallas y calidad
java: application/query/* · application/event/TicketCacheInvalidationHandler · infrastructure/redis/InboxCache ·
infrastructure/demo/DemoDataSeeder · web/{TicketListController, TicketPageController, TicketQueryApiController}
res: V5__inbox_index.sql · templates/{ticket-list, new-ticket, ticket-detail}.html · test: isolation/TenantIsolationTest

## Puntos donde una persona depende de otra (acuérdenlos antes de programar)
- P1 define `User` y `UserRepository`; P2, P3 y P4 los usan. P3 y P4 usan `TenantContext` y `CurrentUser` de P1.
- P2 define `Category` y `SlaPolicy`; P3 las usa al crear un ticket.
- P3 define `TicketChangedEvent`; P4 lo escucha para invalidar la caché.
- Los formularios de `ticket-detail.html` (P4) envían a los endpoints de `TicketController` (P3).
