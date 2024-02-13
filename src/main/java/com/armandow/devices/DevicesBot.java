package com.armandow.devices;

import com.armandow.devices.command.CurrentCommand;
import com.armandow.devices.controller.DeviceController;
import com.armandow.devices.service.ASUSService;
import com.armandow.devices.service.DeviceService;
import com.armandow.devices.task.VerifyDeviceMetadata;
import com.armandow.devices.task.VerifyDeviceStatus;
import com.armandow.devices.utils.DevicesUtils;
import com.armandow.telegrambotapi.TelegramBot;
import com.armandow.telegrambotapi.methods.SendMessage;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class DevicesBot {
    public static void main(String[] args) {
        log.info(">> Initializing devicesBot <<");

        try { // load config
            DevicesUtils.loadConfig();
            log.trace(DevicesUtils.getConfig().toString());
        } catch (Exception e) {
            log.error("load config", e);
            Sentry.captureException(e);
            System.exit(-1);
        }

        try { // Sentry config
            Sentry.init(options -> {
                options.setDsn(DevicesUtils.getConfig().sentry().dsn());
                options.setEnvironment(DevicesUtils.getConfig().sentry().environment());

                if ( DevicesUtils.getConfig().sentry().environment().equalsIgnoreCase("pi3B") ) {
                    options.setRelease(DevicesUtils.getRelease());
                }
            });
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        try { // Database
            DevicesUtils.dbConnect();
        } catch (Exception e) {
            log.error("DB Connect", e);
            Sentry.captureException(e);
            System.exit(-1);
        }

        try {
            // Register telegram commands
            var bot = new TelegramBot(DevicesUtils.getConfig().bot().token());
            bot.registerCommand(new CurrentCommand());
            bot.run();
        } catch (Exception e) {
            log.error("Bot config", e);
            Sentry.captureException(e);
        }

        try {
            // Load devices in memory
            if ( DeviceService.countDBDevices() == 0 ) {
                // No hay datos en BD
                ASUSService.refreshToken();
                DevicesUtils.setDbDevices(new HashMap<>());
                var asusMap = DeviceController.parseAsusDevices();
                ASUSService.logout();

                asusMap.forEach((k, v) -> {
                    log.trace("{} {}", k, v.toString(2));

                    try {
                        DevicesUtils.getDbDevices().put(k, DeviceService.saveAsusDevice(v));
                    } catch (Exception e) {
                        log.error("SAVE NEW", e);
                        Sentry.captureException(e);
                    }
                });
            } else {
                DevicesUtils.setDbDevices(DeviceController.loadDBDevices());
            }

            var message = new SendMessage();
            message.setChatId(DevicesUtils.getConfig().bot().userId());
            message.setText(">> End DevicesBotConfig [" + DevicesUtils.getConfig().sentry().environment() + "] <<");
            message.send();
        } catch (Exception e) {
            log.error("RUN", e);
            Sentry.captureException(e);
            System.exit(-1);
        }

        try {
            // Start tasks
            var scheduler = Executors.newSingleThreadScheduledExecutor();
            var eService = Executors.newVirtualThreadPerTaskExecutor();

            scheduler.scheduleWithFixedDelay(() -> eService.execute(new VerifyDeviceStatus()), 15, DevicesUtils.getConfig().scheduler().updateDevicesStatus(), SECONDS);
            scheduler.scheduleWithFixedDelay(() -> eService.execute(new VerifyDeviceMetadata()), 45, DevicesUtils.getConfig().scheduler().updateDevicesMetadata(), SECONDS);
        } catch (Exception e) {
            log.error("Schedulers", e);
            Sentry.captureException(e);
        }
    }
}
