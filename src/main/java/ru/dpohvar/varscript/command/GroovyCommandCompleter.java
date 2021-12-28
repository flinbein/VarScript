package ru.dpohvar.varscript.command;

import groovy.lang.*;
import groovyjarjarantlr.Token;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.tools.shell.ParseStatus;
import org.codehaus.groovy.tools.shell.Parser;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.utils.PreparedScriptProperties;
import ru.dpohvar.varscript.utils.ScriptProperties;
import ru.dpohvar.varscript.workspace.CallerScript;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.*;

public class GroovyCommandCompleter implements TabCompleter {

    private final VarScript plugin;
    private static Pattern propertyPattern = Pattern.compile("[a-zA-Z0-9_$]+");
    private static Pattern methodPattern = Pattern.compile("[a-zA-Z0-9_$]+\\([a-zA-Z0-9_$'\",]*\\)");
    private final List<TabCompleter> delegateCompleters = new ArrayList<TabCompleter>();

    public GroovyCommandCompleter(VarScript plugin) {
        this.plugin = plugin;
    }

    public List<TabCompleter> getDelegateCompleters() {
        return delegateCompleters;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        for (TabCompleter completer : delegateCompleters) {
            List<String> list = completer.onTabComplete(commandSender, command, s, strings);
            if (list != null) return list;
        }

        String expression = strings[strings.length-1];
        TreeSet<String> result = new TreeSet<String>();
        Caller caller = plugin.getCallerService().getCaller(commandSender);
        WorkspaceService service = plugin.getWorkspaceService();
        String workspaceName = service.getWorkspaceName(commandSender);
        Workspace workspace = service.getWorkspace(workspaceName);
        LinkedList<String> tokens = new LinkedList<String>();
        boolean needHelp = expression.endsWith("?");
        if (needHelp) expression = expression.substring(0, expression.length()-1);
        Collections.addAll(tokens, expression.split("\\."));
        if (expression.endsWith(".")) tokens.add("");

        if (needHelp) {
            getHelp(caller, workspace, tokens);
            return Collections.singletonList(expression);
        }

        String firstToken = tokens.pollFirst();
        if (firstToken == null) firstToken = "";
        MetaClass callerScriptMetaClass = InvokerHelper.getMetaClass(CallerScript.class);
        MetaClass workspaceMetaClass = InvokerHelper.getMetaClass(Workspace.class);
        Map workspaceVars = null;
        if (workspace != null) workspaceVars = workspace.getBinding().getVariables();
        Map globalVars = service.getBinding().getVariables();
        PreparedScriptProperties properties = new PreparedScriptProperties();
        properties.setCaller(caller);
        properties.setServer(plugin.getServer());
        properties.setWorkspace(workspace);

        if (tokens.isEmpty()) { // get current method or class
            for (MetaProperty metaProperty : callerScriptMetaClass.getProperties()) {
                String name = metaProperty.getName();
                if (name.contains(firstToken)) result.add(name);
            }
            for (String name : service.getImportTabCompleteClasses().keySet()) {
                if (name.contains(firstToken)) result.add(name);
            }
            for (MetaMethod metaMethod : callerScriptMetaClass.getMetaMethods()) {
                if (metaMethod.getDeclaringClass().getTheClass().equals(Object.class)) continue;
                String name = metaMethod.getName();
                if (name.contains(firstToken)) {
                    String methodEnd = "(";
                    if (metaMethod.isValidMethod(new Class[]{Closure.class})) methodEnd = "{";
                    else if (metaMethod.getParameterTypes().length == 0) methodEnd = "()";
                    result.add(name+methodEnd);
                }
                int args = metaMethod.getParameterTypes().length;
                if ((name.startsWith("get") && args == 0 || name.startsWith("set") && args == 1) && name.length() > 3 ){
                    String propertyName = getPropertyName(name);
                    if (propertyName != null && propertyName.contains(firstToken)) result.add(propertyName);
                }
            }
            for (MetaMethod metaMethod : workspaceMetaClass.getMetaMethods()) {
                if (metaMethod.getDeclaringClass().getTheClass().equals(Object.class)) continue;
                String name = metaMethod.getName();
                if (name.contains(firstToken)) {
                    String methodEnd = "(";
                    if (metaMethod.isValidMethod(new Class[]{Closure.class})) methodEnd = "{";
                    else if (metaMethod.getParameterTypes().length == 0) methodEnd = "()";
                    result.add(name+methodEnd);
                }
            }
            for (Method method : CallerScript.class.getMethods()) {
                if (method.getDeclaringClass().equals(Object.class)) continue;
                String name = method.getName();
                if (name.contains(firstToken)) {
                    String methodEnd = "(";
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 1 && Closure.class.isAssignableFrom(types[0])) methodEnd = "{";
                    else if (types.length == 0) methodEnd = "()";
                    result.add(name+methodEnd);
                    int args = method.getParameterTypes().length;
                    if ((name.startsWith("get") && args == 0 || name.startsWith("set") && args == 1) && name.length() > 3 ){
                        String propertyName = getPropertyName(name);
                        if (propertyName != null && propertyName.contains(firstToken)) result.add(propertyName);
                    }
                }
            }
            for (Method method : Workspace.class.getMethods()) {
                if (method.getDeclaringClass().equals(Object.class)) continue;
                String name = method.getName();
                if (name.contains(firstToken)) {
                    String methodEnd = "(";
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 1 && Closure.class.isAssignableFrom(types[0])) methodEnd = "{";
                    else if (types.length == 0) methodEnd = "()";
                    result.add(name+methodEnd);
                }
            }
            if (workspaceVars != null) for (Object key : workspaceVars.keySet()) {
                String name = key.toString();
                if (name.contains(firstToken)) result.add(name);
            }
            if (globalVars != null) for (Object key : globalVars.keySet()) {
                String name = key.toString();
                if (name.contains(firstToken)) result.add(name);
            }
            for (GroovyObject modifier : CallerScript.getDynamicModifiers()) {
                Object[] params = {properties};
                try {
                    Map<?,?> map = (Map) modifier.getMetaClass().invokeMethod(modifier, "getPropertyMapFor", params);
                    for (Object key : map.keySet()) {
                        String name = key.toString();
                        if (name.contains(firstToken)) result.add(name);
                    }
                } catch (Exception ignored) {}
                try {
                    Map<?,?> map = (Map) modifier.getMetaClass().invokeMethod(modifier, "getMethodMapFor", params);
                    for (Object key : map.keySet()) {
                        String name = key.toString();
                        if (name.contains(firstToken)) result.add(name+"(");
                    }
                } catch (Exception ignored) {}
            }
            if (globalVars != null) for (Object key : globalVars.keySet()) {
                String name = key.toString();
                if (name.contains(firstToken)) result.add(name);
            }
            return new ArrayList<String>(result);
        }


        // get metaclass of first token
        MetaClass metaClass = getFirstTokenMeta(
                caller,
                firstToken,
                commandSender,
                service,
                callerScriptMetaClass,
                workspaceMetaClass,
                workspace,
                workspaceVars,
                globalVars,
                properties
        );
        boolean classHook = tokens.size() <= 1 && service.getImportTabCompleteClasses().containsKey(firstToken);

        if (metaClass == null) return null;
        metaClass = skipTokens(tokens, metaClass);
        if (metaClass == null) return null;

        // select property or method of last metaclass
        String token = tokens.pollFirst();
        Class theClass = metaClass.getTheClass();
        String inputPrefix = expression.substring(0, expression.lastIndexOf('.')) + ".";
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            String name = metaProperty.getName();
            if (name.startsWith(token)) result.add(inputPrefix+name);
        }
        for (MetaMethod metaMethod : metaClass.getMetaMethods()) {
            if (metaMethod.getDeclaringClass().getTheClass().equals(Object.class)) continue;
            String name = metaMethod.getName();
            if (name.startsWith(token)) {
                String methodEnd = "(";
                if (metaMethod.isValidMethod(new Class[]{Closure.class})) methodEnd = "{";
                else if (metaMethod.getNativeParameterTypes().length == 0) methodEnd = "()";
                result.add(inputPrefix+name+methodEnd);
            }
            int args = metaMethod.getParameterTypes().length;
            if ((name.startsWith("get") && args == 0 || name.startsWith("set") && args == 1) && name.length() > 3 ){
                String propertyName = getPropertyName(name);
                if (propertyName != null && propertyName.startsWith(token)) result.add(inputPrefix+propertyName);
            }
        }
        for (Method method : theClass.getMethods()) {
            if (method.getDeclaringClass().equals(Object.class)) continue;
            String name = method.getName();
            if (name.startsWith(token)) {
                String methodEnd = "(";
                Class<?>[] types = method.getParameterTypes();
                if (types.length == 1 && Closure.class.isAssignableFrom(types[0])) methodEnd = "{";
                if (types.length == 0) methodEnd = "()";
                result.add(inputPrefix+name+methodEnd);
            }
            int args = method.getParameterTypes().length;
            if ((name.startsWith("get") && args == 0 || name.startsWith("set") && args == 1) && name.length() > 3 ){
                String propertyName = getPropertyName(name);
                if (propertyName != null && propertyName.startsWith(token)) result.add(inputPrefix+propertyName);
            }
        }
        if (Enum.class.isAssignableFrom(theClass)) {
            Enum[] enumValues = getEnumValues(theClass);
            if (enumValues != null) for (Enum anEnum : enumValues) {
                String name = anEnum.name();
                if (name.startsWith(token)) result.add(inputPrefix+name);
            }
        }
        if (classHook) {
            for (MetaProperty metaProperty : InvokerHelper.getMetaClass(Class.class).getProperties()) {
                String name = metaProperty.getName();
                if (name.startsWith(token)) result.add(inputPrefix+name);
            }
            for (Method method : Class.class.getMethods()) {
                if (method.getDeclaringClass().equals(Object.class)) continue;
                String name = method.getName();
                if (name.startsWith(token)) {
                    String methodEnd = "(";
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 1 && Closure.class.isAssignableFrom(types[0])) methodEnd = "{";
                    if (types.length == 0) methodEnd = "()";
                    result.add(inputPrefix+name+methodEnd);
                }
                int args = method.getParameterTypes().length;
                if ((name.startsWith("get") && args == 0 || name.startsWith("set") && args == 1) && name.length() > 3 ){
                    String propertyName = getPropertyName(name);
                    if (propertyName != null && propertyName.startsWith(token)) result.add(inputPrefix+propertyName);
                }
            }
        }
        return new ArrayList<>(result);
    }

    public void getHelp(Caller caller, Workspace workspace, LinkedList<String> tokens){

        WorkspaceService service = plugin.getWorkspaceService();
        String source = service.getWorkspaceName(caller.getSender());
        String firstToken = tokens.pollFirst();
        if (firstToken == null) firstToken = "";
        MetaClass callerScriptMetaClass = InvokerHelper.getMetaClass(CallerScript.class);
        MetaClass workspaceMetaClass = InvokerHelper.getMetaClass(Workspace.class);
        Map workspaceVars = null;
        if (workspace != null) workspaceVars = workspace.getBinding().getVariables();
        Map globalVars = service.getBinding().getVariables();
        PreparedScriptProperties properties = new PreparedScriptProperties();
        properties.setCaller(caller);
        properties.setServer(plugin.getServer());
        properties.setWorkspace(workspace);

        if (tokens.isEmpty()) { // get current method or class
            if (firstToken.equals("_")) {
                Object result = caller.getLastResult();
                String type = "null";
                if (result != null) type = result.getClass().getName();
                caller.sendPrintMessage("Last result: " + AQUA + type + RESET , source);
                return;
            }
            MetaProperty prop = callerScriptMetaClass.getMetaProperty(firstToken);
            if (prop != null) {
                caller.sendPrintMessage(
                        String.format("Script property %s: %s",
                                YELLOW + prop.getName() + RESET,
                                AQUA + prop.getType().getName() + RESET
                        ), source
                );
                return;
            }
            Class compClass = service.getImportTabCompleteClasses().get(firstToken);
            if (compClass != null) {
                String type = "Class";
                if (compClass.isInterface()) type = "Interface";
                else if (compClass.isEnum()) type = "Enum class";
                StringBuilder buf = new StringBuilder();
                Class superClass = compClass.getSuperclass();
                buf.append(type).append(" ").append(YELLOW).append(compClass.getName()).append(RESET);
                if (superClass != null) {
                    buf.append(" extends ").append(AQUA).append(superClass.getName()).append(RESET);
                }
                caller.sendPrintMessage(buf, source);
                return;
            }
            for (MetaMethod metaMethod : callerScriptMetaClass.getMetaMethods()) {
                String name = metaMethod.getName();
                String methodEnd = "(";
                if (metaMethod.isValidMethod(new Class[]{Closure.class})) methodEnd = "{";
                else if (metaMethod.getParameterTypes().length == 0) methodEnd = "()";
                if (firstToken.equals(name) || firstToken.equals(name+methodEnd)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Script meta method ").append(YELLOW).append(metaMethod.getName()).append(RESET);
                    buf.append(" returns ").append(AQUA).append(metaMethod.getReturnType().getName());
                    buf.append(RESET).append('\n');
                    Class[] types = metaMethod.getNativeParameterTypes();
                    buf.append("arguments: ").append(YELLOW).append(types.length).append(RESET).append('\n');
                    for (Class type : types) buf.append(AQUA).append(type.getName()).append('\n').append(RESET);
                    buf.deleteCharAt(buf.length()-1);
                    caller.sendPrintMessage(buf, source);
                }
                int args = metaMethod.getParameterTypes().length;
                if ((name.startsWith("get") && args == 0 || name.startsWith("set") && args == 1) && name.length() > 3 ){
                    String propertyName = getPropertyName(name);
                    if (propertyName != null && propertyName.equals(firstToken)) {
                        caller.sendPrintMessage(
                                String.format("Script meta getter %s returns %s",
                                        YELLOW + name + "()" + RESET,
                                        AQUA + metaMethod.getReturnType().getName() + RESET
                                ), source
                        );
                    }
                }
            }
            for (MetaMethod metaMethod : workspaceMetaClass.getMetaMethods()) {
                String name = metaMethod.getName();
                String methodEnd = "(";
                if (metaMethod.isValidMethod(new Class[]{Closure.class})) methodEnd = "{";
                else if (metaMethod.getParameterTypes().length == 0) methodEnd = "()";
                if (firstToken.equals(name) || firstToken.equals(name+methodEnd)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Workspace meta method ").append(YELLOW).append(metaMethod.getName()).append(RESET);
                    buf.append(" returns ").append(AQUA).append(metaMethod.getReturnType().getName());
                    buf.append(RESET).append('\n');
                    Class[] types = metaMethod.getNativeParameterTypes();
                    buf.append("arguments: ").append(YELLOW).append(types.length).append(RESET).append('\n');
                    for (Class type : types) buf.append(AQUA).append(type.getName()).append(RESET).append('\n');
                    buf.deleteCharAt(buf.length()-1);
                    caller.sendPrintMessage(buf, source);
                }
            }
            for (Method method : CallerScript.class.getMethods()) {
                String name = method.getName();
                String methodEnd = "(";
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && Closure.class.isAssignableFrom(params[0])) methodEnd = "{";
                else if (params.length == 0) methodEnd = "()";
                if (firstToken.equals(name) || firstToken.equals(name+methodEnd)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Script method ").append(YELLOW).append(method.getName()).append(RESET);
                    buf.append(" returns ").append(AQUA).append(method.getReturnType().getName());
                    buf.append(RESET).append('\n');
                    buf.append("arguments: ").append(YELLOW).append(params.length).append(RESET).append('\n');
                    for (Class<?> type : params) buf.append(AQUA).append(type.getName()).append(RESET).append('\n');
                    buf.deleteCharAt(buf.length()-1);
                    caller.sendPrintMessage(buf, source);
                }
                int args = params.length;
                if ((name.startsWith("get") && args == 0 || name.startsWith("set") && args == 1) && name.length() > 3 ){
                    String propertyName = getPropertyName(name);
                    if (propertyName != null && propertyName.equals(firstToken)) {
                        caller.sendPrintMessage(
                                String.format("Script getter %s returns %s",
                                        YELLOW + name + "()" + RESET,
                                        AQUA + method.getReturnType().getName() + RESET
                                ), source
                        );
                    }
                }
            }
            for (Method method : Workspace.class.getMethods()) {
                String name = method.getName();
                String methodEnd = "(";
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && Closure.class.isAssignableFrom(params[0])) methodEnd = "{";
                else if (params.length == 0) methodEnd = "()";
                if (firstToken.equals(name) || firstToken.equals(name+methodEnd)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Workspace method ").append(YELLOW).append(method.getName()).append(RESET);
                    buf.append(" returns ").append(AQUA).append(method.getReturnType().getName());
                    buf.append(RESET).append('\n');
                    buf.append("arguments: ").append(YELLOW).append(params.length).append(RESET).append('\n');
                    for (Class<?> type : params) buf.append(AQUA).append(type.getName()).append(RESET).append('\n');
                    buf.deleteCharAt(buf.length()-1);
                    caller.sendPrintMessage(buf, source);
                }
            }
            if (workspaceVars != null) {
                Object result = workspaceVars.get(firstToken);
                if (result != null || workspaceVars.containsKey(firstToken)) {
                    caller.sendPrintMessage(
                            String.format("Workspace variable %s: %s",
                                    YELLOW + firstToken + RESET,
                                    AQUA + (result == null ? "null" : result.getClass().getName()) + RESET
                            ), source
                    );
                    return;
                }
            }
            if (globalVars != null) {
                Object result = globalVars.get(firstToken);
                if (result != null || globalVars.containsKey(firstToken)) {
                    caller.sendPrintMessage(
                            String.format("Workspace variable %s: %s",
                                    YELLOW + firstToken + RESET,
                                    AQUA + (result == null ? "null" : result.getClass().getName()) + RESET
                            ), source
                    );
                    return;
                }
            }
            for (GroovyObject modifier : CallerScript.getDynamicModifiers()) {
                Object[] params = {properties};
                try {
                    Map<?,?> map = (Map) modifier.getMetaClass().invokeMethod(modifier, "getPropertyMapFor", params);
                    Object result = map.get(firstToken);
                    if (result != null || map.containsKey(firstToken)) {
                        Class resultClass = result instanceof Class ? (Class) result : null;
                        caller.sendPrintMessage(
                                String.format("Dynamic variable %s: %s",
                                        YELLOW + firstToken + RESET,
                                        AQUA + (resultClass == null ? "unknown type" : resultClass.getName()) + RESET
                                ), source
                        );
                    }
                } catch (Exception ignored) {}
                try {
                    Map<?,?> map = (Map) modifier.getMetaClass().invokeMethod(modifier, "getMethodMapFor", params);
                    String funToken = firstToken;
                    if (funToken.endsWith("(")) funToken = firstToken.substring(0, funToken.length()-1);
                    Object result = map.get(funToken);
                    if (result != null || map.containsKey(funToken)) {
                        Class resultClass = result instanceof Class ? (Class) result : null;
                        caller.sendPrintMessage(
                                String.format("Dynamic function %s: %s",
                                        YELLOW + firstToken + RESET,
                                        AQUA + (resultClass == null ? "unknown type" : resultClass.getName()) + RESET
                                ), source
                        );
                    }
                } catch (Exception ignored) {}
            }
            return;
        }

        MetaClass metaClass = getFirstTokenMeta(
                caller,
                firstToken,
                caller.getSender(),
                service,
                callerScriptMetaClass,
                workspaceMetaClass,
                workspace,
                workspaceVars,
                globalVars,
                properties
        );
        boolean classHook = tokens.size() <= 1 && service.getImportTabCompleteClasses().containsKey(firstToken);

        metaClass = skipTokens(tokens, metaClass);
        if (metaClass == null) return;


        // select property or method of last metaclass
        String token = tokens.pollFirst();
        Class theClass = metaClass.getTheClass();
        MetaProperty metaProperty = metaClass.getMetaProperty(token);
        if (metaProperty != null) {
            caller.sendPrintMessage(
                    String.format("Meta property %s: %s",
                            YELLOW + token + RESET,
                            AQUA + metaProperty.getType().getName() + RESET
                    ), source
            );
        }
        for (MetaMethod metaMethod : metaClass.getMetaMethods()) {
            String name = metaMethod.getName();
            String methodEnd = "(";
            Class<?>[] params = metaMethod.getNativeParameterTypes();
            if (params.length == 1 && Closure.class.isAssignableFrom(params[0])) methodEnd = "{";
            else if (params.length == 0) methodEnd = "()";
            if (token.equals(name) || token.equals(name+methodEnd)) {
                StringBuilder buf = new StringBuilder();
                buf.append("Meta method ").append(YELLOW).append(metaMethod.getName()).append(RESET);
                buf.append(" returns ").append(AQUA).append(metaMethod.getReturnType().getName());
                buf.append(RESET).append('\n');
                Class[] types = metaMethod.getNativeParameterTypes();
                buf.append("arguments: ").append(YELLOW).append(types.length).append(RESET).append('\n');
                for (Class type : types) buf.append(AQUA).append(type.getName()).append(RESET).append('\n');
                buf.deleteCharAt(buf.length()-1);
                caller.sendPrintMessage(buf, source);
            }
            int args = params.length;
            if (name.startsWith("get") && args == 0 && name.length() > 3 ){
                String propertyName = getPropertyName(name);
                if (propertyName != null && propertyName.equals(token)) {
                    caller.sendPrintMessage(
                            String.format("Meta getter %s returns %s",
                                    YELLOW + name + "()" + RESET,
                                    AQUA + metaMethod.getReturnType().getName() + RESET
                            ), source
                    );
                }
            }
        }
        for (Method method : theClass.getMethods()) {
            String name = method.getName();
            String methodEnd = "(";
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && Closure.class.isAssignableFrom(params[0])) methodEnd = "{";
            else if (params.length == 0) methodEnd = "()";
            if (token.equals(name) || token.equals(name+methodEnd)) {
                StringBuilder buf = new StringBuilder();
                buf.append("Method ").append(YELLOW).append(method.getName()).append(RESET);
                buf.append(" returns ").append(AQUA).append(method.getReturnType().getName());
                buf.append(RESET).append('\n');
                Class[] types = method.getParameterTypes();
                buf.append("arguments: ").append(YELLOW).append(types.length).append(RESET).append('\n');
                for (Class type : types) buf.append(AQUA).append(type.getName()).append(RESET).append('\n');
                buf.deleteCharAt(buf.length()-1);
                caller.sendPrintMessage(buf, source);
            }
            int args = params.length;
            if (name.startsWith("get") && args == 0 && name.length() > 3){
                String propertyName = getPropertyName(name);
                if (propertyName != null && propertyName.equals(token)) {
                    caller.sendPrintMessage(
                            String.format("Getter %s returns %s",
                                    YELLOW + name + "()" + RESET,
                                    AQUA + method.getReturnType().getName() + RESET
                            ), source
                    );
                }
            }
        }
        if (Enum.class.isAssignableFrom(theClass)) {
            Enum[] enumValues = getEnumValues(theClass);
            if (enumValues != null) for (Enum anEnum : enumValues) {
                String name = anEnum.name();
                if (name.equals(token)) {
                    caller.sendPrintMessage(
                            String.format("Enum value %s: %s",
                                    YELLOW + name + RESET,
                                    AQUA + theClass.getName() + RESET
                            ), source
                    );
                }
            }
        }
        if (classHook) {
            MetaProperty property = InvokerHelper.getMetaClass(Class.class).getMetaProperty(token);
            if (property != null) {
                caller.sendPrintMessage(
                        String.format("Meta property %s: %s",
                                YELLOW + token + RESET,
                                AQUA + property.getType().getName() + RESET
                        ), source
                );
            }
            for (MetaMethod metaMethod : InvokerHelper.getMetaClass(Class.class).getMetaMethods()) {
                String name = metaMethod.getName();
                String methodEnd = "(";
                Class<?>[] params = metaMethod.getNativeParameterTypes();
                if (params.length == 1 && Closure.class.isAssignableFrom(params[0])) methodEnd = "{";
                else if (params.length == 0) methodEnd = "()";
                if (firstToken.equals(name) || firstToken.equals(name+methodEnd)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Method ").append(YELLOW).append(metaMethod.getName()).append(RESET);
                    buf.append(" returns ").append(AQUA).append(metaMethod.getReturnType().getName());
                    buf.append(RESET).append('\n');
                    Class[] types = metaMethod.getNativeParameterTypes();
                    buf.append("arguments: ").append(YELLOW).append(types.length).append(RESET).append('\n');
                    for (Class type : types) buf.append(AQUA).append(type.getName()).append(RESET).append('\n');
                    buf.deleteCharAt(buf.length()-1);
                    caller.sendPrintMessage(buf, source);
                }
            }
            for (Method method : Class.class.getMethods()) {
                String name = method.getName();
                String methodEnd = "(";
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && Closure.class.isAssignableFrom(params[0])) methodEnd = "{";
                else if (params.length == 0) methodEnd = "()";
                if (firstToken.equals(name) || firstToken.equals(name+methodEnd)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Method ").append(YELLOW).append(method.getName()).append(RESET);
                    buf.append(" returns ").append(AQUA).append(method.getReturnType().getName());
                    buf.append(RESET).append('\n');
                    Class[] types = method.getParameterTypes();
                    buf.append("arguments: ").append(YELLOW).append(types.length).append(RESET).append('\n');
                    for (Class type : types) buf.append(AQUA).append(type.getName()).append(RESET).append('\n');
                    buf.deleteCharAt(buf.length()-1);
                    caller.sendPrintMessage(buf, source);
                }
            }
        }

    }

    private MetaClass getFirstTokenMeta(
            Caller caller,
            String firstToken,
            CommandSender commandSender,
            WorkspaceService service,
            MetaClass callerScriptMetaClass,
            MetaClass workspaceMetaClass,
            Workspace workspace,
            Map workspaceVars,
            Map globalVars,
            PreparedScriptProperties properties

    ){
        if (firstToken.isEmpty() || propertyPattern.matcher(firstToken).matches()){
            if (firstToken.equals("me")) return InvokerHelper.getMetaClass(commandSender.getClass());
            if (firstToken.equals("this")) return InvokerHelper.getMetaClass(CallerScript.class);
            if (firstToken.equals("_")) {
                Object result = caller.getLastResult();
                if (result == null) return null;
                return InvokerHelper.getMetaClass(result.getClass());
            }
            Class aClass = service.getImportTabCompleteClasses().get(firstToken);
            if (aClass != null) return InvokerHelper.getMetaClass(aClass);
            MetaProperty property = callerScriptMetaClass.getMetaProperty(firstToken);
            if (property != null) return InvokerHelper.getMetaClass(property.getType());
            if (workspaceVars != null) for (Object key : workspaceVars.keySet()) {
                String name = key.toString();
                if (name.contains(firstToken)) {
                    Object val = workspaceVars.get(key);
                    return InvokerHelper.getMetaClass(val.getClass());
                }
            }
            if (globalVars != null) for (Object key : globalVars.keySet()) {
                String name = key.toString();
                if (name.contains(firstToken)) {
                    Object val = globalVars.get(key);
                    return InvokerHelper.getMetaClass(val.getClass());
                }
            }
            if (workspace != null) {
                for (GroovyObject modifier : CallerScript.getDynamicModifiers()) {
                    Object[] params = {properties};
                    try {
                        Map<?,?> map = (Map) modifier.getMetaClass().invokeMethod(modifier, "getPropertyMapFor", params);
                        Object value = map.get(firstToken);
                        if (!(value instanceof Class)) return null;
                        return InvokerHelper.getMetaClass((Class) value);
                    } catch (Exception ignored) {}
                }
            }
        } else if (methodPattern.matcher(firstToken).matches()) {
            String curMethodName = firstToken.substring(0, firstToken.indexOf('('));
            for (MetaMethod metaMethod : callerScriptMetaClass.getMetaMethods()) {
                String name = metaMethod.getName();
                if (name.equals(curMethodName)) {
                    return InvokerHelper.getMetaClass(metaMethod.getReturnType());
                }
            }
            for (MetaMethod metaMethod : workspaceMetaClass.getMetaMethods()) {
                String name = metaMethod.getName();
                if (name.equals(curMethodName)) {
                    return InvokerHelper.getMetaClass(metaMethod.getReturnType());
                }
            }
            for (GroovyObject modifier : CallerScript.getDynamicModifiers()) {
                Object[] params = {properties};
                try {
                    Map<?,?> map = (Map) modifier.getMetaClass().invokeMethod(modifier, "getMethodMapFor", params);
                    Object value = map.get(curMethodName);
                    if (!(value instanceof Class)) return null;
                    return InvokerHelper.getMetaClass(value);
                } catch (Exception ignored) {}
            }
        } else if (firstToken.endsWith("\'") || firstToken.endsWith("\"")) {
            return InvokerHelper.getMetaClass(String.class);
        }
        return null;
    }

    private MetaClass skipTokens(LinkedList<String> tokens, MetaClass metaClass){
        while (tokens.size() > 1) {
            Class theClass = metaClass.getTheClass();
            String token = tokens.pollFirst();
            Class selectedClass = null;
            if (propertyPattern.matcher(token).matches()) {
                String getterName = "get" + StringGroovyMethods.capitalize((CharSequence)token);
                for (MetaMethod metaMethod : metaClass.getMetaMethods()) {
                    if (metaMethod.getName().equals(getterName)) {
                        selectedClass = metaMethod.getReturnType();
                        break;
                    }
                }
                if (selectedClass == null) for (Method method : theClass.getMethods()) {
                    if (method.getParameterTypes().length != 0) continue;
                    if (method.getName().equals(getterName)) {
                        selectedClass = method.getReturnType();
                        break;
                    }
                }
                if (selectedClass == null) {
                    MetaProperty metaProperty = metaClass.getMetaProperty(token);
                    if (metaProperty == null) return null;
                    selectedClass = metaProperty.getType();
                }
            } else if (methodPattern.matcher(token).matches()) {
                String curMethodName = token.substring(0, token.indexOf('('));
                for (MetaMethod metaMethod : metaClass.getMetaMethods()) {
                    String name = metaMethod.getName();
                    if (name.equals(curMethodName)) {
                        selectedClass = metaMethod.getReturnType();
                        break;
                    }
                }
                if (selectedClass == null) for (Method method : theClass.getMethods()) {
                    String name = method.getName();
                    if (name.equals(curMethodName)) {
                        selectedClass = method.getReturnType();
                        break;
                    }
                }
            }
            if (selectedClass == null) return null;
            metaClass = InvokerHelper.getMetaClass(selectedClass);
        }
        return metaClass;
    }

    private static String getPropertyName(String methodName){
        String property = methodName.substring(3);
        if (Character.isLowerCase(property.charAt(0))) return null;
        if (property.length() <= 1 ) return property.toLowerCase();
        if (Character.isUpperCase(property.charAt(1))) return property;
        return property.substring(0,1).toLowerCase() + property.substring(1);
    }

    private static Enum[] getEnumValues(Class<?> enumClass){
        try {
            return (Enum[]) enumClass.getMethod("values").invoke(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
