package com.iboot.pf.module.aps.api;

import com.iboot.pf.module.aps.api.load.LoadApi;
import com.iboot.pf.module.aps.api.projection.ProjectionApi;
import com.iboot.pf.module.aps.api.scenario.ScenarioApi;
import com.iboot.pf.module.aps.api.shortage.ShortageApi;
import com.iboot.pf.module.aps.api.task.ScheduleTaskApi;
import com.iboot.pf.module.aps.api.version.VersionApi;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiContractTest {

    @Test
    void apiMustNotExposeInternalLayers() {
        List<Class<?>> apis = List.of(ScenarioApi.class, VersionApi.class, ScheduleTaskApi.class,
                ProjectionApi.class, ShortageApi.class, LoadApi.class);
        Set<String> forbiddenPackages = Set.of(".dal.", ".service.", ".controller.");

        for (Class<?> api : apis) {
            for (Method method : api.getMethods()) {
                assertFalse(forbiddenPackages.stream().anyMatch(method.getReturnType().getName()::contains),
                        () -> method + " 返回了内部类型");
                for (Class<?> parameterType : method.getParameterTypes()) {
                    assertFalse(forbiddenPackages.stream().anyMatch(parameterType.getName()::contains),
                            () -> method + " 暴露了内部参数类型");
                }
            }
        }
    }

    @Test
    void allSixApisMustExist() {
        assertAll(
                () -> assertTrue(ScenarioApi.class.isInterface()),
                () -> assertTrue(VersionApi.class.isInterface()),
                () -> assertTrue(ScheduleTaskApi.class.isInterface()),
                () -> assertTrue(ProjectionApi.class.isInterface()),
                () -> assertTrue(ShortageApi.class.isInterface()),
                () -> assertTrue(LoadApi.class.isInterface())
        );
    }
}
