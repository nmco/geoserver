package org.geoserver.params.extractor.web;

import java.io.Serializable;

public class RuleTestModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String input;
    private String output;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
