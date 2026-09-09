# Spring Boot and REST API 

## Предметна область проєкту

PurchaseRecord (запис про закупівлю) - основна сутність проєкту, яка представляє окрему позицію (рядок) виробничого замовлення на закупівлю матеріалів.
Кожен запис PurchaseRecord відповідає одному конкретному матеріалу та містить його необхідну кількість. Кожна позиція також має ідентифікатор виробничого замовлення orderId, до якого вона належить.
У межах цього проєкту PurchaseRecord розглядається як самостійна сутність із власним ідентифікатором, що дозволяє однозначно ідентифікувати та керувати кожною позицією через REST API.

Атрибути сутності:
- id (Long) - унікальний ідентифікатор позиції закупівлі, первинний ключ.
- orderId (Long) - ідентифікатор виробничого замовлення PurchaseOrder, до якого належить позиція. У межах цього проєкту PurchaseOrder не є окремою сутністю REST API, тому orderId зберігається як звичайний атрибут.
- material (Material) - матеріал, який закуповується;
- quantity (BigDecimal) - кількість матеріалу;

Material (матеріал) - другорядна сутність, яка містить інформацію про матеріали, що використовуються у виробничому процесі та можуть бути включені до позицій закупівлі.
Кожен матеріал має унікальну назву та визначену одиницю вимірювання.

Атрибути сутності:
- id (Long) - унікальний ідентифікатор матеріалу, первинний ключ;
- name (String) - назву матеріалу;
- unit (Unit) - одиницю вимірювання. Можливі значення: KG, LITERS, METERS, PCS.
- description (String) - опис матеріала.

Зв'язок між сутностями
Основним зв'язком у проєкті є зв'язок Many-to-One між PurchaseRecord та Material.
Кожен PurchaseRecord посилається рівно на один Material, а один Material може використовуватися у багатьох PurchaseRecord;
Зв'язок реалізується через зовнішній ключ material_id у таблиці purchase_record.

Додатково для PurchaseRecord встановлено обмеження унікальності комбінації:
order_id + material_id

Тобто один і той самий матеріал не може бути доданий до одного виробничого замовлення більше одного разу.

Сутність PurchaseOrder використовується лише для збереження ідентифікатора виробничого замовлення orderId у PurchaseRecord та не входить до переліку сутностей, які необхідно реалізувати в межах цього завдання.

## Database Schema

