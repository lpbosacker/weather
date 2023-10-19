
CREATE TABLE weather.observation (
    station_id VARCHAR(40) NOT NULL
  , timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL 
  , temperature NUMERIC(4,1)
  , dew_point NUMERIC(4,1)
  , wind_direction NUMERIC(3)
  , wind_speed NUMERIC(5,2)
  , wind_gust NUMERIC(5,2)
  , barometric_pressure NUMERIC(6)
  , sea_level_pressure NUMERIC(6)
  , max_temp_last_24hr NUMERIC(4,1)
  , min_temp_last_24hr NUMERIC(4,1)
  , precipitation_last_hr NUMERIC(4,2)
  , precipitation_last_3hr NUMERIC(4,2)
  , precipitation_last_6hr NUMERIC(4,2)
  , relative_humidity NUMERIC(5,2)
  , wind_chill NUMERIC(4,1)
  , heat_index NUMERIC(4,1)
  , CONSTRAINT observation_pk PRIMARY KEY (station_id, timestamp)
) ;

