# Statuer, a monitor server

Statuer is a self hosted service to help you manage all your importants services (HTTP, TCP, databases...).

## Requirements

- A Mysql database (required)
- A Discord bot to manage the configuration (optional)
- A Web client or Phone app to manage the configuration (optional)

## How to install your statuer instance ?

### Using docker

Image of the api: `lefuturiste/statuer-api`

## Environment variables

- `MYSQL_CONNECTION_URL`, example: `jdbc:mysql://HOST:3306/statuer`
- `MYSQL_USERNAME`, example: `statuer`
- `MYSQL_PASSWORD`, example: `very_secure_pass`
- (optional) `DISCORD_BOT_TOKEN`
- (optional) `DISCORD_CLIENT_ID`
- (optional) `DISCORD_PREFIX` default to `??`

## Discord commands

prefix: ??|&&|##|mention

commands:

`??ping`

`??help`

`??about`

`??status` number of service down now

`??get <path>` show information about a namespace|project|service

`??create <path>` create a path

`??edit <path> name=<new name>` edit a path with a new name

`??delete <path>` delete a path

`??webhook <path> <channel>` set a webhook channel for a path (this will manage auto the webhooks)

`??edit <path> proprety=1 proprety=2`

- ??last :show the
- ??get 'path"

## Structure, architecture

### Database entities

The statuer data structure is divided into distinct entities:

- **Namespace**: the data structure for a organization
- **Project**: the data structure for a project inside a organization, for example a company can have multiple projects or products
- **Service**: inside a project you will have many service for example an API, a database, a user dashboard or panel, workers and more
- **Incident**: when a service fail a incident will be created this incident will be considered as 'resolved' when the service is up again

### Alerting

This piece of code will be called when there is a need of alerting.

- Webhook
- Specific webhook integration for Slack or Discord 
- Twitter bot integration
- SMS with different API
- Email

### Checker

This piece of code will check the service using a specific method.
This will return the availability (up|down) of the service along with the response time in ms.

- HTTP
- PING
- TCP
- SMTP
- MYSQL
- MongoDB
- RabbitMQ
- SSH