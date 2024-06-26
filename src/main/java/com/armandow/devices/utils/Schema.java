package com.armandow.devices.utils;

public class Schema {
    private Schema() {
        // Schema
    }

    public static void init() {
        try {
            DevicesUtils.getDbConnection().executeUpdate("""
                    CREATE TABLE `device`
                    (
                        `i_id`            INTEGER PRIMARY KEY,
                        `v_name`          TEXT     DEFAULT NULL,
                        `v_nickname`      TEXT     DEFAULT NULL,
                        `v_mac_addr`      TEXT NOT NULL UNIQUE,
                        `v_vendor`        TEXT     DEFAULT NULL,
                        `v_ip`            TEXT     DEFAULT NULL,
                        `v_interface`     TEXT     DEFAULT NULL,
                        `v_group`         TEXT     DEFAULT NULL,
                        `c_is_online`     TEXT     DEFAULT 'N',
                        `d_register_date` DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'localtime')),
                        `d_update_date`   DATETIME DEFAULT NULL
                    )""");

            DevicesUtils.getDbConnection().executeUpdate("""
                    CREATE TABLE `connection_logs`
                    (
                        "i_id"           INTEGER PRIMARY KEY,
                        "i_device_id_fk" INTEGER NOT NULL,
                        "v_type"         TEXT     DEFAULT NULL,
                        "d_date"         DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'localtime')),
                        CONSTRAINT "logs_device_id_fk" FOREIGN KEY ("i_device_id_fk") REFERENCES "device" ("i_id") ON DELETE NO ACTION ON UPDATE NO ACTION
                    )""");

            DevicesUtils.getDbConnection().executeUpdate("""
                    CREATE TRIGGER `device_ai`
                        AFTER INSERT
                        ON `device`
                        WHEN NEW.c_is_online = 'S'
                    BEGIN
                        INSERT INTO connection_logs(i_id, i_device_id_fk, v_type, d_date)
                        VALUES ((SELECT COUNT(1) FROM connection_logs), NEW.i_id, 'CHECKIN', DATETIME(CURRENT_TIMESTAMP, 'localtime'));
                    END""");

            DevicesUtils.getDbConnection().executeUpdate("""
                    CREATE TRIGGER `device_au`
                        AFTER UPDATE
                        ON `device`
                    BEGIN
                        INSERT INTO connection_logs(i_id, i_device_id_fk, v_type, d_date)
                        VALUES ((SELECT COUNT(1) FROM connection_logs), NEW.i_id, IIF(NEW.c_is_online = 'S', 'CHECKIN', 'CHECKOUT'), DATETIME(CURRENT_TIMESTAMP, 'localtime'));

                        UPDATE device SET d_update_date = DATETIME(CURRENT_TIMESTAMP, 'localtime') WHERE i_id = NEW.i_id;
                    END""");
        } catch (Exception ex) {
            System.exit(-1);
        }
    }
}
