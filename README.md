# Laboratorio_BankX

Levantar base de datos de mongo con docker:


```
docker run -d --name mongo-bankx -p 27017:27017 mongo:6
```
Ejecutar la aplicación en el puerto 8084:
```
mvn spring-boot:run
```


Pruebas con Postman:

Crear Transacción (OK)
```
curl -X POST http://localhost:8084/api/transactions \
-H "Content-Type: application/json" \
-d '{
  "accountNumber": "001-0001",
  "type": "DEBIT",
  "amount": 100
}'
```
<img width="600" height="711" alt="image" src="https://github.com/user-attachments/assets/7f6fc378-e839-4b9a-812b-3b9fbd726f86" />


Crear Transacción (rechazo por riesgo)
```
  curl -X POST http://localhost:8084/api/transactions \
-H "Content-Type: application/json" \
-d '{
  "accountNumber": "001-0001",
  "type": "DEBIT",
  "amount": 2000
}'
```

<img width="625" height="640" alt="image" src="https://github.com/user-attachments/assets/dbc899f7-aebf-4302-b257-b05d94fa04fe" />


Crear Transacción (rechazo por fondos insuficientes)
```
curl -X POST http://localhost:8084/api/transactions \
-H "Content-Type: application/json" \
-d '{
  "accountNumber": "001-0002",
  "type": "DEBIT",
  "amount": 400
}'
```
<img width="487" height="607" alt="image" src="https://github.com/user-attachments/assets/8376fb31-88ad-4a5f-98cd-11441a7855c4" />

Listar Transacciones por Cuenta
```
curl -X GET http://localhost:8084/api/transactions?accountNumber=001-0001
```
<img width="622" height="747" alt="image" src="https://github.com/user-attachments/assets/ecdd424e-7543-40e1-8cc9-c7798ef40109" />


Stream de transacciones (SSE)
```
curl -X GET http://localhost:8084/api/stream/transactions
```
<img width="1479" height="888" alt="image" src="https://github.com/user-attachments/assets/02935e0e-44a0-4b66-813f-5ca7f3f2545c" />

