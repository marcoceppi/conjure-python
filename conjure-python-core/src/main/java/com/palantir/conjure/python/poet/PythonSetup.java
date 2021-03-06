/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.python.poet;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.immutables.value.Value;

@Value.Immutable
public interface PythonSetup extends PythonClass {

    ImmutableSet<PythonImport> DEFAULT_IMPORTS = ImmutableSet.of(
            PythonImport.of(PythonClassName.of("setuptools", "find_packages")),
            PythonImport.of(PythonClassName.of("setuptools", "setup")));

    @Override
    @Value.Default
    default Set<PythonImport> requiredImports() {
        return DEFAULT_IMPORTS;
    }

    @Override
    default String className() {
        return "";
    }

    @Override
    default String packageName() {
        return "";
    }

    Map<String, String> options();

    List<String> installDependencies();

    @Override
    default void emit(PythonPoetWriter poetWriter) {
        poetWriter.maintainingIndent(() -> {
            poetWriter.writeIndentedLine("setup(");
            poetWriter.increaseIndent();

            options().forEach((key, value) -> {
                poetWriter.writeIndentedLine("%s='%s',", key, value);
            });
            poetWriter.writeIndentedLine("packages=find_packages(),");

            poetWriter.writeIndentedLine("install_requires=[");
            poetWriter.increaseIndent();
            installDependencies().forEach(dependency -> {
                poetWriter.writeIndentedLine("'%s',", dependency);
            });
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("],");

            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine(")");
        });
    }

    class Builder extends ImmutablePythonSetup.Builder {}

    static Builder builder() {
        return new Builder();
    }

}
