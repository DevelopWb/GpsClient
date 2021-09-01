package com.juntai.disabled.basecomponent.bean;


import com.juntai.disabled.basecomponent.base.BaseResult;

/**
 * @Author: tobato
 * @Description: 作用描述
 * @CreateDate: 2020/8/23 16:52
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/8/23 16:52
 */
public class BaseStreamBean extends BaseResult {
    private int errcode;
    private String errdesc;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrdesc() {
        return errdesc == null ? "" : errdesc;
    }

    public void setErrdesc(String errdesc) {
        this.errdesc = errdesc == null ? "" : errdesc;
    }
}
