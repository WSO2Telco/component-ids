delimiter //

drop procedure if exists populate_am_database_procedure;
create procedure populate_am_database_procedure (IN app_name varchar(255),OUT app_id int)
    BEGIN
        select am_application.APPLICATION_ID from am_application where am_application.NAME= app_name into app_id;

        update am_application set am_application.APPLICATION_STATUS='APPROVED' where am_application.APPLICATION_ID=app_id;
        update am_application set am_application.APPLICATION_TIER='Unlimited' where am_application.APPLICATION_ID=app_id;

    END //

delimiter ;