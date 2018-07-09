delimiter //

drop procedure if exists populate_sp_config_procedure;
create procedure populate_sp_config_procedure (IN consumer_key varchar(255))
    BEGIN
        insert into sp_configuration values(consumer_key, 'scope' , 'openid', 'algar'),(consumer_key, 'scope' , 'openid', 'claro'),(consumer_key, 'scope' , 'openid', 'oi'),(consumer_key, 'scope' , 'openid', 'sercomtel'),(consumer_key, 'scope' , 'openid', 'spark'),(consumer_key, 'scope' , 'openid', 'tim'),(consumer_key, 'scope' , 'openid', 'vivo'),(consumer_key, 'scope' , 'phone', 'algar'),(consumer_key, 'scope' , 'phone', 'claro') ,(consumer_key, 'scope' , 'phone', 'oi') ,(consumer_key, 'scope' , 'phone', 'sercomtel'),(consumer_key, 'scope' , 'phone', 'spark'),(consumer_key, 'scope' , 'phone', 'tim'),(consumer_key, 'scope' , 'phone', 'vivo');
    END //

delimiter ;