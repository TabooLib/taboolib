package taboolib.module.lang;

public class StackTracePrinter {

    public static void printStackTrace(Throwable exception, String packageFilter) {
        String msg = exception.getLocalizedMessage();
        LangLevelKt.log("&7===================================&c&l printStackTrace &7===================================");
        LangLevelKt.log("&7Exception Type ▶");
        LangLevelKt.log("&c" + exception.getClass().getName());
        LangLevelKt.log("&c" + ((msg == null || msg.length() == 0) ? "&7No description." : msg));
        String lastPackage = "";
        for (StackTraceElement elem : exception.getStackTrace()) {
            String key = elem.getClassName();

            boolean pass = true;
            if (packageFilter != null) {
                pass = key.contains(packageFilter);
            }

            final String[] nameSet = key.split("[.]");
            final String className = nameSet[nameSet.length - 1];
            final String[] packageSet = new String[nameSet.length - 2];
            System.arraycopy(nameSet, 0, packageSet, 0, nameSet.length - 2);

            StringBuilder packageName = new StringBuilder();
            int counter = 0;
            for (String nameElem : packageSet) {
                packageName.append(nameElem);
                if (counter < packageSet.length - 1) {
                    packageName.append(".");
                }
                counter++;
            }

            if (pass) {
                if (!packageName.toString().equals(lastPackage)) {
                    lastPackage = packageName.toString();
                    LangLevelKt.log("");
                    LangLevelKt.log("&7Package &c" + packageName + " &7▶");
                }
                LangLevelKt.log("  &7▶ at Class &c" + className + "&7, Method &c" + elem.getMethodName() + "&7. (&c" + elem.getFileName() + "&7, Line &c" + elem.getLineNumber() + "&7)");
            }
        }
        LangLevelKt.log("&7===================================&c&l printStackTrace &7===================================");
    }
}
