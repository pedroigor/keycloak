/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.quarkus.runtime.cli.command;

import java.io.File;
import java.nio.file.Path;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.cli.Picocli;

import picocli.CommandLine;

public final class StartMixin {

    public static final String IMPORT_REALM = "--import-realms";

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = IMPORT_REALM,
            arity = "0",
            description = "Indicates if realms should be imported during start-up by looking at the files from the `data/import` directory.",
            paramLabel = Picocli.NO_PARAM_LABEL)
    public void setRealmFilesToImport(boolean importRealms) {
        Path homePath = Environment.getHomePath();

        if (homePath != null) {
            File importDir = homePath.resolve("data").resolve("import").toFile();

            if (importDir.isFile()) {
                spec.commandLine().getErr().println("The 'data" + File.pathSeparator + "import' directory is a file");
                System.exit(spec.exitCodeOnExecutionException());
            }

            if (!importDir.exists()) {
                spec.commandLine().getErr().println("The 'data" + File.pathSeparator + "import' directory does not exist");
                System.exit(spec.exitCodeOnExecutionException());
            }

            StringBuilder realmFiles = new StringBuilder();

            for (File realmFile : importDir.listFiles()) {
                if (realmFiles.length() > 0) {
                    realmFiles.append(",");
                }
                realmFiles.append(realmFile.getAbsolutePath());
            }

            System.setProperty("keycloak.import", realmFiles.toString());
        }
    }
}
