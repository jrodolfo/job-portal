show databases;
use jobportal;
show tables;

desc applications;
desc jobs;
desc users;

select * from jobs;
select * from users;
select * from applications;

-- DANGER ZONE :^)
-- SET FOREIGN_KEY_CHECKS = 0;
-- truncate table applications;
-- truncate table jobs;
-- truncate table users;
