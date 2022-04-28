# catalog

A product catalog, where you can view and modify items.

![alt text](/preview/all.png?raw=true)

![alt text](/preview/item.png?raw=true)

![alt text](/preview/item2.png?raw=true)

![alt text](/preview/manage.png?raw=true)

![alt text](/preview/add.png?raw=true)

![alt text](/preview/edit.png?raw=true)



#application.yml
```
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            clientId: ***
            clientSecret: ***

  data:
    mongodb:
      uri: ***
      database: ***
```
