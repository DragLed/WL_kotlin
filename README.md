# Wishlist API

<div align="center">
  <h3>Аккуратный Kotlin backend для wishlist-сервиса</h3>
  <p>JWT-аутентификация, PostgreSQL, Exposed, Ktor и структура, которую приятно развивать дальше.</p>

  <p>
    <img src="https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
    <img src="https://img.shields.io/badge/Ktor-2.3.12-111111?style=for-the-badge&logo=ktor&logoColor=white" alt="Ktor">
    <img src="https://img.shields.io/badge/PostgreSQL-42.7.1-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
    <img src="https://img.shields.io/badge/Exposed-0.45.0-1D63ED?style=for-the-badge" alt="Exposed">
    <img src="https://img.shields.io/badge/JDK-21-E76F00?style=for-the-badge" alt="JDK 21">
  </p>
</div>

---

## Что это

`Wishlist API` это backend-приложение на `Kotlin + Ktor`, которое уже умеет:

- регистрировать пользователей;
- логинить через `JWT access token`;
- обновлять access token через `refresh_token` cookie;
- создавать wishlist от имени авторизованного пользователя;
- получать списки пользователей и wishlist-ов;
- публиковать OpenAPI и Swagger UI.

Проект выглядит как крепкая база под pet-project, учебный сервис или стартовую backend-архитектуру для полноценного wishlist-продукта.

---

## Почему проект уже приятно трогать

| Блок | Что внутри |
|---|---|
| `Auth` | access token + refresh token, защищённые маршруты через `authenticate("auth-jwt")` |
| `Persistence` | PostgreSQL + `Exposed` + автоматическое создание таблиц |
| `API layout` | роуты разнесены по `routing/Users.kt` и `routing/Wishlists.kt` |
| `Validation` | базовые проверки входных данных в `service`-слое |
| `Docs` | `GET /openapi` и `GET /swagger` уже подключены |

---

## API Snapshot

### User routes

| Method | Path | Описание | Auth |
|---|---|---|---|
| `POST` | `/user` | регистрация пользователя | нет |
| `POST` | `/login` | логин и выдача access token | нет |
| `POST` | `/logout` | удаление refresh cookie | нет |
| `POST` | `/refresh` | обновление access token по cookie | нет |
| `GET` | `/me` | получить `userId` из JWT | да |
| `GET` | `/users` | список пользователей | нет |
| `GET` | `/user/{id}` | получить пользователя по id | нет |
| `PUT` | `/user/{id}` | изменить username | нет |
| `PUT` | `/user/password/{id}` | изменить пароль | нет |
| `DELETE` | `/user/{id}` | удалить пользователя | нет |

### Wishlist routes

| Method | Path | Описание | Auth |
|---|---|---|---|
| `POST` | `/wishlist` | создать wishlist | да |
| `GET` | `/wishlists` | получить все wishlist-ы | нет |
| `GET` | `/wishlist/{id}` | получить wishlist по id | да |

---

## Как это работает

```text
Client
  -> POST /user
  -> POST /login
     <- access token + refresh_token cookie
  -> Authorization: Bearer <token>
  -> POST /wishlist
  -> GET /me
  -> POST /refresh
     <- новый access token
```

---

## Быстрый старт

### 1. Требования

- `JDK 21`
- `PostgreSQL`
- свободный порт `8080`

### 2. Создайте базу

```sql
CREATE DATABASE wishlist_db;
```

### 3. Проверьте подключение к БД

Сейчас подключение захардкожено в [Main.kt](src/main/kotlin/com/wishlistApp/Main.kt):

```kotlin
Database.connect(
    url = "jdbc:postgresql://localhost:5432/wishlist_db",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "0000"
)
```

Если у вас другие логин, пароль или имя базы, обновите эти значения перед запуском.

### 4. Запуск

Windows:

```powershell
.\gradlew.bat run
```

macOS / Linux:

```bash
./gradlew run
```

После старта API будет доступен по адресу:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger
```

OpenAPI:

```text
http://localhost:8080/openapi
```

---

## Быстрые примеры

### Регистрация

```bash
curl -X POST http://localhost:8080/user ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"strongpass123\"}"
```

### Логин

```bash
curl -X POST http://localhost:8080/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"strongpass123\"}"
```

### Создание wishlist

```bash
curl -X POST http://localhost:8080/wishlist ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" ^
  -d "{\"title\":\"Birthday\",\"description\":\"Tech gifts\",\"visibility\":\"PUBLIC\"}"
```

### Получить все wishlist-ы

```bash
curl http://localhost:8080/wishlists
```

---

## Структура проекта

```text
src/main/kotlin/com/wishlistApp
|-- Main.kt
|-- core/
|-- dto/
|-- model/
|-- repository/
|   |-- impl/
|   `-- tables/
|-- routing/
|   |-- Users.kt
|   `-- Wishlists.kt
`-- service/
```

Ключевые точки:

- [Main.kt](src/main/kotlin/com/wishlistApp/Main.kt) отвечает за запуск сервера, БД, Swagger и регистрацию роутов.
- [Users.kt](src/main/kotlin/com/wishlistApp/routing/Users.kt) содержит регистрацию, логин, refresh и CRUD по пользователям.
- [Wishlists.kt](src/main/kotlin/com/wishlistApp/routing/Wishlists.kt) отвечает за создание и чтение wishlist-ов.

---

## Что важно знать

- пароли при создании пользователя хэшируются через `BCrypt`;
- `refresh_token` хранится в `httpOnly` cookie;
- часть конфигурации пока хранится прямо в коде;
- проект ещё не выглядит как production-ready, но уже хорошо подходит как сильная база под дальнейшее развитие.

---

## Roadmap

- вынести секреты и настройки БД в `application.conf` или переменные окружения;
- закрыть изменение профиля и пароля авторизацией;
- добавить CRUD для подарков и правил доступа;
- покрыть маршруты и сервисы тестами;
- довести OpenAPI-спецификацию до полного соответствия актуальным эндпоинтам.

---

## Финальный вайб

Это уже не просто учебная заготовка, а backend, у которого есть понятная структура, аутентификация, работа с БД и первые реальные бизнес-сценарии. Для GitHub-репозитория такой README сразу объясняет, что проект живой, осмысленный и у него есть направление роста.
