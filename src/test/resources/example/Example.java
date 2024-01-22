/**
 * An example for comment formatting. This example is meant to illustrate the
 * various possibilities offered by <i>Eclipse</i> in order to format comments.
 */
package mypackage;

/**
 * This is the comment for the example interface.
 */
interface Example {
    // This is a long comment with whitespace that should be split in multiple
    // line comments in case the line comment formatting is enabled
    int foo3();

    // void commented() {
    // System.out.println("indented");
    // }

    // void indentedCommented() {
    // System.out.println("indented");
    // }

    /* block comment on first column */
    int bar();

    /*
     * These possibilities include: <ul><li>Formatting of header
     * comments.</li><li>Formatting of Javadoc tags</li></ul>
     */
    int bar2(); // This is a long comment that should be split in multiple line
    // comments in case the line comment formatting is enabled

    /**
     * The following is some sample code which illustrates source formatting
     * within javadoc comments:
     *
     * <pre>
     * public class Example {
     *     final int a = 1;
     *     final boolean b = true;
     * }
     * </pre>
     *
     * Descriptions of parameters and return values are best appended at end of
     * the javadoc comment.
     * @param first
     *            The first parameter. For an optimum result, this should be an
     *            odd number between 0 and 100.
     * @param second
     *            The second parameter.
     * @throws Exception
     *             when the foo operation cannot be performed for one reason or
     *             another.
     * @return The result of the foo operation, usually an even number within 0
     *         and 1000.
     */
    int foo(int first, int second) throws Exception;
}

class Test {
    void trailingCommented() {
        System.out.println("indented"); // comment
        System.out.println("indent"); // comment
    }
}
