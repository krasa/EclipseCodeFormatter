/**
 * Wrapper for java.lang.Object.wait
 *
 * can be called only within a sync method
 */
function wait(object) {
    var objClazz = java.lang.Class.forName('java.lang.Object');
    var waitMethod = objClazz.getMethod('wait', null);
    waitMethod.invoke(object, null);
}
wait.docString = "convenient wrapper for java.lang.Object.wait method";