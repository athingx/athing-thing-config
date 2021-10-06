package com.github.athingx.athing.aliyun.config.api;

/**
 * 提交器
 */
public interface Committer {

    /**
     * 提交本次变更
     * <pre>
     *     1. 只能提交一次，第二次提交返回false
     *     2. 提交和回滚只能有一个成功
     * </pre>
     *
     * @return TRUE | FALSE
     */
    boolean commit();

    /**
     * 回滚本次变更
     * <pre>
     *     1. 只能回滚一次，第二次回滚返回false
     *     2. 提交和回滚只能有一个成功
     * </pre>
     *
     * @param reason 原因
     * @return TRUE | FALSE
     */
    boolean rollback(String reason);

    /**
     * 回滚本次变更
     *
     * @param cause 异常
     * @return TRUE | FALSE
     */
    boolean rollback(Throwable cause);

}
