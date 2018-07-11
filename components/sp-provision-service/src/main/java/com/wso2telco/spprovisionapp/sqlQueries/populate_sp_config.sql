delimiter //

drop procedure if exists populate_sp_config_procedure;
create procedure populate_sp_config_procedure (IN consumer_key varchar(255), IN scope varchar(255), IN operator_name varchar(255))
    BEGIN
        insert into sp_configuration values(consumer_key, 'scope' , scope, operator_name);
    END //

delimiter ;