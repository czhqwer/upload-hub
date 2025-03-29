package cn.czh.service;

public interface IAuthService {

    String getMainUserPassword();

    void setMainUserPassword(String password);

    boolean isMainUser(String remoteAddress);
}
