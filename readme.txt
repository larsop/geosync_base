This project based on the spec. in http://www.geosynkronisering.no/.

It's used for distribution of data with geometries between different databses.

The exchange format used is XML which is described in product xsd.   

This geosync_base is a module that contains common code and is used as basic for other modules like
geosync_client, geosync_server and geosync_program.

Test subscriber with ar5
- add ip for subscriber_host distributor_hostin /etc/hosts
127.0.0.1 subscriber_db distributor_db
 
- create subscriber_db (src/main/sql/postgres/subscriber/create_db_subscriber.txtl)
 
 