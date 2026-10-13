# Race Shop Demo — intentionally vulnerable Spring Boot REST API + SPA

Mini sklep demonstracyjny do szkoleń AppSec. **Podatności Race Condition są celowe.**

## Stack
- Java 21
- Spring Boot 3.5.6
- Spring Web / Security / Data JPA
- H2 in-memory
- JWT podpisywany HMAC
- BCrypt do haseł
- Proste SPA (`src/main/resources/static/index.html`) do ataku z przeglądarki

## Uruchomienie
```bash
mvn spring-boot:run
```
API: `http://localhost:8088`  
UI (atak SPA): `http://localhost:8088/`

## Dane startowe
Produkty są tworzone przy starcie.

Użytkownik demo:
- email: `demo@example.com`
- hasło: `Password123!`

Kody:
- `RACE10` — 10%, **1 globalne użycie**, celowo podatny na Race Condition
- `WELCOME5` — 5%, 100 użyć (można łączyć z innymi kodami na jednym zamówieniu)

## Kody rabatowe na zamówieniu

- Koszyk i zamówienie trzymają **kolekcję** kodów (`discountCodes`), nie jeden string.
- **Różne** kody można dodać do tego samego koszyka (suma procentów od subtotalu, ograniczona do wartości koszyka).
- **Ten sam** kod nie powinien pojawić się na koszyku więcej niż raz ani przekroczyć globalnego `maxUses`.

Wrażliwy fragment w `applyDiscount` wykonuje logicznie:

1. odczytaj `usedCount` i listę kodów na koszyku,
2. sprawdź limity,
3. odczekaj 150 ms — celowo powiększa okno wyścigu,
4. zwiększ `usedCount` i dopisz kod do kolekcji.

Nie ma blokady pesymistycznej ani optymistycznej wersji encji. Równoległe żądania mogą przejść punkt 2, zanim którykolwiek zapisze stan.

## API

### Rejestracja
`POST /api/auth/register`
```json
{"email":"alice@example.com","firstName":"Alice","lastName":"Example","password":"Password123!"}
```

### Logowanie
`POST /api/auth/login`
```json
{"email":"demo@example.com","password":"Password123!"}
```
Odpowiedź zawiera JWT. Następne requesty wymagają:
`Authorization: Bearer <token>`

Po 3 błędnych hasłach konto jest logicznie blokowane.

### Produkty
`GET /api/products`

### Dodanie do koszyka
`POST /api/cart/items`
```json
{"productId":1,"quantity":2}
```

### Koszyk
`GET /api/cart`

Odpowiedź zawiera m.in. `discountCodes` (tablica stringów).

### Zmiana ilości
`PUT /api/cart/items/1`
```json
{"quantity":3}
```

### Kod rabatowy
`POST /api/cart/discount`
```json
{"code":"RACE10"}
```

Każde wywołanie dodaje kolejny (inny) kod do koszyka. Powtórzenie tego samego kodu zwraca błąd — o ile nie wygra wyścig.

### Zamówienie
`POST /api/orders`

Brak płatności — utworzenie zamówienia jest końcem procesu. Pole `discountCodes` w odpowiedzi odzwierciedla kolekcję z koszyka.

---

# Celowe podatności Race Condition

## 1. Race Condition w kodach rabatowych

### Atak SPA (wykład)

1. Otwórz `http://localhost:8088/`, zaloguj się jako `demo@example.com`.
2. Dodaj produkt do koszyka.
3. W UI przycisk „Zastosuj kod” respektuje **limit jednego kodu w interfejsie** (po sukcesie blokuje pole).
4. Kliknij **„Atak SPA: 20 równoległych POST”** — przeglądarka wysyła wiele równoczesnych `fetch()` z tym samym JWT.

Efekt szkoleniowy: mimo że użytkownik „widzi” jeden kod, w odpowiedzi koszyka może pojawić się **ten sam kod wielokrotnie** w `discountCodes`, a globalny `usedCount` dla `RACE10` może zostać przekroczony ponad `maxUses=1`.

### Demonstracja z curl

```bash
TOKEN=$(curl -s -X POST http://localhost:8088/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"Password123!"}' | jq -r .token)

seq 1 20 | xargs -P20 -I{} curl -s -X POST http://localhost:8088/api/cart/discount \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"code":"RACE10"}'

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8088/api/cart | jq .
```

### Jak naprawić
Przykładowe podejścia:
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` na użytkowniku i kodzie rabatowym + transakcja obejmująca check/update,
- atomowy SQL typu `UPDATE discount_codes SET used_count = used_count + 1 WHERE code=? AND used_count < max_uses`, a następnie sprawdzenie liczby zmienionych rekordów,
- unikalny constraint `(user_id, code)` na tabeli kodów koszyka + obsługa konfliktu,
- optimistic locking przez `@Version` i retry/obsługę konfliktu,
- ewentualnie mechanizm dystrybucyjny, jeżeli stan jest współdzielony między instancjami.

## 2. Race Condition w blokadzie konta

Logowanie celowo wykonuje check/increment/write bez blokady rekordu:

```text
if account is not locked
    verify password
    failedAttempts = failedAttempts + 1
    save()
```

Dodatkowe opóźnienie 100 ms zwiększa okno wyścigu. Kilka równoległych prób błędnego hasła może równocześnie zobaczyć ten sam wcześniejszy licznik i przejść kontrolę, zanim stan zostanie zapisany.

Istotny aspekt szkoleniowy: **nawet jeśli końcowa wartość `failedAttempts` nie przekroczy 3, wiele requestów mogło już zostać zaakceptowanych przed momentem, w którym konto stało się zablokowane.** To jest inna forma problemu niż zwykłe "lost update".

### Demonstracja
Najpierw upewnij się, że konto nie jest zablokowane. Następnie:

```bash
seq 1 10 | xargs -P10 -I{} curl -s -o /dev/null -w '%{http_code}\n' \
  -X POST http://localhost:8088/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"WrongPassword!"}'
```

Dla powtarzalnej demonstracji restart aplikacji resetuje bazę H2.

### Jak naprawić
Najprościej:
- pessimistic row lock (`SELECT ... FOR UPDATE`) obejmujący cały check/increment,
- atomowy warunkowy update licznika,
- optimistic locking (`@Version`) + poprawna obsługa konfliktów,
- albo dedykowany rate limiter / account-lock service z atomowym storage.

**Ważne:** sam `synchronized` nie rozwiązuje problemu w środowisku wieloinstancyjnym.

---

## Ćwiczenie hardeningowe

Po demonstracji podatności uczestnicy mogą przygotować dwie poprawki:

1. zabezpieczyć `RACE10` tak, aby dokładnie jedno równoległe żądanie mogło zużyć ostatnie dostępne użycie i aby ten sam kod nie mógł zostać dopisany do koszyka dwukrotnie;
2. zagwarantować, że po osiągnięciu 3 nieudanych prób żadne równoległe logowanie nie przejdzie dalej.

Następnie warto uruchomić test integracyjny z np. 20–100 równoległymi requestami i zweryfikować invarianty:

- `usedCount <= maxUses`,
- liczba skutecznych redemptionów nie przekracza `maxUses`,
- brak duplikatów w `discountCodes` na koszyku,
- po trzeciej nieudanej próbie kolejne próby są odrzucane atomowo.

## Uwaga
To jest aplikacja szkoleniowa. Celowe race conditions i uproszczenia bezpieczeństwa nie powinny być kopiowane do systemu produkcyjnego.
