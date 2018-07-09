delimiter //

drop procedure if exists update_subscription_procedure;
create procedure update_subscription_procedure (IN app_id INT)
    BEGIN

        UPDATE am_subscription SET am_subscription.SUB_STATUS='UNBLOCKED' WHERE am_subscription.APPLICATION_ID = app_id;
        UPDATE am_subscription SET am_subscription.TIER_ID='Unlimited' WHERE am_subscription.APPLICATION_ID = app_id;

    END //

delimiter ;



select  * from am_subscription' WHERE am_subscription.APPLICATION_ID = 283;