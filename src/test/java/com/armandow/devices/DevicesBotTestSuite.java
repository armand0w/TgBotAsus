package com.armandow.devices;

import com.armandow.devices.command.CurrentCommandTest;
import com.armandow.devices.controller.DeviceControllerTest;
import com.armandow.devices.service.ASUSServiceTest;
import com.armandow.devices.service.DeviceServiceTest;
import com.armandow.devices.task.VerifyDeviceMetadataTest;
import com.armandow.devices.task.VerifyDeviceStatusTest;
import com.armandow.devices.utils.DevicesUtilsTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        SetUpConfigTest.class,
        DevicesUtilsTest.class,
        DeviceControllerTest.class,
        CurrentCommandTest.class,
        VerifyDeviceStatusTest.class,
        VerifyDeviceMetadataTest.class,
        DeviceServiceTest.class,
        ASUSServiceTest.class
})
public class DevicesBotTestSuite {
}
