# Portfolio-Konzept: BudgetPilot (SaaS)

## 1. Zielbild

Du baust ein öffentlich zugängliches SaaS-Produkt, mit dem Teams ihre Finanzen gemeinsam planen, überwachen und steuern können.  
Das Projekt soll nicht nur "funktionieren", sondern in Architektur, Qualität, Sicherheit und Produktreife zeigen, dass du große Systeme professionell umsetzen kannst.

Kernidee:
- Eine Organisation (Team/Firma) verwaltet Budgets, Konten, Kategorien und Transaktionen.
- Mitglieder arbeiten rollenbasiert zusammen.
- Kritische Prozesse (Freigaben, Rollenwechsel, Monatsabschluss) sind sauber abgesichert.
- Das System ist vollständig getestet und öffentlich deployt.
- Eine öffentliche MainPage erklärt Produktnutzen, Features, Preise, Sicherheit und bietet einen klaren Einstieg in Demo/Registrierung.

Finaler Produktname:
- **BudgetPilot**

---

## 2. Technologie-Entscheidung: React oder Angular?

## Empfehlung für dein Portfolio: **React (mit Next.js)**

Begründung:
- Sehr hohe Verbreitung im Markt und starke Signalwirkung im Portfolio.
- Next.js bringt direkt Production-Features mit (Routing, Server Components, API-nahe Struktur, Performance).
- Starkes Testing-Ökosystem (Vitest/Jest, Testing Library, Playwright, MSW).
- Gute Kombinierbarkeit mit Design-System und modernen UI-Bibliotheken.

Wann Angular trotzdem sinnvoll wäre:
- Wenn du gezielt Enterprise-Frontend-Kompetenz in einem klassischen Angular-Umfeld zeigen willst.
- Wenn du bereits in Angular sehr stark bist und dadurch schneller auf ein "polished" Endergebnis kommst.

Finale Entscheidung:
- Für maximale Portfolio-Wirkung: **Frontend mit React + Next.js**.
- Du kannst in der README transparent schreiben, dass du aus einem Angular-Umfeld kommst und bewusst React gewählt hast, um Breite zu demonstrieren.

Ergänzung:
- Das Backend nutzt **Gradle** (Kotlin DSL empfohlen), um Build-Qualität, modulare Struktur und CI-Performance klar zu zeigen.

---

## 3. High-Level Architektur

## Zielarchitektur (startbar, aber ausbaufähig)

1. **Modularer Monolith in Spring Boot** (Startphase)
- Saubere Modulgrenzen innerhalb eines Deployments.
- Schneller lieferbar als verteilte Microservices.
- Später optional in Services trennbar.

2. **API-First Backend**
- Extern: REST API (OpenAPI dokumentiert).
- Intern (optional Phase 2+): gRPC zwischen internen Modulen/Services (z. B. Reporting, Notification).

3. **Datenspeicher**
- PostgreSQL als primäre relationale Datenbank.
- Redis für Cache, Rate Limiting, Session/Token-Invalidation, Job-Queue-Unterstützung.
- Optional Cassandra für sehr große Event-/Audit-Historie.

4. **Asynchrone Verarbeitung**
- Domain Events (z. B. `TransactionCreated`, `BudgetThresholdReached`).
- Jobs für Benachrichtigungen, wiederkehrende Buchungen, Report-Generierung.

5. **Beobachtbarkeit**
- Strukturierte Logs.
- Metriken (Prometheus).
- Dashboards (Grafana).
- Tracing optional (OpenTelemetry).

6. **Infrastruktur-Standard**
- Containerisierung mit Docker (Multi-Stage Builds).
- Deployment über Kubernetes-kompatible Artefakte (Helm Chart oder Kustomize + Manifeste).

---

## 4. Fachliche Kern-Features

## 4.1 MVP (erste öffentliche Version)

- Öffentliche MainPage (Produktseite) mit Conversion-Fokus
- Registrierung/Login
- Organisation erstellen
- Mitglieder einladen
- Rollen und Berechtigungen
- Konten und Kategorien
- Transaktionen (Einnahme/Ausgabe)
- Budgets pro Zeitraum
- Dashboard (Soll/Ist, Restbudget, Trend)

## 4.2 Erweiterte Features (Portfolio-Boost)

- Wiederkehrende Transaktionen
- Freigabeprozess bei Ausgaben > Schwellwert
- Monatsabschluss mit Sperrlogik
- Aktivitätsfeed und Audit-Trail
- CSV-Import und regelbasierte Kategorisierung
- Benachrichtigungen (E-Mail/In-App)
- Team-Kommentare und Beleg-Uploads
- Öffentliche Demo-Organisation (read-only)

