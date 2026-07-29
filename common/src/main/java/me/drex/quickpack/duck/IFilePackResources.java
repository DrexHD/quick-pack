package me.drex.quickpack.duck;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public interface IFilePackResources {
    void quick_pack$initializeFileTree(TreeSet<String> fileTree, Map<String, Set<String>> namespaces);
}
