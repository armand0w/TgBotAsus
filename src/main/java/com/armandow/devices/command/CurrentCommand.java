package com.armandow.devices.command;

import com.armandow.devices.task.VerifyDeviceStatus;
import com.armandow.devices.utils.DevicesUtils;
import com.armandow.telegrambotapi.annotations.BotCommand;
import com.armandow.telegrambotapi.interfaces.IBotCommand;
import com.armandow.telegrambotapi.methods.SendMessage;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.Arrays;

import static com.armandow.devices.utils.Constants.C_IS_ONLINE;

@Slf4j
@BotCommand(value = "/current", description = "Get currently connected devices")
public class CurrentCommand implements IBotCommand {

    @Override
    public void execute(JSONObject user, JSONObject chat, String[] arguments) {
        if ( DevicesUtils.isAuthorized(user) ) {
            new VerifyDeviceStatus().updateStatus(false);
            var sb = new StringBuilder();

            DevicesUtils.getDbDevices().values().stream()
                    .filter(d -> d.getString(C_IS_ONLINE).equals("S"))
                    .forEach(k -> sb.append(DevicesUtils.getNotEmptyName(k)).append("\n\n"));

            var msg = new SendMessage();
            msg.setText(sb.toString());
            msg.setChatId(chat.getLong("id"));

            try {
                var resp = msg.send();
                log.debug(resp.toString(2));
            } catch (Exception e) {
                log.error("execute CurrentCommand", e);
                Sentry.captureException(e);
            }
        } else {
            Sentry.captureMessage("Usuario externo", scope -> {
                scope.setLevel(SentryLevel.INFO);
                scope.setExtra("jsonUser", user.toString(2));
                scope.setExtra("jsonChat", chat.toString(2));
                scope.setExtra("args", Arrays.toString(arguments));
            });
        }
    }
}
