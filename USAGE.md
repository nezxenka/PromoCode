# Руководство по использованию PromoCode

## Команды

### Административные команды

Перезагрузить конфигурацию плагина:
```
/promocode reload
```

Получить информацию о промокоде:
```
/promocode info <код>
```

Просмотреть статистику активаций:
```
/promocode stats
```

Справка по командам:
```
/promocode help
```

### Активация промокодов

Игроки активируют промокоды просто вводя их как команду:
```
/freecase
/dimasik
/zako
```

## Настройка промокодов

### Структура промокода в config.yml

```yaml
promocodes:
  код_промокода:
    player_uses: 1              # Сколько раз может использовать один игрок (-1 = бесконечно)
    global_uses: -1             # Общий лимит активаций (-1 = бесконечно)
    group: название_группы      # Группа промокода (игрок может активировать только 1 из группы)
    needLink: false             # Требуется ли привязка аккаунта через LinkAlert
    messages:                   # Сообщения при активации
      - "Строка 1"
      - "Строка 2"
    commands:                   # Команды, выполняемые при активации
      - "команда1 %player%"
      - "команда2 %player%"
```

### Пример промокода

```yaml
promocodes:
  welcome2026:
    player_uses: 1
    global_uses: 1000
    group: welcome
    needLink: false
    messages:
      - ""
      - " &6Добро пожаловать!"
      - ""
      - " &7Вы получили приветственный бонус"
      - ""
    commands:
      - "eco give %player% 10000 -s"
      - "essentials:exp give %player% 500"
```

## Группы промокодов

Группы позволяют ограничить активацию нескольких промокодов одним игроком.

Например, если у вас есть промокоды `dimasik` и `zako` в группе `public`,
игрок сможет активировать только один из них.

```yaml
promocodes:
  dimasik:
    group: public
    # ...
  
  zako:
    group: public
    # ...
```

## Интеграция с LinkAlert

Если у промокода установлено `needLink: true`, игрок должен привязать
свой аккаунт через плагин LinkAlert перед активацией.

```yaml
promocodes:
  premium_code:
    needLink: true
    # ...
```

## Плейсхолдеры

В командах и сообщениях доступны плейсхолдеры:

- `%player%` - имя игрока
- `%code%` - код промокода

## База данных

Плагин использует MySQL для хранения информации об активациях.

### Таблицы

**promo_activations** - активации игроков
- player_name - имя игрока
- ip_address - IP адрес
- promo_code - код промокода
- promo_group - группа промокода
- activation_count - количество активаций
- first_activation - дата первой активации
- last_activation - дата последней активации

**promo_global_activations** - глобальная статистика
- promo_code - код промокода
- activation_count - общее количество активаций
- last_activation - дата последней активации

## Настройка database.yml

```yaml
type: MYSQL

mysql:
  host: localhost
  port: 3306
  database: promocode
  user: root
  password: "пароль"

hikari:
  maximum-pool-size: 10
  minimum-idle: 2
  max-lifetime: 1800000
  connection-timeout: 5000
  idle-timeout: 600000
  keepalive-time: 300000
```

## Настройка messages.yml

Все сообщения плагина настраиваются в `messages.yml`:

```yaml
general:
  prefix: "&6[PromoCode]"
  no-permission: "&cУ вас нет прав!"

activation:
  success: "&aПромокод активирован!"
  already-activated: "&cВы уже использовали этот промокод!"
```

## Права доступа

```yaml
promocode.admin - Доступ ко всем командам (по умолчанию: op)
```

## Советы

1. Используйте группы для взаимоисключающих промокодов
2. Устанавливайте `global_uses` для ограничения общего количества активаций
3. Используйте `needLink: true` для важных промокодов
4. Регулярно проверяйте статистику через `/promocode stats`
5. Все сообщения можно настроить в `messages.yml`
6. Промокоды регистронезависимы (FREECASE = freecase)
