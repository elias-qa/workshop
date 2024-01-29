package com.teamcity.api.generators;

import com.teamcity.api.annotations.Random;
import com.teamcity.api.enums.Endpoint;
import com.teamcity.api.enums.UserRole;
import com.teamcity.api.models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import static java.util.Comparator.reverseOrder;

public final class TestDataGenerator {

    private static final String SIMPLE_RUNNER_STEP_TYPE = "simpleRunner";

    private TestDataGenerator() {
    }

    public static BaseModel generate(Class<? extends BaseModel> generatorClass, Collection<BaseModel> generatedModels) {
        try {
            var instance = generatorClass.getDeclaredConstructor().newInstance();
            for (var field : generatorClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(instance) == null) {
                    var generatedClass = generatedModels.stream().filter(m
                            -> m.getClass().equals(field.getType())).findFirst();
                    if (field.getAnnotation(Random.class) != null && String.class.equals(field.getType())) {
                        field.set(instance, RandomData.getString());
                    } else if (BaseModel.class.isAssignableFrom(field.getType())) {
                        field.set(instance, generatedClass.orElseGet(()
                                -> generate(field.getType().asSubclass(BaseModel.class), generatedModels)));
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        if (field.getGenericType() instanceof ParameterizedType pt) {
                            var typeClass = (Class<?>) pt.getActualTypeArguments()[0];
                            if (BaseModel.class.isAssignableFrom(typeClass)) {
                                field.set(instance, generatedClass.map(List::of).orElseGet(()
                                        -> List.of(generate(typeClass.asSubclass(BaseModel.class), generatedModels))));
                            }
                        }
                    }
                }
                field.setAccessible(false);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                 | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot generate test data", e);
        }
    }

    public static BaseModel generate(Class<? extends BaseModel> generatorClass) {
        return generate(generatorClass, null);
    }

    public static EnumMap<Endpoint, BaseModel> generate() {
        var generatedTestData = new EnumMap<Endpoint, BaseModel>(Endpoint.class);
        Arrays.stream(Endpoint.values()).filter(e -> e.getGeneratorClass() != null).sorted(reverseOrder())
                .forEach(endpoint -> generatedTestData.put(endpoint, generate(endpoint.getGeneratorClass(), generatedTestData.values())));
        return generatedTestData;
        /*var project = NewProjectDescription.builder()
                .id(RandomData.getString())
                .name(RandomData.getString())
                .parentProject(Project.builder()
                        .locator("_Root")
                        .build())
                .build();

        var user = User.builder()
                .username(RandomData.getString())
                .email(RandomData.getString() + "@gmail.com")
                .password(RandomData.getString())
                .roles(generateRoles(UserRole.SYSTEM_ADMIN, "g"))
                .build();

        var buildType = BuildType.builder()
                .id(RandomData.getString())
                .name(RandomData.getString())
                .project(project)
                .build();

        return TestData.builder()
                .project(project)
                .user(user)
                .buildType(buildType)
                .build();*/
    }

    public static Roles generateRoles(UserRole role, String scope) {
        return Roles.builder()
                .role(List.of(Role.builder()
                        .roleId(role.toString())
                        .scope(scope)
                        .build()))
                .build();
    }

    // Генерация дефолтных степов для запуска command line скрипта, в параметр метода передается исполняемая команда
    public static Steps generateSimpleRunnerSteps(String propertyValue) {
        return Steps.builder()
                .step(List.of(Step.builder()
                        .name(RandomData.getString())
                        .type(SIMPLE_RUNNER_STEP_TYPE)
                        .properties(Properties.builder()
                                .property(List.of(Property.builder()
                                        .name("script.content")
                                        .value(propertyValue)
                                        .build(), Property.builder()
                                        .name("use.custom.script")
                                        .value("true")
                                        .build()))
                                .build())
                        .build()))
                .build();
    }

    // Необходимый модуль в запросах на изменение прав (для тестов роли project admin)
    // https://www.jetbrains.com/help/teamcity/authentication-modules.html
    public static AuthModules generateAuthModules() {
        return AuthModules.builder()
                .module(List.of(AuthModule.builder()
                        .name("HTTP-Basic")
                        .build()))
                .build();
    }

}
