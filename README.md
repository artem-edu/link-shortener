# Приложение «сервис сокращения ссылок» (Link Shortnener cli)

Итоговый проект (сессионное задание). Программирование на языке Java

---

## Архитектура приложения

Проект спроектирован с упором на принципы SOLID, слабую связанность и явное разделение ответственности. В основе лежит многослойная архитектура

## Компоненты архитектуры:

* Слой модели: инкапсулирует бизнес-правила системы (service) и логику персистентности данных (repository). Сервисы изолированы от консольного вывода и оперируют чистыми данными.
* Слой Представления: разделен на 3 класса
    * App - собирает в себе все необходимые сервисы, управляет логикой выполнения команд, координирует потоки данных между сервисами и выводом 
    * ConsolerReader - чтение команд пользователя
    * ConsleView - глупый компонент, отвечающий только за вывод информации
* Ядро: содержит в себе абстракцию файлового репозитория
## Диаграммы взаимодействия
### 1. Инициализация приложения

```mermaid
flowchart TD
    A["mephi.Main.main()"] --> B["App.bootstrap()"]
    B --> C["new UserRepository()<br/>загрузка db/users.json"]
    B --> D["new LinkRepository()<br/>загрузка db/links.json<br/>+ построение hashIndex"]
    B --> E["new UserService(userRepository)"]
    B --> F["new ConsoleView()"]
    B --> G["new LinkService(linkRepository,<br/>new BrowserService())"]
    B --> H["new ExpiredLinksCleaner(linkRepository,<br/>view::printNotification, userService)<br/>чтение CLEANUP_INTERVAL_MS из .env"]
    B --> I["new ConsoleReader(view)"]
    C --> J["new App(userService, linkService,<br/>linkRepository, cleaner, view, reader)"]
    D --> J
    E --> J
    F --> J
    G --> J
    H --> J
    I --> J
    J --> K["run()"]
    K --> L["view.printWelcome()"]
    L --> M["expiredLinksCleaner.start()<br/>планировщик чистки истёкших ссылок"]
    M --> N["view.printMenu()"]
    N --> O{"Цикл меню<br/>reader.readMenuChoice()"}
    O -->|"1–7, help"| P["Обработка команды"]
    P --> O
    O -->|"0"| Q["expiredLinksCleaner.stop()<br/>выход"]
```

### 2. Поток данных при выполнении команд

```mermaid
sequenceDiagram
    actor П as Пользователь
    participant CR as ConsoleReader
    participant App
    participant CV as ConsoleView
    participant US as UserService
    participant LS as LinkService
    participant BS as BrowserService
    participant LR as LinkRepository
    participant UR as UserRepository
    participant DB as JSON-файлы (db/)

    loop Цикл меню
        П->>CR: ввод команды (0–7, help)
        CR->>App: readMenuChoice()
        alt 1 — создать ссылку
            App->>US: getCurrentUser() или createUser()
            US->>UR: save(User)
            UR->>DB: db/users.json
            App->>CR: readUrl(), readMaxClicks()
            CR-->>App: url, maxClicks
            App->>LS: create(CreateLink)
            LS->>LS: HashGenerator.generate(5)
            LS->>LR: findByHash(hash) — проверка уникальности
            LS->>LR: save(Link)
            LR->>DB: db/links.json
            App->>LS: edit(EditLink, now + TTL_MS)
            LS->>LR: save(Link)
            App->>CV: printShortLinkCreated(link, ttl)
        else 2 — открыть ссылку
            App->>CR: readHash()
            App->>LS: goTo(hash)
            LS->>LR: findByHash(hash)
            alt ссылка активна
                LS->>BS: open(url)
                LS->>LR: save(incrementClicks())
                LR->>DB: db/links.json
            else истекла / лимит исчерпан
                LS-->>App: RuntimeException
                App->>CV: printNotification(ошибка)
            end
        else 3 — мои ссылки
            App->>LR: findByUserId(userId)
            App->>LR: delete(истёкшие ссылки)
            App->>CV: printLinks(активные)
        else 4 — редактировать
            App->>LR: findByUserId(userId)
            App->>LS: edit(EditLink)
        else 5 — удалить
            App->>LR: findByUserId(userId)
            App->>LR: delete(link)
        else 6 — вход / 7 — выход
            App->>US: login(UUID) / logout()
            US->>UR: findUserById(UUID)
        else 0 — выход
            App->>App: expiredLinksCleaner.stop()
        end
    end
```

### 3. Связи моделей

```mermaid
classDiagram
    class User {
        +UUID id
        +create() User
    }
    class Link {
        +UUID id
        +UUID userId
        +String url
        +String hash
        +int maxClicks
        +int clickCount
        +long createdAt
        +long expiresAt
        +create(userId, url, hash, maxClicks) Link
        +incrementClicks() Link
        +isExhausted() boolean
        +isExpired() boolean
    }

    User "1" --> "0..*" Link : id = Link.userId
```

## Структура пакетов проекта
```
└── mephi
    ├── cli
    │   ├── App.java
    │   ├── ConsoleReader.java
    │   └── ConsoleView.java
    ├── core
    │   └── FileRepository.java
    ├── dto
    │   ├── CreateLink.java
    │   └── EditLink.java
    ├── Main.java
    ├── model
    │   ├── Link.java
    │   └── User.java
    ├── repository
    │   ├── LinkRepository.java
    │   └── UserRepository.java
    └── service
        ├── BrowserService.java
        ├── ConfigService.java
        ├── ExpiredLinksCleaner.java
        ├── HashGenerator.java
        ├── LinkService.java
        └── UserService.java
```
## Сборка и запуск проекта

```bash
cp example.env .env
```
Сборка проекта:
```bash
mvn clean package
```
Запуск:
```bash
mvn exec:java -Dexec.mainClass="mephi.Main"
```

Сборка и запуск через podman (docker):

```bash
podman build -t link-short .  
```

```bash
podman run -it --rm \
  -v $(pwd)/data:/data:Z \
  -v $(pwd)/.env:/data/.env:ro,Z \
  link-short:latest
```

## Справка по командам
```
1 - Сократить ссылку
2 - Перейти по короткой ссылке
3 - Мои ссылки
4 - Изменить лимит переходов
5 - Удалить ссылку
6 - Войти по UUID
7 - Выйти из аккаунта
0 - Выход
help - вывод спправки
```

## Технологический стек
* Язык: Java 17+
* Google Gson (работа с файловой БД в формате JSON)
* Lombok (инструмент кодогенерации на этапе компиляции)
* Сборка проекта: Maven
* Визуализация архитектуры: Mermaid.js (интегрировано в GitHub Markdown)
