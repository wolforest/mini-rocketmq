package cn.coderule.minimq.domain.domain.exception;

import cn.coderule.common.lang.exception.BusinessException;

public class InvalidConfigException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "invalid config";

    public InvalidConfigException(String message) {
        super(500, message);
    }

}
