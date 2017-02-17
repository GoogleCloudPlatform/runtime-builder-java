java_library(
    name = "maven",
    srcs = glob(["**/*.java"]),
    deps = [
        "@com_google_guava_guava//jar",
        "@com_google_inject_guice//jar",
        "@org_apache_maven_shared_maven_invoker//jar",
        "@org_slf4j_slf4j_api//jar",
        "//src/main/java/com/google/cloud/runtimes/builder/buildsteps/base:build_step_base",
    ],
)

java_binary(
    name = "maven-bin",
    main_class = "com.google.cloud.runtimes.builder.buildsteps.maven.MavenBuildStep",
    runtime_deps = [
        "@org_slf4j_slf4j_jdk14//jar",
        ":maven",
    ],
)

load("@bazel_tools//tools/build_defs/docker:docker.bzl", "docker_build")
docker_build(
    name = "maven-builder-docker",
    # base = 
    files = [":maven-bin"],
    entrypoint = ["java", "-jar", ":maven-bin"]
)

# is this even necessary? for the simple builders, jars may not be necessary if they are just invoking a build tool...
