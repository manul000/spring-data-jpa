# Task Manager — Spring Data JPA

Установите JDK 22 и настройте `JAVA_HOME` и `Path`. Откройте терминал в папке проекта, где находится `pom.xml`. Первый запуск Maven Wrapper требует интернета.

Сборка:

```text
.\mvnw.cmd package
```

Запуск:

```text
java -jar target/15-spring-data-jpa-1.0-SNAPSHOT.jar
```

API: http://localhost:8080/tasks. Остановка — `Ctrl+C`. Запускайте работы по одной: обе используют порт 8080. Настройки уже заполнены; передавать переменные окружения не требуется.

Проверка в Postman:

1. Запустите приложение. При каждом запуске хранилище пустое.
2. Импортируйте `TaskManager.postman_collection.json` через `Import`.
3. Запустите всю коллекцию по порядку через `Run collection`. `baseUrl` уже задан: `http://localhost:8080`.
4. Перед повторным полным прогоном перезапустите приложение, чтобы вернуть пустое хранилище.

Консоль базы: http://localhost:8080/h2-console. Драйвер: `org.h2.Driver`, JDBC URL: `jdbc:h2:mem:taskdb`, пользователь: `sa`, пароль оставьте пустым.

Для защиты выполняйте запросы коллекции по одному: после пяти POST покажите пять строк в H2-консоли, затем проверьте изменение строки после PUT и её удаление после DELETE. SQL-запросы выводятся в терминал приложения.

Проверка таблицы:

```sql
SELECT ID, TITLE, PRIORITY, STATUS, CREATED_AT FROM TASKS ORDER BY ID;
```

Список возвращается постранично: `http://localhost:8080/tasks?page=0&size=3&sort=id`. Поиск: `http://localhost:8080/tasks/search?keyword=купить`. Задачи за сегодня: `http://localhost:8080/tasks/today`.

База H2 находится в памяти. После остановки приложения данные исчезают — это предусмотрено настройками `create-drop` из ТЗ.
