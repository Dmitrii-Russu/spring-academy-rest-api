# Message Management Service

## Description
This project is a RESTful service for managing text messages using Spring Boot. It provides CRUD operations (create, read, update, delete) for messages and implements authentication and authorization with Spring Security.

Users can only work with their own messages, and access to different operations is managed through roles and permissions.

## Features
### CRUD Operations for Message Management:
- **GET** - retrieve a message by ID.
- **POST** - create a new message.
- **PUT** - update an existing message.
- **DELETE** - delete a message.

### Authentication and Authorization
- Uses **Spring Security** for authentication and authorization.
- Access control: users can only work with their own messages.

### Pagination and Sorting
- Sorting and pagination of message lists.

## Technologies
- **Spring Boot** - the main framework for development.
- **Spring Data JPA** - for working with the database.
- **Spring Security** - for implementing authentication and authorization.
- **H2** - an in-memory database for testing.

## Testing
### Unit Tests:
- Testing serialization/deserialization of **Message** objects using **Jackson**.
- Testing basic CRUD operations with **Mockito**.

### Integration Tests:
- Verifying interactions with the real database.
- Testing all core message operations.
- Security, authentication, and authorization tests via controllers.

### Tools:
- **@SpringBootTest**, **Mockito**, **WebTestClient**, **@Transactional**.

## Running the Project

1. **Clone the repository:**
    ```bash
    git clone <repository_url>
    ```

2. **Build and run:**
    ```bash
    mvn spring-boot:run
    ```

## License
This project is licensed under the **MIT** License.
