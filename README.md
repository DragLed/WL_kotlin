# Wishlist API

Лаконичный backend-сервис для wishlist-приложения на `Kotlin`, `Ktor`, `Exposed` и `PostgreSQL`.

Сейчас проект уже умеет работать с пользователями, а доменные модели для wishlist-ов, подарков и ролей доступа подготовлены как основа для следующего этапа разработки.

## О проекте

`Wishlist API` - это серверное приложение, в котором заложена база для сервиса списков желаний:

- создание и хранение пользователей;
- работа с PostgreSQL через `Exposed`;
- JSON API на `Ktor`;
- заготовка под OpenAPI/Swagger;
- подготовленные модели для `wishlist`, `gift` и `wishlist access`.

Проект хорошо подходит как учебная backend-база, старт для pet-project или основа для дальнейшего развития полноценного REST API.

## Что уже работает

- `POST /users` - создать пользователя;
- `GET /users` - получить список пользователей;
- `GET /users/{id}` - получить пользователя по `id`;
- `DELETE /users/{id}` - удалить пользователя;
- автоматическое создание таблицы `users` при старте приложения.

## Что важно знать

- подключение к PostgreSQL сейчас захардкожено в [`src/main/kotlin/com/wishlistApp/Main.kt`](src/main/kotlin/com/wishlistApp/Main.kt);
- пароль пользователя пока хранится в открытом виде;
- OpenAPI-зависимости уже подключены, а файл спецификации лежит в [`src/main/resources/openapi/documentation.yaml`](src/main/resources/openapi/documentation.yaml), но полноценная выдача Swagger UI из приложения ещё не настроена;
- модели `Wishlist`, `Gift` и `WishlistAccess` уже есть в коде, но их маршруты и репозитории пока не реализованы.

## Стек

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-purple?logo=kotlin)](https://kotlinlang.org/)
[![Ktor](https://img.shields.io/badge/Ktor-2.3.7-orange)](https://ktor.io/)
[![Exposed](https://img.shields.io/badge/Exposed-0.45.0-blue)](https://github.com/JetBrains/Exposed)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue?logo=postgresql)](https://www.postgresql.org/)

## Быстрый старт

### 1. Требования

- `JDK 21`
- `PostgreSQL`
- доступный порт `8080`

### 2. Создайте базу данных

```sql
CREATE DATABASE wishlist_db;
```

### 3. Проверьте настройки подключения

Сейчас проект использует такие параметры подключения в [`src/main/kotlin/com/wishlistApp/Main.kt`](src/main/kotlin/com/wishlistApp/Main.kt):

```kotlin
Database.connect(
    url = "jdbc:postgresql://localhost:5432/wishlist_db",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "0000"
)
```

Если у вас другие логин, пароль или имя БД, просто обновите эти значения перед запуском.

### 4. Запустите приложение

Для Windows:

```powershell
.\gradlew.bat run
```

Для macOS/Linux:

```bash
./gradlew run
```

После старта приложение будет доступно по адресу:

```text
http://localhost:8080
```

## Примеры запросов

### Создание пользователя

```http
POST /users
Content-Type: application/json
```

```json
{
  "username": "alice",
  "password": "strongpass123"
}
```

Пример через `curl`:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"strongpass123\"}"
```

### Получить всех пользователей

```bash
curl http://localhost:8080/users
```

### Получить пользователя по `id`

```bash
curl http://localhost:8080/users/1
```

### Удалить пользователя

```bash
curl -X DELETE http://localhost:8080/users/1
```

## Правила валидации

На текущий момент сервис проверяет следующее:

- `username` не должен быть пустым;
- `password` не должен быть пустым;
- длина `password` должна быть не меньше `8` символов;
- `id` в маршрутах должен быть положительным числом.

## Структура проекта

```text
src/main/kotlin/com/wishlistApp
|-- Main.kt                 # точка входа и подключение к БД
|-- Routes.kt               # HTTP-маршруты
|-- dto/                    # входные DTO для запросов
|-- model/                  # доменные модели
|-- repository/             # контракт и таблицы
`-- service/                # бизнес-логика
```

Дополнительно:

- [`build.gradle.kts`](build.gradle.kts) - зависимости и конфигурация сборки;
- [`docs/index.html`](docs/index.html) - статический Swagger UI-файл в репозитории;
- [`src/main/resources/openapi/documentation.yaml`](src/main/resources/openapi/documentation.yaml) - черновик OpenAPI-спецификации.

## Ближайшие улучшения

- вынести настройки БД в `application.conf` или переменные окружения;
- добавить хэширование паролей;
- реализовать CRUD для wishlist-ов и подарков;
- подключить полноценную Swagger/OpenAPI-документацию;
- добавить тесты для `service` и `routes`;
- расширить обработку ошибок и ответы API.

## Итог

Сейчас это аккуратная и понятная база для будущего `wishlist`-сервиса: приложение уже запускается как Ktor API, сохраняет пользователей в PostgreSQL и задаёт хорошую структуру для дальнейшего роста проекта.
