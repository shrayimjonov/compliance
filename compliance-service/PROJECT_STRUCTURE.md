# Davrbank Project Structure

```
uz.davrbank.<projectName>/
├── Application.java
├── config/
│   ├── DatabaseConfig.java
│   └── WebConfig.java
├── controller/
│   ├── UserController.java
│   └── ProductController.java
├── service/
│   ├── impl/
│   │   ├── UserServiceImpl.java
│   │   └── ProductServiceImpl.java
│   ├── UserService.java
│   └── ProductService.java
├── repository/
│   ├── UserRepository.java
│   └── ProductRepository.java
├── domain/
│   ├── User.java
│   └── Product.java
├── dto/
│   ├── UserDTO.java
│   ├── CreateUserRequest.java
│   └── ProductDTO.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ValidationException.java
├── mapper/
│   ├── UserMapper.java
│   └── ProductMapper.java
└── util/
    ├── DateUtil.java
    └── ValidationUtil.java
```