## 4.3 MainPage / Marketing-Website (separater Bereich)

- Hero mit klarem Value Proposition Satz
- Feature-Sektionen mit konkreten Screens und Nutzen
- Vergleich "Spreadsheet vs BudgetPilot"
- Sicherheits- und Datenschutzabschnitt
- Preis-/Planübersicht (auch wenn zunächst nur Free + Demo)
- CTA: "Live Demo", "Kostenlos starten", "Kontakt"
- SEO-Basis (Meta Tags, OpenGraph, strukturierte Daten)
- Rechtliches: Impressum, Datenschutz, Cookie-Hinweis (falls Tracking)

---

## 5. Multi-Tenancy und Rollenmodell

## 5.1 Organisationsmodell

- Jeder Datensatz ist einer `organization_id` zugeordnet.
- Jede Anfrage wird auf Organisationsebene autorisiert.
- Kein Datenzugriff über Organisationsgrenzen hinweg.

## 5.2 Rollen

- `OWNER`
    - Vollzugriff
    - Organisationseinstellungen
    - Owner-Transfer
    - Kritische Löschoperationen
- `ADMIN`
    - Mitglieder einladen/verwalten
    - Budgets/Kategorien/Konten verwalten
    - Freigaben erteilen
- `MEMBER`
    - Transaktionen erstellen/bearbeiten
    - Budgetvorschläge
    - Fachliche Teamarbeit
- `VIEWER`
    - Lesender Zugriff auf Dashboards/Reports

## 5.3 Sicherheitsregeln Rollenverwaltung

- Mindestens ein `OWNER` muss immer existieren.
- `ADMIN` darf keine `OWNER`-Rolle vergeben/entfernen.
- Kein unkontrollierter Self-Demotion, der den letzten `OWNER` entfernt.
- Jede Rollenänderung erzeugt ein Audit-Event.

---

## 6. Datenmodell (vereinfachter Entwurf)

## 6.1 Haupttabellen

- `users`
    - `id`, `email`, `password_hash`, `status`, `created_at`
- `organizations`
    - `id`, `name`, `slug`, `created_by`, `created_at`
- `organization_memberships`
    - `organization_id`, `user_id`, `role`, `joined_at`, `status`
- `invites`
    - `id`, `organization_id`, `email`, `role`, `token_hash`, `expires_at`, `accepted_at`, `invited_by`
- `accounts`
    - `id`, `organization_id`, `name`, `type`, `currency`
- `categories`
    - `id`, `organization_id`, `name`, `kind` (INCOME/EXPENSE)
- `budgets`
    - `id`, `organization_id`, `category_id`, `period_start`, `period_end`, `limit_amount`
- `transactions`
    - `id`, `organization_id`, `account_id`, `category_id`, `amount`, `direction`, `booking_date`, `created_by`, `approval_state`
- `recurring_transactions`
    - `id`, `organization_id`, `rule`, `next_run_at`, `active`
- `audit_events`
    - `id`, `organization_id`, `actor_user_id`, `event_type`, `entity_type`, `entity_id`, `payload`, `created_at`

## 6.2 Wichtige Constraints und Indizes

- Unique:
    - `users.email`
    - `organizations.slug`
    - `organization_memberships (organization_id, user_id)`
- Indizes:
    - `transactions (organization_id, booking_date)`
    - `transactions (organization_id, category_id, booking_date)`
    - `audit_events (organization_id, created_at)`
    - `invites (organization_id, email, accepted_at)`

---

## 7. API-Design (Version 1)

## 7.1 Auth

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

## 7.2 Organisation & Mitglieder

- `POST /api/v1/organizations`
- `GET /api/v1/organizations/{orgId}`
- `GET /api/v1/organizations/{orgId}/members`
- `POST /api/v1/organizations/{orgId}/invites`
- `POST /api/v1/invites/{token}/accept`
- `PATCH /api/v1/organizations/{orgId}/members/{userId}/role`
- `DELETE /api/v1/organizations/{orgId}/members/{userId}`

## 7.3 Finance

- `POST /api/v1/organizations/{orgId}/accounts`
- `POST /api/v1/organizations/{orgId}/categories`
- `POST /api/v1/organizations/{orgId}/budgets`
- `GET /api/v1/organizations/{orgId}/budgets`
- `POST /api/v1/organizations/{orgId}/transactions`
- `GET /api/v1/organizations/{orgId}/transactions`
- `PATCH /api/v1/organizations/{orgId}/transactions/{transactionId}`
- `POST /api/v1/organizations/{orgId}/transactions/{transactionId}/approve`

