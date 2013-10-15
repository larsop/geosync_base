--used by class no.skogoglandskap.model.ar5.Ar5ArealRessursMvEntity
-- DROP TABLE ar5.ar5_geosync_flate;
 
CREATE TABLE ar5.ar5_geosync_flate(
id serial PRIMARY KEY not null,
objtype VARCHAR(40),
artype int4 CONSTRAINT artype_between_0_100 CHECK (artype > 0 and artype < 100),
arskogbon int4 CONSTRAINT arskogbon_between_0_100 CHECK (arskogbon > 0 and arskogbon < 100),
artreslag int4 CONSTRAINT artreslag_between_0_100 CHECK (artreslag > 0 and artreslag < 100),
argrunnf int4 CONSTRAINT argrunnf_between_0_100 CHECK (argrunnf > 0 and argrunnf < 100),
areal NUMERIC(18,5),
maalemetode int4,
noyaktighet NUMERIC(5),
synbarhet int4,
verifiseringsdato DATE,
datafangstdato DATE,
kartid VARCHAR(13),
kjoringsident DATE,
arkartstd VARCHAR(8),
opphav VARCHAR(50),
kartblad VARCHAR(13),
  geo geometry(POLYGON,4258)
);

-- ade_ar5 user to own table ar5_flate
ALTER TABLE ar5.ar5_geosync_flate OWNER TO ar5;

-- Add more indexes
CREATE INDEX geoidx_ar5_ar5_geosync_flate ON ar5.ar5_geosync_flate USING GIST (geo); 

-- give subriber role update
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE  ON TABLE
ar5.ar5_geosync_flate TO subscriber_update_role;

GRANT UPDATE ON TABLE
ar5.ar5_geosync_flate_id_seq TO subscriber_update_role;




