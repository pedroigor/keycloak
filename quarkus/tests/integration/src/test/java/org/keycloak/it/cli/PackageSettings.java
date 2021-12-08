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

package org.keycloak.it.cli;

/**
 * Used to specify the output directory for the received / to-be-approved outputs of this packages tests.
 * In our case they should be stored under resources/clitest/approvals or resources/rawdist/approvals depending
 * on the runtype of the tests (@DistributionTest in Raw mode, or @CLITest, leading to either using "kc.sh"
 * or "java -jar $KEYCLOAK_HOME/lib/quarkus-run.jar" as command in the usage output).
 *
 * Note: Creates the directories if they don't exist yet.
 * **/
public class PackageSettings {
    public String UseApprovalSubdirectory = "approvals/cli/help";
    public String ApprovalBaseDirectory = "../resources";
}