## 7.4 Reporting

- `GET /api/v1/organizations/{orgId}/dashboard/summary`
- `GET /api/v1/organizations/{orgId}/reports/monthly`
- `GET /api/v1/organizations/{orgId}/audit-events`

## 7.5 MainPage / Public Website

- `GET /api/v1/public/features`
- `GET /api/v1/public/pricing`
- `POST /api/v1/public/contact`
- `POST /api/v1/public/waitlist`

---

## 8. Backend-Struktur (Spring Boot)

Vorschlag als modulare Struktur:

- `com.yourname.finance.auth`
- `com.yourname.finance.organization`
- `com.yourname.finance.membership`
- `com.yourname.finance.invite`
- `com.yourname.finance.accounting`
- `com.yourname.finance.budget`
- `com.yourname.finance.reporting`
- `com.yourname.finance.audit`
- `com.yourname.finance.shared`

Pro Modul:
- `controller`
- `service`
- `domain` (Entities/Value Objects/Rules)
- `repository`
- `dto`
- `mapper`

Zusätzlich:
- `security` (JWT, RBAC, Method Security)
- `config`
- `exception` (globales Error Handling)

Build und Tooling:
- Gradle Wrapper (`./gradlew`) als verpflichtender Einstieg
- Gradle Kotlin DSL (`build.gradle.kts`)
- Quality Gates mit JaCoCo, Checkstyle/Spotless, Dependency-Check

---

## 9. Frontend-Struktur (React + Next.js)

Vorschlag:

- `src/app` (Routes)
- `src/features/auth`
- `src/features/organization`
- `src/features/members`
- `src/features/transactions`
- `src/features/budgets`
- `src/features/reports`
- `src/shared/api`
- `src/shared/ui`
- `src/shared/lib`
- `src/shared/types`

UX-Leitlinien:
- Klare Rollenanzeige an jeder kritischen Aktion.
- Optimistisches UI nur bei unkritischen Aktionen.
- Bei Rollen-/Löschaktionen immer Confirm-Dialog.
- Leere Zustände und Fehlerzustände sauber behandeln.

Top UI/UX Standards:
- Einheitliches Design-System (Farben, Typografie, Spacing, States, Komponentenregeln)
- WCAG-orientierte Accessibility (Kontrast, Tastatur-Navigation, Screenreader Labels)
- Sehr schnelle Interaktion (Skeletons, sensible Prefetching-Strategien, keine unnötigen Reloads)
- Konsistente Microinteractions (Hover, Fokus, Transition, Success/Error Feedback)
- Onboarding-Flow in unter 3 Minuten bis zur ersten sinnvollen Aktion
- Mobile-First Layout mit sauberer Desktop-Erweiterung

---

## 10. Internationalisierung (Deutsch/Englisch, ohne Hardcoding)

Ziel:
- Komplette UI in `de` und `en` umschaltbar.
- Keine hartcodierten UI-Texte in Komponenten.
- Einheitliche Sprache in UI, E-Mails und serverseitigen Fehlermeldungen.

Technischer Ansatz (Frontend):
- `next-intl` als i18n-Layer in Next.js.
- Nur Translation Keys im Code, keine Rohtexte.
- Namespaces pro Feature (`common`, `auth`, `members`, `budget`, `emails`).
- Sprachumschaltung global im Header/User-Menü.

Empfohlene Ordnerstruktur:
- `src/i18n/request.ts`
- `src/i18n/routing.ts`
- `messages/de/common.json`
- `messages/de/auth.json`
- `messages/de/members.json`
- `messages/en/common.json`
- `messages/en/auth.json`
- `messages/en/members.json`

Locale-Auflösung:
1. Gespeicherte User-Sprache (höchste Priorität)
2. Browser `Accept-Language`
3. Fallback `en`

Backend i18n:
- Fehlerantworten als stabile Error-Codes (z. B. `INVITE_EXPIRED`, `ROLE_FORBIDDEN`).
- Spring `MessageSource` für serverseitige Texte (`messages_de.properties`, `messages_en.properties`).
- E-Mail-Templates sprachabhängig (`invite.de.html`, `invite.en.html`, `reset.de.html`, `reset.en.html`).

