/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fonimus.ssh.shell.commands;

import com.github.fonimus.ssh.shell.SimpleTable;
import com.github.fonimus.ssh.shell.SshShellHelper;
import com.github.fonimus.ssh.shell.SshShellProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.shell.Availability;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.*;
import org.springframework.stereotype.Component;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JMX command
 */
@SshShellComponent
@ShellCommandGroup("Jmx Commands")
public class JmxCommand extends AbstractCommand {

    private static final String GROUP = "jmx";
    private static final String COMMAND_JMX_LIST = GROUP + "-list";
    private static final String COMMAND_JMX_INFO = GROUP + "-info";
    private static final String COMMAND_JMX_INVOKE = GROUP + "-invoke";

    private static final String OBJECT_NAME_EXAMPLE = "org.springframework.boot:type=Endpoint,name=Info";

    private final SshShellHelper helper;

    public JmxCommand(SshShellHelper helper, SshShellProperties properties) {
        super(helper, properties, properties.getCommands().getJmx());
        this.helper = helper;
    }

    /**
     * List jmx mbeans
     *
     * @param pattern (optional) allows you to narrow search
     */
    @ShellMethod(key = COMMAND_JMX_LIST, value = "List jmx mbeans.")
    @ShellMethodAvailability("jmxListAvailability")
    public void jmxList(
            @ShellOption(
                    help = "Pattern to search for (ex: org.springframework.boot:*, org.springframework" +
                            ".boot:type=Endpoint,name=*, " + OBJECT_NAME_EXAMPLE + ")",
                    defaultValue = ShellOption.NULL
            ) String pattern) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName patternName = pattern != null ? ObjectName.getInstance(pattern) : null;
            SimpleTable.SimpleTableBuilder builder = SimpleTable.builder().column("Object name").column("Class name");
            Set<ObjectInstance> result = server.queryMBeans(patternName, null);
            for (ObjectInstance objectInstance :
                    result.stream().sorted(Comparator.comparing(ObjectInstance::getObjectName)).collect(Collectors.toList())) {
                builder.line(Arrays.asList(objectInstance.getObjectName().toString(), objectInstance.getClassName()));
            }
            helper.print(helper.renderTable(builder.build()));
            helper.print("\nNote: mBean count : " + server.getMBeanCount());
        } catch (MalformedObjectNameException e) {
            helper.printError("Pattern [" + pattern + "] is not in expected format (expected example : " + OBJECT_NAME_EXAMPLE + "). " + e.getMessage());
        }
    }

    /**
     * Displays information about jmx mbean
     *
     * @param objectName          mbean object name
     * @param allAttributesValues set to true to displays attributes values, false by default
     * @throws JMException if error occurs with jmx server
     */
    @ShellMethod(key = COMMAND_JMX_INFO, value = "Displays information about jmx mbean. Use -a option to query " +
            "attribute values.")
    @ShellMethodAvailability("jmxInfoAvailability")
    public void jmxInfo(
            @ShellOption(value = {"-n", "--object-name"}, help = "Object name (ex: " + OBJECT_NAME_EXAMPLE
                    + ")", valueProvider = ObjectNameValuesProvider.class) String objectName,
            @ShellOption(value = {"-a",
                    "--all-attributes-value"}, help = "Get all attributes", defaultValue = ShellOption.NULL) Boolean allAttributesValues
    ) throws JMException {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectNameBean = ObjectName.getInstance(objectName);
            MBeanInfo info = server.getMBeanInfo(objectNameBean);
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append("Object name : ").append(objectName).append("\n")
                    .append("Class name  : ").append(info.getClassName()).append("\n")
                    .append("Description : ").append(info.getDescription()).append("\n");

            SimpleTable.SimpleTableBuilder builder = SimpleTable.builder()
                    .column("Field name").column("Field value");
            for (String fieldName : info.getDescriptor().getFieldNames()) {
                builder.line(Arrays.asList(fieldName, info.getDescriptor().getFieldValue(fieldName)));
            }
            sb.append("Descriptor  : ").append("\n").append(helper.renderTable(builder.build()));
            builder = SimpleTable.builder().column("Name").column("Type").column("Description");
            if (allAttributesValues != null && allAttributesValues) {
                builder.column("Value");
            }
            if (info.getAttributes().length > 0) {
                for (MBeanAttributeInfo attribute : info.getAttributes()) {
                    List<Object> list = new ArrayList<>();
                    list.add(attribute.getName());
                    list.add(attribute.getType());
                    list.add(attribute.getDescription());
                    if (allAttributesValues != null && allAttributesValues) {
                        if (attribute.isReadable()) {
                            try {
                                list.add(server.getAttribute(objectNameBean, attribute.getName()));
                            } catch (JMException e) {
                                list.add("Error while reading attribute : " + e.getMessage());
                            }
                        } else {
                            list.add("Not readable");
                        }
                    }
                    builder.line(list);
                }
                sb.append("Attributes  : ").append("\n").append(helper.renderTable(builder.build()));
            }
            builder = SimpleTable.builder().column("Name").column("Description").column("Impact").column("Return " +
                    "type").column("Parameters");
            if (info.getOperations().length > 0) {
                for (MBeanOperationInfo operation : info.getOperations()) {
                    builder.line(Arrays
                            .asList(operation.getName(), operation.getDescription(), impact(operation.getImpact()),
                                    operation.getReturnType(),
                                    Arrays.stream(operation.getSignature()).map(p -> p.getName() + ":" + p.getType()).collect(Collectors.toList())));
                }
                sb.append("Operations  : ").append("\n").append(helper.renderTable(builder.build()));
            }
            helper.print(sb.toString());
        } catch (MalformedObjectNameException e) {
            helper.printError(
                    "Object name [" + objectName + "] is not in expected format (expected example : " + OBJECT_NAME_EXAMPLE + "). " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            helper.printWarning("Instance not found for name [" + objectName + "]. Check available object names with " +
                    "command jmx-list");
        }
    }

    /**
     * Invoke operation on mbean
     *
     * @param objectName      mbean object name
     * @param operationName   operation name to invoke
     * @param parametersParam parameters, separated by coma
     * @return result of invocation, or null if operation is void type
     * @throws JMException if error occurs with jmx server
     */
    @ShellMethod(key = COMMAND_JMX_INVOKE, value = "Invoke operation on object name.")
    @ShellMethodAvailability("jmxInvokeAvailability")
    public Object jmxInvoke(
            @ShellOption(value = {"-n", "--object-name"}, help = "Object name (ex: " + OBJECT_NAME_EXAMPLE
                    + ")", valueProvider = ObjectNameValuesProvider.class) String objectName,
            @ShellOption(value = {"-o", "--operation-name"}, help = "Operation name (ex: info, for spring boot info " +
                    "mbean)") String operationName,
            @ShellOption(value = {"-p", "--parameters"}, help = "Parameters", defaultValue = ShellOption.NULL) String parametersParam
    ) throws JMException {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectNameBean = ObjectName.getInstance(objectName);
            MBeanInfo info = server.getMBeanInfo(objectNameBean);
            MBeanOperationInfo operation = null;
            for (MBeanOperationInfo o : info.getOperations()) {
                if (o.getName().equals(operationName)) {
                    operation = o;
                }
            }
            if (operation == null) {
                helper.printError("Object name [" + objectName + "] does not have operation with name [" + operationName + "]. Available are : " + Arrays
                        .stream(info.getOperations()).map(MBeanFeatureInfo::getName).collect(Collectors.joining()));
            } else {
                Object[] parameters = parametersParam != null ? parametersParam.split(",") : new Object[0];
                String[] signature =
                        parametersParam != null ?
                                Arrays.stream(operation.getSignature()).map(MBeanParameterInfo::getType).toArray(String[]::new) : new String[0];
                Object result = server.invoke(objectNameBean, operationName, parameters, signature);
                helper.printSuccess("Operation [" + operationName + "] invoked on mbean [" + objectName + "] " +
                        "successfully");
                if (result != null) {
                    return result;
                }
            }
        } catch (MalformedObjectNameException e) {
            helper.printError(
                    "Object name [" + objectName + "] is not in expected format (expected example : " + OBJECT_NAME_EXAMPLE + "). " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            helper.printWarning("Instance not found for name [" + objectName + "]. Check available object names with " +
                    "command jmx-list");
        }
        return null;
    }

    private Object impact(int impact) {
        switch (impact) {
            case MBeanOperationInfo.ACTION:
                return "action";
            case MBeanOperationInfo.ACTION_INFO:
                return "action/info";
            case MBeanOperationInfo.INFO:
                return "info";
            case MBeanOperationInfo.UNKNOWN:
                return "unknown";
            default:
                return "(" + impact + ")";
        }
    }

    private Availability jmxListAvailability() {
        return availability(GROUP, COMMAND_JMX_LIST);
    }

    private Availability jmxInfoAvailability() {
        return availability(GROUP, COMMAND_JMX_INFO);
    }

    private Availability jmxInvokeAvailability() {
        return availability(GROUP, COMMAND_JMX_INVOKE);
    }
}

@Slf4j
@Component
class ObjectNameValuesProvider
        extends ValueProviderSupport {

    @Override
    public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext,
                                             String[] hints) {
        try {
            return ManagementFactory.getPlatformMBeanServer().queryMBeans(null, null).stream()
                    .map(o -> new CompletionProposal(o.getObjectName().toString())).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.debug("Unable to provide completion for jmx object names", e);
            return Collections.emptyList();
        }
    }
}