Database schema створюється та контролюється за допомогою Liquibase.
Детальна структура таблиць, зв'язків, constraints та indexes описана у:
[docs/database-schema.md](https://github.com/KaterynaUmanska/Block2_Kateryna_Umanska/blob/master/docs/database-schema.md)

## REST API
### PurchaseRecord
* POST /api/purchase-records - Створити запис про покупку
* GET /api/purchase-records/{id} - Отримати запис про покупку за ID
* PUT /api/purchase-records/{id} - Оновити запис про покупку
* DELETE /api/purchase-records/{id} - Видалити запис про покупку
* POST /api/purchase-records/_list - Отримати відфільтрований список із пагінацією
* POST /api/purchase-records/_report - Згенерувати звіт у форматі CSV
* POST /api/purchase-records/upload - Імпортувати записи про покупки з JSON

### Material
* GET /api/materials - Отримати всі матеріали
* GET /api/materials/{id} - Отримати матеріал за ID
* POST /api/materials - Створити новий матеріал
* PUT /api/materials/{id} - Оновити матеріал
* DELETE /api/materials/{id} - Видалити матеріал

## Фільтрація та пагінація
Кінцева точка (ендпоінт) списку записів про покупки підтримує необов'язкові фільтри:
* orderId (ID замовлення)
* materialName (назва матеріалу)
* quantityFrom (кількість від)
* quantityTo (кількість до)

Пагінація керується параметрами:
* page (сторінка)
* size (розмір)

Значення за замовчуванням:
* page = 1
* size = 10

Максимальний розмір сторінки: 100
Фільтрація та пагінація виконуються на рівні запитів до бази даних за допомогою Spring Data JPA Specifications та Pageable.

API повертає скорочений набір полів для запитів списку:

JSON
`{
"list": [
{
"id": 1,
"orderId": 1001,
"materialName": "Steel Beam",
"quantity": 10.000
}
],
"totalPages": 1
}`

## Звіт
Кінцева точка звіту використовує ті самі критерії фільтрації, що й кінцева точка списку.
Звіт містить усі записи, які відповідають вибраним фільтрам, і повертається у вигляді CSV-файлу для завантаження.
Відповідь містить відповідний тип вмісту (content type) CSV та заголовок Content-Disposition.

Приклад:
`HTTP
Content-Type: text/csv
Content-Disposition: attachment; filename="purchase-records.csv"`

## Імпорт JSON
Кінцева точка: `POST /api/purchase-records/upload` приймає JSON-файл, що містить записи про покупки.

Кожен імпортований запис проходить такі етапи:
* парситься з JSON;
* валідується;
* перевіряється на наявність зв'язаного матеріалу;
* зберігається в базу даних, якщо він валідний;
* зараховується як успішний або невдалий.

Імпорт повертає статистику обробки:

JSON
`{
"successful": 8,
"failed": 2
}`

Невалідні записи не перешкоджають імпорту інших валідних записів.
Кожен запис обробляється в ізольованій транзакції, завдяки чому збій в одному записі не відкочує успішно імпортовані записи.

## База даних
Додаток використовує PostgreSQL.

Liquibase відповідає за створення схеми бази даних та початкове заповнення даних (seed data).

Hibernate налаштований лише на перевірку схеми:

`spring.jpa.hibernate.ddl-auto=validate`

Налаштування підключення до бази даних задаються у файлі application.properties.


## Налаштування

Для роботи проєкту необхідно мати:

* Java 21
* Maven
* PostgreSQL

Необхідні Java-залежності визначені у pom.xml.


### Збірка та тестування

Для запуску всіх тестів:
`./mvnw test`
або:
`mvn test`

Для повної збірки:
`./mvnw clean package`

## Запуск програми

### Створення бази даних

Необхідно створити PostgreSQL database:
`purchase_db`
Параметри підключення повинні відповідати налаштуванням у:

`src/main/resources/application.properties`

### Запуск застосунку
За допомогою Maven:
`./mvnw spring-boot:run`
або:
`mvn spring-boot:run`
Під час запуску Liquibase автоматично створює необхідну структуру бази даних та додає початкові дані.

## Основні бібліотеки та залежності

Основні залежності проєкту:
* Spring Boot - основа застосунку.
* Spring Web - REST API.
* Spring Data JPA - робота з PostgreSQL через JPA.
* Spring Validation - validation DTO.
* Liquibase - керування database schema та migrations.
* PostgreSQL Driver - підключення до PostgreSQL.
* Jackson - JSON parsing, включно з потоковим імпортом.
* Springdoc OpenAPI - Swagger/OpenAPI documentation.
* Spring AOP - monitoring аспектів.
* Lombok - зменшення boilerplate-коду.
* JUnit 5 / Spring Boot Test / MockMvc - тестування REST API.

## Структура проєкту
### Java (main)

Основна структура застосунку:
src/main/java
* controller
* data
* dict
* dto
* exception
* monitor
* repository
* service
* utils

### Java (test)

Тести розташовані у:
src/test/java
* controller
* service
* utils


## Тестування

Проєкт містить інтеграційні тести REST API.

Для тестування використовуються:
* JUnit 5;
* Spring Boot Test;
* MockMvc;
* Spring Data JPA;
* Mockito там, де необхідно тестувати окремі компоненти.

Тести перевіряють:
* створення сутностей;
* отримання сутностей;
* оновлення;
* видалення;
* validation;
* обробку 404 Not Found;
* обробку 409 Conflict;
* pagination;
* filtering;
* CSV report;
* JSON import;
* частково успішний import.

### Результати тестування 

Після запуску:
`mvn test`
усі integration tests повинні завершуватися успішно.

Для перевірки результату збірки використовується:
`mvn clean test`
Фактична кількість тестів та результат їх виконання визначаються Maven Surefire після запуску тестового набору.

## Swagger / OpenAPI

REST API документується за допомогою OpenAPI/Swagger.
Після запуску застосунку Swagger UI доступний за адресою:
http://localhost:8080/swagger-ui/index.html

OpenAPI specification:
/v3/api-docs

## Ефективність

Фільтрація purchase records виконується на рівні бази даних за допомогою Spring Data JPA Specifications.
Pagination виконується через Pageable, тому для _list не потрібно завантажувати всі записи в пам'ять.

Для часто використовуваних полів у database schema створені індекси:
* purchase_records.order_id;
* purchase_records.material_id;
* unique (order_id, material_id);
* unique materials.name.

Для quantity окремий індекс не створюється, оскільки поточний workload не вимагає його і селективність range-фільтрів залежить від фактичного розподілу даних.

JSON import використовує потоковий parsing та ізольовані транзакції для окремих записів, що дозволяє продовжувати обробку після помилки одного запису.


