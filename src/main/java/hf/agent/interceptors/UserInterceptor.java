package hf.agent.interceptors;

import hf.base.interceptors.LoginInterceptor;

public class UserInterceptor extends LoginInterceptor {
    @Override
    public String getSystem() {
        return "agent";
    }
}
