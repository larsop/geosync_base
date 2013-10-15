 -- create ar5 user that ownes all ar5 data, tables ... 
CREATE ROLE ar5 LOGIN
  NOSUPERUSER NOCREATEROLE;

  -- give the user ar5 right to update spatial_ref_sys and update geometry_columns (This is needed to create tables with geometry collums)
GRANT ALL ON spatial_ref_sys, geometry_columns TO GROUP ar5;

-- create schema for ar5 data, tables, .... 
CREATE SCHEMA ar5
  AUTHORIZATION ar5;
  
  -- grant usage on cheam to 
GRANT USAGE ON SCHEMA ar5 TO subscriber_update_role;



