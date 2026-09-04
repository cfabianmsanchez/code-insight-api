package com.codeinsight.api.domain.exception;

import com.codeinsight.api.domain.model.AnalysisType;

public class UnsupportedAnalysisTypeException extends RuntimeException {
    public UnsupportedAnalysisTypeException(AnalysisType type) {
        super("Unsupported code analysis type: " + type);
    }
}
