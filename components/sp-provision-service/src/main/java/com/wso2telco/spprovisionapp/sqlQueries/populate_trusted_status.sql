delimiter //


drop procedure if exists populate_trusted_status_procedure;
create procedure populate_trusted_status_procedure (IN consumer_key varchar(255), IN operator_name varchar(255))
    BEGIN
        insert into sp_configuration (client_id, config_key, config_value, operator) values (consumer_key,'trusted_sp','true', operator_name);
    END //

delimiter ;