Qualitätsregeln gegen Hardcoding:
- ESLint-Regel oder Team-Rule: keine sichtbaren UI-Literals in Feature-Komponenten.
- CI-Check auf fehlende Keys zwischen `de` und `en`.
- Snapshot/Unit-Tests für zentrale Seiten in beiden Sprachen.

Formatierung und UX:
- Währung, Datum und Zahlen locale-sensitiv per `Intl.*`.
- Beispiel:
    - `de-DE`: `1.234,56 EUR`
    - `en-US`: `EUR 1,234.56`
- Sprachwechsel ohne vollen Reload, wenn technisch möglich.

---

## 11. Teststrategie: Full Unit-Tests Frontend + Backend

## 11.1 Testpyramide

- Viele Unit-Tests
- Einige Integrationstests
- Wenige, aber geschäftskritische E2E-Tests

## 11.2 Backend-Tests

Tools:
- JUnit 5
- Mockito
- AssertJ
- Testcontainers (PostgreSQL, Redis)
- Spring Boot Test / MockMvc
- JaCoCo für Coverage-Reports im Gradle-Build

Pflichtabdeckung:
- Services:
    - Rollenlogik
    - Einladungstoken
    - Budgetregeln
    - Freigabeprozess
- Controller:
    - Statuscodes
    - Validation Errors
    - Security (401/403)
- Repository/Integration:
    - Query-Korrektheit
    - Constraints
    - Migrations (Flyway)

Beispielhafte Business-Testfälle:
- Admin kann Member einladen.
- Admin kann keinen Owner entfernen.
- Letzter Owner kann sich nicht selbst entfernen.
- Ausgabe über Schwellwert benötigt Freigabe.
- Monatsabschluss sperrt neue Buchungen für den Zeitraum.

## 11.3 Frontend-Tests

Tools:
- Vitest oder Jest
- React Testing Library
- MSW für API-Mocks
- Playwright für E2E

Pflichtabdeckung:
- Komponenten:
    - Form-Validierung
    - Rollenabhängige UI
    - Error/Loading States
- State/API:
    - Query/Mutation-Flows
    - Retry/Error Handling
- E2E-Kernflüsse:
    - Registrierung
    - Organisation erstellen
    - Einladung senden/akzeptieren
    - Rolle ändern
    - Transaktion anlegen und genehmigen

## 11.4 CI-Testpipeline

Reihenfolge:
1. Lint
2. Typecheck
3. Unit-Tests Frontend
4. Unit-Tests Backend
5. Integrationstests Backend (Testcontainers)
6. E2E Smoke Tests
7. Build und Artefakte

---

## 12. Security-Konzept

- JWT Access + Refresh Tokens
- Rotation/Invalidation von Refresh Tokens
- Passwort-Hashing mit BCrypt/Argon2
- RBAC und optional ABAC für feinere Regeln
- Input Validation überall
- Rate Limiting (Login, Invite, sensible Endpoints)
- CORS nur für erlaubte Origins
- Security Headers
- Audit Logging für sicherheitsrelevante Aktionen
- Keine Secrets im Repo (nur Env Vars/Secret Manager)

---

## 13. DevOps und Deployment (öffentlich)

## 13.1 Lokale Entwicklung

- `docker-compose` für PostgreSQL + Redis
- Backend und Frontend lokal startbar
- Seed-Skript für Demo-Daten
- Lokaler Mailhog/Mailpit Container für E-Mail-Flow-Tests

## 13.2 Staging/Production

- Frontend: Vercel oder Netlify
- Backend: Render/Fly.io/Railway oder AWS
- Datenbank: Managed PostgreSQL
- Redis: Managed Redis
- Eigener Mail-Server oder SMTP-Provider (z. B. Mailgun, SendGrid, Postmark)
- Kubernetes-kompatibler Betrieb über Docker-Images + Helm/Kustomize

## 13.3 CI/CD

- GitHub Actions:
    - `pull_request`: Lint + Tests
    - `main`: Build + Deploy
- Migrations automatisch im Deploy-Prozess
- Health Checks und Rollback-Strategie
- Build- und Release-Pipeline für Kubernetes-Artefakte (Image Tags, Helm Release)

## 13.4 Domain- und Namensstrategie

- Finaler Produktname: `BudgetPilot`
- Domain-Ziel: `de.budgetpilot.finance` (alternativ `app.budgetpilot.finance`)
- Landing/MainPage unter `www.budgetpilot.finance` oder Root-Domain
- App unter Subdomain (`app`), API unter `api`, Monitoring intern abgesichert
- SPF, DKIM, DMARC für E-Mail-Zustellbarkeit verpflichtend

---

