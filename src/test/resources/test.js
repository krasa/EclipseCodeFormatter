/**
 * Plugin used to check the component has at least one of the permissions
 * provided.
 *
 * Usage:
 *
 * This checks only for one permission
 *
 * var button = new Ext.Button({ plugins: new
 * Abc.security.AnyGranted('PERM_LOGIN') });
 *
 * This checks only for all permissions
 *
 * var button = new Ext.Button({ plugins: new
 * Abc.security.AnyGranted(['PERM_LOGIN','PERM_DASHBOARD']) });
 *
 *  /
 */
function wait(object) {
    var objClazz = java.lang.Class.forName('java.lang.Object');
    var waitMethod = objClazz.getMethod('wait', null);
    waitMethod.invoke(object, null);
}
wait.docString = "convenient wrapper for java.lang.Object.wait method";
