
package cn.coderule.minimq.rpc.common.core.relay.response;

public class Result<T> {
    private int code;
    private String remark;
    private T result;

    public Result(int code, String remark, T result) {
        this.code = code;
        this.remark = remark;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
