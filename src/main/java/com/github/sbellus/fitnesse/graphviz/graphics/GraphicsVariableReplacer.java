package com.github.sbellus.fitnesse.graphviz.graphics;

import java.util.regex.Pattern;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.VariableSource;
import util.GracefulNamer;

public class GraphicsVariableReplacer {
    private static final Pattern VariablePattern = Pattern.compile("\\$\\{((?!\\$\\{).)*?\\}");
    private static final Pattern RegraceVariablePattern = Pattern.compile(".+-REGRACE$");
    private static final Integer MaxRecursionDepth = 30;
    private VariableSource variableSource;
    private Integer recursionDepth;

    public GraphicsVariableReplacer(VariableSource variableSource) {
        this.variableSource = variableSource;
        this.recursionDepth = 0;
    }

    public String replaceVariablesIn(String str) {
        recursionDepth = 0;
        return replaceVariablesRecursively(str);
    }

    private String replaceVariablesRecursively(String str) {
        boolean isAtLeastOneVariableReplaced = false;
        java.util.regex.Matcher m = VariablePattern.matcher(str);
        while (m.find()) {
            String var = m.group();
            String varName = var.substring(2, var.length() - 1);
            java.util.regex.Matcher regraceMatcher = RegraceVariablePattern.matcher(varName);
            Boolean regrace = false;
            if (regraceMatcher.find()) {
                varName = varName.replace("-REGRACE", "");
                regrace = true;
            }
                
            Maybe<String> value = variableSource.findVariable(varName);
            if (!value.isNothing()) {
                isAtLeastOneVariableReplaced = true;
                String varValue = value.getValue();
                if (regrace) {
                    varValue = GracefulNamer.regrace(varValue);
                }
                str = str.replace(var, varValue);
            }
        }

        if (isAtLeastOneVariableReplaced && recursionDepth < MaxRecursionDepth) {
            recursionDepth++;
            return replaceVariablesRecursively(str);
        }

        return str;
    }
}
