Microservices-UserManagement
-

A Spring Boot microservices application for user profile management. This project demonstrates a modular architecture with separate services for user, person, contact, address, authentication, and a web frontend. It features:

User signup and login
Profile creation and management
Address and contact information storage
Service discovery with Eureka
API Gateway for routing
CSV-based persistence for demo purposes
Modern web UI with Thymeleaf


Microservices Structure
- 
web-app: The frontend (Thymeleaf/HTML) that users interact with.
user-service: Handles user authentication, signup, and links users to person/contact/address.
person-service: Manages personal details (name, age, etc.).
contact-service: Manages contact info (phone, email).
address-service: Manages address info.
role-service: Manages user roles.
apiGateway: Central entry point for all API requests.
eureka: Service registry for service discovery

Request Flow Example (Signup/Login)
-
User interacts with web-app (e.g., submits signup or login form).
web-app builds a REST request (using RestTemplate) to the appropriate microservice endpoint.
apiGateway acts as a reverse proxy. All requests from web-app go through apiGateway, which forwards them to the correct microservice (e.g., /user-service/signup).
Eureka is used by all services (including apiGateway) to register themselves and discover each other. This means apiGateway doesn’t need hardcoded addresses—it asks Eureka for the current location of each service.


Eureka (Service Discovery)
-
Eureka is a registry where all microservices register themselves at startup.
When a service (like apiGateway) needs to call another service, it queries Eureka for the address.
This enables dynamic scaling, load balancing, and resilience (if a service moves or restarts, Eureka knows).


API Gateway
-

The apiGateway receives all external requests.
It routes requests to the correct microservice based on the URL path (e.g., /user-service/signup goes to user-service).
It can also handle cross-cutting concerns like authentication, logging, rate limiting, etc.


Data Flow Example (Signup)
-
User fills out the signup form in web-app.
web-app sends a POST request to /user-service/signup (via apiGateway).
apiGateway forwards the request to user-service.
user-service processes the signup, calls address-service, contact-service, and person-service to create related records.
Each service saves its data (often to CSV for demo purposes).
user-service returns a response to web-app, which displays a success message.



Login Flow
-
User submits login form in web-app.
web-app sends a GET request to /user-service/login?loginName=...&password=... (via apiGateway).
apiGateway forwards to user-service.
user-service authenticates, fetches related person/contact/address info, and returns it.
web-app displays the user’s profile.


Why Use Eureka and API Gateway?
-
Eureka: Makes your system scalable and resilient. Services can move, restart, or scale up/down, and Eureka keeps track of their locations.
API Gateway: Simplifies client interactions, centralizes routing, and enables security and monitoring in one place.

