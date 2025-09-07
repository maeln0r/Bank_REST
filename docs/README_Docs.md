# Bank Cards API — Документация

Этот документ описывает, как пользоваться REST API сервиса банковских карт: аутентификация, управление пользователями и картами, переводы, формат ошибок, пагинация и соглашения.

> **Спецификация API**: OpenAPI 3.1 — файл `src/main/resources/docs/openapi.yaml`
---

## Содержание

* [Обзор](#обзор)
* [Аутентификация](#аутентификация)
* [Заголовки и язык](#заголовки-и-язык)
* [Формат ошибок (RFC 7807)](#формат-ошибок-rfc-7807)
* [Пагинация](#пагинация)
* [Сущности и основные операции](#сущности-и-основные-операции)

    * [Auth](#auth)
    * [Текущий пользователь](#текущий-пользователь)
    * [Админ — пользователи](#админ--пользователи)
    * [Карты](#карты)
* [Примеры запросов (curl)](#примеры-запросов-curl)
* [CORS (для фронтенда)](#cors-для-фронтенда)
* [Версионирование спецификации и клиента](#версионирование-спецификации-и-клиента)
* [Коллекция Postman/Insomnia](#коллекция-postmaninsomnia)

---

## Обзор

* **Базовый URL (local)**: `http://localhost:8080`
* **Безопасность**: `Bearer JWT` — заголовок `Authorization: Bearer <token>`
* **Формат дат/времени**: ISO‑8601 UTC (`2025-09-07T09:00:00Z`)
* **Деньги/суммы**: десятичные числа, сервер валидирует масштаб до 2 знаков и лимиты.

## Аутентификация

1. `POST /api/auth/login` — по `usernameOrEmail` и `password` возвращает `TokenResponse`:

   ```json
   {
     "tokenType": "Bearer",
     "accessToken": "<jwt>",
     "refreshToken": "<jwt>",
     "expiresInSeconds": 3600
   }
   ```
2. `POST /api/auth/refresh` — обмен `refreshToken` на новый `accessToken`.
3. Для защищённых эндпоинтов добавляйте: `Authorization: Bearer <accessToken>`.

## Заголовки и язык

* `Authorization: Bearer <token>` — обязательный для защищённых операций.
* `Accept-Language: ru | en` — влияет на человекочитаемое поле `detail` в ответах об ошибках.
* `X-Request-Id` — корреляция запросов; также `traceId` возвращается в теле ошибок.

## Формат ошибок (RFC 7807)

Все ошибки — `application/problem+json`, расширено полями:

* `code` — стабильный код (например, `error.password.current_invalid`)
* `timestamp` (ISO‑UTC), `epochMillis`, `traceId`, `path`
* `errors[]` — список ошибок полей `{ field, message, code }`

**Пример 401 (неверный refresh):**

```json
{
  "type": "urn:error:error.refresh.invalid",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Invalid refresh token",
  "code": "error.refresh.invalid",
  "traceId": "...",
  "timestamp": "2025-09-07T09:00:00Z",
  "epochMillis": 1757258531734,
  "path": "/api/auth/refresh"
}
```

**Пример 422 (неверный текущий пароль):**

```json
{
  "type": "urn:error:error.password.current_invalid",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Текущий пароль указан неверно",
  "code": "error.password.current_invalid",
  "errors": [
    { "field": "currentPassword", "message": "Текущий пароль указан неверно", "code": "error.password.current_invalid" }
  ],
  "traceId": "...",
  "timestamp": "2025-09-07T09:00:00Z",
  "epochMillis": 1757258531734,
  "path": "/api/users/me/password"
}
```

## Пагинация

Листинги возвращают объект страницы (`Page<T>`):

```json
{
  "content": [ /* элементы */ ],
  "totalElements": 123,
  "totalPages": 7,
  "size": 20,
  "number": 0,
  "sort": "createdAt,desc",
  "first": true,
  "last": false,
  "numberOfElements": 20,
  "empty": false
}
```

Параметры: `page` (0..), `size` (1..200), `sort` (например, `createdAt,desc`).

## Сущности и основные операции

### Auth

* `POST /api/auth/login` — логин (200)
* `POST /api/auth/refresh` — обновление токена (200), при ошибке 401 `error.refresh.invalid`

### Текущий пользователь

* `GET /api/users/me` — профиль (200)
* `POST /api/users/me/password` — смена пароля (204)

### Админ — пользователи (ROLE\_ADMIN)

* `GET /api/admin/users` — список (paged)
* `POST /api/admin/users` — создать (200 — возвращает созданного пользователя)
* `GET /api/admin/users/{id}` — детально (200)
* `PATCH /api/admin/users/{id}` — частичное обновление (200)
* `POST /api/admin/users/{id}/password` — смена пароля (204)
* `DELETE /api/admin/users/{id}` — удалить (204)

### Карты

* `GET /api/cards` — список (paged; фильтры: `status`, `createdFrom`, `createdTo`, `last4`)
* `POST /api/cards` — создать (201)
* `GET /api/cards/{id}` — получить (200)
* `PATCH /api/cards/{id}/block` — блокировать (200)
* `PATCH /api/cards/{id}/activate` — активировать (200)
* `POST /api/cards/{id}/block-request` — запросить блокировку владельцем (204)
* `POST /api/cards/transfer` — перевод между своими картами (204)
* `DELETE /api/cards/{id}` — удалить (204)

## Примеры запросов (curl)

> Замените `BASE=http://localhost:8080`

**Логин**

```bash
BASE=http://localhost:8080
curl -s -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"usernameOrEmail":"user@example.com","password":"secret"}'
```

**Использование токена**

```bash
TOKEN="<accessToken>"
curl -s "$BASE/api/users/me" -H "Authorization: Bearer $TOKEN"
```

**Смена пароля (self)**

```bash
curl -i -X POST "$BASE/api/users/me/password" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"currentPassword":"old","newPassword":"newStrong"}'
```

**Создание карты**

```bash
curl -s -X POST "$BASE/api/cards" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "ownerId":"11111111-1111-1111-1111-111111111111",
    "expiry":"2030-12",
    "pan":"4111 1111 1111 1111",
    "initialBalance": 1000.00
  }'
```

**Перевод между картами (204)**

```bash
curl -i -X POST "$BASE/api/cards/transfer" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "fromCardId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "toCardId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    "amount": 25.50
  }'
```

## CORS (для фронтенда)

Настраивается через свойства `app.cors.*` (бин `CorsProps`). Из переменных application.yaml