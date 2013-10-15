
-- give all users select to spatial info 
GRANT SELECT ON spatial_ref_sys, geometry_columns TO GROUP public;

-- posgres 2.0

GRANT SELECT ON  geography_columns TO GROUP public;

GRANT SELECT ON  geometry_columns  TO GROUP public;


-- create subscriber_update role tables that should be updated by 
CREATE ROLE subscriber_update_role;

-- create ok_grl user that ownes all ok_grl data, tables ... 
CREATE ROLE subscriber_user LOGIN
  PASSWORD 'subscriber_password'
  NOSUPERUSER NOCREATEROLE;

-- give user update rights  
GRANT subscriber_update_role TO subscriber_user;

  
  