## 14. Observability und Betriebsqualität

- `/actuator/health` und Readiness Checks
- Prometheus-Metriken:
    - Request-Latenz
    - Fehlerquote
    - DB-Query-Dauer
    - Queue-Länge/Job-Fehler
- Grafana-Dashboards:
    - API Health
    - Business KPI (z. B. Anzahl Transaktionen/Tag)
- Strukturierte Logs mit Correlation ID

---

## 15. Portfolio-Inszenierung (sehr wichtig)

Was dein Projekt "seniorig" wirken lässt:
- Saubere Architekturübersicht in der README.
- Klare Trade-off-Entscheidungen dokumentiert.
- Öffentliche Demo mit Test-Account.
- Realistische Seed-Daten.
- Gute Screenshots / kurzes Demo-Video.
- API-Doku und Postman-Collection.
- Test- und Coverage-Badges.
- Roadmap/Changelog mit Releases.
- Öffentliche Landing Page mit klarer Story und echter Demo-Route.

---

## 16. Roadmap in Phasen (12 Wochen)

## Phase 1 (Woche 1-2): Foundation

- Repo-Struktur
- Auth-Grundlagen
- Organization + Membership Kern
- CI-Basis
- Docker lokale Umgebung
- Produktname + Domain-Setup + MainPage Wireframe

## Phase 2 (Woche 3-4): Finance Core

- Konten, Kategorien, Transaktionen
- Budgets und Dashboard v1
- Erste umfangreiche Unit-Tests

## Phase 3 (Woche 5-6): Team-Funktionen

- Invite-Flow
- Rollenwechsel
- Audit-Events
- Frontend-Rollensteuerung

## Phase 4 (Woche 7-8): Advanced Business Logic

- Approval Workflow
- Recurring Transactions
- Monatsabschluss
- Report-Endpunkte

## Phase 5 (Woche 9-10): Qualität und Betrieb

- Integrationstests mit Testcontainers
- E2E Kernflüsse
- Monitoring + Alerting
- Security-Härtung
- E-Mail-Server-Integration inkl. Zustellbarkeitstests

## Phase 6 (Woche 11-12): Public Launch

- Deployment Staging/Prod
- Demo-Daten + Landing Page
- README/Architekturdiagramme
- Portfolio-Präsentation finalisieren
- Kubernetes-Rollout und Betriebsdokumentation

---

## 17. Nächste konkrete Schritte (Start-Backlog)

1. Technologiestack final fixieren:
- Frontend: React + Next.js
- Backend: Spring Boot 3
- DB: PostgreSQL + Redis
- Build: Gradle (Kotlin DSL)

2. Monorepo oder getrennte Repos festlegen.

3. API-Verträge für Auth/Organization/Invite definieren.

4. Datenbankschema via Flyway initialisieren.

5. Erste 10 Tickets im Board anlegen:
- Auth Register/Login
- Organization Create/Get
- Membership Tabelle + Roles
- Invite Create/Accept
- Role Update Endpoint
- Frontend Login/Onboarding
- Members UI mit Rollen-Dropdown
- Backend Unit Tests InviteService
- Frontend Unit Tests Members Screen
- CI Pipeline für Lint + Unit Tests

6. Branding-Tickets ergänzen:
- Produktname festschreiben: `BudgetPilot`
- Domain registrieren und DNS setzen (`de.budgetpilot.finance`)
- MainPage v1 (Hero, Features, CTA, Demo)
- SMTP/E-Mail-Provider anbinden (Invite, Verify, Password Reset)
- i18n-Setup für `de` und `en` inkl. Locale-Switcher und CI-Key-Check

---

## 18. Optionale Erweiterungen (nach MVP)

- Mehrwährungsfähigkeit mit Wechselkursen
- Anomalie-Erkennung bei Ausgaben
- KI-gestützte Budgetprognose
- Mobile App (React Native)
- Event Sourcing für Audit-Intensität
- Mandantenfähiges Billing mit Stripe

---

## 19. Fazit

Dieses Projekt ist ideal, um dich als sehr starken Full-Stack-Entwickler zu positionieren:
- Tiefes Backend (Sicherheit, Business-Regeln, Tests)
- Modernes Frontend (UX, Testing, State-Management)
- Reale Teamprozesse (Rollen, Freigaben, Audit)
- Betriebskompetenz (CI/CD, Monitoring, Deployment)

Wenn du das sauber umsetzt und öffentlich zeigst, ist es deutlich mehr als ein Standard-Portfolio-Projekt und wirkt wie ein echtes Produkt.
