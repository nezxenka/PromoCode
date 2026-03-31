# Архитектура PromoCode

## Обзор

PromoCode построен по принципам Clean Architecture с четким разделением ответственности.

## Модули

### Command Layer
Обработка пользовательских команд:
- **PromoCodeCommand** - точка входа для команд
- **CommandExecutor** - выполнение команд
- **TabCompleter** - автодополнение аргументов

### Configuration Layer
Управление конфигурацией:
- **ConfigManager** - управление config.yml
- **DatabaseConfig** - управление database.yml
- **MessageConfig** - управление messages.yml
- **Data models** - типобезопасные модели (PromoCodeData, PromoActivation)

### Database Layer
Работа с базой данных:
- **DatabaseManager** - менеджер базы данных
- **HikariCP** - пул соединений
- **SQL queries** - оптимизированные запросы

### Service Layer
Бизнес-логика:
- **PromoCodeService** - сервис активации промокодов
- **ActivationResult** - результаты активации
- **Async processing** - асинхронная обработка

### Listener Layer
Обработка событий:
- **PromoCodeListener** - обработка команд игроков
- **Event handling** - обработка событий Bukkit

### Utility Layer
Вспомогательные классы:
- **ValidationUtil** - валидация данных
- **ColorUtil** - обработка цветов

### Model Layer
Модели данных:
- **PromoCodeData** - данные промокода
- **PromoActivation** - данные активации

## Паттерны проектирования

### Builder Pattern
Создание сложных объектов конфигурации.

### Service Pattern
Изоляция бизнес-логики в сервисах.

### Repository Pattern
Абстракция работы с базой данных.

### Observer Pattern
Система событий Bukkit.

### Singleton Pattern
ConfigManager, MessageConfig, DatabaseManager.

## Принципы SOLID

- **Single Responsibility** - один класс = одна задача
- **Open/Closed** - открыт для расширения
- **Liskov Substitution** - подтипы взаимозаменяемы
- **Interface Segregation** - минимальные интерфейсы
- **Dependency Inversion** - зависимость от абстракций

## Особенности

- Immutable объекты через @Value и @Builder
- Асинхронная обработка через CompletableFuture
- HikariCP для эффективного пула соединений
- Валидация на входе
- Fail-fast подход
- Логирование через Logger
- Полная локализация
- Типобезопасность
- Интеграция с LinkAlert через рефлексию
