
CREATE TABLE weather.station (
    station_id VARCHAR(40) NOT NULL PRIMARY KEY
  , station_name VARCHAR(120)
  , county_id VARCHAR(120)
  , lon DOUBLE PRECISION NOT NULL
  , lat DOUBLE PRECISION NOT NULL
  , elev NUMERIC
  , timezone VARCHAR(50)
  , active BOOLEAN NOT NULL DEFAULT false
  , last_updated TIMESTAMP WITHOUT TIME ZONE NOT NULL 
    DEFAULT (NOW() at time zone 'utc')
) ;

