# Store Application
The Store application keeps track of customers and orders in a database.

# Assumptions
This README assumes you're using a posix environment. It's possible to run this on Windows as well:
* Instead of `./gradlew` use `gradlew.bat`
* The syntax for creating the Docker container is different. You could also install PostgreSQL on bare metal if you prefer


# Prerequisites
This service assumes the presence of a postgresql 16.2 database server running on localhost:5433 (note the non-standard port)
It assumes a username and password `admin:admin` can be used.
It assumes there's already a database called `store`

You can start the PostgreSQL instance like this:
```shell
docker run -d \
  --name postgres \
  --restart always \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=store \
  -v postgres:/var/lib/postgresql/data \
  -p 5433:5432 \
  postgres:16.2 \
  postgres -c wal_level=logical
```

# Running the application
You should be able to run the service using
```shell
./gradlew bootRun
```

The application uses Liquibase to migrate the schema. Some sample data is provided. You can create more data by reading the documentation in utils/README.md

# Data model
An order has an ID, a description, and is associated with the customer which made the order.
A customer has an ID, a name, and 0 or more orders.

# API
Two endpoints are provided:
   * /order
   * /customer

Each of them supports a POST and a GET. The data model is circular - a customer owns a number of orders, and that order necessarily refers back to the customer which owns it.
To avoid loops in the serializer, when writing out a Customer or an Order, they're mapped to CustomerDTO and OrderDTO which contain truncated versions of the dependent object - CustomerOrderDTO and OrderCustomerDTO respectively.

The API is documented in the OpenAPI file OpenAPI.yaml. Note that this spec includes part of one of the tasks below (the new /products endpoint)

# Tasks

1. Extend the order endpoint to find a specific order, by ID
2. Extend the customer endpoint to find customers based on a query string to match a substring of one of the words in their name
3. Users have complained that in production the GET endpoints can get very slow. The database is unfortunately not co-located with the application server, and there's high latency between the two. Identify if there are any optimisations that can improve performance
4. Add a new endpoint /products to model products which appear in an order:
      * A single order contains 1 or more products. 
      * A product has an ID and a description. 
      * Add a POST endpoint to create a product
      * Add a GET endpoint to return all products, and a specific product by ID
        * In both cases, also return a list of the order IDs which contain those products
      * Change the orders endpoint to return a list of products contained in the order

# Bonus points
1. Implement a CI pipeline on the platform of your choice to build the project and deliver it as a Dockerized image

# Notes on the tasks
Assume that the project represents a production application.
Think carefully about the impact on performance when implementing your changes
The specifications of the tasks have been left deliberately vague. You will be required to exercise judgement about what to deliver - in a real world environment, you would clarify these points in refinement, but since this is a project to be completed without interaction, feel free to make assumptions - but be prepared to defend them when asked.
There's no CI pipeline associated with this project, but in reality there would be. Consider the things that you would expect that pipeline to verify before allowing your code to be promoted
Feel free to refactor the codebase if necessary. Bad choices were deliberately made when creating this project.

## CI/CD and Docker

### Overview

A CI pipeline has been implemented using GitHub Actions to build and test the application and publish a Docker image to GitHub Container Registry (GHCR).

The pipeline:
- Runs the Gradle test suite on pushes and pull requests.
- Uses PostgreSQL as a service for database-dependent tests.
- Builds and publishes a Docker image to GHCR after successful tests on branch pushes and version tags.
- Publishes multi-platform images for `linux/amd64` and `linux/arm64`.

### Published Docker image

The application image is available from GHCR:

```text
ghcr.io/craig-fox/store-main:main
```

Pull the latest image from the `main` tag:

```bash
docker pull ghcr.io/craig-fox/store-main:main
```

The package should be publicly accessible so that reviewers can pull the image without authenticating to GHCR.

### Prerequisites

- Docker installed and running.
- PostgreSQL 16 or a compatible PostgreSQL version.
- A PostgreSQL database named `store`.
- A database user with permission to create and modify the application schema.

The application uses Liquibase to apply database schema migrations and seed the initial product data during startup.

### Environment variables

The application supports the following environment variables:

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5433/store` | JDBC URL for PostgreSQL |
| `DB_USERNAME` | `admin` | Database username |
| `DB_PASSWORD` | `admin` | Database password |

When running the application in Docker, override these defaults to point to the PostgreSQL instance accessible from the container.

For local development, the default values can be used when PostgreSQL is available on port `5433` with the expected credentials and database.

### Running the application

The following example assumes PostgreSQL is already running on the host at port `5433`, with a database named `store` and the credentials shown above.

Pull the image:

```bash
docker pull ghcr.io/craig-fox/store-main:main
```

Start the application container:

```bash
docker run --rm \
  --name store-main \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5433/store \
  -e DB_USERNAME=admin \
  -e DB_PASSWORD=admin \
  ghcr.io/craig-fox/store-main:main
```

The application starts on port `8080`. On startup, Liquibase applies any pending database changesets.

Keep the terminal open to view application logs. Press `Ctrl+C` to stop the container.

**Note:** `host.docker.internal` is suitable for connecting from Docker Desktop on macOS and Windows to a service running on the host. Linux users may need to configure host access differently.

### Verifying the application

Check that the orders endpoint responds:

```bash
curl -i http://localhost:8080/order
```

Retrieve a specific order:

```bash
curl -i http://localhost:8080/order/1
```

Create an order using an existing customer ID and product IDs:

```bash
curl -i -X POST http://localhost:8080/order \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productIds": [1, 2]
  }'
```

A successful order creation returns `201 Created`.

The supplied customer and product IDs must exist in the database.

### CI pipeline

The GitHub Actions workflow is defined in `.github/workflows/ci.yml`. Workflow runs can be inspected in the repository's **Actions** tab.

The Docker image is built and published only after the test job succeeds, helping ensure that the published image corresponds to a successfully tested revision.
