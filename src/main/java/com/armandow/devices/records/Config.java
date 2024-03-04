package com.armandow.devices.records;

public record Config(SentryCnf sentry, Bot bot, Router router, DataSource dataSource, Scheduler scheduler) { }
