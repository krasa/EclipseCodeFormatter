This directory contains a repackaged version of [`org.eclipse.jdt.core`](https://www.eclipse.org/jdt/core/) (sources can be found [here](https://git.eclipse.org/c/jdt/eclipse.jdt.core.git/tree/org.eclipse.jdt.core)), which is licensed under [EPL-2.0](https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html).

The original version was changed as follows:
* It is repackaged together with all its dependencies into a single jar file ([`eclipse.jar`](eclipse.jar))
* From all modules, the `META-INF/*.SF`, `META-INF/*.DSA`, `META-INF/*.RSA` and `plugin.xml` are removed

The "modified" source code can be found in [`eclipse-sources.jar`](eclipse-sources.jar).
