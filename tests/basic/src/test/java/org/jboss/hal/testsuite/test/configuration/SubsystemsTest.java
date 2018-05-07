/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.testsuite.test.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.finder.ColumnFragment;
import org.jboss.hal.testsuite.fragment.finder.FinderPath;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Arquillian.class)
public class SubsystemsTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);
    private static List<String> subsystems = new ArrayList<>();
    // this is from org.jboss.hal.client.configuration.subsystem.SubsystemColumn
    private static final List<String> EMPTY_SUBSYSTEMS = asList("bean-validation", "core-management", "ee-security",
            "jaxrs", "jdr", "jsr77", "pojo", "sar");

    @BeforeClass
    public static void beforeClass() throws Exception {
        subsystems = operations.readChildrenNames(Address.root(), SUBSYSTEM).stringListValue();
    }

    @Inject private Console console;
    private ColumnFragment column;

    @Before
    public void setUp() throws Exception {
        column = console.finder(NameTokens.CONFIGURATION,
                new FinderPath().append(Ids.CONFIGURATION, Ids.asId(Names.SUBSYSTEMS)))
                .column(Ids.CONFIGURATION_SUBSYSTEM);
    }

    @Test
    public void numberOfSubsystems() {
        assertEquals(subsystems.size() - EMPTY_SUBSYSTEMS.size(), column.getItems().size());
    }

    @Test
    public void emptySubsystemsNotDisplayed() {
        for (String subsys: EMPTY_SUBSYSTEMS) {
            assertFalse(column.containsItem(subsys));
        }
    }

    @Test
    public void filter() {
        String filter = "io";
        column.filter(filter);

        long filtered = subsystems.stream()
                .filter(subsystem -> subsystem.toLowerCase().contains(filter)
                        && EMPTY_SUBSYSTEMS.indexOf(subsystem) < 0)
                .count();
        long visible = column.getItems().stream()
                .filter(WebElement::isDisplayed)
                .count();
        assertEquals(filtered, visible);
    }
}
