# Core


Manages users, their profiles, faces, and authentication.
Provides a single source of user data for the entire system

## Scripts

|          |                            |                                              |     |
| -------- | -------------------------- | -------------------------------------------- | --- |
| Role     | Script                     | Result                                       |     |
| User     | Registration/Authorization | A profile is created and issued to JWT token |     |
| Gateway  | Cheack a token             | Retrieves user data                          |     |
| Services | Requesting profile/goals   | User parameters and settings are returned    |     |

## Integrations

  
- Works directly with `gateway`
- Provides REST APIs for `meal` and `workout` 
- Uses `common-lib` DTOs

## Technologes

`Spring Boot WebFlux`

`**Spring Security**`

`**JWT**`

`PostgreSQL`
