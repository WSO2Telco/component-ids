delimiter //


drop procedure if exists populate_axiata_database_procedure;
create procedure populate_axiata_database_procedure (IN app_id INT)
    BEGIN
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 1, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 2, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 3, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 4, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 5, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 6, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 7, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 8, 1);
        insert into operatorapps (applicationid, operatorid, isactive) values (app_id, 9, 1);

        insert into endpointapps(endpointid , applicationid , isactive ) values (29, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (30, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (31, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (32, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (33, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (34, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (35, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (36, app_id, 1);

        insert into endpointapps(endpointid , applicationid , isactive ) values (37, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (38, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (39, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (40, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (41, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (42, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (43, app_id, 1);

        insert into endpointapps(endpointid , applicationid , isactive ) values (47, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (49, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (50, app_id, 1);

        insert into endpointapps(endpointid , applicationid , isactive ) values (70, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (71, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (72, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (73, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (74, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (75, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (76, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (77, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (78, app_id, 1);

        insert into endpointapps(endpointid , applicationid , isactive ) values (79, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (80, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (81, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (82, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (83, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (84, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (85, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (86, app_id, 1);
        insert into endpointapps(endpointid , applicationid , isactive ) values (87, app_id, 1);

    END //

delimiter ;