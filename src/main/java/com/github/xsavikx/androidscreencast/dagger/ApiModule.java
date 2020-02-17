package com.github.xsavikx.androidscreencast.dagger;

import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.NullOutputReceiver;
import com.github.xsavikx.androidscreencast.api.AndroidDevice;
import com.github.xsavikx.androidscreencast.api.AndroidDeviceImpl;
import com.github.xsavikx.androidscreencast.api.command.executor.CommandExecutor;
import com.github.xsavikx.androidscreencast.api.command.executor.ShellCommandExecutor;
import com.github.xsavikx.androidscreencast.api.command.factory.AdbInputCommandFactory;
import com.github.xsavikx.androidscreencast.api.command.factory.InputCommandFactory;
import com.github.xsavikx.androidscreencast.api.injector.MultiLineReceiverPrinter;
import com.github.xsavikx.androidscreencast.configuration.ApplicationConfiguration;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

import static com.github.xsavikx.androidscreencast.configuration.ApplicationConfigurationProperty.*;
import static com.github.xsavikx.androidscreencast.configuration.ApplicationConfigurationPropertyKeys.*;

@Module
public class ApiModule {
    @Singleton
    @Named(ADB_COMMAND_TIMEOUT_KEY)
    @Provides
    public static long adbCommandTimeout(ApplicationConfiguration applicationConfiguration) {
        return Long.valueOf(applicationConfiguration.getProperty(ADB_COMMAND_TIMEOUT));
    }

    @Singleton
    @Named(ADB_DEVICE_TIMEOUT_KEY)
    @Provides
    public static long adbDeviceTimeout(ApplicationConfiguration applicationConfiguration) {
        return Long.valueOf(applicationConfiguration.getProperty(ADB_DEVICE_TIMEOUT));
    }

    @Singleton
    @Named(ADB_PATH_KEY)
    @Provides
    public static String adbPath(ApplicationConfiguration applicationConfiguration) {
        return applicationConfiguration.getProperty(ADB_PATH);
    }

    @Singleton
    @Provides
    public static CommandExecutor commandExecutor(ShellCommandExecutor shellCommandExecutor) {
        return shellCommandExecutor;
    }

    @Singleton
    @Provides
    public static AndroidDevice androidDevice(AndroidDeviceImpl androidDevice) {
        return androidDevice;
    }

    @Singleton
    @Provides
    public static InputCommandFactory inputCommandFactory(AdbInputCommandFactory adbInputCommandFactory) {
        return adbInputCommandFactory;
    }

    @Singleton
    @Named(APP_DEBUG_ENABLED_KEY)
    @Provides
    public static boolean isDebugEnabled(ApplicationConfiguration applicationConfiguration) {
        return Boolean.valueOf(applicationConfiguration.getProperty(APP_DEBUG_ENABLED));
    }

    @Singleton
    @Provides
    public static IShellOutputReceiver iShellOutputReceiver(@Named(APP_DEBUG_ENABLED_KEY) boolean isDebugEnabled, MultiLineReceiverPrinter multiLineReceiverPrinter) {
        if (isDebugEnabled) {
            return multiLineReceiverPrinter;
        }
        return NullOutputReceiver.getReceiver();
    }
}
