# Athena / SpringBoot Sample Application



# Initialize Athena

## S3 Artifacts

make bucket ```world-cities-athena```

create folder ```input```

create folder ```queries```

## Create DB

```create database worldcitiesdb```

## Insert Data

```CREATE EXTERNAL TABLE IF NOT EXISTS `worldcitiesdb`.`worldcities` (
`city` string,
`city_ascii` string,
`lat` double,
`lng` double,
`country` string,
`iso2` string,
`iso3` string,
`admin_name` string,
`capital` string,
`population` int,
`id` int
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'
WITH SERDEPROPERTIES (
'serialization.format' = ',',
'field.delim' = ','
)
LOCATION 's3://world-cities-athena/input/'
TBLPROPERTIES ('skip.header.line.count'='1')```

## Test Query

```select city, population from worldcities where iso2='US' order by population desc limit 10;```