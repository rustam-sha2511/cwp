--DROP TABLE cw_cases IF EXISTS;

CREATE TABLE cw_cases (
  cw_cases_id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY,
  cw_id BIGINT,
  start_date VARCHAR(50),
  description VARCHAR(200),
  status VARCHAR(50),
  cw_assigned_name VARCHAR(50)
);